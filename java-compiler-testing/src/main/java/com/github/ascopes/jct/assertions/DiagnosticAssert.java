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

package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.compilers.TraceDiagnostic;
import java.util.Locale;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.AbstractInstantAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

/**
 * Assertions to apply to a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class DiagnosticAssert
    extends AbstractObjectAssert<DiagnosticAssert, TraceDiagnostic<? extends JavaFileObject>> {

  /**
   * Initialize this set of assertions.
   *
   * @param traceDiagnostic the diagnostic to assert upon.
   */
  private DiagnosticAssert(TraceDiagnostic<? extends JavaFileObject> traceDiagnostic) {
    super(traceDiagnostic, DiagnosticAssert.class);
  }

  /**
   * Perform an assertion on the timestamp of the diagnostic.
   *
   * @return an assertion to perform on the timestamp.
   */
  public AbstractInstantAssert<?> timestamp() {
    return Assertions.assertThat(actual.getTimestamp());
  }

  /**
   * Perform an assertion on the thread ID that the diagnostic was reported from.
   *
   * @return an assertion to perform on the thread ID.
   */
  public AbstractLongAssert<?> threadId() {
    return Assertions.assertThat(actual.getThreadId());
  }

  /**
   * Perform an assertion on the optional thread name that the diagnostic was reported from.
   *
   * @return an assertion to perform on the thread name.
   */
  public AbstractStringAssert<?> threadName() {
    return Assertions.assertThat(actual.getThreadName().orElse(null));
  }

  /**
   * Perform an assertion on the stack trace location that the diagnostic was reported from.
   *
   * @return an assertion to perform on the list of stack trace frames.
   */
  public ListAssert<StackTraceElement> stackTrace() {
    return Assertions.assertThat(actual.getStackTrace());
  }

  /**
   * Perform an assertion on the kind of the diagnostic.
   *
   * @return an assertion to perform on the kind.
   */
  public AbstractComparableAssert<?, Kind> kind() {
    return Assertions.assertThat(actual.getKind());
  }

  /**
   * Perform an assertion on the source that the diagnostic was reported from.
   *
   * @return the assertion to perform on the source.
   */
  public AbstractObjectAssert<?, JavaFileObject> source() {
    return Assertions.assertThat(actual.getSource());
  }

  /**
   * Perform an assertion on the position in the source that the diagnostic was reported from.
   *
   * @return an assertion to perform on the position.
   */
  public AbstractLongAssert<?> position() {
    return Assertions.assertThat(actual.getPosition());
  }

  /**
   * Perform an assertion on the start position in the source that the diagnostic was reported
   * from.
   *
   * @return an assertion to perform on the start position.
   */
  public AbstractLongAssert<?> startPosition() {
    return Assertions.assertThat(actual.getStartPosition());
  }

  /**
   * Perform an assertion on the end position in the source that the diagnostic was reported from.
   *
   * @return an assertion to perform on the end position.
   */
  public AbstractLongAssert<?> endPosition() {
    return Assertions.assertThat(actual.getEndPosition());
  }

  /**
   * Perform an assertion on the line number in the source that the diagnostic was reported from.
   *
   * @return an assertion to perform on the line number.
   */
  public AbstractLongAssert<?> lineNumber() {
    return Assertions.assertThat(actual.getLineNumber());
  }

  /**
   * Perform an assertion on the column number in the source that the diagnostic was reported from.
   *
   * @return an assertion to perform on the column number.
   */
  public AbstractLongAssert<?> columnNumber() {
    return Assertions.assertThat(actual.getColumnNumber());
  }

  /**
   * Perform an assertion on the diagnostic code.
   *
   * <p>This is usually only present for compiler-provided diagnostics, and is not able to be
   * specified in the standard annotation processing API (where this will be null).
   *
   * @return an assertion to perform on the diagnostic code.
   */
  public AbstractStringAssert<?> code() {
    return Assertions.assertThat(actual.getCode());
  }

  /**
   * Perform an assertion on the diagnostic message, assuming a root locale.
   *
   * @return an assertion to perform on the message, assuming a root locale.
   */
  public AbstractStringAssert<?> message() {
    return message(Locale.ROOT);
  }

  /**
   * Perform an assertion on the diagnostic message, using the given locale.
   *
   * @param locale the locale to use.
   * @return an assertion to perform on the message.
   */
  public AbstractStringAssert<?> message(Locale locale) {
    return Assertions.assertThat(actual.getMessage(locale));
  }

  /**
   * Create a new set of assertions for a specific diagnostic.
   *
   * @param diagnostic the diagnostic to assert on.
   * @return the assertions.
   */
  public static DiagnosticAssert assertThat(
      TraceDiagnostic<? extends JavaFileObject> diagnostic
  ) {
    return new DiagnosticAssert(diagnostic)
        .withRepresentation(new DiagnosticRepresentation());
  }
}

