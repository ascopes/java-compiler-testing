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
import io.github.ascopes.jct.junit.JavacCompilers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest

import java.nio.file.Path

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.TemporaryFileSystem.named

@DisplayName("Dagger acceptance tests")
class DaggerTest {
  @DisplayName("Dagger DI runs as expected in the annotation processing phase")
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void daggerDiRunsAsExpectedInTheAnnotationProcessingPhase(Compilable compiler) {
    // Given
    def sources = named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code"),
            "org/example"
        )

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation).isSuccessfulWithoutWarnings()

    assertThatCompilation(compilation)
        .sourceOutput().packages()
        .withFile("org/example/WebServer_Factory.java").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServer.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServerConfiguration.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServer_Factory.class").isRegularFile()
  }

  @Disabled("Module lookup logic needs working on.")
  @DisplayName("Dagger DI runs as expected in the annotation processing phase with modules")
  @JavacCompilers(modules = true)
  @ParameterizedTest(name = "for {0}")
  void daggerDiRunsAsExpectedInTheAnnotationProcessingPhaseWithModules(Compilable compiler) {
    // Given
    def sources = named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code"),
            "org/example"
        )
        .createFile(
            "module-info.java",
            """
            |module org.example {
            |  requires dagger;
            |  requires java.annotation;
            |  requires java.base;
            |  exports org.example;
            |  opens org.example to dagger;
            |}
            """.stripMargin()
        )

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation).isSuccessfulWithoutWarnings()

    assertThatCompilation(compilation)
        .sourceOutput().packages()
        .withFile("org/example/WebServer_Factory.java").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("module-info.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServer.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServerConfiguration.class").isRegularFile()

    assertThatCompilation(compilation)
        .classOutput().packages()
        .withFile("org/example/WebServer_Factory.class").isRegularFile()
  }
}
