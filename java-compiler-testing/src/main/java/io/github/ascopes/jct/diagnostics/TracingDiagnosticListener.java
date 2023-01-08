/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.diagnostics;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * A diagnostics listener that wraps all diagnostics in additional invocation information, and then
 * stores them in a queue for processing later.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public class TracingDiagnosticListener<S extends JavaFileObject> implements DiagnosticListener<S> {

  private final ConcurrentLinkedQueue<TraceDiagnostic<S>> diagnostics;
  private final Logger logger;
  private final Supplier<? extends Thread> threadGetter;
  private final boolean logging;
  private final boolean stackTraces;

  /**
   * Initialize this listener.
   *
   * @param logging     {@code true} if logging is enabled, {@code false} otherwise.
   * @param stackTraces {@code true} if logging stack traces is enabled, {@code false} otherwise.
   *                    This is ignored if {@code logging} is {@code false}.
   */
  public TracingDiagnosticListener(
      boolean logging,
      boolean stackTraces
  ) {
    this(
        LoggerFactory.getLogger(TracingDiagnosticListener.class),
        Thread::currentThread,
        logging,
        stackTraces
    );
  }

  /**
   * Only visible for testing.
   *
   * @param logger       the logger to use.
   * @param threadGetter the supplier of the current thread.
   * @param logging      whether to enable logging.
   * @param stackTraces  whether to enable stack traces in the logging.
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  protected TracingDiagnosticListener(
      Logger logger,
      Supplier<? extends Thread> threadGetter,
      boolean logging,
      boolean stackTraces
  ) {
    diagnostics = new ConcurrentLinkedQueue<>();
    this.logger = requireNonNull(logger, "logger");
    this.threadGetter = requireNonNull(threadGetter, "threadGetter");
    this.logging = logging;
    this.stackTraces = stackTraces;
  }

  /**
   * Determine if logging is enabled.
   *
   * @return {@code true} if enabled, or {@code false} if disabled.
   */
  public boolean isLoggingEnabled() {
    return logging;
  }

  /**
   * Determine if stack trace reporting is enabled.
   *
   * <p>Note that stack traces are only reported if {@link #isLoggingEnabled() logging is enabled}
   * as well.
   *
   * @return {@code true} if enabled, or {@code false} if disabled.
   */
  public boolean isStackTraceReportingEnabled() {
    return stackTraces;
  }

  /**
   * Get a copy of the queue containing all the diagnostics that have been detected.
   *
   * @return the diagnostics in a list.
   */
  public List<TraceDiagnostic<? extends S>> getDiagnostics() {
    return List.copyOf(diagnostics);
  }

  @Override
  public final void report(Diagnostic<? extends S> diagnostic) {
    requireNonNull(diagnostic);

    var now = Instant.now();
    var thisThread = threadGetter.get();
    var threadName = thisThread.getName();
    var stackTrace = List.of(thisThread.getStackTrace());

    // Thread#getId deprecated for Thread#threadId in Java 19.
    @SuppressWarnings("deprecation")
    var threadId = thisThread.getId();

    var wrapped = new TraceDiagnostic<S>(now, threadId, threadName, stackTrace, diagnostic);

    diagnostics.add(wrapped);

    if (!logging) {
      return;
    }

    logger
        .atLevel(diagnosticToLevel(diagnostic))
        .setMessage("{}{}")
        .addArgument(messageGetter(wrapped))
        .addArgument(stackTraceFormatter(stackTrace))
        .log();
  }

  private Level diagnosticToLevel(Diagnostic<?> diagnostic) {
    switch (diagnostic.getKind()) {
      case ERROR:
        return Level.ERROR;
      case WARNING:
      case MANDATORY_WARNING:
        return Level.WARN;
      default:
        return Level.INFO;
    }
  }

  private Supplier<String> messageGetter(Diagnostic<?> diagnostic) {
    return () -> diagnostic.getMessage(Locale.ROOT);
  }

  private Supplier<String> stackTraceFormatter(List<StackTraceElement> stackTrace) {
    if (!stackTraces) {
      return () -> "";
    }

    return () -> stackTrace
        .stream()
        .map(frame -> "\n\t" + frame)
        .collect(Collectors.joining());
  }
}
