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
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A diagnostics listener that wraps all diagnostics in additional invocation information, and then
 * stores them in a queue for processing later.
 *
 * @param <S> the file type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class TracingDiagnosticsListener<S> implements DiagnosticListener<S> {

  private final Clock clock;
  private final ConcurrentLinkedQueue<TraceDiagnostic<S>> diagnostics;
  private final Function<Thread, List<StackTraceElement>> stackTraceSupplier;

  /**
   * Initialize this listener.
   */
  public TracingDiagnosticsListener() {
    this(Clock.systemDefaultZone(), Thread::getStackTrace);
  }

  /**
   * Initialize this listener.
   *
   * @param clock              the clock to use for getting the current time.
   * @param stackTraceSupplier a function that gets the stacktrace to use from a given thread.
   */
  public TracingDiagnosticsListener(
      Clock clock,
      Function<Thread, StackTraceElement[]> stackTraceSupplier
  ) {
    this.clock = Objects.requireNonNull(clock);
    this.stackTraceSupplier = Objects.requireNonNull(stackTraceSupplier).andThen(List::of);
    diagnostics = new ConcurrentLinkedQueue<>();
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
    diagnostics.add(handleDiagnostic(diagnostic));
  }

  protected TraceDiagnostic<S> handleDiagnostic(Diagnostic<? extends S> diagnostic) {
    var now = Instant.now(clock);
    var thisThread = Thread.currentThread();
    var threadId = thisThread.getId();
    var threadName = thisThread.getName();
    var stackTrace = stackTraceSupplier.apply(thisThread);
    return new TraceDiagnostic<>(now, threadId, threadName, stackTrace, diagnostic);
  }
}
