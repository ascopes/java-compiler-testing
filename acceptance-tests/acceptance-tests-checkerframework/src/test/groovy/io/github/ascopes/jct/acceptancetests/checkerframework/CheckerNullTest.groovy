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
package io.github.ascopes.jct.acceptancetests.checkerframework

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.checkerframework.checker.nullness.NullnessChecker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.JRE

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.Assumptions.assumeThat

@DisplayName("Checkerframework Nullness acceptance tests")
class CheckerNullTest {

  @BeforeEach
  void setUp() {
    assumeThat(JRE.currentVersion())
        .as("Checkerframework may misbehave on this JVM, so has been disabled")
        .isIn(JRE.JAVA_16, JRE.JAVA_17, JRE.JAVA_18, JRE.JAVA_19, JRE.JAVA_20)
  }

  @DisplayName("Happy paths work as expected")
  @JavacCompilerTest
  void happyPathsWorkAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "nullness", "happy")

      // When
      def compilation = compiler
          .addAnnotationProcessors(new NullnessChecker())
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
    }
  }

  @DisplayName("Sad paths fail as expected")
  @JavacCompilerTest
  void sadPathsFailAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "nullness", "sad")

      // When
      def compilation = compiler
          .addAnnotationProcessors(new NullnessChecker())
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isFailure()
          .diagnostics()
          .errors()
          .singleElement()
          .message()
          .startsWith("[assignment] incompatible types in assignment.")
    }
  }
}
