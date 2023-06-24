/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.filemanagers.LoggingMode
import io.github.ascopes.jct.junit.EcjCompilerTest
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.PathStrategy
import io.github.ascopes.jct.workspaces.Workspace
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

@DisplayName("Avaje Inject acceptance tests")
class AvajeInjectTest {
  @DisplayName("Dependency injection code gets generated as expected for Javac")
  @JavacCompilerTest(minVersion = 11)
  void dependencyInjectionCodeGetsGeneratedAsExpectedForJavac(JctCompiler compiler) {
    // Given
    try (def workspace = Workspaces.newWorkspace()) {
      runTest(compiler, workspace)
    }
  }

  @DisplayName("Dependency injection code gets generated as expected for ECJ")
  @EcjCompilerTest(minVersion = 11)
  void dependencyInjectionCodeGetsGeneratedAsExpectedForEcj(JctCompiler compiler) {
    // Given
    try (def workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      runTest(compiler, workspace)
    }
  }

  private static void runTest(JctCompiler compiler, Workspace workspace) {
    workspace
        .createSourcePathPackage()
        .copyContentsFrom("src", "test", "resources", "code")

    // When
    def compilation = compiler
        // TODO(ascopes): disable this
        .fileManagerLoggingMode(LoggingMode.ENABLED)
        .diagnosticLoggingMode(LoggingMode.STACKTRACES)
        .verbose(true)
        // end temporary block
        .compile(workspace)

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutputPackages()
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
