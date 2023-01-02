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

import static org.assertj.core.api.Assumptions.assumeThat;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.utils.UtilityClass;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.Assumptions;
import org.assertj.core.api.InstanceOfAssertFactory;

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
  public static CompilationAssert assertThatCompilation(@Nullable JctCompilation compilation) {
    return new CompilationAssert(compilation);
  }

  /**
   * Perform a regular compilation assumption.
   *
   * <p>Assumptions work the same as assertions, but will mark the test as skipped if they fail
   * rather than marking the test as failed. This is useful for skipping tests if specific
   * conditions are not met.
   *
   * @param compilation the compilation to assume on.
   * @return the assumption.
   * @see Assumptions
   */
  public static CompilationAssert assumeThatCompilation(@Nullable JctCompilation compilation) {
    return assumeThat(compilation)
        .extracting(Function.identity(), new InstanceOfAssertFactory<>(
            JctCompilation.class,
            JctAssertions::assertThatCompilation
        ));
  }

  /**
   * Perform a BDD-style compilation assumption.
   *
   * <p>Assumptions work the same as assertions, but will mark the test as skipped if they fail
   * rather than marking the test as failed. This is useful for skipping tests if specific
   * conditions are not met.
   *
   * @param compilation the compilation to assume on.
   * @return the assumption.
   * @see Assumptions
   */
  public static CompilationAssert givenCompilation(@Nullable JctCompilation compilation) {
    return assumeThatCompilation(compilation);
  }

  /**
   * Perform a BDD-style compilation assertion.
   *
   * @param compilation the compilation to assert on.
   * @return the assertion.
   */
  public static CompilationAssert thenCompilation(@Nullable JctCompilation compilation) {
    return assertThatCompilation(compilation);
  }
}
