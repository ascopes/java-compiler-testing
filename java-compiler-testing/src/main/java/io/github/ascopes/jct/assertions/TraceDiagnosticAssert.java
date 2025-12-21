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
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.repr.TraceDiagnosticRepresentation;
import java.util.Locale;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractInstantAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for an individual {@link TraceDiagnostic trace diagnostic}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class TraceDiagnosticAssert
    extends AbstractAssert<TraceDiagnosticAssert, TraceDiagnostic<? extends JavaFileObject>> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  @SuppressWarnings("DataFlowIssue")
  public TraceDiagnosticAssert(@Nullable TraceDiagnostic<? extends JavaFileObject> value) {
    super(value, TraceDiagnosticAssert.class);
    info.useRepresentation(TraceDiagnosticRepresentation.getInstance());
  }

  /**
   * Get assertions for the kind of the diagnostic.
   *
   * @return the assertions for the diagnostic kind.
   * @throws AssertionError if the diagnostic is null.
   */
  public DiagnosticKindAssert kind() {
    isNotNull();

    return new DiagnosticKindAssert(actual.getKind());
  }

  /**
   * Get assertions for the source of the diagnostic.
   *
   * <p>If no source is present, then the value in the returned assertions may be {@code null}.
   *
   * @return the assertions for the source of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public JavaFileObjectAssert source() {
    isNotNull();

    return new JavaFileObjectAssert(actual.getSource());
  }

  /**
   * Get assertions for the position of the diagnostic.
   *
   * <p>The value may be -1 if no information is available.
   *
   * @return the assertions for the position of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> position() {
    isNotNull();

    return assertThat(actual.getPosition()).describedAs("position");
  }

  /**
   * Get assertions for the start position of the diagnostic.
   *
   * <p>The value may be -1 if no information is available.
   *
   * @return the assertions for the start position of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> startPosition() {
    isNotNull();

    return assertThat(actual.getStartPosition()).describedAs("start position");
  }

  /**
   * Get assertions for the end position of the diagnostic.
   *
   * <p>The value may be -1 if no information is available.
   *
   * @return the assertions for the end position of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> endPosition() {
    isNotNull();

    return assertThat(actual.getEndPosition()).describedAs("end position");
  }

  /**
   * Get assertions for the line number of the diagnostic.
   *
   * <p>The value may be -1 if no information is available.
   *
   * @return the assertions for the line number of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> lineNumber() {
    isNotNull();

    return assertThat(actual.getLineNumber()).describedAs("line number");
  }

  /**
   * Get assertions for the column number of the diagnostic.
   *
   * <p>The value may be -1 if no information is available.
   *
   * @return the assertions for the column number of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> columnNumber() {
    isNotNull();

    return assertThat(actual.getColumnNumber()).describedAs("column number");
  }

  /**
   * Get assertions for the code of the diagnostic.
   *
   * @return the assertions for the code of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractStringAssert<?> code() {
    isNotNull();

    return assertThat(actual.getCode()).describedAs("code");
  }

  /**
   * Get assertions for the message of the diagnostic, assuming the default locale.
   *
   * @return the assertions for the message of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractStringAssert<?> message() {
    isNotNull();

    return assertThat(actual.getMessage(null));
  }

  /**
   * Get assertions for the message of the diagnostic.
   *
   * @param locale the locale to use.
   * @return the assertions for the message of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractStringAssert<?> message(Locale locale) {
    requireNonNull(locale, "locale must not be null");

    isNotNull();

    return assertThat(actual.getMessage(locale));
  }

  /**
   * Get assertions for the timestamp of the diagnostic.
   *
   * @return the assertions for the timestamp of the diagnostic.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractInstantAssert<?> timestamp() {
    isNotNull();

    return assertThat(actual.getTimestamp());
  }

  /**
   * Get assertions for the thread ID of the thread that reported the diagnostic to the compiler.
   *
   * @return the assertions for the thread ID.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractLongAssert<?> threadId() {
    isNotNull();

    return assertThat(actual.getThreadId());
  }

  /**
   * Get assertions for the thread name of the thread that reported the diagnostic.
   *
   * <p>This may not be present in some situations, in which case the returned assertions will be
   * performed on a null value instead.
   *
   * @return the assertions for the thread name.
   * @throws AssertionError if the diagnostic is null.
   */
  public AbstractStringAssert<?> threadName() {
    isNotNull();

    return assertThat(actual.getThreadName());
  }

  /**
   * Get assertions for the stack trace of the location the diagnostic was reported to.
   *
   * @return the assertions for the stack trace.
   * @throws AssertionError if the diagnostic is null.
   */
  public StackTraceAssert stackTrace() {
    isNotNull();

    return new StackTraceAssert(actual.getStackTrace());
  }
}
