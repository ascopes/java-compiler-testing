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
import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.testing.helpers.Slf4jLoggerFake;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

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
    final var listener = new TracingDiagnosticListener<>(false, false);

    var diag1 = someDiagnostic();
    when(diag1.getKind()).thenReturn(Kind.OTHER);
    when(diag1.getMessage(ROOT)).thenReturn("getDiagnosticsTest1");

    var diag2 = someDiagnostic();
    when(diag2.getKind()).thenReturn(Kind.NOTE);
    when(diag2.getMessage(ROOT)).thenReturn("getDiagnosticsTest2");

    var diag3 = someDiagnostic();
    when(diag3.getKind()).thenReturn(Kind.WARNING);
    when(diag3.getMessage(ROOT)).thenReturn("getDiagnosticsTest3");

    var diag4 = someDiagnostic();
    when(diag4.getKind()).thenReturn(Kind.MANDATORY_WARNING);
    when(diag4.getMessage(ROOT)).thenReturn("getDiagnosticsTest4");

    var diag5 = someDiagnostic();
    when(diag5.getKind()).thenReturn(Kind.ERROR);
    when(diag5.getMessage(ROOT)).thenReturn("getDiagnosticsTest5");

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
  @ParameterizedTest(name = "for logging={0}, stackTraces={1}")
  void diagnosticsAreLoggedWithTheExpectedTimestamp(boolean logging, boolean stackTraces) {
    // Given
    var listener = new AccessibleImpl<>(logging, stackTraces);
    var now = Instant.now();
    try (var mockedInstant = mockStatic(Instant.class)) {
      mockedInstant.when(Instant::now).thenReturn(now);
      var originalDiagnostic = someDiagnostic();
      when(originalDiagnostic.getKind()).thenReturn(Kind.OTHER);
      when(originalDiagnostic.getMessage(ROOT)).thenReturn("Testing timestamps");

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
  @ParameterizedTest(name = "for logging={0}, stackTraces={1}")
  @SuppressWarnings("deprecation")
  void diagnosticsAreLoggedWithTheExpectedThreadId(boolean logging, boolean stackTraces) {
    // Given
    var threadId = new Random().nextInt(15_999) + 1L;
    var currentThread = mock(Thread.class);

    // Thread#getId deprecated for Thread#threadId in Java 19.
    when(currentThread.getId()).thenReturn(threadId);
    when(currentThread.getStackTrace()).thenReturn(new StackTraceElement[0]);
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(Kind.OTHER);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("Testing thread IDs");

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
  @ParameterizedTest(name = "For logging={0}, stackTraces={1}")
  void diagnosticsAreLoggedWithTheExpectedThreadName(boolean logging, boolean stackTraces) {
    // Given
    var threadName = UUID.randomUUID().toString();
    var currentThread = mock(Thread.class);
    when(currentThread.getName()).thenReturn(threadName);
    when(currentThread.getStackTrace()).thenReturn(new StackTraceElement[0]);
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(Kind.OTHER);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("Testing non null thread names");

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
  @ParameterizedTest(name = "for logging={0}, stackTraces={1}")
  void diagnosticsAreLoggedWithNoThreadName(boolean logging, boolean stackTraces) {
    // Given
    var currentThread = mock(Thread.class);
    when(currentThread.getName()).thenReturn(null);
    when(currentThread.getStackTrace()).thenReturn(new StackTraceElement[0]);
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(Kind.OTHER);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("Testing null thread names");

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
  @ParameterizedTest(name = "for logging={0}, stackTraces={1}")
  void diagnosticsAreLoggedWithTheExpectedStackTrace(boolean logging, boolean stackTraces) {
    // Given
    var stackTrace = new StackTraceElement[0];
    var currentThread = mock(Thread.class);
    when(currentThread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(() -> currentThread, logging, stackTraces);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(Kind.OTHER);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("OTHER logging tests");

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
  @ParameterizedTest(name = "for kind={0}, stackTraces={1}")
  void nothingIsLoggedIfLoggingIsDisabled(Kind kind, boolean stackTraces) {
    // Given
    var logger = mock(Logger.class);
    var listener = new AccessibleImpl<>(logger, false, stackTraces);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("LoggingMode disabled tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    verifyNoInteractions(logger);
  }

  @DisplayName("Errors should be logged as errors when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = "ERROR")
  @ParameterizedTest(name = "for kind = {0}")
  void errorsShouldBeLoggedAsErrorsWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();
    var listener = new AccessibleImpl<>(logger, true, false);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("ERROR logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.ERROR,
        null,
        "{}{}",
        "ERROR logging tests", ""
    );
  }

  @DisplayName("Errors should be logged as errors when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = "ERROR")
  @ParameterizedTest(name = "for kind = {0}")
  void errorsShouldBeLoggedAsErrorsWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();

    var thread = mock(Thread.class);
    var stackTrace = new StackTraceElement[0];
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    final var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("ERROR logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.ERROR,
        null,
        "{}{}",
        "ERROR logging tests",
        expectedTraceString
    );
  }

  @DisplayName("Warnings should be logged as warnings when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = {"WARNING", "MANDATORY_WARNING"})
  @ParameterizedTest(name = "for kind = {0}")
  void warningsShouldBeLoggedAsWarningsWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();
    var listener = new AccessibleImpl<>(logger, true, false);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("WARNING logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.WARN,
        null,
        "{}{}",
        "WARNING logging tests", ""
    );
  }

  @DisplayName("Warnings should be logged as warnings when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = {"WARNING", "MANDATORY_WARNING"})
  @ParameterizedTest(name = "for kind = {0}")
  void warningsShouldBeLoggedAsWarningsWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();
    var thread = mock(Thread.class);
    var stackTrace = new StackTraceElement[0];
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    final var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("WARNING logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.WARN,
        null,
        "{}{}",
        "WARNING logging tests",
        expectedTraceString
    );
  }

  @DisplayName("Info should be logged as info when stacktraces are disabled")
  @EnumSource(value = Kind.class, names = {"NOTE", "OTHER"})
  @ParameterizedTest(name = "for kind = {0}")
  void infoShouldBeLoggedAsInfoWhenStackTracesAreDisabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();
    var listener = new AccessibleImpl<>(logger, true, false);

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("INFO logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.INFO,
        null,
        "{}{}",
        "INFO logging tests", ""
    );
  }

  @DisplayName("Info should be logged as info when stacktraces are enabled")
  @EnumSource(value = Kind.class, names = {"NOTE", "OTHER"})
  @ParameterizedTest(name = "for kind = {0}")
  void infoShouldBeLoggedAsInfoWhenStackTracesAreEnabled(Kind kind) {
    // Given
    var logger = new Slf4jLoggerFake();

    var thread = mock(Thread.class);
    var stackTrace = new StackTraceElement[0];
    when(thread.getStackTrace()).thenReturn(stackTrace);
    var listener = new AccessibleImpl<>(logger, () -> thread, true, true);

    final var expectedTraceString = Stream
        .of(stackTrace)
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());

    var originalDiagnostic = someDiagnostic();
    when(originalDiagnostic.getKind()).thenReturn(kind);
    when(originalDiagnostic.getMessage(ROOT)).thenReturn("INFO logging tests");

    // When
    listener.report(originalDiagnostic);

    // Then
    logger.assertThatEntryLogged(
        Level.INFO,
        null,
        "{}{}",
        "INFO logging tests",
        expectedTraceString
    );
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

  static Supplier<Thread> dummyThreadSupplier() {
    var thread = mock(Thread.class);
    when(thread.getStackTrace()).thenReturn(new StackTraceElement[0]);
    return () -> thread;
  }

  static class AccessibleImpl<T extends JavaFileObject> extends TracingDiagnosticListener<T> {

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
