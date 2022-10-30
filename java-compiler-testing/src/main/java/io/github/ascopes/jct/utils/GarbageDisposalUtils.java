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
package io.github.ascopes.jct.utils;

import java.lang.ref.Cleaner;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common global hook for intercepting garbage collection events.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class GarbageDisposalUtils {

  // Tuning for JVMs on Windows.
  private static final int THREAD_PRIORITY = Thread.MAX_PRIORITY - 1;
  private static final int THREAD_STACK_SIZE = 20 * 1_024;

  private static final AtomicInteger CLEANER_THREAD_ID = new AtomicInteger(0);
  private static final ThreadGroup THREAD_GROUP = new ThreadGroup("JCT GarbageDisposal thread");
  private static final Logger LOGGER = LoggerFactory.getLogger(GarbageDisposalUtils.class);
  private static final Lazy<Cleaner> CLEANER = new Lazy<>(GarbageDisposalUtils::newCleaner);

  /**
   * Close the given resources when the given reference becomes a phantom reference.
   *
   * <p>The hook must not reference the {@code ref} parameter, either directly or via a closure,
   * otherwise the resource will be leaked.
   *
   * @param ref   the reference to watch.
   * @param hooks the hooks to perform.
   */
  public static void onPhantom(Object ref, Map<?, ? extends AutoCloseable> hooks) {
    hooks.forEach((name, hook) -> onPhantom(ref, Objects.toString(name), hook));
  }

  /**
   * Perform the given closure hook when the given reference becomes a phantom reference.
   *
   * <p>The hook must not reference the {@code ref} parameter, either directly or via a closure,
   * otherwise the resource will be leaked.
   *
   * @param ref  the reference to watch.
   * @param hook the hook to perform.
   */
  public static void onPhantom(Object ref, String name, AutoCloseable hook) {
    CLEANER.access().register(ref, new CloseableDelegate(name, hook));
  }

  private static Cleaner newCleaner() {
    // This thread factory has exactly 1 thread created from it.
    return Cleaner.create(runnable -> newThread("cleaner thread", runnable));
  }

  private static Thread newThread(String name, Runnable runnable) {
    var thread = new Thread(
        THREAD_GROUP,
        runnable,
        "JCT GarbageDisposal - thread #"
            + CLEANER_THREAD_ID.incrementAndGet()
            + " - "
            + name,
        THREAD_STACK_SIZE,
        false
    );
    thread.setDaemon(false);
    thread.setPriority(THREAD_PRIORITY);
    return thread;
  }

  private GarbageDisposalUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  private static final class CloseableDelegate implements Runnable {

    private final String name;
    private final AutoCloseable closeable;

    private CloseableDelegate(String name, AutoCloseable closeable) {
      this.name = name;
      this.closeable = closeable;
    }

    @Override
    public void run() {
      newThread("dispose of " + name, this::runSync).start();
    }

    private void runSync() {
      try {
        LOGGER.debug("Closing {} ({})", name, closeable);
        closeable.close();
      } catch (Exception ex) {
        var thisThread = Thread.currentThread();
        LOGGER.error(
            "Failed to close {} ({}) on thread {} [{}]",
            name,
            closeable,
            thisThread.getId(),
            thisThread.getName(),
            ex
        );
      }
    }
  }
}
