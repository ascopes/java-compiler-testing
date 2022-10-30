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

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.context.index.processor.CandidateComponentsIndexer

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory

@DisplayName("Spring Context Indexer acceptance tests")
class SpringContextIndexerTest {

  // TODO(ascopes): use JPMS modules when we move to Spring Framework v6.0.0
  @DisplayName("Spring will index the application context as expected")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest
  void springWillIndexTheApplicationContextAsExpected(JctCompiler compiler) {
    // Given
    def sources = newRamDirectory("sources")
        .createDirectory("org", "example")
        .copyContentsFrom("src", "test", "resources", "code")

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
        .fileExists("META-INF/spring.components")
        .isNotEmptyFile()
  }
}
