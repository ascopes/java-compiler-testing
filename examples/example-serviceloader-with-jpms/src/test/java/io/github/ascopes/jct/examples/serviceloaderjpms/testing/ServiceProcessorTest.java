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

package io.github.ascopes.jct.examples.serviceloaderjpms.testing;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static io.github.ascopes.jct.paths.RamPath.createPath;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.examples.serviceloaderjpms.ServiceProcessor;
import io.github.ascopes.jct.junit.EcjCompilers;
import io.github.ascopes.jct.junit.JavacCompilers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

@DisplayName("ServiceProcessor tests (JPMS)")
class ServiceProcessorTest {

  @DisplayName("Expected files get created when the processor is run")
  @EcjCompilers(modules = true)
  @JavacCompilers(modules = true)
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
            "import io.github.ascopes.jct.examples.serviceloaderjpms.Service;",
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
        .isSuccessfulWithoutWarnings();
    // TODO(ascopes): fix this to work with the file manager rewrite.
    //.classOutput()
    //.file("META-INF/services/com.example.InsultProvider")
    //.exists()
    //.hasContent("com.example.MeanInsultProviderImpl");
  }
}
