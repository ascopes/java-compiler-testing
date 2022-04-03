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

import java.time.Clock;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of {@link TracingDiagnosticsListener} which also logs the diagnostics that occur.
 *
 * @param <S> the file object implementation type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class LoggingTracingDiagnosticsListener<S extends JavaFileObject>
    extends TracingDiagnosticsListener<S> {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      LoggingTracingDiagnosticsListener.class
  );

  private final boolean dumpStackTraces;

  /**
   * Initialize this listener.
   *
   * @param clock           the clock to use for the current time.
   * @param dumpStackTraces {@code true} to dump a stacktrace of each diagnostic to the logger, or
   *                        {@code false} to hide the stack traces in log output.
   */
  public LoggingTracingDiagnosticsListener(Clock clock, boolean dumpStackTraces) {
    super(clock);
    this.dumpStackTraces = dumpStackTraces;
  }

  @Override
  protected TraceDiagnostic<S> handleDiagnostic(Diagnostic<? extends S> diagnostic) {
    var wrappedDiagnostic = super.handleDiagnostic(diagnostic);

    var message = new StringBuilder("[").append(diagnostic.getKind()).append("] ")
        .append("<code=").append(diagnostic.getCode()).append("> ")
        .append(diagnostic.getMessage(Locale.ROOT));

    if (dumpStackTraces) {
      for (var frame : wrappedDiagnostic.getStackTrace()) {
        message.append("\n\tat ").append(frame);
      }
    }

    switch (diagnostic.getKind()) {
      case ERROR:
        LOGGER.error("{}", message);
        break;
      case MANDATORY_WARNING:
      case WARNING:
        LOGGER.warn("{}", message);
        break;
      default:
        LOGGER.info("{}", message);
        break;
    }

    return wrappedDiagnostic;
  }
}
