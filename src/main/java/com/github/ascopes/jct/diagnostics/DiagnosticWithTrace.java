package com.github.ascopes.jct.diagnostics;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.tools.Diagnostic;


/**
 * A wrapper around a {@link Diagnostic} which contains additional information about where the
 * diagnostic was reported.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class DiagnosticWithTrace<S> extends ForwardingDiagnostic<S> {

  private final Instant timestamp;
  private final long threadId;
  private final String threadName;
  private final List<StackTraceElement> stackTrace;

  /**
   * Initialize this diagnostic.
   *
   * @param timestamp  the timestamp.
   * @param threadId   the thread ID.
   * @param threadName the thread name, or {@code null} if not known.
   * @param stackTrace the stacktrace.
   * @param original   the original diagnostic that was reported.
   */
  public DiagnosticWithTrace(
      Instant timestamp,
      long threadId,
      String threadName,
      StackTraceElement[] stackTrace,
      Diagnostic<? extends S> original
  ) {
    super(original);
    this.timestamp = Objects.requireNonNull(timestamp);
    this.threadId = threadId;
    this.threadName = threadName;
    this.stackTrace = List.of(Objects.requireNonNull(stackTrace));
  }

  /**
   * Get the timestamp that the diagnostic was created at.
   *
   * @return the diagnostic timestamp.
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  /**
   * Get the thread ID for the thread that created this diagnostic.
   *
   * @return the thread ID.
   */
  public long getThreadId() {
    return threadId;
  }

  /**
   * Get the thread name for the thread that created this diagnostic.
   *
   * @return the thread name, if known.
   */
  public Optional<String> getThreadName() {
    return Optional.ofNullable(threadName);
  }

  /**
   * Get the stacktrace of where the diagnostic was written from.
   *
   * @return the stacktrace.
   */
  public List<StackTraceElement> getStackTrace() {
    return stackTrace;
  }

  @Override
  public String toString() {
    return String.format(
        "%s [%s]: %s",
        getKind(),
        getCode(),
        getMessage(Locale.ROOT)
    );
  }
}
