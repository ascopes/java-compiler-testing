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
package io.github.ascopes.jct.acceptancetests.springcontextindexer

import io.github.ascopes.jct.compilers.Compilable
import io.github.ascopes.jct.junit.JavacCompilers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.context.index.processor.CandidateComponentsIndexer

import java.nio.file.Path

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.TemporaryFileSystem.named

@DisplayName("Spring Context Indexer acceptance tests")
@EnabledOnOs(value = [OS.LINUX, OS.MAC], disabledReason = "Unexpected behaviour on this OS")
class SpringContextIndexerTest {
  @DisplayName("Spring will index the application context as expected")
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void springWillIndexTheApplicationContextAsExpected(Compilable compiler) {
    // Given
    def sources = named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code"),
            "org/example"
        )

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .addAnnotationProcessors(new CandidateComponentsIndexer())
        .compile()

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutput()
        .packages()
        .withFile("META-INF/spring.components")
        .isNotEmptyFile()
  }
}
