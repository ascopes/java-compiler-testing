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
import org.junit.jupiter.api.DisplayName;

/**
 * Basic multi-module compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic multi-module compilation integration tests")
class BasicMultiModuleCompilationIntegrationTest {

  @DisplayName("I can compile a single module using multi-module layout using a RAM disk")
  @JavacCompilerTest(minVersion = 9)
  void singleModuleInMultiModuleLayoutRamDisk(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathModule("hello.world")
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(\"Hello, World\");",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module hello.world {",
              "  exports com.example;",
              "}"
          );

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("com", "example", "HelloWorld.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("module-info.class").isNotEmptyFile();
    }
  }

  @DisplayName("I can compile a single module using multi-module layout using a temp directory")
  @JavacCompilerTest(minVersion = 9)
  void singleModuleInMultiModuleLayoutTempDirectory(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathModule("hello.world")
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(\"Hello, World\");",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module hello.world {",
              "  exports com.example;",
              "}"
          );

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("com", "example", "HelloWorld.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("module-info.class").isNotEmptyFile();
    }
  }

  @DisplayName("I can compile multiple modules using multi-module layout using a RAM disk")
  @JavacCompilerTest(minVersion = 9)
  void multipleModulesInMultiModuleLayoutRamDisk(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathModule("hello.world")
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "import com.example.greeter.Greeter;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(Greeter.greet(\"World\"));",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module hello.world {",
              "  requires greeter;",
              "  exports com.example;",
              "}"
          );

      workspace
          .createSourcePathModule("greeter")
          .createFile("com", "example", "greeter", "Greeter.java").withContents(
              "package com.example.greeter;",
              "public class Greeter {",
              "  public static String greet(String name) {",
              "    return \"Hello, \" + name + \"!\";",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module greeter {",
              "  exports com.example.greeter;",
              "}"
          );

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("com", "example", "HelloWorld.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("module-info.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("greeter")
          .fileExists("com", "example", "greeter", "Greeter.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("greeter")
          .fileExists("module-info.class").isNotEmptyFile();
    }
  }

  @DisplayName("I can compile multiple modules using multi-module layout using a temp directory")
  @JavacCompilerTest(minVersion = 9)
  void multipleModulesInMultiModuleLayoutTempDirectory(JctCompiler<?, ?> compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathModule("hello.world")
          .createFile("com", "example", "HelloWorld.java").withContents(
              "package com.example;",
              "import com.example.greeter.Greeter;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(Greeter.greet(\"World\"));",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module hello.world {",
              "  requires greeter;",
              "  exports com.example;",
              "}"
          );

      workspace
          .createSourcePathModule("greeter")
          .createFile("com", "example", "greeter", "Greeter.java").withContents(
              "package com.example.greeter;",
              "public class Greeter {",
              "  public static String greet(String name) {",
              "    return \"Hello, \" + name + \"!\";",
              "  }",
              "}"
          )
          .and().createFile("module-info.java").withContents(
              "module greeter {",
              "  exports com.example.greeter;",
              "}"
          );

      // When
      var compilation = compiler.compile(workspace);

      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("com", "example", "HelloWorld.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("hello.world")
          .fileExists("module-info.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("greeter")
          .fileExists("com", "example", "greeter", "Greeter.class").isNotEmptyFile();

      assertThatCompilation(compilation)
          .classOutput().modules()
          .moduleExists("greeter")
          .fileExists("module-info.class").isNotEmptyFile();
    }
  }
}
