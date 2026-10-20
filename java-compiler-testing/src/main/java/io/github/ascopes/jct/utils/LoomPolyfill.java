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
package io.github.ascopes.jct.utils;

/**
 * Polyfill to enable supporting using the newer Thread APIs on newer platforms.
 *
 * <p>This specifically targets the new APIs introduced as part of Project Loom
 * in Java 19.
 *
 * <p><strong>This class is now suitable to be removed as we only target Java 21 and
 * newer.</strong>
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class LoomPolyfill {

  private LoomPolyfill() {
    // Static-only class.
  }

  /**
   * Get the thread ID.
   *
   * <p>This ensures that the underlying system does not spoof the thread ID on JDK 19 and newer.
   *
   * @param thread the thread to use.
   * @return the thread ID.
   */
  public static long getThreadId(Thread thread) {
    // Now we baseline to Java 21, we don't have to use reflection to invoke this Java 19 API.
    return thread.threadId();
  }

  /**
   * Get the current thread.
   *
   * @return the current thread.
   */
  public static Thread getCurrentThread() {
    return Thread.currentThread();
  }
}
