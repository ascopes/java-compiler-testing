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
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Utilities for handling {@link IOException}s and converting them to
 * {@link UncheckedIOException}s.
 *
 * @author Ashley Scopes
 */
@API(since = "0.0.1", status = Status.INTERNAL)
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
      rethrowAsUncheckedIo(ex);
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
  public static <T> T uncheckedIo(SupplierWithIo<T> supplier) {
    try {
      return supplier.get();
    } catch (IOException ex) {
      return rethrowAsUncheckedIo(ex);
    }
  }

  /**
   * Throw a given {@link IOException} within an {@link UncheckedIOException}.
   *
   * <p>The stacktrace of the given exception will be used if one is present.
   *
   * @param ex the exception to throw.
   * @param <T> the dummy return value to pretend to have.
   * @return nothing, this is just used to fool the type-checker.
   * @throws UncheckedIOException wrapping {@code ex}, in all cases.
   */
  public static <T> T rethrowAsUncheckedIo(IOException ex) {
    var newEx = new UncheckedIOException(ex.getClass().getName() + ": " + ex.getMessage(), ex);
    var existingStack = ex.getStackTrace();
    if (existingStack != null) {
      newEx.setStackTrace(existingStack);
    }
    throw newEx;
  }

  /**
   * A supplier interface that may throw an {@link IOException}.
   *
   * @param <T> the return type of the supplier.
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
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
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
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
