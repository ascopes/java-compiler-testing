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
package io.github.ascopes.jct.acceptancetests.dagger

import io.github.ascopes.jct.compilers.Compilable
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamFileSystem.newRamFileSystem

@DisplayName("Dagger acceptance tests")
class DaggerTest {
  @DisplayName("Dagger DI runs as expected in the annotation processing phase")
  @JavacCompilerTest
  void daggerDiRunsAsExpectedInTheAnnotationProcessingPhase(Compilable compiler) {
    // Given
    def sources = newRamFileSystem("sources")
        .createDirectory("org", "example")
        .copyContentsFrom("src", "test", "resources", "code")

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation).isSuccessfulWithoutWarnings()

    assertThatCompilation(compilation)
        .sourceOutput().packages()
        .fileExists("org/example/WebServer_Factory.java").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .fileExists("org/example/WebServer.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .fileExists("org/example/WebServerConfiguration.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .fileExists("org/example/WebServer_Factory.class").isRegularFile()
  }
}
