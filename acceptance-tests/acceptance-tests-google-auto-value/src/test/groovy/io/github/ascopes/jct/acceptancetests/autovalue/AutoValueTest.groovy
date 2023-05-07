/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.autovalue

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import java.time.Instant

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.SoftAssertions.assertSoftly

@DisplayName("AutoValue integration tests")
@SuppressWarnings('GrUnresolvedAccess')
class AutoValueTest {

  @DisplayName("The AutoValue implementation class is created as expected")
  @JavacCompilerTest
  void autoValueImplementationClassIsCreatedAsExpected(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
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

      assertThatCompilation(compilation)
          .classOutputPackages()
          .allFilesExist(
              "org/example/AutoValue_User.class",
              "org/example/AutoBuilder_UserBuilder.class"
          )

      def userBuilder = compilation
          .classOutputs
          .classLoader
          .loadClass("org.example.UserBuilder")

      def now = Instant.now()

      def user = userBuilder
          .builder()
          .setId("123456")
          .setName("Roy Rodgers McFreely")
          .setCreatedAt(now)
          .build()

      assertSoftly { softly ->
        softly.assertThatObject(user)
            .hasFieldOrPropertyWithValue("id", "123456")
        softly.assertThatObject(user)
            .hasFieldOrPropertyWithValue("name", "Roy Rodgers McFreely")
        softly.assertThatObject(user)
            .hasFieldOrPropertyWithValue("createdAt", now)
      }
    }
  }
}
