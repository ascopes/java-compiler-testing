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
package io.github.ascopes.jct.acceptancetests.errorprone

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation

@DisplayName("Error-prone acceptance tests")
class ErrorProneTest {

  @DisplayName("Happy paths work as expected")
  @JavacCompilerTest
  void happyPathsWorkAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "nullness", "happy")

      // When
      def compilation = compiler
          .addCompilerOptions(
              "-Xplugin:ErrorProne",
              "-XDcompilePolicy=simple",
          )
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
    }
  }

  @DisplayName("Sad paths fail as expected")
  @JavacCompilerTest
  void sadPathsFailAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "nullness", "sad")

      // When
      def compilation = compiler
          .addCompilerOptions(
              "-Xplugin:ErrorProne",
              "-XDcompilePolicy=simple",
          )
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isFailure()
          .diagnostics()
          .errors()
          .singleElement()
          .message()
          .startsWith(
              "[MustBeClosedChecker] This method returns a resource which must be managed "
                  + "carefully, not just left for garbage collection."
          )
    }
  }
}
