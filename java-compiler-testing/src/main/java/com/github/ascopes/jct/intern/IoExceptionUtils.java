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

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utilities for handling {@link IOException}s and converting them to
 * {@link UncheckedIOException}s.
 *
 * @author Ashley Scopes
 */
public final class IoExceptionUtils {

  private IoExceptionUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Run some logic with no return value, throwing any {@link IOException} as an
   * {@link UncheckedIOException}.
   *
   * @param runnable the runnable to run.
   * @throws UncheckedIOException if an {@link IOException} occurs.
   */
  public static void uncheckedIo(RunnableWithIo runnable) {
    try {
      runnable.run();
    } catch (IOException ex) {
      rethrow(ex);
    }
  }

  /**
   * Run some logic with a return value, throwing any {@link IOException} as an
   * {@link UncheckedIOException}, or returning the result otherwise.
   *
   * @param supplier the supplier to run.
   * @return the result of the supplier.
   * @throws UncheckedIOException if an {@link IOException} occurs.
   */
  public static <T> T uncheckedIo(SupplierWithIo<T> supplier) {
    try {
      return supplier.get();
    } catch (IOException ex) {
      return rethrow(ex);
    }
  }

  private static <T> T rethrow(IOException ex) {
    var newEx = new UncheckedIOException(ex.getMessage(), ex);
    newEx.setStackTrace(ex.getStackTrace());
    throw newEx;
  }

  /**
   * A supplier interface that may throw an {@link IOException}.
   *
   * @param <T> the return type.
   */
  @FunctionalInterface
  public interface SupplierWithIo<T> {

    /**
     * Get the result.
     *
     * @return the result.
     * @throws IOException if an IOException occurs.
     */
    T get() throws IOException;
  }

  /**
   * A runnable interface that may throw an {@link IOException}.
   */
  @FunctionalInterface
  public interface RunnableWithIo {

    /**
     * Execute the logic.
     *
     * @throws IOException if an IOException occurs.
     */
    void run() throws IOException;
  }
}
