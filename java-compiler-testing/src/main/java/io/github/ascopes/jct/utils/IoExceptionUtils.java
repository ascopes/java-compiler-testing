/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utilities for handling {@link IOException}s and converting them to
 * {@link UncheckedIOException}s.
 *
 * @author Ashley Scopes
 */
public final class IoExceptionUtils extends UtilityClass {

  private IoExceptionUtils() {
    // Disallow initialisation.
  }

  /**
   * Run some logic with no return value, throwing any {@link IOException} as an
   * {@link UncheckedIOException}.
   *
   * @param runnable the runnable to run.
   * @throws UncheckedIOException if an {@link IOException} occurs.
   */
  public static void uncheckedIo(IoRunnable runnable) {
    try {
      runnable.run();
    } catch (IOException ex) {
      throw wrapWithUncheckedIoException(ex);
    }
  }

  /**
   * Run some logic with a return value, throwing any {@link IOException} as an
   * {@link UncheckedIOException}, or returning the result otherwise.
   *
   * @param supplier the supplier to run.
   * @param <T>      the result of the supplier.
   * @return the result of the supplier.
   * @throws UncheckedIOException if an {@link IOException} occurs.
   */
  public static <T> T uncheckedIo(IoSupplier<T> supplier) {
    T result;

    try {
      result = supplier.get();
    } catch (IOException ex) {
      throw wrapWithUncheckedIoException(ex);
    }

    return result;
  }

  /**
   * Wrap a given {@link IOException} within an {@link UncheckedIOException}.
   *
   * <p>The stacktrace of the given exception will be used if one is present.
   *
   * @param ex the exception to throw.
   * @return the new unchecked exception.
   */
  public static UncheckedIOException wrapWithUncheckedIoException(IOException ex) {
    return new UncheckedIOException(ex.getClass().getName() + ": " + ex.getMessage(), ex);
  }

  /**
   * A runnable interface that may throw an {@link IOException}.
   *
   * @author Ashley Scopes
   */
  @FunctionalInterface
  public interface IoRunnable {

    /**
     * Execute the logic.
     *
     * @throws IOException if an IOException occurs.
     */
    void run() throws IOException;
  }

  /**
   * A supplier interface that may throw an {@link IOException}.
   *
   * @param <T> the return type of the supplier.
   * @author Ashley Scopes
   */
  @FunctionalInterface
  public interface IoSupplier<T> {

    /**
     * Get the result.
     *
     * @return the result.
     * @throws IOException if an IOException occurs.
     */
    T get() throws IOException;
  }
}
