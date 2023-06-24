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
import io.github.ascopes.jct.junit.EcjCompilerTest;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.tests.integration.AbstractIntegrationTest;
import io.github.ascopes.jct.tests.integration.IntegrationTestConfigurer;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
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
          + "for Javac"
  )
  @JavacCompilerTest(configurers = IntegrationTestConfigurer.class)
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilationForJavac(
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      runClassPathPackagesTest(compiler, firstWorkspace, secondWorkspace);
    }
  }

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second "
          + "compilation within a JAR for Javac"
  )
  @JavacCompilerTest(configurers = IntegrationTestConfigurer.class)
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilationWithinJarForJavac(
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace();
        var secondWorkspace = Workspaces.newWorkspace()
    ) {
      runClassPathJarTest(compiler, firstWorkspace, secondWorkspace);
    }
  }

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second compilation"
          + "for ECJ"
  )
  @EcjCompilerTest(configurers = IntegrationTestConfigurer.class)
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilationForEcj(
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES);
        var secondWorkspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)
    ) {
      runClassPathPackagesTest(compiler, firstWorkspace, secondWorkspace);
    }
  }

  @DisplayName(
      "I can compile sources to classes and provide them in the classpath to a second "
          + "compilation within a JAR for ECJ"
  )
  @EcjCompilerTest(configurers = IntegrationTestConfigurer.class)
  void compileSourcesToClassesAndProvideThemInClassPathToSecondCompilationWithinJarForEcj(
      JctCompiler compiler
  ) {
    try (
        var firstWorkspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES);
        var secondWorkspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)
    ) {
      runClassPathJarTest(compiler, firstWorkspace, secondWorkspace);
    }
  }

  private void runClassPathPackagesTest(
      JctCompiler compiler,
      Workspace firstWorkspace,
      Workspace secondWorkspace
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

    secondWorkspace.addClassPathPackage(firstWorkspace.getClassOutputPackages().get(0).getPath());
    secondWorkspace.createSourcePathPackage()
        .createDirectory("org", "example", "second")
        .copyContentsFrom(resourcesDirectory().resolve("second"));

    var secondCompilation = compiler.compile(secondWorkspace);
    assertThatCompilation(secondCompilation)
        .isSuccessfulWithoutWarnings()
        .classOutputPackages()
        .fileExists("org", "example", "second", "Main.class")
        .isRegularFile()
        .isNotEmptyFile();
  }

  private void runClassPathJarTest(
      JctCompiler compiler,
      Workspace firstWorkspace,
      Workspace secondWorkspace
  ) {
    firstWorkspace
        .createSourcePathPackage()
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

    var firstJar = firstWorkspace.getClassOutputPackages().get(1).getPath().resolve("first.jar");
    secondWorkspace.addClassPathPackage(firstJar);
    secondWorkspace
        .createSourcePathPackage()
        .copyContentsFrom(resourcesDirectory().resolve("second"));

    var secondCompilation = compiler.compile(secondWorkspace);
    assertThatCompilation(secondCompilation)
        .isSuccessfulWithoutWarnings()
        .classOutputPackages()
        .fileExists("org", "example", "second", "Main.class")
        .isRegularFile()
        .isNotEmptyFile();
  }
}
