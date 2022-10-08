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
import static io.github.ascopes.jct.paths.RamPath.createPath;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.junit.JavacCompilers;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Basic multi-module compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic multi-module compilation integration tests")
class BasicMultiModuleCompilationTest {

  @DisplayName("I can compile a single module using multi-module layout")
  @JavacCompilers(modules = true)
  @ParameterizedTest(name = "targeting {0}")
  void singleModuleInMultiModuleLayout(Compilable<?, ?> compiler) {
    var source = createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(\"Hello, World\");",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
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
        .files()
        .classOutput().exists()
        .file("hello.world", "com/example/HelloWorld.class").exists().isNotEmptyFile();

    assertThatCompilation(compilation)
        .files()
        .classOutput().exists()
        .file("hello.world", "module-info.class").exists().isNotEmptyFile();
  }

  @DisplayName("I can compile multiple modules using multi-module layout")
  @JavacCompilers(modules = true)
  @ParameterizedTest(name = "targeting {0}")
  void multipleModulesInMultiModuleLayout(Compilable<?, ?> compiler) {
    var helloWorld = createPath("hello.world")
        .createFile(
            "com/example/HelloWorld.java",
            "package com.example;",
            "import com.example.greeter.Greeter;",
            "public class HelloWorld {",
            "  public static void main(String[] args) {",
            "    System.out.println(Greeter.greet(\"World\"));",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
            "module hello.world {",
            "  requires greeter;",
            "  exports com.example;",
            "}"
        );
    var greeter = createPath("greeter")
        .createFile(
            "com/example/greeter/Greeter.java",
            "package com.example.greeter;",
            "public class Greeter {",
            "  public static String greet(String name) {",
            "    return \"Hello, \" + name + \"!\";",
            "  }",
            "}"
        )
        .createFile(
            "module-info.java",
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
        .files()
        .classOutput().exists()
        .file("hello.world", "com/example/HelloWorld.class").exists().isNotEmptyFile();

    assertThatCompilation(compilation)
        .files()
        .classOutput().exists()
        .file("hello.world", "module-info.class").exists().isNotEmptyFile();

    assertThatCompilation(compilation)
        .files()
        .classOutput().exists()
        .file("greeter", "com/example/greeter/Greeter.class").exists().isNotEmptyFile();

    assertThatCompilation(compilation)
        .files()
        .classOutput().exists()
        .file("greeter", "module-info.class").exists().isNotEmptyFile();
  }
}
