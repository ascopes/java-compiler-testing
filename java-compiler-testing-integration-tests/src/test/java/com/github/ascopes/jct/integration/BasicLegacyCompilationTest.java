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

package com.github.ascopes.jct.integration;

import static com.github.ascopes.jct.assertions.CompilationAssert.assertThat;
import static com.github.ascopes.jct.integration.helpers.CompilerSources.allCompilers;

import com.github.ascopes.jct.paths.InMemoryPath;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Basic legacy compilation tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Basic legacy compilation integration tests")
class BasicLegacyCompilationTest {

  @DisplayName("I can compile a 'Hello, World!' program without warnings")
  @TestFactory
  Stream<DynamicTest> helloWorld() {
    return allCompilers(compiler -> {
      var sources = InMemoryPath
          .createPath()
          .createFile(
              "com/example/HelloWorld.java",
              "package com.example;",
              "public class HelloWorld {",
              "  public static void main(String[] args) {",
              "    System.out.println(\"Hello, World\");",
              "  }",
              "}"
          );

      var compilation = compiler
          .addSourcePath(sources)
          .deprecationWarnings(true)
          .compile();

      assertThat(compilation).isSuccessfulWithoutWarnings();
    });
  }
}
