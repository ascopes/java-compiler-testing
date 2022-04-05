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

package com.github.ascopes.jct.test.diagnostics;

import static com.github.ascopes.jct.test.helpers.MoreMocks.hasToString;
import static com.github.ascopes.jct.test.helpers.MoreMocks.stub;
import static com.github.ascopes.jct.test.helpers.MoreMocks.stubCast;
import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.github.ascopes.jct.diagnostics.TraceDiagnostic;
import com.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import com.github.ascopes.jct.test.helpers.TypeRef;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TracingDiagnosticListener} tests.
 *
 * <p>Due to the reflective nature of this class, this test pack is somewhat delicate and
 * may break easily. Please be careful when editing it.
 *
 * @author Ashley Scopes
 */
@DisplayName("TracingDiagnosticListener test")
class TracingDiagnosticListenerTest {

  @DisplayName("getDiagnostics() returns a copy")
  @Test
  void getDiagnosticsReturnsCopy() {
    // Given
    var listener = new TracingDiagnosticListener<>(false, false);
    var diag1 = someDiagnostic(Kind.OTHER, "getDiagnosticsTest1");
    var diag2 = someDiagnostic(Kind.NOTE, "getDiagnosticsTest2");
    var diag3 = someDiagnostic(Kind.WARNING, "getDiagnosticsTest3");
    var diag4 = someDiagnostic(Kind.MANDATORY_WARNING, "getDiagnosticsTest4");
    var diag5 = someDiagnostic(Kind.ERROR, "getDiagnosticsTest5");

    // When
    listener.report(diag1);
    listener.report(diag2);
    final var firstDiagnostics = listener.getDiagnostics();
    listener.report(diag3);
    listener.report(diag4);
    listener.report(diag5);
    final var secondDiagnostics = listener.getDiagnostics();

    // Then
    assertThat(firstDiagnostics)
        .hasSize(2)
        .satisfiesExactly(
            trace1 -> assertThat(trace1.getMessage(ROOT)).isEqualTo(diag1.getMessage(ROOT)),
            trace2 -> assertThat(trace2.getMessage(ROOT)).isEqualTo(diag2.getMessage(ROOT))
        );

    assertThat(secondDiagnostics)
        .hasSize(5)
        .satisfiesExactly(
            trace1 -> assertThat(trace1.getMessage(ROOT)).isEqualTo(diag1.getMessage(ROOT)),
            trace2 -> assertThat(trace2.getMessage(ROOT)).isEqualTo(diag2.getMessage(ROOT)),
            trace3 -> assertThat(trace3.getMessage(ROOT)).isEqualTo(diag3.getMessage(ROOT)),
            trace4 -> assertThat(trace4.getMessage(ROOT)).isEqualTo(diag4.getMessage(ROOT)),
            trace5 -> assertThat(trace5.getMessage(ROOT)).isEqualTo(diag5.getMessage(ROOT))
        );
  }

  @DisplayName("Diagnostics are logged with the expected timestamp")
  @MethodSource("loggingArgs")
  @ParameterizedTest(
      name = "Diagnostics are logged with the expected timestamp for logging={0}, stackTraces={1}"
  )
  void diagnosticsAreLoggedWithTheExpectedTimestamp(boolean logging, boolean stackTraces) {
    // Given
    var listener = new AccessibleImpl<>(logging, stackTraces);
    var now = Instant.now();
    try (var mockedInstant = mockStatic(Instant.class)) {
      mockedInstant.when(Instant::now).thenReturn(now);
      var originalDiagnostic = someDiagnostic(Kind.NOTE, "Testing timestamps");

      // When
      listener.report(originalDiagnostic);

      // Then
      assertThat(listener.getDiagnostics())
          .singleElement()
          .extracting(TraceDiagnostic::getTimestamp)
          .isEqualTo(now);
    }
  }

  @DisplayName("Diagnostics are logged with the expected thread ID")
  @MethodSource("loggingArgs")
  @ParameterizedTest(
      name = "Diagnostics are logged with the expected thread ID for logging={0}, stackTraces={1}"
  )
  void diagnosticsAreLoggedWithTheExpectedThreadId(boolean logging, boolean stackTraces) {
    // Given
    var threadId = new Random().nextInt(15_999) + 1L;
    var currentThread = stub(Thread.class);
    when(currentThread.getId()).thenReturn(threadId);
    when(currentThread.getStackTrace()).thenAnswer(ctx -> someStackTrace());
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic(Kind.OTHER, "Testing thread IDs");

    // When
    listener.report(originalDiagnostic);

    // Then
    assertThat(listener.getDiagnostics())
        .singleElement()
        .extracting(TraceDiagnostic::getThreadId)
        .isEqualTo(threadId);
  }

  @DisplayName("Diagnostics are logged with the expected thread name")
  @MethodSource("loggingArgs")
  @ParameterizedTest(
      name = "Diagnostics are logged with the expected thread name for logging={0}, stackTraces={1}"
  )
  void diagnosticsAreLoggedWithTheExpectedThreadName(boolean logging, boolean stackTraces) {
    // Given
    var threadName = UUID.randomUUID().toString();
    var currentThread = stub(Thread.class);
    when(currentThread.getName()).thenReturn(threadName);
    when(currentThread.getStackTrace()).thenAnswer(ctx -> someStackTrace());
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic(Kind.OTHER, "Testing non-null thread names");

    // When
    listener.report(originalDiagnostic);

    // Then
    assertThat(listener.getDiagnostics())
        .singleElement()
        .extracting(TraceDiagnostic::getThreadName, optional(String.class))
        .isPresent()
        .hasValue(threadName);
  }

  @DisplayName("Diagnostics are logged with no thread name")
  @MethodSource("loggingArgs")
  @ParameterizedTest(
      name = "Diagnostics are logged with no thread name for logging={0}, stackTraces={1}"
  )
  void diagnosticsAreLoggedWithNoThreadName(boolean logging, boolean stackTraces) {
    // Given
    var currentThread = stub(Thread.class);
    when(currentThread.getName()).thenReturn(null);
    when(currentThread.getStackTrace()).thenAnswer(ctx -> someStackTrace());
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic(Kind.OTHER, "Testing null thread names");

    // When
    listener.report(originalDiagnostic);

    // Then
    assertThat(listener.getDiagnostics())
        .singleElement()
        .extracting(TraceDiagnostic::getThreadName, optional(String.class))
        .isNotPresent();
  }

  @DisplayName("Diagnostics are logged with the expected stacktrace")
  @MethodSource("loggingArgs")
  @ParameterizedTest(
      name = "Diagnostics are logged with the expected stacktrace for logging={0}, stackTraces={1}"
  )
  void diagnosticsAreLoggedWithTheExpectedStackTrace(boolean logging, boolean stackTraces) {
    // Given
    var stackTrace = someStackTrace();
    var currentThread = stub(Thread.class);
    when(currentThread.getStackTrace()).thenAnswer(ctx -> stackTrace);
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic(Kind.OTHER, "Testing stack traces");

    // When
    listener.report(originalDiagnostic);

    // Then
    assertThat(listener.getDiagnostics())
        .singleElement()
        .extracting(TraceDiagnostic::getStackTrace, list(StackTraceElement.class))
        .isEqualTo(List.of(stackTrace));
  }

  @DisplayName("Nothing is logged if logging is disabled")
  @MethodSource("loggingDisabledArgs")
  @ParameterizedTest(
      name = "Nothing is logged if logging is disabled for kind={0}, stackTraces={1}"
  )
  void nothingIsLoggedIfLoggingIsDisabled(Kind kind, boolean stackTraces) {
    // Given
    var logger = mock(Logger.class);
    var listener = new AccessibleImpl<>(logger, false, stackTraces);

    // When
    var originalDiagnostic = someDiagnostic(kind, "Logging disabled tests");
    listener.report(originalDiagnostic);

    // Then
    verifyNoInteractions(logger);
  }

  @DisplayName("Errors should be logged as errors when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = "ERROR")
  @ParameterizedTest(name = "{0} should be logged as errors when stacktraces are disabled")
  void errorsShouldBeLoggedAsErrorsWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);
    var listener = new AccessibleImpl<>(logger, true, false);

    // When
    var originalDiagnostic = someDiagnostic(kind, "ERROR logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger).error("{}{}", "ERROR logging tests", "");
    verifyNoMoreInteractions(logger);
  }

  @DisplayName("Errors should be logged as errors when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = "ERROR")
  @ParameterizedTest(name = "{0} should be logged as errors when stacktraces are enabled")
  void errorsShouldBeLoggedAsErrorsWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);

    var thread = stub(Thread.class);
    var stackTrace = someStackTrace();
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    // When
    var originalDiagnostic = someDiagnostic(kind, "ERROR logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger)
        .error(eq("{}{}"), eq("ERROR logging tests"), hasToString(expectedTraceString));
    verifyNoMoreInteractions(logger);
  }

  @DisplayName("Warnings should be logged as warnings when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = {"WARNING", "MANDATORY_WARNING"})
  @ParameterizedTest(name = "{0} should be logged as warnings when stacktraces are disabled")
  void warningsShouldBeLoggedAsWarningsWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);
    var listener = new AccessibleImpl<>(logger, true, false);

    // When
    var originalDiagnostic = someDiagnostic(kind, "WARNING logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger).warn("{}{}", "WARNING logging tests", "");
    verifyNoMoreInteractions(logger);
  }

  @DisplayName("Warnings should be logged as warnings when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = {"WARNING", "MANDATORY_WARNING"})
  @ParameterizedTest(name = "{0} should be logged as warnings when stacktraces are enabled")
  void warningsShouldBeLoggedAsWarningsWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);

    var thread = stub(Thread.class);
    var stackTrace = someStackTrace();
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    // When
    var originalDiagnostic = someDiagnostic(kind, "WARNING logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger)
        .warn(eq("{}{}"), eq("WARNING logging tests"), hasToString(expectedTraceString));
    verifyNoMoreInteractions(logger);
  }

  @DisplayName("Info should be logged as info when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = {"NOTE", "OTHER"})
  @ParameterizedTest(name = "{0} should be logged as info when stacktraces are disabled")
  void infoShouldBeLoggedAsInfoWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);
    var listener = new AccessibleImpl<>(logger, true, false);

    // When
    var originalDiagnostic = someDiagnostic(kind, "INFO logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger).info("{}{}", "INFO logging tests", "");
    verifyNoMoreInteractions(logger);
  }

  @DisplayName("Info should be logged as info when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = {"NOTE", "OTHER"})
  @ParameterizedTest(name = "{0} should be logged as info when stacktraces are enabled")
  void infoShouldBeLoggedAsInfoWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = mock(Logger.class);

    var thread = stub(Thread.class);
    var stackTrace = someStackTrace();
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    // When
    var originalDiagnostic = someDiagnostic(kind, "INFO logging tests");
    listener.report(originalDiagnostic);

    // Then
    verify(logger)
        .info(eq("{}{}"), eq("INFO logging tests"), hasToString(expectedTraceString));
    verifyNoMoreInteractions(logger);
  }

  static Stream<Arguments> loggingArgs() {
    return Stream.of(
        Arguments.of(false, false),
        Arguments.of(false, true),
        Arguments.of(true, false),
        Arguments.of(true, true)
    );
  }

  static Stream<Arguments> loggingDisabledArgs() {
    return Stream
        .of(Kind.values())
        .flatMap(kind -> Stream.of(
            Arguments.of(kind, true),
            Arguments.of(kind, false)
        ));
  }

  static <T> Diagnostic<T> someDiagnostic(Kind kind, String message) {
    var diagnostic = stubCast(new TypeRef<Diagnostic<T>>() {}, withSettings().lenient());
    when(diagnostic.getKind()).thenReturn(kind);
    when(diagnostic.getMessage(any())).thenReturn(message);
    return diagnostic;
  }

  static StackTraceElement[] someStackTrace() {
    var stackTrace = Thread.currentThread().getStackTrace();
    var maxSize = Math.min(5, stackTrace.length);
    return Arrays.copyOf(stackTrace, maxSize);
  }

  static Supplier<Thread> dummyThreadSupplier() {
    var thread = stub(Thread.class);
    when(thread.getStackTrace()).thenReturn(someStackTrace());
    return () -> thread;
  }

  static class AccessibleImpl<T> extends TracingDiagnosticListener<T> {

    AccessibleImpl(
        boolean logging,
        boolean stackTraces
    ) {
      super(
          LoggerFactory.getLogger(AccessibleImpl.class),
          dummyThreadSupplier(),
          logging,
          stackTraces
      );
    }

    AccessibleImpl(
        Logger logger,
        boolean logging,
        boolean stackTraces
    ) {
      super(
          logger,
          dummyThreadSupplier(),
          logging,
          stackTraces
      );
    }

    AccessibleImpl(
        Supplier<Thread> currentThreadSupplier,
        boolean logging,
        boolean stackTraces
    ) {
      super(
          LoggerFactory.getLogger(AccessibleImpl.class),
          currentThreadSupplier,
          logging,
          stackTraces
      );
    }

    AccessibleImpl(
        Logger logger,
        Supplier<Thread> currentThreadSupplier,
        boolean logging,
        boolean stackTraces
    ) {
      super(
          logger,
          currentThreadSupplier,
          logging,
          stackTraces
      );
    }
  }
}
