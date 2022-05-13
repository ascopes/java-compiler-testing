/*
 * Copyright (C) 2022 Ashley Scopes
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

package io.github.ascopes.jct.testing.unit.intern;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import io.github.ascopes.jct.intern.AsyncResourceCloser;
import io.github.ascopes.jct.testing.helpers.ConcurrentRuns;
import io.github.ascopes.jct.testing.helpers.MoreMocks;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link AsyncResourceCloser} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("AsyncResourceCloser tests")
class AsyncResourceCloserTest {

  @DisplayName("Initializing with a null name throws a NullPointerException")
  @Test
  void initializingWithNullNameThrowsNullPointerException() {
    // Given
    var resource = MoreMocks.stub(AutoCloseable.class);

    // Then
    assertThatThrownBy(() -> new AsyncResourceCloser(null, resource))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("0 resources do not cause a deadlock")
  @Test
  @Timeout(5)
  void zeroResourcesDoNotCauseDeadLock() {
    // Given
    var closer = new AsyncResourceCloser(Map.of());

    // Then
    assertThatCode(closer::run)
        .doesNotThrowAnyException();
  }

  @DisplayName("1 resource gets closed")
  @Test
  void resourceGetsClosed() throws InterruptedException {
    // Given
    var resource = new CloseableResource();
    var closer = new AsyncResourceCloser("foobar", resource);
    then(resource.closed).isFalse();

    // When
    closer.run();

    // Then
    await()
        .atMost(ofSeconds(10))
        .pollInterval(ofMillis(1))
        .untilAsserted(() -> assertThat(resource.closed)
            .withFailMessage("resource was not closed")
            .isTrue());
  }

  @DisplayName("Resources get closed")
  @ValueSource(ints = {1, 2, 3, 5, 10, 100, 1000})
  @ParameterizedTest(name = "for {0} resource(s)")
  void resourcesGetClosed(int count) {
    // Given
    var resources = new HashMap<String, CloseableResource>();
    for (var i = 0; i < count; ++i) {
      var resource = new CloseableResource();
      assertThat(resource.closed).isFalse();
      resources.put(Integer.toString(i), resource);
    }

    var closer = new AsyncResourceCloser(resources);

    // When
    closer.run();

    // Then
    await()
        .atMost(ofSeconds(10))
        .pollInterval(ofMillis(1))
        .untilAsserted(() -> assertThat(resources)
            .allSatisfy((name, resource) -> assertThat(resource.closed)
                .withFailMessage("resource %s was not closed", name)
                .isTrue()));
  }

  @DisplayName("Resources get closed if some fail")
  @ConcurrentRuns
  @ParameterizedTest(name = "for {0} resource(s)")
  void resourcesGetClosedIfSomeFail(int count) {
    // Given
    var resources = new HashMap<String, CloseableResource>();
    for (var i = 0; i < count; ++i) {
      var resource = new CloseableResource();
      assertThat(resource.closed).isFalse();
      resources.put(Integer.toString(i), resource);
    }

    var allResources = new HashMap<String, AutoCloseable>(resources);
    allResources.put("bad1", new BadCloseableResource());
    allResources.put("bad2", new BadCloseableResource());
    allResources.put("bad3", new BadCloseableResource());

    var closer = new AsyncResourceCloser(allResources);

    // When
    closer.run();

    // Then
    await()
        .atMost(ofSeconds(10))
        .pollInterval(ofMillis(1))
        .untilAsserted(() -> assertThat(resources)
            .allSatisfy((name, resource) -> assertThat(resource.closed)
                .withFailMessage("resource %s was not closed", name)
                .isTrue()));
  }

  static class CloseableResource implements AutoCloseable {

    private volatile boolean closed;

    CloseableResource() {
      closed = false;
    }

    @Override
    public void close() {
      closed = true;
    }
  }

  static class BadCloseableResource implements AutoCloseable {

    @Override
    public void close() {
      throw new IllegalStateException("Bad things happened!");
    }
  }
}
