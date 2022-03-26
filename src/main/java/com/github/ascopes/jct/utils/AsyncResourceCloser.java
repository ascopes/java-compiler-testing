package com.github.ascopes.jct.utils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class used with {@link java.lang.ref.Cleaner} to handle disposing of an object safely on
 * closure.
 *
 * <p>This will discard the object in a separate thread asynchronously when invoked, which is
 * done to prevent blocking the garbage collection thread if a closure operation is blocking.
 *
 * <p>The nature of this implementation enables you to append elements to any collection
 * provided on the fly after initializing this object, although if you plan to do so, then you
 * should use a concurrent-safe collection type to prevent the risk of thread-safety issues.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class AsyncResourceCloser implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncResourceCloser.class);

  private final Map<String, ? extends AutoCloseable> closeables;

  /**
   * Create a resource closer for a single closeable.
   *
   * @param name      a descriptive name for the resource.
   * @param closeable the closeable to close.
   */
  public AsyncResourceCloser(String name, AutoCloseable closeable) {
    this(Map.of(name, closeable));
  }

  /**
   * Create a resource closer for multiple closeables.
   *
   * @param closeables the closeables to close, with each human-readable name as the key.
   */
  public AsyncResourceCloser(Map<String, ? extends AutoCloseable> closeables) {
    this.closeables = closeables;
  }

  /**
   * Trigger each close operation asynchronously, without waiting for them to complete.
   *
   * <p>If no elements exist in the collection of closeables, then this will do nothing.
   *
   * <p>Any exceptions will be discarded.
   */
  @Override
  public void run() {
    closeables.forEach((name, closeable) -> CompletableFuture.runAsync(() -> {
      try {
        closeable.close();
        LOGGER.info("Closed resource {} ({})", name, closeable);
      } catch (Throwable ex) {
        LOGGER.error("Failed to close resource {} ({})", name, closeable, ex);
      }
    }));
  }
}
