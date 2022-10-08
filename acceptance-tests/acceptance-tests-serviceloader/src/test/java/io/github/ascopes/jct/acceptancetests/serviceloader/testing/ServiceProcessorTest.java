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
package io.github.ascopes.jct.acceptancetests.serviceloader.testing;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.paths.RamPath.createPath;

import io.github.ascopes.jct.acceptancetests.serviceloader.ServiceProcessor;
import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.junit.JavacCompilers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

@DisplayName("ServiceProcessor tests (no JPMS)")
class ServiceProcessorTest {

  @DisplayName("Expected files get created when the processor is run")
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void expectedFilesGetCreated(Compilable<?, ?> compiler) {
    var sources = createPath("sources")
        .createFile(
            "com/example/InsultProvider.java",
            "package com.example;",
            "public interface InsultProvider {",
            "  String getInsult();",
            "}"
        )
        .createFile(
            "com/example/MeanInsultProviderImpl.java",
            "package com.example;",
            "",
            "import io.github.ascopes.jct.acceptancetests.serviceloader.Service;",
            "",
            "@Service(InsultProvider.class)",
            "public class MeanInsultProviderImpl implements InsultProvider {",
            "  @Override",
            "  public String getInsult() {",
            "    return \"Silence is the best answer for a fool\";",
            "  }",
            "}"
        );

    var compilation = compiler
        .addAnnotationProcessors(new ServiceProcessor())
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()
        .files()
        .classOutput().exists()
        .file("META-INF/services/com.example.InsultProvider").exists()
        .hasContent("com.example.MeanInsultProviderImpl");
  }
}
