/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.integration.compilation;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.integration.AbstractIntegrationTest;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic module compilation integration tests")
class BasicModuleCompilationIntegrationTest extends AbstractIntegrationTest {

  @DisplayName("I can compile a 'Hello, World!' module program using a RAM disk")
  @JavacCompilerTest(minVersion = 9)
  void helloWorldRamDisk(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory());

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutputPackages()
          .fileExists("com", "example", "HelloWorld.class")
          .isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutputPackages()
          .fileExists("module-info.class")
          .isNotEmptyFile();
    }
  }

  @DisplayName("I can compile a 'Hello, World!' module program using a temporary directory")
  @JavacCompilerTest(minVersion = 9)
  void helloWorldUsingTempDirectory(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory());

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutputPackages()
          .fileExists("com", "example", "HelloWorld.class")
          .isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutputPackages()
          .fileExists("module-info.class")
          .isNotEmptyFile();
    }
  }
}
