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

package io.github.ascopes.jct.compilers;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class TraceDiagnostic<S extends JavaFileObject> extends ForwardingDiagnostic<S> {

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
  public TraceDiagnostic(
      Instant timestamp,
      long threadId,
      String threadName,
      List<StackTraceElement> stackTrace,
      Diagnostic<? extends S> original
  ) {
    super(original);
    this.timestamp = requireNonNull(timestamp);
    this.threadId = threadId;
    // Nullable.
    this.threadName = threadName;
    this.stackTrace = requireNonNull(stackTrace);
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
}
