/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.integration.AbstractIntegrationTest;
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
class MultiTieredCompilationIntegrationTest extends AbstractIntegrationTest {

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second compilation"
  )
  @JavacCompilerTest
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilation(
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      firstWorkspace
          .createSourcePathPackage()
          .createDirectory("org", "example", "first")
          .copyContentsFrom(resourcesDirectory().resolve("first"));

      var firstCompilation = compiler.compile(firstWorkspace);

      firstWorkspace.dump(System.err);

      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("org", "example", "first", "Adder.class")
          .isRegularFile()
          .isNotEmptyFile();

      secondWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());
      secondWorkspace.createSourcePathPackage()
          .createDirectory("org", "example", "second")
          .copyContentsFrom(resourcesDirectory().resolve("second"));

      var secondCompilation = compiler.compile(secondWorkspace);

      secondWorkspace.dump(System.err);

      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
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
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      firstWorkspace
          .createSourcePathPackage()
          .createDirectory("org", "example", "first")
          .copyContentsFrom(resourcesDirectory().resolve("first"));

      var firstCompilation = compiler.compile(firstWorkspace);

      assertThatCompilation(firstCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("org", "example", "first", "Adder.class")
          .isRegularFile()
          .isNotEmptyFile();

      firstWorkspace
          .createClassOutputPackage()
          .createFile("first.jar")
          .asJarFrom(firstWorkspace.getClassOutputPackages().get(0));

      firstWorkspace.dump(System.err);

      var firstJar = firstWorkspace.getClassOutputPackages().get(1).getPath().resolve("first.jar");
      secondWorkspace.addClassPathPackage(firstJar);
      secondWorkspace
          .createSourcePathPackage()
          .createDirectory("org", "example", "second")
          .copyContentsFrom(resourcesDirectory().resolve("second"));

      var secondCompilation = compiler.compile(secondWorkspace);

      secondWorkspace.dump(System.err);

      assertThatCompilation(secondCompilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("org", "example", "second", "Main.class")
          .isRegularFile()
          .isNotEmptyFile();
    }
  }
}
