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
package io.github.ascopes.jct.acceptancetests.serviceloaderjpms.testing

import io.github.ascopes.jct.acceptancetests.serviceloaderjpms.ServiceProcessor
import io.github.ascopes.jct.compilers.Compiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory

@DisplayName("ServiceProcessor tests (JPMS)")
class ServiceProcessorTest {

  @DisplayName("Expected files get created when the processor is run")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest(modules = true)
  void expectedFilesGetCreated(Compiler compiler) {
    def sources = newRamDirectory("sources")
        .createDirectory("org", "example")
        .copyContentsFrom("src", "test", "resources", "code")

    def compilation = compiler
        .addAnnotationProcessors(new ServiceProcessor())
        .addSourcePath(sources)
        .compile()

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutput().packages()
        .fileExists("META-INF/services/org.example.InsultProvider")
        .hasContent("org.example.MeanInsultProviderImpl")
  }
}
