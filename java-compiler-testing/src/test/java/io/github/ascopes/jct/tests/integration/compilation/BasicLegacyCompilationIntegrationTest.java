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

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspaces;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic legacy compilation integration tests")
class BasicLegacyCompilationIntegrationTest {

  @DisplayName("I can compile a 'Hello, World!' program using a RAM directory")
  @JavacCompilerTest
  void helloWorldJavacRamDirectory(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES)) {
      workspace
          .createPackage(StandardLocation.SOURCE_PATH)
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(\"Hello, World\");",
              "  }",
              "}"
          );

      var compilation = compiler.compile(workspace);

      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().packages()
          .fileExists("com", "example", "HelloWorld.class")
          .isNotEmptyFile();
    }
  }

  @DisplayName("I can compile a 'Hello, World!' program using a temp directory")
  @JavacCompilerTest
  void helloWorldJavacTempDirectory(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      workspace
          .createPackage(StandardLocation.SOURCE_PATH)
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(\"Hello, World\");",
              "  }",
              "}"
          );

      var compilation = compiler.compile(workspace);

      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().packages()
          .fileExists("com", "example", "HelloWorld.class")
          .isNotEmptyFile();
    }
  }
}
