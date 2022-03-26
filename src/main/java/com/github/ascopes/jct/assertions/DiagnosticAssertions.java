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

import com.github.ascopes.jct.diagnostics.TraceDiagnostic;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.InstantAssert;

/**
 * Assertions to apply to a diagnostic.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class DiagnosticAssertions
    extends AbstractObjectAssert<DiagnosticAssertions, TraceDiagnostic<? extends JavaFileObject>> {

  /**
   * Initialize this set of assertions.
   *
   * @param traceDiagnostic the diagnostic to assert upon.
   */
  private DiagnosticAssertions(TraceDiagnostic<? extends JavaFileObject> traceDiagnostic) {
    super(traceDiagnostic, DiagnosticAssertions.class);
  }

  public InstantAssert timestamp() {
    return new InstantAssert(actual.getTimestamp());
  }

  /**
   * Create a new set of assertions for a specific diagnostic.
   *
   * @param diagnostic the diagnostic to assert on.
   * @param <S>        the type of object within the diagnostic.
   * @return the assertions.
   */
  public static <S> DiagnosticAssertions assertThat(
      TraceDiagnostic<? extends JavaFileObject> diagnostic
  ) {
    return new DiagnosticAssertions(diagnostic);
  }
}

