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
package io.github.ascopes.jct.acceptancetests.lombok;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.JRE;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * Example integration test that makes use of the Lombok annotation processor.
 *
 * <p>This proves the ability to interoperate between Lombok which modifies internal
 * details of the compiler implementation and the java-compiler-testing facility.
 *
 * @author Ashley Scopes
 */
@DisplayName("Lombok Integration test")
class LombokTest {

  @BeforeEach
  void setUp() {
    assumeThat(JRE.currentVersion())
        .withFailMessage("Lombok fails under JDK-21: See Lombok GH-3393")
        .isLessThanOrEqualTo(JRE.JAVA_20);
  }

  @DisplayName("Lombok @Data compiles the expected data class")
  @JavacCompilerTest
  void lombokDataCompilesTheExpectedDataClass(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "flat");

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation).isSuccessful();

      var animalClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.Animal");

      var animal = animalClass
          .getDeclaredConstructor(String.class, int.class, int.class)
          .newInstance("Cat", 4, 5);

      assertSoftly(softly -> {
        softly.assertThat(animal).hasFieldOrPropertyWithValue("name", "Cat");
        softly.assertThat(animal).hasFieldOrPropertyWithValue("legCount", 4);
        softly.assertThat(animal).hasFieldOrPropertyWithValue("age", 5);
      });
    }
  }

  @DisplayName("Lombok @Data compiles the expected data class with module support")
  @JavacCompilerTest(minVersion = 9)
  void lombokDataCompilesTheExpectedDataClassJpms(JctCompiler compiler) throws Throwable {
    try (var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "jpms");

      // When
      var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation).isSuccessful();

      var animalClass = compilation
          .getClassOutputs()
          .getClassLoader()
          .loadClass("org.example.Animal");

      var animal = animalClass
          .getDeclaredConstructor(String.class, int.class, int.class)
          .newInstance("Cat", 4, 5);

      assertSoftly(softly -> {
        softly.assertThat(animal).hasFieldOrPropertyWithValue("name", "Cat");
        softly.assertThat(animal).hasFieldOrPropertyWithValue("legCount", 4);
        softly.assertThat(animal).hasFieldOrPropertyWithValue("age", 5);
      });
    }
  }
}
