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
package io.github.ascopes.jct.acceptancetests.avajeinject


import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.filemanagers.LoggingMode
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation

@DisplayName("Avaje Inject acceptance tests")
class AvajeInjectTest {
  @DisplayName("Dependency injection code gets generated as expected")
  @JavacCompilerTest(modules = true)
  void dependencyInjectionCodeGetsGeneratedAsExpected(JctCompiler compiler) {
    // Given
    try (def workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .rootDirectory()
          .copyContentsFrom("src", "test", "resources", "code")

      // When
      def compilation = compiler
          .diagnosticLoggingMode(LoggingMode.STACKTRACES)
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .allFilesExist(
              'org/example/CoffeeMaker.class',
              'org/example/Grinder.class',
              'org/example/Pump.class',
              'org/example/CoffeeMaker$DI.class',
              'org/example/Grinder$DI.class',
              'org/example/Pump$DI.class',
          )
    }
  }
}
