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

package io.github.ascopes.jct.assertions;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import java.util.Locale;
import java.util.OptionalLong;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstantAssert;
import org.assertj.core.api.LongAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.api.OptionalLongAssert;
import org.assertj.core.api.StringAssert;

/**
 * Assertions for an individual {@link TraceDiagnostic trace diagnostic}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class DiagnosticAssert
    extends AbstractAssert<DiagnosticAssert, TraceDiagnostic<? extends JavaFileObject>> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  public DiagnosticAssert(TraceDiagnostic<? extends JavaFileObject> value) {
    super(value, DiagnosticAssert.class);
    setCustomRepresentation(DiagnosticRepresentation.getInstance());
  }

  /**
   * Get assertions for the kind of the diagnostic.
   *
   * @return the assertions for the diagnostic kind.
   */
  public DiagnosticKindAssert kind() {
    return new DiagnosticKindAssert(actual.getKind());
  }

  /**
   * Get assertions for the source of the diagnostic.
   *
   * @return the assertions for the source of the diagnostic.
   */
  public JavaFileObjectAssert source() {
    return new JavaFileObjectAssert(actual.getSource());
  }

  /**
   * Get assertions for the position of the diagnostic.
   *
   * <p>The value may be empty if no position was provided.
   *
   * @return the assertions for the position of the diagnostic.
   */
  public OptionalLongAssert position() {
    return assertPosition(actual.getPosition(), "position");
  }

  /**
   * Get assertions for the start position of the diagnostic.
   *
   * <p>The value may be empty if no position was provided.
   *
   * @return the assertions for the start position of the diagnostic.
   */
  public OptionalLongAssert startPosition() {
    return assertPosition(actual.getPosition(), "startPosition");
  }

  /**
   * Get assertions for the end position of the diagnostic.
   *
   * <p>The value may be empty if no position was provided.
   *
   * @return the assertions for the end position of the diagnostic.
   */
  public OptionalLongAssert endPosition() {
    return assertPosition(actual.getEndPosition(), "endPosition");
  }

  /**
   * Get assertions for the line number of the diagnostic.
   *
   * <p>The value may be empty if no position was provided.
   *
   * @return the assertions for the line number of the diagnostic.
   */
  public OptionalLongAssert lineNumber() {
    return assertPosition(actual.getLineNumber(), "lineNumber");
  }

  /**
   * Get assertions for the column number of the diagnostic.
   *
   * <p>The value may be empty if no position was provided.
   *
   * @return the assertions for the column number of the diagnostic.
   */
  public OptionalLongAssert columnNumber() {
    return assertPosition(actual.getColumnNumber(), "columnNumber");
  }

  /**
   * Get assertions for the code of the diagnostic.
   *
   * @return the assertions for the code of the diagnostic.
   */
  public StringAssert code() {
    return new StringAssert(actual.getCode());
  }

  /**
   * Get assertions for the message of the diagnostic, assuming the default locale.
   *
   * @return the assertions for the message of the diagnostic.
   */
  public StringAssert message() {
    return new StringAssert(actual.getMessage(null));
  }

  /**
   * Get assertions for the message of the diagnostic.
   *
   * @param locale the locale to use.
   * @return the assertions for the message of the diagnostic.
   */
  public StringAssert message(Locale locale) {
    requireNonNull(locale, "locale");
    return new StringAssert(actual.getMessage(locale));
  }

  /**
   * Get assertions for the timestamp of the diagnostic.
   *
   * @return the assertions for the timestamp of the diagnostic.
   */
  public InstantAssert timestamp() {
    return new InstantAssert(actual.getTimestamp());
  }

  /**
   * Get assertions for the thread ID of the thread that reported the diagnostic to the compiler.
   *
   * @return the assertions for the thread ID.
   */
  public LongAssert threadId() {
    return new LongAssert(actual.getThreadId());
  }

  /**
   * Get assertions for the thread name of the thread that reported the diagnostic. This may not be
   * present in some situations.
   *
   * @return the assertions for the optional thread name.
   */
  public OptionalAssert<String> threadName() {
    // TODO(ascopes): should I be initializing this in this way?
    return new OptionalAssert<>(actual.getThreadName()) {};
  }

  /**
   * Get assertions for the stack trace of the location the diagnostic was reported to.
   *
   * @return the assertions for the stack trace.
   */
  public StackTraceAssert stackTrace() {
    return new StackTraceAssert(actual.getStackTrace());
  }

  private OptionalLongAssert assertPosition(long position, String name) {
    // TODO(ascopes): should I be initializing this in this way?
    return new OptionalLongAssert(
        position == Diagnostic.NOPOS ? OptionalLong.empty() : OptionalLong.of(position)
    ) {}.describedAs("%s of %d", name, position);


  }
}
