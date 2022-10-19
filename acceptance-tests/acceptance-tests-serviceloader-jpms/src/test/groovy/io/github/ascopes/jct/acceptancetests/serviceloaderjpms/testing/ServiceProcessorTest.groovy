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
import io.github.ascopes.jct.compilers.Compilable
import io.github.ascopes.jct.junit.JavacCompilers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest

import java.nio.file.Path

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.TemporaryFileSystem.named

@DisplayName("ServiceProcessor tests (JPMS)")
class ServiceProcessorTest {

  @DisplayName("Expected files get created when the processor is run")
  @JavacCompilers(modules = true)
  @ParameterizedTest(name = "for {0}")
  void expectedFilesGetCreated(Compilable compiler) {
    def sources = named("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code"),
            "org/example"
        )

    def compilation = compiler
        .addAnnotationProcessors(new ServiceProcessor())
        .addSourcePath(sources)
        .compile()

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutput().packages()
        .withFile("META-INF/services/com.example.InsultProvider")
        .hasContent("com.example.MeanInsultProviderImpl")
  }
}
