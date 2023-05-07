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
package io.github.ascopes.jct.acceptancetests.spring

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.JRE
import org.springframework.context.index.processor.CandidateComponentsIndexer

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.Assumptions.assumeThat

@DisplayName("Spring Context Indexer acceptance tests")
class SpringContextIndexerTest {

  @BeforeEach
  void ensureJdk17() {
    assumeThat(JRE.currentVersion())
        .as("Spring 6 requires JDK 17 or newer")
        .isGreaterThanOrEqualTo(JRE.JAVA_17)
  }

  @DisplayName("Spring will index the application context as expected")
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "indexer", "org", "example")

      // When
      def compilation = compiler
          .addAnnotationProcessors(new CandidateComponentsIndexer())
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("META-INF", "spring.components")
          .isNotEmptyFile()
    }
  }

  @DisplayName("Spring will index the application context as expected with modules")
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpectedWithModules(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "indexer")

      // When
      def compilation = compiler
          .addAnnotationProcessors(new CandidateComponentsIndexer())
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("META-INF", "spring.components")
          .isNotEmptyFile()
    }
  }


  @DisplayName("Spring will index the application context as expected with multi-modules")
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpectedWithMultiModules(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathModule("org.example")
          .copyContentsFrom("src", "test", "resources", "code", "indexer")

      // When
      def compilation = compiler
          .addAnnotationProcessors(new CandidateComponentsIndexer())
          .compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputModules().moduleExists("org.example")
          .fileExists("META-INF", "spring.components")
          .isNotEmptyFile()
    }
  }
}
