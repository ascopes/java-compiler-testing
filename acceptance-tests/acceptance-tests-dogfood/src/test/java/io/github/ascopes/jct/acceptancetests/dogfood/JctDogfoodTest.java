/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.dogfood;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThat;

/**
 * Tests that try to make JCT compile itself to see if it can correctly "test itself".
 *
 * @author Ashley Scopes
 */
@DisplayName("JCT dogfood acceptance tests")
class JctDogfoodTest {

  static final String MAIN_MODULE = "io.github.ascopes.jct";
  static final String TEST_MODULE = "io.github.ascopes.jct.testing";

  static final Path PROJECT_ROOT = Path.of(System.getProperty("user.dir"))
      .getParent()
      .getParent()
      .resolve("java-compiler-testing")
      .normalize()
      .toAbsolutePath();

  static final Path SRC_MAIN_JAVA = PROJECT_ROOT
      .resolve("src")
      .resolve("main")
      .resolve("java");

  static final Path SRC_TEST_JAVA = PROJECT_ROOT
      .resolve("src")
      .resolve("test")
      .resolve("java");

  static final Path TARGET_CLASSES = PROJECT_ROOT
      .resolve("target")
      .resolve("classes");

  static final Path TARGET_TEST_CLASSES = PROJECT_ROOT
      .resolve("target")
      .resolve("test-classes");

  @DisplayName("JCT can compile itself as a legacy module source")
  @JavacCompilerTest(minVersion = 11, configurers = JctCompilationConfigurer.class)
  void jctCanCompileItselfAsLegacyModule(JctCompiler compiler) throws IOException {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(SRC_MAIN_JAVA);

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThat(compilation)
          .isSuccessful();

      assertThat(compilation)
          .classOutputPackages()
          .allFilesExist(getClassesFrom(TARGET_CLASSES));
    }
  }

  @DisplayName("JCT can compile its unit tests as a legacy module source")
  @JavacCompilerTest(minVersion = 11, configurers = JctCompilationConfigurer.class)
  void jctCanCompileUnitTestsAsLegacyModule(JctCompiler compiler) throws IOException {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(SRC_TEST_JAVA);

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThat(compilation)
          .isSuccessful();

      assertThat(compilation)
          .classPathPackages()
          .allFilesExist(getClassesFrom(TARGET_TEST_CLASSES));
    }
  }

  @DisplayName("JCT can compile itself and its unit tests as a multiple-module source")
  @JavacCompilerTest(minVersion = 11, configurers = JctCompilationConfigurer.class)
  void jctCanCompileItselfAndUnitTestsAsMultiModule(JctCompiler compiler) throws IOException {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathModule(MAIN_MODULE)
          .copyContentsFrom(SRC_MAIN_JAVA);
      workspace
          .createSourcePathModule(TEST_MODULE)
          .copyContentsFrom(SRC_TEST_JAVA);

      // When
      var compilation = compiler.compile(workspace);

      // Then
      var expectedMainFiles = getClassesFrom(TARGET_CLASSES);
      var expectedTestFiles = getClassesFrom(TARGET_TEST_CLASSES);

      assertThat(compilation)
          .isSuccessful();

      assertThat(compilation)
          .classOutputModules()
          .satisfies(
              modules -> assertThat(modules.getModule(MAIN_MODULE))
                  .withFailMessage("Missing classes from main source root")
                  .allFilesExist(expectedMainFiles),
              modules -> assertThat(modules.getModule(TEST_MODULE))
                  .withFailMessage("Missing classes from test source root")
                  .allFilesExist(expectedTestFiles)
          );
    }
  }

  private static Set<String> getClassesFrom(Path location) throws IOException {
    try (var walker = Files.walk(location)) {
      return walker
          .filter(Files::isRegularFile)
          .filter(file -> file.getFileName().endsWith(".class"))
          .map(location::relativize)
          .map(Path::toString)
          .collect(Collectors.toSet());
    }
  }
}
