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
package io.github.ascopes.jct.testing.unit.diagnostics;

import static io.github.ascopes.jct.testing.helpers.Fixtures.someDiagnostic;
import static io.github.ascopes.jct.testing.helpers.Fixtures.someStackTraceList;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
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
    var stack = someStackTraceList();
    thenCode(() -> new TraceDiagnostic<>(now(), 123, "foo", stack, null))
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
    given(original.getKind()).willReturn(expected);

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

    given(original.getSource()).willReturn(source);

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
    given(original.getPosition()).willReturn(position);

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
    given(original.getStartPosition()).willReturn(startPosition);

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
    given(original.getEndPosition()).willReturn(endPosition);

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
    given(original.getLineNumber()).willReturn(lineNumber);

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
    given(original.getColumnNumber()).willReturn(columnNumber);

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
    given(original.getCode()).willReturn(expected);

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
    var message = UUID.randomUUID().toString();
    given(original.getMessage(any())).willReturn(message);

    // Then
    assertThat(wrapped.getMessage(Locale.TAIWAN)).isSameAs(message);
    verify(original).getMessage(Locale.TAIWAN);
  }

  @DisplayName("null timestamps are rejected")
  @Test
  void nullTimestampsAreRejected() {
    var diag = someDiagnostic();
    var stack = someStackTraceList();
    thenCode(() -> new TraceDiagnostic<>(null, 123, "foo", stack, diag))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timestamp");
  }

  @DisplayName("null stack traces are rejected")
  @Test
  void nullStackTracesAreRejected() {
    var now = now();
    var diag = someDiagnostic();
    thenCode(() -> new TraceDiagnostic<>(now, 123, "foo", null, diag))
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
    then(actualTimestamp).isEqualTo(expectedTimestamp);
  }

  @DisplayName("getThreadId() returns the thread ID")
  @Test
  void getThreadIdReturnsTheThreadId() {
    // Given
    var expectedThreadId = Thread.currentThread().getId() + new Random().nextInt(100);
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
    then(actualThreadId).isEqualTo(expectedThreadId);
  }

  @DisplayName("getThreadName() returns the thread name when known")
  @Test
  void getThreadNameReturnsTheThreadNameWhenKnown() {
    // Given
    var expectedThreadName = UUID.randomUUID().toString();
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
    then(actualThreadName).isPresent().contains(expectedThreadName);
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
    then(actualThreadName).isEmpty();
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
    then(actualStackTrace).isSameAs(expectedStackTrace);
  }
}
