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
package io.github.ascopes.jct.tests.integration.compilation;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.assertions.JctAssertions.assertThatContainerGroup;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.EcjCompilerTest;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.tests.integration.AbstractIntegrationTest;
import io.github.ascopes.jct.tests.integration.IntegrationTestConfigurer;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic module compilation integration tests")
class BasicModuleCompilationIntegrationTest extends AbstractIntegrationTest {

  @DisplayName("I can compile a 'Hello, World!' module program using a RAM disk on Javac")
  @JavacCompilerTest(minVersion = 9, configurers = IntegrationTestConfigurer.class)
  void helloWorldRamDiskJavac(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES)) {
      runHelloWorldTestExpectingPackages(compiler, workspace);
    }
  }

  @DisplayName("I can compile a 'Hello, World!' module program using a temp directory on Javac")
  @JavacCompilerTest(minVersion = 9, configurers = IntegrationTestConfigurer.class)
  void helloWorldUsingTempDirectoryJavac(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      runHelloWorldTestExpectingPackages(compiler, workspace);
    }
  }

  @DisplayName("I can compile a 'Hello, World!' module program using a temp directory on ECJ")
  @EcjCompilerTest(minVersion = 9, configurers = IntegrationTestConfigurer.class)
  void helloWorldUsingTempDirectoryEcj(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      runHelloWorldTestExpectingModules(compiler, workspace);
    }
  }

  private void runHelloWorldTestExpectingPackages(JctCompiler compiler, Workspace workspace) {
    var compilation = runHelloWorldTestStart(compiler, workspace);

    assertThatCompilation(compilation)
        .classOutputPackages()
        .fileExists("com", "example", "HelloWorld.class")
        .isNotEmptyFile();

    assertThatCompilation(compilation)
        .classOutputPackages()
        .fileExists("module-info.class")
        .isNotEmptyFile();
  }

  private void runHelloWorldTestExpectingModules(JctCompiler compiler, Workspace workspace) {
    var compilation = runHelloWorldTestStart(compiler, workspace);

    assertThatCompilation(compilation)
        .classOutputModules()
        .moduleExists("hello.world")
        .satisfies(
            module -> assertThatContainerGroup(module)
                .fileExists("com", "example", "HelloWorld.class")
                .isNotEmptyFile(),
            module -> assertThatContainerGroup(module)
                .fileExists("module-info.class")
                .isNotEmptyFile()
        );
  }

  private JctCompilation runHelloWorldTestStart(JctCompiler compiler, Workspace workspace) {
    // Given
    workspace
        .createSourcePathPackage()
        .copyContentsFrom(resourcesDirectory());

    // When
    var compilation = compiler.compile(workspace);

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    return compilation;
  }
}
