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

package com.github.ascopes.jct.intern;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper type that wraps an initializer and invokes it lazily as required.
 *
 * <p>The initializer can be cleared to force future accesses to re-initialize it again
 * if needed.
 *
 * <p>This is thread-safe.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class Lazy<T> {

  private final Supplier<T> initializer;
  private final Object lock;
  private volatile boolean initialized;
  private volatile T data;

  /**
   * Initialize the object.
   *
   * @param initializer the initializer to call.
   */
  public Lazy(Supplier<T> initializer) {
    this.initializer = Objects.requireNonNull(initializer);
    lock = new Object();
    initialized = false;
    data = null;
  }

  /**
   * Get the value, initializing it first if it does not yet exist.
   *
   * @return the value.
   */
  public T access() {
    if (!initialized) {
      synchronized (lock) {
        if (!initialized) {
          data = initializer.get();
          initialized = true;
        }
      }
    }

    return data;
  }

  /**
   * Clear any existing value, if there is one.
   *
   * <p>Future accesses will re-initialize the value from the initializer.
   */
  public void destroy() {
    if (initialized) {
      synchronized (lock) {
        initialized = false;
        data = null;
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    var builder = new StringBuilder("Lazy{data=");

    if (data instanceof String) {
      builder.append(StringUtils.quoted(data));
    } else if (initialized) {
      builder.append(data);
    } else {
      builder.append("<uninitialized>");
    }

    return builder.append("}").toString();
  }
}
