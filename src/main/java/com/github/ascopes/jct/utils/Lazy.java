package com.github.ascopes.jct.utils;

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
