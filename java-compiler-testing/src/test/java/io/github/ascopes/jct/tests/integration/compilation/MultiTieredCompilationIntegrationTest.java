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
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

/**
 * Integration tests to test the mechanisms used to create JARs and inputs from one compilation and
 * feed them into a second compilation.
 *
 * @author Ashley Scopes
 */
@DisplayName("Multi-tiered compilation integration tests")
class MultiTieredCompilationIntegrationTest {

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second compilation"
  )
  @JavacCompilerTest
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilation(
      JctCompiler<?, ?> compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      firstWorkspace
          .createSourcePathPackage()
          .createFile("org", "example", "first", "Adder.java")
          .withContents(
              "package org.example.first;",
              "",
              "public class Adder {",
              "  public int add(int a, int b) {",
              "    return a + b;",
              "  }",
              "}"
          );

      var firstCompilation = compiler.compile(firstWorkspace);
      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .fileExists("org", "example", "first", "Adder.class")
          .isRegularFile()
          .isNotEmptyFile();

      secondWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());
      secondWorkspace.createSourcePathPackage()
          .createFile("org", "example", "second", "Main.java")
          .withContents(
              "package org.example.second;",
              "",
              "import org.example.first.Adder;",
              "",
              "public class Main {",
              "  public static int addTogether(int a, int b) {",
              "    Adder adder = new Adder();",
              "    return adder.add(a, b);",
              "  }",
              "}"
          );

      var secondCompilation = compiler.compile(secondWorkspace);
      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .fileExists("org", "example", "second", "Main.class")
          .isRegularFile()
          .isNotEmptyFile();
    }
  }

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second "
          + "compilation within a JAR"
  )
  @JavacCompilerTest
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilationWithinJar(
      JctCompiler<?, ?> compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      firstWorkspace
          .createSourcePathPackage()
          .createFile("org", "example", "first", "Adder.java")
          .withContents(
              "package org.example.first;",
              "",
              "public class Adder {",
              "  public int add(int a, int b) {",
              "    return a + b;",
              "  }",
              "}"
          );

      var firstCompilation = compiler.compile(firstWorkspace);
      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .fileExists("org", "example", "first", "Adder.class")
          .isRegularFile()
          .isNotEmptyFile();

      firstWorkspace
          .createClassOutputPackage()
          .createFile("first.jar")
          .asJarFrom(firstWorkspace.getClassOutputPackages().get(0));

      var firstJar = firstWorkspace.getClassOutputPackages().get(1).getPath().resolve("first.jar");
      secondWorkspace.addClassPathPackage(firstJar);
      secondWorkspace.createSourcePathPackage()
          .createFile("org", "example", "second", "Main.java")
          .withContents(
              "package org.example.second;",
              "",
              "import org.example.first.Adder;",
              "",
              "public class Main {",
              "  public static int addTogether(int a, int b) {",
              "    Adder adder = new Adder();",
              "    return adder.add(a, b);",
              "  }",
              "}"
          );

      var secondCompilation = compiler.compile(secondWorkspace);
      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .fileExists("org", "example", "second", "Main.class")
          .isRegularFile()
          .isNotEmptyFile();
    }
  }
}
