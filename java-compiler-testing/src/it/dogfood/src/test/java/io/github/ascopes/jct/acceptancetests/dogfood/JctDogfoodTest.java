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
package io.github.ascopes.jct.acceptancetests.dogfood;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThat;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;

/**
 * Tests that try to make JCT compile itself to see if it can correctly "test itself".
 *
 * @author Ashley Scopes
 */
@DisplayName("JCT dogfood acceptance tests")
class JctDogfoodTest {

  static final String MAIN_MODULE = "io.github.ascopes.jct";

  static final Path PROJECT_ROOT = projectBase()
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

  private static Path projectBase() {
    var path = Path.of(System.getProperty("user.dir"));
    for (var i = 0; i < 4; ++i) {
      path = path.getParent();
    }
    return path;
  }
}
