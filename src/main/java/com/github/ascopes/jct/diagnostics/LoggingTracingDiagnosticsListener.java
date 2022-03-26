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
   * @param clock           the clock to use for the current time.
   * @param dumpStackTraces {@code true} to dump a stacktrace of each diagnostic to the logger, or
   *                        {@code false} to hide the stack traces in log output.
   */
  public LoggingTracingDiagnosticsListener(Clock clock, boolean dumpStackTraces) {
    super(clock);
    this.dumpStackTraces = dumpStackTraces;
  }

  @Override
  protected DiagnosticWithTrace<S> handleDiagnostic(Diagnostic<? extends S> diagnostic) {
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
