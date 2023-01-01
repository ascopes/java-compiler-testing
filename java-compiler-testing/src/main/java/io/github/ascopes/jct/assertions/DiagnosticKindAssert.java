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
  public DiagnosticKindAssert(Kind value) {
    super(value, DiagnosticKindAssert.class, "kind");
  }

  /**
   * Assert that the kind is {@link Kind#ERROR}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isError() {
    return isOneOf(Kind.ERROR);
  }

  /**
   * Assert that the kind is either {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isWarning() {
    return isOneOf(Kind.WARNING, Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#WARNING}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isCustomWarning() {
    return isOneOf(Kind.WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#MANDATORY_WARNING}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isMandatoryWarning() {
    return isOneOf(Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that the kind is {@link Kind#NOTE}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isNote() {
    return isOneOf(Kind.NOTE);
  }

  /**
   * Assert that the kind is {@link Kind#OTHER}.
   *
   * @return this assertion object.
   */
  public DiagnosticKindAssert isOther() {
    return isOneOf(Kind.OTHER);
  }
}
