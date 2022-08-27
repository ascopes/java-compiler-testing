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

import java.util.Objects;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper type that wraps an initializer and invokes it lazily as required.
 *
 * <p>The initializer can be cleared to force future accesses to re-initialize it again
 * if needed.
 *
 * <p>This is thread-safe.
 *
 * @param <T> the type of lazy value to return when accessed.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
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

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("data", data)
        .attribute("initialized", initialized)
        .toString();
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
        if (initialized) {
          initialized = false;
          data = null;
        }
      }
    }
  }

  /**
   * Attempt to run some logic on the initialized value if and only if the value has already been
   * initialized by the time this is called.
   *
   * @param consumer the consumer to consume the value if it is initialized.
   * @param <E>      the exception type that the consumer can throw.
   * @throws E the exception type that the consumer can throw.
   */
  public <E extends Throwable> void ifInitialized(ThrowingConsumer<T, E> consumer) throws E {
    if (initialized) {
      synchronized (lock) {
        if (initialized) {
          consumer.consume(data);
        }
      }
    }
  }

  /**
   * Consumer that throws some form of checked exception if something goes wrong.
   *
   * @param <E> the exception type.
   * @param <T> the type to consume.
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  @FunctionalInterface
  public interface ThrowingConsumer<T, E extends Throwable> {

    /**
     * Consume a value.
     *
     * @param arg the value to consume.
     * @throws E the exception that may be thrown if something goes wrong in the consumer.
     */
    void consume(T arg) throws E;
  }
}
