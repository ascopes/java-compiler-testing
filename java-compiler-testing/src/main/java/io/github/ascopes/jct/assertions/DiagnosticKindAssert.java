/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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

import javax.annotation.Nullable;
import javax.tools.Diagnostic.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Assertions for an individual diagnostic kind.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class DiagnosticKindAssert
    extends AbstractEnumAssert<DiagnosticKindAssert, Kind> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  public DiagnosticKindAssert(@Nullable Kind value) {
    super(value, DiagnosticKindAssert.class);
  }

  /**
   * Assert that the kind is {@link Kind#ERROR}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not {@link Kind#ERROR}.
   */
  public DiagnosticKindAssert isError() {
    return isAnyOf(Kind.ERROR);
  }

  /**
   * Assert that the kind is either {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not either {@link Kind#WARNING}
   *                        or {@link Kind#MANDATORY_WARNING}.
   */
  public DiagnosticKindAssert isWarning() {
    return isAnyOf(Kind.WARNING, Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#WARNING}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not {@link Kind#WARNING}.
   */
  public DiagnosticKindAssert isCustomWarning() {
    return isAnyOf(Kind.WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#MANDATORY_WARNING}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not
   *                        {@link Kind#MANDATORY_WARNING}.
   */
  public DiagnosticKindAssert isMandatoryWarning() {
    return isAnyOf(Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#NOTE}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not {@link Kind#NOTE}.
   */
  public DiagnosticKindAssert isNote() {
    return isAnyOf(Kind.NOTE);
  }

  /**
   * Assert that the kind is {@link Kind#OTHER}.
   *
   * @return this assertion object.
   * @throws AssertionError if this value is null, or the value is not {@link Kind#OTHER}.
   */
  public DiagnosticKindAssert isOther() {
    return isAnyOf(Kind.OTHER);
  }
}
