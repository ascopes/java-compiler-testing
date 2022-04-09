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

package com.github.ascopes.jct.unittests.diagnostics;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;

import com.github.ascopes.jct.diagnostics.TraceDiagnostic;
import com.github.ascopes.jct.unittests.helpers.MoreMocks;
import com.github.ascopes.jct.unittests.helpers.TypeRef;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TraceDiagnostic} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnostic tests")
class TraceDiagnosticTest {

  @DisplayName("null timestamps are rejected")
  @Test
  void nullTimestampsAreRejected() {
    var stack = MoreMocks.stubCast(new TypeRef<List<StackTraceElement>>() {});
    var diag = MoreMocks.stubCast(new TypeRef<Diagnostic<?>>() {});
    thenCode(() -> new TraceDiagnostic<>(null, 123, "foo", stack, diag))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("null stack traces are rejected")
  @Test
  void nullStackTracesAreRejected() {
    var now = Instant.now();
    var diag = MoreMocks.stubCast(new TypeRef<Diagnostic<?>>() {});
    thenCode(() -> new TraceDiagnostic<>(now, 123, "foo", null, diag))
        .isInstanceOf(NullPointerException.class);
  }

  @DisplayName("getTimestamp() returns the timestamp")
  @Test
  void getTimestampReturnsTheTimestamp() {
    // Given
    var expectedTimestamp = Instant.now();
    var diagnostic = new TraceDiagnostic<>(
        expectedTimestamp,
        123,
        "foo",
        MoreMocks.stubCast(new TypeRef<>() {}),
        MoreMocks.stubCast(new TypeRef<>() {})
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
        Instant.now(),
        expectedThreadId,
        "foo",
        MoreMocks.stubCast(new TypeRef<>() {}),
        MoreMocks.stubCast(new TypeRef<>() {})
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
        Instant.now(),
        1234,
        expectedThreadName,
        MoreMocks.stubCast(new TypeRef<>() {}),
        MoreMocks.stubCast(new TypeRef<>() {})
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
        Instant.now(),
        1234,
        null,
        MoreMocks.stubCast(new TypeRef<>() {}),
        MoreMocks.stubCast(new TypeRef<>() {})
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
    var expectedStackTrace = MoreMocks.stubCast(new TypeRef<List<StackTraceElement>>() {});
    var diagnostic = new TraceDiagnostic<>(
        Instant.now(),
        1234,
        "foo",
        expectedStackTrace,
        MoreMocks.stubCast(new TypeRef<>() {})
    );

    // When
    var actualStackTrace = diagnostic.getStackTrace();

    // Then
    then(actualStackTrace).isSameAs(expectedStackTrace);
  }
}
