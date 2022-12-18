/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.acceptancetests.micronaut

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation

@DisplayName("Micronaut integration tests")
class MicronautIntegrationTest {
  @DisplayName("Micronaut generates the expected code")
  @JavacCompilerTest(configurers = [MicronautConfigurer])
  void micronautGeneratesTheExpectedCode(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .allFilesExist(
              // Micronaut will generate these files.
              'org/example/$HelloController$Definition.class',
              'org/example/$HelloController$Definition$Exec.class',
              'org/example/$HelloController$Definition$Reference.class',
          )
    }
  }
}
