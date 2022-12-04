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
package io.github.ascopes.jct.acceptancetests.autoservice


import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspace
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation

@DisplayName("AutoService integration tests")
class AutoServiceTest {

  @DisplayName("The AutoService descriptor is created as expected")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest
  void autoServiceDescriptorIsCreatedAsExpected(JctCompiler compiler) {
    try (def workspace = Workspace.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org/example")
          .copyContentsFrom("src/test/resources/code")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutput()
          .packages()
          .fileExists("META-INF/services/org.example.SomeInterface")
          .hasContent("org.example.SomeImpl")
    }
  }
}
