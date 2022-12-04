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
package io.github.ascopes.acceptancetests.manifold

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.PathStrategy
import io.github.ascopes.jct.workspaces.Workspace
import org.junit.jupiter.api.DisplayName

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.SoftAssertions.assertSoftly

@DisplayName("Manifold Tuple acceptance tests")
@SuppressWarnings(["GroovyAssignabilityCheck", "GrUnresolvedAccess"])
class ManifoldTupleTest {
  @DisplayName("Tuple expressions compile as expected")
  @JavacCompilerTest(configurers = [ManifoldPluginConfigurer])
  void tupleExpressionsCompileAsExpected(JctCompiler compiler) {
    try (def workspace = Workspace.newWorkspace(PathStrategy.TEMP_DIRECTORIES)) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org", "example")
          .copyContentsFrom("src", "test", "resources", "code", "tuple")

      // When
      def compilation = compiler.compile(workspace)

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()

      def userType = compilation
          .classOutputs
          .classLoader
          .loadClass("org.example.User")
          .getDeclaredConstructor(long, String, int)

      def users = [
          userType.newInstance(123, "Roy Rodgers McFreely", 25),
          userType.newInstance(456, "Steve-O", 30),
          userType.newInstance(789, "Dave Davison", 23)
      ]

      def oldestUsers = compilation
          .classOutputs
          .classLoader
          .loadClass("org.example.UserRecords")
          .oldestUsers(users)

      assertThat(oldestUsers).hasSize(3)

      assertSoftly { softly ->
        softly.assertThat(oldestUsers[0].name).isEqualTo("Steve-O")
        softly.assertThat(oldestUsers[0].age).isEqualTo(30)
        softly.assertThat(oldestUsers[1].name).isEqualTo("Roy Rodgers McFreely")
        softly.assertThat(oldestUsers[1].age).isEqualTo(25)
        softly.assertThat(oldestUsers[2].name).isEqualTo("Dave Davison")
        softly.assertThat(oldestUsers[2].age).isEqualTo(23)
      }
    }
  }
}
