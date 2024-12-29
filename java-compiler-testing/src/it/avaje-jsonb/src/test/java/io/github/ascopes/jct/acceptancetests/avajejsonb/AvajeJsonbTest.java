/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.avajejsonb;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.generator.Processor;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.skyscreamer.jsonassert.JSONAssert;

@DisplayName("Avaje Jsonb acceptance tests")
class AvajeJsonbTest {
  @DisplayName("JSON handling logic is generated as expected")
  @JavacCompilerTest(minVersion = 11)
  void jsonHandlingLogicIsGeneratedAsExpected(JctCompiler compiler) throws Throwable {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code");

      // When
      var compilation = compiler
          .addAnnotationProcessors(new Processor())
          .compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings();

      var userClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.User");

      var adapterClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.jsonb.UserJsonAdapter");

      var jsonb = Jsonb.builder()
          .add(userClass, (Jsonb jsonBuilder) -> {
            try {
              return (JsonAdapter<?>) adapterClass.getDeclaredConstructor(Jsonb.class)
                  .newInstance(jsonBuilder);
            } catch (ReflectiveOperationException ex) {
              throw new RuntimeException(ex);
            }
          })
          .build();

      var user = userClass
          .getConstructor(String.class, String.class, Instant.class)
          .newInstance("1234", "ascopes", Instant.ofEpochSecond(1_671_879_153L));

      JSONAssert.assertEquals(
          "{\"id\": \"1234\", \"userName\": \"ascopes\", \"createdAt\": \"2022-12-24T10:52:33Z\"}",
          jsonb.toJson(user),
          true
      );
    }
  }
}
