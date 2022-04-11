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

package com.github.ascopes.jct.examples.serviceloader.testing;

import static com.github.ascopes.jct.assertions.CompilationAssert.assertThat;

import com.github.ascopes.jct.compilers.Compilers;
import com.github.ascopes.jct.examples.serviceloader.ServiceProcessor;
import com.github.ascopes.jct.paths.InMemoryPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ServiceProcessor tests (no JPMS)")
class ServiceProcessorTest {

  @DisplayName("Expected files get created when the processor is run")
  @Test
  void expectedFilesGetCreated() {
    var sources = InMemoryPath
        .createPath()
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
            "import com.github.ascopes.jct.examples.serviceloader.Service;",
            "",
            "@Service(InsultProvider.class)",
            "public class MeanInsultProviderImpl implements InsultProvider {",
            "  @Override",
            "  public String getInsult() {",
            "    return \"Silence is the best answer for a fool\";",
            "  }",
            "}"
        );

    var compilation = Compilers
        .javac()
        .addAnnotationProcessors(new ServiceProcessor())
        .addSourcePath(sources)
        .includeCurrentClassPath(true)
        .releaseVersion(11)
        .compile();

    assertThat(compilation)
        .isSuccessfulWithoutWarnings()
        .classOutput()
        .file("META-INF/services/com.example.InsultProvider")
        .exists()
        .hasContent("com.example.MeanInsultProviderImpl");
  }
}
