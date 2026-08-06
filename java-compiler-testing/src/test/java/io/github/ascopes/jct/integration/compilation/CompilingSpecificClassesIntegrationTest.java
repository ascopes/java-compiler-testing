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
package io.github.ascopes.jct.integration.compilation;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThat;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.integration.AbstractIntegrationTest;
import io.github.ascopes.jct.junit.EcjCompilerTest;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests that test the compilation of specific classes only.
 *
 * @author Ashley Scopes
 */
@DisplayName("Compiling specific classes integration tests")
class CompilingSpecificClassesIntegrationTest extends AbstractIntegrationTest {

  @DisplayName("Only the classes that I specify get compiled")
  @EcjCompilerTest
  @JavacCompilerTest
  void onlyTheClassesSpecifiedGetCompiled(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(resourcesDirectory());

      var compilation = compiler.compile(workspace, "Fibonacci", "HelloWorld");

      assertThat(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .allFilesExist("Fibonacci.class", "HelloWorld.class")
          .fileDoesNotExist("Sum.class");
    }
  }
}
