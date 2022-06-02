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

package io.github.ascopes.jct.testing.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.assertj.core.api.InstanceOfAssertFactories.array;

import io.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import io.github.ascopes.jct.utils.IoExceptionUtils;
import io.github.ascopes.jct.utils.IoExceptionUtils.IoRunnable;
import io.github.ascopes.jct.utils.IoExceptionUtils.IoSupplier;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IoExceptionUtils}.
 *
 * @author Ashley Scopes
 */
@SuppressWarnings("RedundantCast")
@DisplayName("IoExceptionUtils tests")
class IoExceptionUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return IoExceptionUtils.class;
  }

  @DisplayName("A runnable will be run correctly")
  @Test
  void runnableWillBeRunCorrectly() {
    // Given
    var wasRun = new AtomicBoolean(false);

    // When
    IoExceptionUtils.uncheckedIo((IoRunnable) () -> wasRun.set(true));

    // Then
    then(wasRun).isTrue();
  }

  @DisplayName(
      "A runnable will have any IOException rethrown as an UncheckedIOException if an"
          + "existing stack trace is provided"
  )
  @Test
  void runnableWillRethrowAsUncheckedIoExceptionWithStackTrace() {
    // Given
    var stackTrace = someStackTrace();

    var originalEx = new IOException("bang");
    originalEx.setStackTrace(stackTrace);

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo((IoRunnable) () -> {
      throw originalEx;
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage("%s: %s", originalEx.getClass().getName(), originalEx.getMessage())
        .cause()
        .isSameAs(originalEx)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isEqualTo(stackTrace);
  }

  @DisplayName(
      "A runnable will have any IOException rethrown as an UncheckedIOException if an"
          + "existing stack trace is not provided"
  )
  @Test
  void runnableWillRethrowAsUncheckedIoExceptionWithoutStackTrace() {
    // Given
    var ex = new IOException("bang");

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo((IoSupplier<?>) () -> {
      throw ex;
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage("%s: %s", ex.getClass().getName(), ex.getMessage())
        .hasCause(ex)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isNotNull()
        .isNotEmpty();
  }

  @DisplayName("A supplier will be run correctly and will return the expected result")
  @Test
  void supplierWillBeRunCorrectly() {
    // Given
    var wasRun = new AtomicBoolean(false);
    var expected = new Object();

    // When
    var actual = IoExceptionUtils.uncheckedIo(() -> {
      wasRun.set(true);
      return expected;
    });

    // Then
    then(wasRun).isTrue();
    then(actual).isSameAs(expected);
  }

  @DisplayName(
      "A supplier will have any IOException rethrown as an UncheckedIOException if an "
          + "existing stack trace is provided"
  )
  @SuppressWarnings("ConstantConditions")
  @Test
  void supplierWillRethrowAsUncheckedIoExceptionWithStackTrace() {
    // Given
    var stackTrace = someStackTrace();

    var originalEx = new IOException("bong");
    originalEx.setStackTrace(stackTrace);

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo((IoSupplier<Integer>) () -> {
      if (true) {
        throw originalEx;
      } else {
        return 12345;
      }
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage("%s: %s", originalEx.getClass().getName(), originalEx.getMessage())
        .cause()
        .isSameAs(originalEx)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isEqualTo(stackTrace);
  }

  @DisplayName(
      "A supplier will have any IOException rethrown as an UncheckedIOException if an "
          + "existing stack trace is not provided"
  )
  @SuppressWarnings("ConstantConditions")
  @Test
  void supplierWillRethrowAsUncheckedIoExceptionWithoutStackTrace() {
    // Given
    var ex = new IOException("bong");

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo((IoSupplier<Integer>) () -> {
      if (true) {
        throw ex;
      } else {
        return 12345;
      }
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage("%s: %s", ex.getClass().getName(), ex.getMessage())
        .hasCause(ex)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isNotNull()
        .isNotEmpty();
  }

  @DisplayName("wrapWithUncheckedIoException() wraps IOExceptions with stack traces correctly")
  @Test
  void wrapWithUncheckedIoExceptionWrapsIoExceptionsWithStackTracesCorrectly() {
    // Given
    var stackTrace = someStackTrace();
    var originalEx = new IOException("a stacktrace is set!");
    originalEx.setStackTrace(stackTrace);

    // When
    var newEx = IoExceptionUtils.wrapWithUncheckedIoException(originalEx);

    assertThat(newEx)
        .hasMessage("%s: %s", originalEx.getClass().getName(), originalEx.getMessage())
        .cause()
        .isSameAs(originalEx)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isEqualTo(stackTrace);
  }

  @DisplayName("wrapWithUncheckedIoException() wraps IOExceptions without stack traces correctly")
  @Test
  void wrapWithUncheckedIoExceptionWrapsIoExceptionsWithoutStackTracesCorrectly() {
    // Given
    var originalEx = new IOException("no stacktrace set!");
    // IOException will initialize the stacktrace by default. We want to first undo this.
    originalEx.setStackTrace(new StackTraceElement[0]);

    // When
    var newEx = IoExceptionUtils.wrapWithUncheckedIoException(originalEx);

    assertThat(newEx)
        .hasMessage("%s: %s", originalEx.getClass().getName(), originalEx.getMessage())
        .hasCause(originalEx)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isNotNull()
        .isNotEmpty();
  }

  private static StackTraceElement[] someStackTrace() {
    return Thread.currentThread().getStackTrace();
  }
}
