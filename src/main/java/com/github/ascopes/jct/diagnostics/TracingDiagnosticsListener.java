package com.github.ascopes.jct.diagnostics;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

/**
 * A diagnostics listener that wraps all diagnostics in additional invocation information, and then
 * stores them in a queue for processing later.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class TracingDiagnosticsListener<S> implements DiagnosticListener<S> {

  private final Clock clock;
  private final ConcurrentLinkedQueue<DiagnosticWithTrace<S>> diagnostics;

  /**
   * Initialize this listener.
   *
   * @param clock the clock to use for getting the current time.
   */
  public TracingDiagnosticsListener(Clock clock) {
    this.clock = clock;
    diagnostics = new ConcurrentLinkedQueue<>();
  }

  /**
   * Get a copy of the queue containing all the diagnostics that have been detected.
   *
   * @return the diagnostics in a list.
   */
  public List<DiagnosticWithTrace<? extends S>> getDiagnostics() {
    return List.copyOf(diagnostics);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void report(Diagnostic<? extends S> diagnostic) {
    diagnostics.add(handleDiagnostic(diagnostic));
  }

  protected DiagnosticWithTrace<S> handleDiagnostic(Diagnostic<? extends S> diagnostic) {
    var now = Instant.now(clock);
    var thisThread = Thread.currentThread();
    var threadId = thisThread.getId();
    var threadName = thisThread.getName();
    var stackTrace = thisThread.getStackTrace();
    return new DiagnosticWithTrace<>(now, threadId, threadName, stackTrace, diagnostic);
  }
}
