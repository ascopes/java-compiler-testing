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

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.ToStringBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around a {@link Diagnostic} which contains additional information about where the
 * diagnostic was reported.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@Immutable
@ThreadSafe
public class TraceDiagnostic<S extends JavaFileObject> implements Diagnostic<S> {

  private final Instant timestamp;
  private final long threadId;
  private final @Nullable String threadName;
  private final List<StackTraceElement> stackTrace;
  private final Diagnostic<? extends S> original;

  /**
   * Initialize this diagnostic.
   *
   * @param timestamp  the timestamp.
   * @param threadId   the thread ID.
   * @param threadName the thread name, or {@code null} if not known.
   * @param stackTrace the stacktrace.
   * @param original   the original diagnostic that was reported.
   */
  public TraceDiagnostic(
      Instant timestamp,
      long threadId,
      @Nullable String threadName,
      List<StackTraceElement> stackTrace,
      Diagnostic<? extends S> original
  ) {
    this.timestamp = requireNonNull(timestamp, "timestamp");
    this.threadId = threadId;
    this.threadName = threadName;
    this.stackTrace = unmodifiableList(requireNonNull(stackTrace, "stackTrace"));
    this.original = requireNonNull(original, "original");
  }

  @Override
  public Kind getKind() {
    return original.getKind();
  }

  @Nullable
  @Override
  public S getSource() {
    return original.getSource();
  }

  @Override
  public long getPosition() {
    return original.getPosition();
  }

  @Override
  public long getStartPosition() {
    return original.getStartPosition();
  }

  @Override
  public long getEndPosition() {
    return original.getEndPosition();
  }

  @Override
  public long getLineNumber() {
    return original.getLineNumber();
  }

  @Override
  public long getColumnNumber() {
    return original.getColumnNumber();
  }

  @Nullable
  @Override
  public String getCode() {
    return original.getCode();
  }

  @Override
  public String getMessage(@Nullable Locale locale) {
    return original.getMessage(locale);
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
   * @return the thread name, if known, or else {@code null}.
   */
  @Nullable
  public String getThreadName() {
    return threadName;
  }

  /**
   * Get the stacktrace of where the diagnostic was written from.
   *
   * @return the stacktrace, in an unmodifiable list.
   */
  public List<StackTraceElement> getStackTrace() {
    return stackTrace;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("timestamp", timestamp)
        .attribute("threadId", threadId)
        .attribute("threadName", threadName)
        .attribute("kind", original.getKind())
        .attribute("code", original.getCode())
        .attribute("column", original.getColumnNumber())
        .attribute("line", original.getLineNumber())
        .attribute("message", original.getMessage(Locale.ROOT))
        .toString();
  }
}
