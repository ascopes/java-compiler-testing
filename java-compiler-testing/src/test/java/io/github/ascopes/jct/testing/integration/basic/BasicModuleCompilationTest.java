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

package io.github.ascopes.jct.testing.integration.basic;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.paths.RamPath.createPath;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.compilers.ecj.EcjCompiler;
import io.github.ascopes.jct.junit.EcjCompilers;
import io.github.ascopes.jct.junit.JavacCompilers;
import io.github.ascopes.jct.testing.helpers.Skipping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic module compilation integration tests")
class BasicModuleCompilationTest {

  @DisplayName("I can compile a 'Hello, World!' module program")
  @JavacCompilers(modules = true)
  @EcjCompilers(modules = true)
  @ParameterizedTest(name = "targeting {0}")
  void helloWorld(Compilable<?, ?> compiler) {
    if (compiler instanceof EcjCompiler) {
      Skipping.becauseEcjFailsToSupportModulesCorrectly();
    }

    var sources = createPath("sources")
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
            "  requires java.base;",
            "  exports com.example;",
            "}"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings();
  }
}
