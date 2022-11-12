/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ascopes.jct.testing.integration.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assumptions.abort;

import io.github.ascopes.jct.utils.GarbageDisposalUtils;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for {@link GarbageDisposalUtils}.
 *
 * @author Ashley Scopes
 */
@DisplayName("GarbageDisposalUtils integration tests")
class GarbageDisposalUtilsIntegrationTest {

  static final Logger LOGGER = LoggerFactory.getLogger(GarbageDisposalUtilsIntegrationTest.class);

  volatile boolean stop;
  Thread thread;

  @BeforeEach
  void setUp() {
    var initialCollections = ManagementFactory
        .getGarbageCollectorMXBeans()
        .stream()
        .mapToLong(GarbageCollectorMXBean::getCollectionCount)
        .sum();

    System.gc();

    try {
      await("ensure System.gc() calls garbage collector immediately")
          .pollInterval(10, MILLISECONDS)
          .atMost(5, SECONDS)
          .until(() -> initialCollections < ManagementFactory
              .getGarbageCollectorMXBeans()
              .stream()
              .mapToLong(GarbageCollectorMXBean::getCollectionCount)
              .sum());

    } catch (ConditionTimeoutException ex) {
      abort(
          "Calling System.gc() did not trigger the GC hook in time, this test pack would be flaky"
      );
    }

    // Set up GC stress threads.
    var random = new Random();
    thread = new Thread(() -> {
      LOGGER.info("Starting GC stress thread");
      // Put stress on the garbage collector to run during the tests many times.

      var i = 0L;

      while (!stop) {
        ++i;

        var array = new int[10_000_000];
        Arrays.fill(array, random.nextInt());
        Arrays.fill(array, array[random.nextInt(array.length)]);

        if (i % 100 == 0) {
          var memory = ManagementFactory.getMemoryMXBean();
          var garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
          var gcSummary = garbageCollectors
              .stream()
              .map(gc -> gc.getName() + " GC'ed " + gc.getCollectionCount() + " times")
              .collect(Collectors.joining(", "));

          LOGGER.info(
              "100 GC stress test passes completed -- heap={} -- nonHeap={} -- gc={}",
              memory.getHeapMemoryUsage(),
              memory.getNonHeapMemoryUsage(),
              gcSummary
          );
        }
      }
    });

    thread.setDaemon(true);
    thread.start();
  }

  @AfterEach
  void stopGcStress() throws InterruptedException {
    LOGGER.info("Stopping GC stress thread");
    stop = true;
    thread.join();
  }

  @DisplayName("onPhantom(Object, String, AutoCloseable) cleans up reference on garbage disposal")
  @RepeatedTest(3)
  void onPhantomForSingleReferenceCleansUpOnGarbageDisposal() {
    // Given
    var closedCount = new AtomicInteger(0);
    AutoCloseable closeable = closedCount::incrementAndGet;

    // When
    GarbageDisposalUtils.onPhantom(new Object(), "foobar baz bork", closeable);

    System.gc();

    // Then
    await("the closeable object gets closed during garbage collection")
        .atMost(20, SECONDS)
        .pollInterval(10, MILLISECONDS)
        .untilAtomic(closedCount, is(greaterThanOrEqualTo(1)));

    assertThat(closedCount)
        .withFailMessage("Expected exactly one closure to occur")
        .hasValue(1);
  }

  @DisplayName("onPhantom(Object, String, AutoCloseable) cleans up once if an exception is raised")
  @RepeatedTest(3)
  void onPhantomForSingleReferenceCleansUpOnceIfAnExceptionIsRaised() {
    // Given
    var closedCount = new AtomicInteger(0);
    AutoCloseable closeable = () -> {
      closedCount.incrementAndGet();
      throw new IllegalStateException("Something is wrong here, I can feel it...");
    };

    // When
    GarbageDisposalUtils.onPhantom(new Object(), "throwing closeable", closeable);

    System.gc();

    // Then
    await("the closeable object gets closed during garbage collection")
        .atMost(20, SECONDS)
        .pollInterval(10, MILLISECONDS)
        .untilAtomic(closedCount, is(greaterThanOrEqualTo(1)));

    assertThat(closedCount)
        .withFailMessage("Expected exactly one closure to occur")
        .hasValue(1);
  }

  @DisplayName("onPhantom(Object, Map) cleans up references on garbage disposal")
  @RepeatedTest(3)
  void onPhantomForMultipleReferencesCleansUpOnGarbageDisposal() {
    // Given
    var closeCounts = new AtomicInteger[50];
    var mapping = new HashMap<Integer, AutoCloseable>();
    for (var i = 0; i < closeCounts.length; ++i) {
      closeCounts[i] = new AtomicInteger(0);
      mapping.put(i, closeCounts[i]::incrementAndGet);
    }

    // When
    GarbageDisposalUtils.onPhantom(new Object(), mapping);

    System.gc();

    // Then
    await("the closeable objects get closed during garbage collection")
        .atMost(40, SECONDS)
        .pollInterval(1, MILLISECONDS)
        .untilAsserted(() -> assertThat(mapping)
            .extracting(Map::keySet, iterable(int.class))
            .allSatisfy(index -> assertThat(closeCounts[index]).hasValueGreaterThanOrEqualTo(1)));

    assertThat(closeCounts)
        .withFailMessage("Expected exactly one closure to occur in all closables")
        .allSatisfy(count -> assertThat(count).hasValue(1));
  }

  @DisplayName("onPhantom(Object, Map) cleans up all resources once if an exception is raised")
  @RepeatedTest(3)
  void onPhantomForSingleReferenceCleansUpAllResourcesOnceIfAnExceptionIsRaised() {

    // Given
    var closeCounts = new AtomicInteger[50];
    var mapping = new HashMap<Integer, AutoCloseable>();
    for (var i = 0; i < closeCounts.length; ++i) {
      closeCounts[i] = new AtomicInteger(0);

      // Last closeable will raise an exception in this scenario.
      if (i == closeCounts.length - 1) {
        var closeCount = closeCounts[i];
        mapping.put(i, () -> {
          closeCount.incrementAndGet();
          throw new IllegalStateException("Something is wrong here, I can feel it...");
        });
      } else {
        mapping.put(i, closeCounts[i]::incrementAndGet);
      }
    }

    // When
    GarbageDisposalUtils.onPhantom(new Object(), mapping);

    System.gc();

    // Then
    await("the closeable objects get closed during garbage collection")
        .atMost(20, SECONDS)
        .pollInterval(10, MILLISECONDS)
        .untilAsserted(() -> assertThat(mapping)
            .extracting(Map::keySet, iterable(int.class))
            .allSatisfy(index -> assertThat(closeCounts[index]).hasValueGreaterThanOrEqualTo(1)));

    assertThat(closeCounts)
        .withFailMessage("Expected exactly one closure to occur in all closables")
        .allSatisfy(count -> assertThat(count).hasValue(1));
  }
}
