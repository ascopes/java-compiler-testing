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

package com.github.ascopes.jct.diagnostics;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A diagnostics listener that wraps all diagnostics in additional invocation information, and then
 * stores them in a queue for processing later.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class TracingDiagnosticListener<S> implements DiagnosticListener<S> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TracingDiagnosticListener.class);

  private final ConcurrentLinkedQueue<TraceDiagnostic<S>> diagnostics;
  private final boolean logging;
  private final boolean stackTraces;

  /**
   * Initialize this listener.
   *
   * @param logging     {@code true} if logging is enabled, {@code false} otherwise.
   * @param stackTraces {@code true} if logging stack traces is enabled, {@code false} otherwise.
   *                    This is ignored if {@code logging} is {@code false}.
   */
  public TracingDiagnosticListener(boolean logging, boolean stackTraces) {
    diagnostics = new ConcurrentLinkedQueue<>();
    this.logging = logging;
    this.stackTraces = stackTraces;
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
    var now = Instant.now();
    var thisThread = Thread.currentThread();
    var threadId = thisThread.getId();
    var threadName = thisThread.getName();
    var stackTrace = List.of(Thread.currentThread().getStackTrace());
    var wrapped = new TraceDiagnostic<S>(now, threadId, threadName, stackTrace, diagnostic);
    diagnostics.add(wrapped);

    if (!logging) {
      return;
    }

    var formattedMessage = wrapped.getMessage(Locale.ROOT);
    var formattedStackTrace = !stackTraces ? "" : new Object() {
      @Override
      public String toString() {
        // Evaluate this here; SLF4J will then only generate these large strings if the log level
        // is enabled.
        return stackTrace
            .stream()
            .map(frame -> "\n\t" + frame)
            .collect(Collectors.joining());
      }
    };

    switch (diagnostic.getKind()) {
      case ERROR:
        LOGGER.error("{}{}", formattedMessage, formattedStackTrace);
        break;

      case WARNING:
      case MANDATORY_WARNING:
        LOGGER.warn("{}{}", formattedMessage, formattedStackTrace);
        break;

      default:
        LOGGER.info("{}{}", formattedMessage, formattedStackTrace);
        break;
    }
  }
}
