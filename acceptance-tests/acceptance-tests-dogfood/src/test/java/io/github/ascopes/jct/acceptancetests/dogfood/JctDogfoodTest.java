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
package io.github.ascopes.jct.acceptancetests.dogfood;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThat;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests that try to make JCT compile itself to see if it can correctly "test itself".
 *
 * @author Ashley Scopes
 */
@DisplayName("JCT dogfood acceptance tests")
class JctDogfoodTest {

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

  static final Path TARGET_CLASSES = PROJECT_ROOT
      .resolve("target")
      .resolve("classes");

  @DisplayName("JCT can compile itself as a legacy module source")
  @JavacCompilerTest(minVersion = 11, configurers = JctCompilationConfigurer.class)
  void jctCanCompileItselfAsLegacyModule(JctCompiler<?, ?> compiler) throws IOException {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom(SRC_MAIN_JAVA);

      // When
      var compilation = compiler.compile(workspace);

      // Then
      try (var walker = Files.walk(TARGET_CLASSES)) {
        var expectedFiles = walker
            .filter(Files::isRegularFile)
            .filter(file -> file.getFileName().endsWith(".class"))
            .map(TARGET_CLASSES::relativize)
            .map(Path::toString)
            .collect(Collectors.toSet());

        assertThat(compilation)
            .isSuccessful();

        assertThat(compilation)
            .classOutput()
            .packages()
            .allFilesExist(expectedFiles);
      }
    }
  }

  @DisplayName("JCT can compile itself as a multiple-module source")
  @JavacCompilerTest(minVersion = 11, configurers = JctCompilationConfigurer.class)
  void jctCanCompileItselfAsMultiModule(JctCompiler<?, ?> compiler) throws IOException {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathModule("io.github.ascopes.jct")
          .copyContentsFrom(SRC_MAIN_JAVA);

      // When
      var compilation = compiler.compile(workspace);

      // Then
      try (var walker = Files.walk(TARGET_CLASSES)) {
        var expectedFiles = walker
            .filter(Files::isRegularFile)
            .filter(file -> file.getFileName().endsWith(".class"))
            .map(TARGET_CLASSES::relativize)
            .map(Path::toString)
            .map("io.github.ascopes.jct/"::concat)
            .collect(Collectors.toSet());

        assertThat(compilation)
            .isSuccessful();

        assertThat(compilation)
            .diagnostics()
            .warnings()
            .filteredOn(diag -> !diag.getCode().equals("compiler.warn.module.not.found"))
            .withFailMessage("Expected no warnings (other than module.not.found)")
            .isEmpty();

        assertThat(compilation)
            .classOutput()
            .packages()
            .allFilesExist(expectedFiles);
      }
    }
  }
}
