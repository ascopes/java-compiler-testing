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
package io.github.ascopes.jct.acceptancetests.manifold

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.PathStrategy
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.Assertions.assertThat

@DisplayName("Manifold Preprocessor acceptance tests")
@SuppressWarnings('GrUnresolvedAccess')
class ManifoldPreprocessorTest {
  @DisplayName("Preprocessor produces the expected code when a preprocessor symbol is defined")
  @JavacCompilerTest(configurers = [ManifoldPluginConfigurer])
  void preprocessorProducesTheExpectedCodeWhenPreprocessorSymbolIsDefined(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "preprocessor", "if")
          .createFile("build.properties").withContents("SOME_SYMBOL=1")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()

      String greeting = compilation
          .classOutputs
          .classLoader
          .loadClass("org.example.HelloWorld")
          .getDeclaredConstructor()
          .newInstance()
          .getGreeting()

      assertThat(greeting).isEqualTo("Hello, World! (symbol was defined)")
    }
  }

  @DisplayName("Preprocessor produces the expected code when a preprocessor symbol is undefined")
  @JavacCompilerTest(configurers = [ManifoldPluginConfigurer])
  void preprocessorProducesTheExpectedCodeWhenPreprocessorSymbolIsUndefined(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "preprocessor", "if")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()

      String greeting = compilation
          .classOutputs
          .classLoader
          .loadClass("org.example.HelloWorld")
          .getDeclaredConstructor()
          .newInstance()
          .getGreeting()

      assertThat(greeting).isEqualTo("Hello, World! (symbol was not defined)")
    }
  }

  @DisplayName("Warning directives produce compiler warnings in JCT")
  @JavacCompilerTest(configurers = [ManifoldPluginConfigurer])
  void warningDirectivesProduceCompilerWarningsInJct(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "preprocessor", "warning")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessful()
          .diagnostics().warnings().singleElement()
          .message().isEqualTo("Hello, this is a friendly warning!")
    }
  }

  @DisplayName("Error directives produce compiler errors in JCT")
  @JavacCompilerTest(configurers = [ManifoldPluginConfigurer])
  void warningDirectivesProduceCompilerErrorsInJct(JctCompiler compiler) {
    // Given
    try (def workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "preprocessor", "error")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isFailure()
          .diagnostics().errors().singleElement()
          .message().isEqualTo("Hello, this is an error!")
    }
  }
}
