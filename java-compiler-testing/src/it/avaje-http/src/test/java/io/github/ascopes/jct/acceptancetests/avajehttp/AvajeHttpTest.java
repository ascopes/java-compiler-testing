/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.avajehttp;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Avaje HTTP acceptance tests")
class AvajeHttpTest {

  @DisplayName("HTTP client code gets generated as expected")
  @JavacCompilerTest(minVersion = 11)
  void httpClientCodeGetsGeneratedAsExpected(JctCompiler compiler) {
    // Given
    try (var workspace = Workspaces.newWorkspace()) {
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code");

      // When
      var compilation = compiler
          .compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .allFilesExist(
              "org/example/httpclient/GeneratedHttpComponent.class",
              "org/example/httpclient/UserApiHttpClient.class"
          );
    }
  }
}
