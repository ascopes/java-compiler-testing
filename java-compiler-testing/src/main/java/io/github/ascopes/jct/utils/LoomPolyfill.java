/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Polyfill support for Project Loom virtual threads.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class LoomPolyfill extends UtilityClass {

  private LoomPolyfill() {
    // Static-only class.
  }

  /**
   * Get the thread ID (possibly the virtual thread ID if Project Loom is enabled).
   *
   * <p>This ensures that the underlying system does not spoof the thread ID on JDK 19 and newer.
   *
   * @param thread the thread to use.
   * @return the thread ID (possibly the virtual thread ID).
   */
  public static long getThreadId(Thread thread) {
    // Note: this test will never get 100% coverage on one JDK, because it totally depends on the
    // JDK in use as to which code path runs. In CI, it should get covered when reports are merged.

    try {
      // If we are on JDK 19, attempt to call the .threadId() method instead of the .getId()
      // method. The former is new to JDK 19 and fetches the virtual thread ID.
      var method = Thread.class.getDeclaredMethod("threadId");
      return (long) method.invoke(thread);
    } catch (Exception ex) {
      @SuppressWarnings("deprecation")
      var tid = thread.getId();
      return tid;
    }
  }
}
