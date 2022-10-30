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
package io.github.ascopes.jct.testing.integration;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory;
import static io.github.ascopes.jct.pathwrappers.TempDirectory.newTempDirectory;

import io.github.ascopes.jct.compilers.Compiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
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
  void helloWorldJavacRamDirectory(Compiler<?, ?> compiler) {
    var sources = newRamDirectory("sources")
        .createFile("com/example/HelloWorld.java").withContents(
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    assertThatCompilation(compilation)
        .classOutput().packages()
        .fileExists("com/example/HelloWorld.class")
        .isNotEmptyFile();
  }

  @DisplayName("I can compile a 'Hello, World!' program using a temp directory")
  @JavacCompilerTest
  void helloWorldJavacTempDirectory(Compiler<?, ?> compiler) {
    var sources = newTempDirectory("sources")
        .createFile("com/example/HelloWorld.java").withContents(
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    assertThatCompilation(compilation)
        .classOutput().packages()
        .fileExists("com/example/HelloWorld.class")
        .isNotEmptyFile();
  }
}
