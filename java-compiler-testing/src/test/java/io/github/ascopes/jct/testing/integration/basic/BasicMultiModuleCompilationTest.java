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
package io.github.ascopes.jct.testing.integration.basic;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.pathwrappers.RamFileSystem.newRamFileSystem;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;

/**
 * Basic multi-module compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic multi-module compilation integration tests")
class BasicMultiModuleCompilationTest {

  @DisplayName("I can compile a single module using multi-module layout")
  @JavacCompilerTest(modules = true)
  void singleModuleInMultiModuleLayout(Compilable<?, ?> compiler) {
    var source = newRamFileSystem("hello.world")
        .createFile("com/example/HelloWorld.java").withContents(
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

    var compilation = compiler
        .addPath(StandardLocation.MODULE_SOURCE_PATH, "hello.world", source)
        .showDeprecationWarnings(true)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("hello.world")
        .withFile("com/example/HelloWorld.class").isNotEmptyFile();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("hello.world")
        .withFile("module-info.class").isNotEmptyFile();
  }

  @DisplayName("I can compile multiple modules using multi-module layout")
  @JavacCompilerTest(modules = true)
  void multipleModulesInMultiModuleLayout(Compilable<?, ?> compiler) {
    var helloWorld = newRamFileSystem("hello.world")
        .createFile("com/example/HelloWorld.java").withContents(
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
    var greeter = newRamFileSystem("greeter")
        .createFile("com/example/greeter/Greeter.java").withContents(
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

    var compilation = compiler
        .addModuleSourcePath("hello.world", helloWorld)
        .addModuleSourcePath("greeter", greeter)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("hello.world")
        .withFile("com/example/HelloWorld.class").isNotEmptyFile();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("hello.world")
        .withFile("module-info.class").isNotEmptyFile();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("greeter")
        .withFile("com/example/greeter/Greeter.class").isNotEmptyFile();

    assertThatCompilation(compilation)
        .classOutput().modules()
        .withModule("greeter")
        .withFile("module-info.class").isNotEmptyFile();
  }
}
