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
package io.github.ascopes.jct.tests.unit.diagnostics;

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someDiagnostic;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someInt;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLong;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someStackTraceList;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.utils.LoomPolyfill;
import io.github.ascopes.jct.utils.StringUtils;
import java.util.Locale;
import java.util.Random;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link TraceDiagnostic} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnostic tests")
class TraceDiagnosticTest {

  @DisplayName("null original diagnostics are rejected")
  @Test
  void nullDiagnosticsAreRejected() {
    // Given
    var stack = someStackTraceList();

    // Then
    assertThatThrownBy(() -> new TraceDiagnostic<>(now(), 123, "foo", stack, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("original");
  }

  @DisplayName("getKind() delegates to the inner diagnostic")
  @EnumSource(Kind.class)
  @ParameterizedTest(name = "for kind = {0}")
  void getKindDelegates(Kind expected) {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    when(original.getKind()).thenReturn(expected);

    // Then
    assertThat(wrapped.getKind()).isSameAs(expected);
  }

  @DisplayName("getSource() delegates to the inner diagnostic")
  @Test
  void getSourceDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var source = mock(JavaFileObject.class);
    when(original.getSource()).thenReturn(source);

    // Then
    assertThat(wrapped.getSource()).isSameAs(source);
  }

  @DisplayName("getPosition() delegates to the inner diagnostic")
  @Test
  void getPositionDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var position = new Random().nextLong();
    when(original.getPosition()).thenReturn(position);

    // Then
    assertThat(wrapped.getPosition()).isEqualTo(position);
  }

  @DisplayName("getStartPosition() delegates to the inner diagnostic")
  @Test
  void getStartPositionDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var startPosition = new Random().nextLong();
    when(original.getStartPosition()).thenReturn(startPosition);

    // Then
    assertThat(wrapped.getStartPosition()).isEqualTo(startPosition);
  }

  @DisplayName("getEndPosition() delegates to the inner diagnostic")
  @Test
  void getEndPositionDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var endPosition = new Random().nextLong();
    when(original.getEndPosition()).thenReturn(endPosition);

    // Then
    assertThat(wrapped.getEndPosition()).isEqualTo(endPosition);
  }

  @DisplayName("getLineNumber() delegates to the inner diagnostic")
  @Test
  void getLineNumberDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var lineNumber = new Random().nextLong();
    when(original.getLineNumber()).thenReturn(lineNumber);

    // Then
    assertThat(wrapped.getLineNumber()).isEqualTo(lineNumber);
  }

  @DisplayName("getColumnNumber() delegates to the inner diagnostic")
  @Test
  void getColumnNumberDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var columnNumber = new Random().nextLong();
    when(original.getColumnNumber()).thenReturn(columnNumber);

    // Then
    assertThat(wrapped.getColumnNumber()).isEqualTo(columnNumber);
  }

  @DisplayName("getCode() delegates to the inner diagnostic")
  @NullSource
  @ValueSource(strings = "you.messed.up.something")
  @ParameterizedTest(name = "for code = \"{0}\"")
  void getCodeDelegates(String expected) {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    when(original.getCode()).thenReturn(expected);

    // Then
    assertThat(wrapped.getCode()).isSameAs(expected);
  }

  @DisplayName("getMessage() delegates to the inner diagnostic")
  @Test
  void getMessageDelegates() {
    // Given
    var original = someDiagnostic();
    var stack = someStackTraceList();
    var wrapped = new TraceDiagnostic<>(now(), 123, "foo", stack, original);
    var message = someText();
    when(original.getMessage(any())).thenReturn(message);

    // Then
    assertThat(wrapped.getMessage(Locale.TAIWAN)).isSameAs(message);
    verify(original).getMessage(Locale.TAIWAN);
  }

  @DisplayName("null timestamps are rejected")
  @Test
  void nullTimestampsAreRejected() {
    var diag = someDiagnostic();
    var stack = someStackTraceList();
    assertThatThrownBy(() -> new TraceDiagnostic<>(null, 123, "foo", stack, diag))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timestamp");
  }

  @DisplayName("null stack traces are rejected")
  @Test
  void nullStackTracesAreRejected() {
    var now = now();
    var diag = someDiagnostic();
    assertThatThrownBy(() -> new TraceDiagnostic<>(now, 123, "foo", null, diag))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("stackTrace");
  }

  @DisplayName("getTimestamp() returns the timestamp")
  @Test
  void getTimestampReturnsTheTimestamp() {
    // Given
    var expectedTimestamp = now();
    var diagnostic = new TraceDiagnostic<>(
        expectedTimestamp,
        123,
        "foo",
        someStackTraceList(),
        someDiagnostic()
    );

    // When
    var actualTimestamp = diagnostic.getTimestamp();

    // Then
    assertThat(actualTimestamp).isEqualTo(expectedTimestamp);
  }

  @DisplayName("getThreadId() returns the thread ID")
  @Test
  void getThreadIdReturnsTheThreadId() {
    // Given
    var expectedThreadId = LoomPolyfill.getThreadId(Thread.currentThread()) + someInt(100);
    var diagnostic = new TraceDiagnostic<>(
        now(),
        expectedThreadId,
        "foo",
        someStackTraceList(),
        someDiagnostic()
    );

    // When
    var actualThreadId = diagnostic.getThreadId();

    // Then
    assertThat(actualThreadId).isEqualTo(expectedThreadId);
  }

  @DisplayName("getThreadName() returns the thread name when known")
  @Test
  void getThreadNameReturnsTheThreadNameWhenKnown() {
    // Given
    var expectedThreadName = someText();
    var diagnostic = new TraceDiagnostic<>(
        now(),
        1234,
        expectedThreadName,
        someStackTraceList(),
        someDiagnostic()
    );

    // When
    var actualThreadName = diagnostic.getThreadName();

    // Then
    assertThat(actualThreadName).isEqualTo(expectedThreadName);
  }

  @DisplayName("getThreadName() returns empty when the thread name is not known")
  @Test
  void getThreadNameReturnsEmptyWhenTheThreadNameIsNotKnown() {
    // Given
    var diagnostic = new TraceDiagnostic<>(
        now(),
        1234,
        null,
        someStackTraceList(),
        someDiagnostic()
    );

    // When
    var actualThreadName = diagnostic.getThreadName();

    // Then
    assertThat(actualThreadName).isNull();
  }

  @DisplayName("getStackTrace() returns the stack trace")
  @Test
  void getStackTraceReturnsTheStackTrace() {
    // Given
    var expectedStackTrace = someStackTraceList();
    var diagnostic = new TraceDiagnostic<>(
        now(),
        1234,
        "foo",
        expectedStackTrace,
        someDiagnostic()
    );

    // When
    var actualStackTrace = diagnostic.getStackTrace();

    // Then
    assertThat(actualStackTrace).isEqualTo(expectedStackTrace);
  }

  @DisplayName("getStackTrace() returns an immutable list")
  @Test
  void getStackTraceReturnsAnImmutableStackTrace() {
    // Given
    var expectedStackTrace = someStackTraceList();
    var diagnostic = new TraceDiagnostic<>(
        now(),
        1234,
        "foo",
        expectedStackTrace,
        someDiagnostic()
    );

    var actualStackTrace = diagnostic.getStackTrace();

    // Then
    assertThatThrownBy(() -> actualStackTrace.remove(0))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @DisplayName("toString() returns the expected value")
  @RepeatedTest(5)
  void toStringReturnsExpectedValue() {
    // Given
    var expectedStackTrace = someStackTraceList();
    var original = someDiagnostic();
    when(original.getKind()).thenReturn(oneOf(Kind.class));
    when(original.getCode()).thenReturn(oneOf(null, someText()));
    when(original.getColumnNumber()).thenReturn(someLong(99));
    when(original.getLineNumber()).thenReturn(someLong(500));
    when(original.getMessage(any())).thenReturn(someText());

    var diagnostic = new TraceDiagnostic<>(
        now(),
        1234,
        "foo",
        expectedStackTrace,
        original
    );

    // Then
    var expectedFmt = String.join(
        "",
        "TraceDiagnostic{",
        "timestamp=%s, ",
        "threadId=%s, ",
        "threadName=%s, ",
        "kind=%s, ",
        "code=%s, ",
        "column=%s, ",
        "line=%s, ",
        "message=%s",
        "}"
    );

    assertThat(diagnostic)
        .asString()
        .as("diagnostic.toString()")
        .isEqualTo(
            expectedFmt,
            StringUtils.quoted(diagnostic.getTimestamp()),
            diagnostic.getThreadId(),
            StringUtils.quoted(diagnostic.getThreadName()),
            diagnostic.getKind(),
            StringUtils.quoted(diagnostic.getCode()),
            diagnostic.getColumnNumber(),
            diagnostic.getLineNumber(),
            StringUtils.quoted(diagnostic.getMessage(Locale.ROOT))
        );
  }
}
