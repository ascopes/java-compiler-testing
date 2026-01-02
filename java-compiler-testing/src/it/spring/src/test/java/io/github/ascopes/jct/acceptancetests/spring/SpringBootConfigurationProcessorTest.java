/*
 * Copyright (C) 2022 Ashley Scopes
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
package io.github.ascopes.jct.acceptancetests.spring;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.configurationprocessor.ConfigurationMetadataAnnotationProcessor;

@DisplayName("Spring Boot Configuration Processor acceptance tests")
class SpringBootConfigurationProcessorTest {

  @DisplayName("Spring will index the application context as expected")
  @EcjCompilerTest(minVersion = 17)
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpected(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "configuration", "org", "example");

      // When
      var compilation = compiler
          .addAnnotationProcessors(new ConfigurationMetadataAnnotationProcessor())
          .compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("META-INF", "spring-configuration-metadata.json")
          .isNotEmptyFile();
    }
  }

  @DisplayName("Spring will index the application context as expected with modules")
  @EcjCompilerTest(minVersion = 17)
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpectedWithModules(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "configuration");

      // When
      var compilation = compiler
          .addAnnotationProcessors(new ConfigurationMetadataAnnotationProcessor())
          .compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .fileExists("META-INF", "spring-configuration-metadata.json")
          .isNotEmptyFile();
    }
  }

  @DisplayName("Spring will index the application context as expected with multi-modules")
  @EcjCompilerTest(minVersion = 17)
  @JavacCompilerTest(minVersion = 17)
  void springWillIndexTheApplicationContextAsExpectedWithMultiModules(JctCompiler compiler) {
    try (var workspace = Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathModule("org.example")
          .copyContentsFrom("src", "test", "resources", "code", "configuration");

      // When
      var compilation = compiler
          .addAnnotationProcessors(new ConfigurationMetadataAnnotationProcessor())
          .compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputModules().moduleExists("org.example")
          .fileExists("META-INF", "spring-configuration-metadata.json")
          .isNotEmptyFile();
    }
  }
}
