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
package io.github.ascopes.jct.acceptancetests.immutables;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;

/**
 * Example integration test that makes use of the Immutables annotation processor.
 *
 * <p>This proves the ability to interoperate between a common and widely used annotation processor
 * library and the JCT library.
 *
 * @author Ashley Scopes
 */
@DisplayName("Immutables Integration test")
class ImmutablesTest {

  @DisplayName("Immutables @Value produces the expected class")
  @JavacCompilerTest
  void immutablesValueProducesTheExpectedClass(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "flat");

      var compilation = compiler.compile(workspace);

      assertThatCompilation(compilation).isSuccessful();

      var animalClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.ImmutableAnimal");

      var animalBuilder = callMethod(animalClass, "builder");
      animalBuilder = callMethod(animalBuilder, "name", "cat");
      animalBuilder = callMethod(animalBuilder, "legCount", 4);
      animalBuilder = callMethod(animalBuilder, "age", 5);
      var animal = callMethod(animalBuilder, "build");

      assertSoftly(softly -> {
        softly.assertThat(animal).hasFieldOrPropertyWithValue("name", "cat");
        softly.assertThat(animal).hasFieldOrPropertyWithValue("legCount", 4);
        softly.assertThat(animal).hasFieldOrPropertyWithValue("age", 5);
      });
    }
  }

  @DisplayName("Immutables @Value produces the expected class for modules")
  @JavacCompilerTest(minVersion = 9)
  void immutablesValueProducesTheExpectedClassForModules(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "jpms");

      var compilation = compiler.compile(workspace);

      assertThatCompilation(compilation).isSuccessful();

      var animalClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.ImmutableAnimal");

      var animalBuilder = callMethod(animalClass, "builder");
      animalBuilder = callMethod(animalBuilder, "name", "cat");
      animalBuilder = callMethod(animalBuilder, "legCount", 4);
      animalBuilder = callMethod(animalBuilder, "age", 5);
      var animal = callMethod(animalBuilder, "build");

      assertSoftly(softly -> {
        softly.assertThat(animal).hasFieldOrPropertyWithValue("name", "cat");
        softly.assertThat(animal).hasFieldOrPropertyWithValue("legCount", 4);
        softly.assertThat(animal).hasFieldOrPropertyWithValue("age", 5);
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
