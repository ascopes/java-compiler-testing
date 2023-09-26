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
package io.github.ascopes.jct.acceptancetests.autofactory;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("AutoFactory integration tests")
class AutoFactoryTest {

  @DisplayName("The AutoFactory class is created as expected")
  @JavacCompilerTest
  void autoFactoryClassIsCreatedAsExpected(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code");

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("org", "example", "UserFactory.class")
          .isNotEmptyFile();

      var userFactory = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.UserFactory")
          .getConstructor()
          .newInstance();

      var now = Instant.now();

      var user = userFactory.getClass()
          .getMethod("create", String.class, String.class, Instant.class)
          .invoke(userFactory, "12345", "Roy Rodgers McFreely", now);

      assertSoftly(softly -> {
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("id", "12345");
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("name", "Roy Rodgers McFreely");
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("createdAt", now);
      });
    }
  }
}
