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
import java.util.List;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.FactoryBasedNavigableListAssert;

/**
 * Assertions to apply to a list of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
//@formatter:off
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class DiagnosticListAssert
    extends FactoryBasedNavigableListAssert<
        DiagnosticListAssert,
        List<? extends TraceDiagnostic<? extends JavaFileObject>>,
        TraceDiagnostic<? extends JavaFileObject>,
        DiagnosticAssert
    > {
  //@formatter:on

  /**
   * Initialize these assertions.
   *
   * @param diagnostics the list of assertions to assert on.
   */
  private DiagnosticListAssert(
      List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics
  ) {
    super(diagnostics, DiagnosticListAssert.class, DiagnosticAssert::assertThat);
  }

  /**
   * Create a new set of assertions for a list of diagnostics.
   *
   * @param diagnostics the list of diagnostics to assert on.
   * @return the assertions.
   */
  public static DiagnosticListAssert assertThat(
      List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics
  ) {
    return new DiagnosticListAssert(diagnostics)
        .withRepresentation(new DiagnosticCollectionRepresentation());
  }
}