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

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.utils.UtilityClass;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper class to provide fluent creation of assertions for compilations.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class JctAssertions extends UtilityClass {

  private JctAssertions() {
    // Disallow initialisation.
  }

  /**
   * Perform a regular compilation assertion.
   *
   * @param compilation the compilation to assert on.
   * @return the assertion.
   */
  public static CompilationAssert assertThatCompilation(JctCompilation compilation) {
    return thenCompilation(compilation);
  }

  /**
   * Perform a BDD-style compilation assertion.
   *
   * @param compilation the compilation to assert on.
   * @return the assertion.
   */
  public static CompilationAssert thenCompilation(JctCompilation compilation) {
    return new CompilationAssert(compilation);
  }
}
