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
public final class GarbageDisposal {

  private static final Logger LOGGER = LoggerFactory.getLogger(GarbageDisposal.class);
  private static final Lazy<Cleaner> CLEANER = new Lazy<>(GarbageDisposal::newCleaner);

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
    return Cleaner.create(runnable -> {
      var thread = new Thread(runnable);
      thread.setName("JCT GC hook caller");
      thread.setPriority(Thread.MIN_PRIORITY);
      return thread;
    });
  }

  private GarbageDisposal() {
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
    public String toString() {
      return "JCT GC hook for " 
          + name
          + " at "
          + Integer.toHexString(System.identityHashCode(closeable))
          + " (delegate at "
          + Integer.toHexString(System.identityHashCode(this))
          + ")";
    }

    @Override
    public void run() {
      var thread = new Thread(() -> {
        try {
          LOGGER.trace("Closing {} ({})", name, closeable);
          closeable.close();
        } catch (Exception ex) {
          var thisThread = Thread.currentThread();
          LOGGER.error(
              "Failed to close resource on thread {} [{}]",
              thisThread.getId(),
              thisThread.getName(),
              ex
          );
        }
      });

      thread.setName(toString());
      thread.setPriority(Thread.MAX_PRIORITY);
      thread.start();
    }
  }
}
