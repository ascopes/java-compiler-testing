/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.assertions;

import static io.github.ascopes.jct.fixtures.Fixtures.oneOf;
import static io.github.ascopes.jct.fixtures.Fixtures.someStackTraceList;
import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import java.time.Instant;
import java.util.Locale;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link TraceDiagnosticAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticAssert tests")
class TraceDiagnosticAssertTest {

  @DisplayName("TraceDiagnosticAssert.kind(...) tests")
  @Nested
  class KindTest {

    @DisplayName(".kind() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::kind)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".kind() returns assertions on the kind")
    @Test
    void returnsAssertionsOnTheKind() {
      // Given
      TraceDiagnostic<?> element = mock();
      var kind = oneOf(Kind.class);
      when(element.getKind()).thenReturn(kind);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.kind().isEqualTo(kind))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("TraceDiagnosticAssert.source(...) tests")
  @Nested
  class SourceTest {

    @DisplayName(".source() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::source)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".source() returns assertions on the source")
    @Test
    void returnsAssertionsOnTheSource() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      var source = mock(JavaFileObject.class);
      when(element.getSource()).thenReturn(source);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.source().isEqualTo(source))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("TraceDiagnosticAssert.position(...) tests")
  @Nested
  class PositionTest {

    @DisplayName(".position() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::position)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".position() returns assertions on the position")
    @Test
    void returnsAssertionsOnThePosition() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getPosition()).thenReturn(5678L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.position().isEqualTo(5678L))
          .doesNotThrowAnyException();

      assertThat(assertions.position().descriptionText())
          .isEqualTo("position");
    }
  }

  @DisplayName("TraceDiagnosticAssert.startPosition(...) tests")
  @Nested
  class StartPositionTest {

    @DisplayName(".startPosition() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::startPosition)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".startPosition() returns assertions on the start position")
    @Test
    void returnsAssertionsOnTheStartPosition() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getStartPosition()).thenReturn(123L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.startPosition().isEqualTo(123L))
          .doesNotThrowAnyException();

      assertThat(assertions.startPosition().descriptionText())
          .isEqualTo("start position");
    }
  }

  @DisplayName("TraceDiagnosticAssert.endPosition(...) tests")
  @Nested
  class EndPositionTest {

    @DisplayName(".endPosition() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::endPosition)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".endPosition() returns assertions on the end position")
    @Test
    void returnsAssertionsOnTheEndPosition() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getEndPosition()).thenReturn(91011L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.endPosition().isEqualTo(91011L))
          .doesNotThrowAnyException();

      assertThat(assertions.endPosition().descriptionText())
          .isEqualTo("end position");
    }
  }

  @DisplayName("TraceDiagnosticAssert.lineNumber(...) tests")
  @Nested
  class LineNumberTest {

    @DisplayName(".lineNumber() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::lineNumber)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".lineNumber() returns assertions on the line number")
    @Test
    void returnsAssertionsOnTheLineNumber() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getLineNumber()).thenReturn(9876L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.lineNumber().isEqualTo(9876L))
          .doesNotThrowAnyException();

      assertThat(assertions.lineNumber().descriptionText())
          .isEqualTo("line number");
    }
  }

  @DisplayName("TraceDiagnosticAssert.columnNumber(...) tests")
  @Nested
  class ColumnNumberTest {

    @DisplayName(".columnNumber() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::columnNumber)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".columnNumber() returns assertions on the column number")
    @Test
    void returnsAssertionsOnTheColumnNumber() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getColumnNumber()).thenReturn(12L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.columnNumber().isEqualTo(12L))
          .doesNotThrowAnyException();

      assertThat(assertions.columnNumber().descriptionText())
          .isEqualTo("column number");
    }
  }

  @DisplayName("TraceDiagnosticAssert.code(...) tests")
  @Nested
  class CodeTest {

    @DisplayName(".code() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::code)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".code() returns assertions on the code")
    @Test
    void returnsAssertionsOnTheCode() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getCode()).thenReturn("it.is.broken");

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.code().isEqualTo("it.is.broken"))
          .doesNotThrowAnyException();

      assertThat(assertions.code().descriptionText())
          .isEqualTo("code");
    }
  }

  @DisplayName("TraceDiagnosticAssert.message() tests")
  @Nested
  class MessageTest {

    @DisplayName(".message() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::message)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".message() returns assertions on the message")
    @Test
    void returnsAssertionsOnTheMessage() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      var message = someText();
      when(element.getMessage(null)).thenReturn(message);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.message().isEqualTo(message))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("TraceDiagnosticAssert.message(Locale) tests")
  @Nested
  class MessageLocaleTest {

    @DisplayName(".message(Locale) fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.message(mock()))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".message(Locale) returns assertions on the message")
    @Test
    void returnsAssertionsOnTheMessage() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      var message = someText();
      var locale = mock(Locale.class);
      when(element.getMessage(any())).thenReturn(message);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.message(locale).isEqualTo(message))
          .doesNotThrowAnyException();

      verify(element).getMessage(locale);
      verifyNoMoreInteractions(element);
    }
  }

  @DisplayName("TraceDiagnosticAssert.timestamp(...) tests")
  @Nested
  class TimestampTest {

    @DisplayName(".timestamp() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::timestamp)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".timestamp() returns assertions on the timestamp")
    @Test
    void returnsAssertionsOnTheTimestamp() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      var timestamp = Instant.now();
      when(element.getTimestamp()).thenReturn(timestamp);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.timestamp().isEqualTo(timestamp))
          .doesNotThrowAnyException();
    }
  }


  @DisplayName("TraceDiagnosticAssert.threadId(...) tests")
  @Nested
  class ThreadIdTest {

    @DisplayName(".threadId() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::threadId)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".threadId() returns assertions on the thread id")
    @Test
    void returnsAssertionsOnTheThreadId() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getThreadId()).thenReturn(12L);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.threadId().isEqualTo(12L))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("TraceDiagnosticAssert.threadName(...) tests")
  @Nested
  class ThreadNameTest {

    @DisplayName(".threadName() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::threadName)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".threadName() returns assertions on the thread name")
    @Test
    void returnsAssertionsOnTheThreadName() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      when(element.getThreadName()).thenReturn("foobar");

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.threadName().isEqualTo("foobar"))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("TraceDiagnosticAssert.stackTrace(...) tests")
  @Nested
  class StackTraceTest {

    @DisplayName(".stackTrace() fails if the trace diagnostic is null")
    @Test
    void failsIfDiagnosticIsNull() {
      // Given
      var assertions = new TraceDiagnosticAssert(null);

      // Then
      assertThatThrownBy(assertions::stackTrace)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".stackTrace() returns assertions on the stack trace")
    @Test
    void returnsAssertionsOnTheStackTrace() {
      // Given
      TraceDiagnostic<JavaFileObject> element = mock();
      var stackTrace = someStackTraceList();
      when(element.getStackTrace()).thenReturn(stackTrace);

      var assertions = new TraceDiagnosticAssert(element);

      // Then
      assertThatCode(() -> assertions.stackTrace().isEqualTo(stackTrace))
          .doesNotThrowAnyException();
    }
  }
}
