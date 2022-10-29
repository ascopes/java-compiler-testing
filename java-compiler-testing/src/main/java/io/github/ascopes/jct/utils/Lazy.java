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

import io.github.ascopes.jct.annotations.WillNotClose;
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
 * <p>Note that closable resources must be closed manually. See
 * {@link Lazy#ifInitialized} for a mechanism to support this.
 *
 * <p>This descriptor is thread-safe. No guarantees are made about the thread-safety of the
 * internally stored data, nor the initializer supplier.
 *
 * @param <T> the type of lazy value to return when accessed.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public class Lazy<@WillNotClose T> {

  private final Supplier<@WillNotClose T> initializer;
  private final Object lock;
  private volatile boolean initialized;
  private volatile T data;

  /**
   * Initialize the object.
   *
   * @param initializer the initializer to call.
   */
  public Lazy(Supplier<T> initializer) {
    this.initializer = Objects.requireNonNull(initializer, "initializer must not be null");
    lock = new Object();
    initialized = false;
    data = null;
  }

  @Override
  public String toString() {
    // Declare outside the synchronized block to help IntelliJ with a coverage bug.
    String repr;

    // Synchronize to prevent a race condition between reading
    // the data and reading the "initialized" flag.
    synchronized (lock) {
      repr = new ToStringBuilder(this)
          .attribute("data", data)
          .attribute("initialized", initialized)
          .toString();
    }

    return repr;
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
   *
   * <p>Use {@link #ifInitialized} to handle closing resources on the wrapped object
   * before making this call, if this behaviour is required.
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
   * @return this lazy object for further call chaining.
   * @throws E the exception type that the consumer can throw.
   */
  public <E extends Throwable> Lazy<T> ifInitialized(
      ThrowingConsumer<? super T, ? extends E> consumer
  ) throws E {
    if (initialized) {
      synchronized (lock) {
        if (initialized) {
          consumer.consume(data);
        }
      }
    }

    return this;
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
     * Consume a non-null value.
     *
     * @param arg the value to consume.
     * @throws E the exception that may be thrown if something goes wrong in the consumer.
     */
    void consume(T arg) throws E;
  }
}
