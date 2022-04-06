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

package com.github.ascopes.jct.test.intern;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.assertj.core.api.InstanceOfAssertFactories.array;

import com.github.ascopes.jct.intern.IoExceptionUtils;
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
class IoExceptionUtilsTest {

  @DisplayName("A runnable will be run correctly")
  @Test
  void runnableWillBeRunCorrectly() {
    // Given
    var wasRun = new AtomicBoolean(false);

    // When
    IoExceptionUtils.uncheckedIo(() -> wasRun.set(true));

    // Then
    then(wasRun).isTrue();
  }

  @DisplayName("A runnable will have any IOException rethrown as an UncheckedIOException")
  @Test
  void runnableWillRethrowAsUncheckedIoException() {
    // Given
    var stackTrace = someStackTrace();

    var ex = new IOException("bang");
    ex.setStackTrace(stackTrace);

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo(() -> {
      throw ex;
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage(ex.getMessage())
        .hasCause(ex)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isEqualTo(stackTrace);
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

  @DisplayName("A supplier will have any IOException rethrown as an UncheckedIOException")
  @SuppressWarnings("ConstantConditions")
  @Test
  void supplierWillRethrowAsUncheckedIoException() {
    // Given
    var stackTrace = someStackTrace();

    var ex = new IOException("bong");
    ex.setStackTrace(stackTrace);

    // Then
    thenCode(() -> IoExceptionUtils.uncheckedIo(() -> {
      if (true) {
        throw ex;
      } else {
        return 12345;
      }
    })).isInstanceOf(UncheckedIOException.class)
        .hasMessage(ex.getMessage())
        .hasCause(ex)
        .extracting(Throwable::getStackTrace, array(StackTraceElement[].class))
        .isEqualTo(stackTrace);
  }

  private static StackTraceElement[] someStackTrace() {
    return Thread.currentThread().getStackTrace();
  }
}
