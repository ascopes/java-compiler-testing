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
  private final ConcurrentLinkedQueue<TraceDiagnostic<S>> diagnostics;

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
    var stackTrace = List.of(thisThread.getStackTrace());
    return new TraceDiagnostic<>(now, threadId, threadName, stackTrace, diagnostic);
  }
}
