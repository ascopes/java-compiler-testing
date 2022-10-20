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
   * Perform the given closure hook when the given reference becomes a phantom reference.
   *
   * <p>The hook must not reference the {@code ref} parameter, either directly or via a closure,
   * otherwise the resource will be leaked.
   *
   * @param ref  the reference to watch.
   * @param hook the hook to perform.
   */
  public static void onPhantom(Object ref, Runnable hook) {
    CLEANER.access().register(ref, hook);
  }

  private static Cleaner newCleaner() {
    return Cleaner.create(runnable -> {
      var thread = new Thread(runnable);
      thread.setDaemon(false);
      thread.setName("java-compiler-testing garbage collector hook for " + runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.setUncaughtExceptionHandler(GarbageDisposal::handleUncaughtException);
      return thread;
    });
  }

  private static void handleUncaughtException(Thread thread, Throwable ex) {
    LOGGER.error("Resource cleaner error on thread {} [{}]", thread.getId(), thread.getName(), ex);
  }

  private GarbageDisposal() {
    throw new UnsupportedOperationException("static-only class");
  }
}
