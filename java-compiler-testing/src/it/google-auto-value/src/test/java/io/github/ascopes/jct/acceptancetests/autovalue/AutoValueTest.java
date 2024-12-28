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
package io.github.ascopes.jct.acceptancetests.autovalue;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;

@DisplayName("AutoValue integration tests")
class AutoValueTest {

  @DisplayName("The AutoValue implementation class is created as expected")
  @JavacCompilerTest
  void autoValueImplementationClassIsCreatedAsExpected(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .createDirectory("org/example")
          .copyContentsFrom("src/test/resources/code");

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation)
          .isSuccessfulWithoutWarnings()
          .classOutputPackages()
          .allFilesExist(
              "org/example/AutoValue_User.class",
              "org/example/AutoBuilder_UserBuilder.class"
          );

      var userBuilderCls = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.UserBuilder");

      var now = Instant.now();

      var userBuilder = callMethod(userBuilderCls, "builder");
      userBuilder = callMethod(userBuilder, "setId", "123456");
      userBuilder = callMethod(userBuilder, "setName", "Roy Rodgers McFreely");
      userBuilder = callMethod(userBuilder, "setCreatedAt", now);
      var user = callMethod(userBuilder, "build");

      assertSoftly(softly -> {
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("id", "123456");
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("name", "Roy Rodgers McFreely");
        softly.assertThatObject(user).hasFieldOrPropertyWithValue("createdAt", now);
      });
    }
  }

  private Object callMethod(Object obj, String method, Object... args) throws Throwable {
    var cls = obj instanceof Class<?> castCls
        ? castCls
        : obj.getClass();

    // Enforce reflective access to be allowed.
    cls.getModule().addOpens(cls.getPackageName(), getClass().getModule());
    cls.getModule().addExports(cls.getPackageName(), getClass().getModule());

    var methodObj = Stream.of(cls.getMethods())
        .filter(m -> m.getName().equals(method))
        .findAny()
        .orElseThrow();

    // Allow access to non-public members.
    methodObj.setAccessible(true);

    return obj instanceof Class<?>
        ? methodObj.invoke(null, args)
        : methodObj.invoke(obj, args);
  }
}
