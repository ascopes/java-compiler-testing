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

import io.github.ascopes.jct.compilers.Compilable
import io.github.ascopes.jct.junit.JavacCompilers
import io.github.ascopes.jct.pathwrappers.TemporaryFileSystem
import org.checkerframework.checker.nullness.NullnessChecker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.JRE
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest

import java.nio.file.Path

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.Assumptions.assumeThat

@DisplayName("Checkerframework Nullness acceptance tests")
class CheckerNullTest {

  @BeforeEach
  void setUp() {
    assumeThat(JRE.currentVersion())
        .withFailMessage(
            "This test is disabled on this JRE due to unexpected behaviour in Checkerframework"
        )
        .isIn(JRE.JAVA_16, JRE.JAVA_17, JRE.JAVA_18, JRE.JAVA_19, JRE.JAVA_20)
  }

  @DisplayName("Happy paths work as expected")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void happyPathsWorkAsExpected(Compilable compiler) {
    // Given
    def sources = TemporaryFileSystem
        .named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code", "nullness", "happy"),
            "org/example"
        )

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .addAnnotationProcessors(new NullnessChecker())
        .compile()

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
  }

  @DisplayName("Sad paths fail as expected")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void sadPathsFailAsExpected(Compilable compiler) {
    // Given
    def sources = TemporaryFileSystem
        .named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code", "nullness", "sad"),
            "org/example"
        )

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .addAnnotationProcessors(new NullnessChecker())
        .compile()

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
