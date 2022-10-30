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
package io.github.ascopes.jct.acceptancetests.immutables

import io.github.ascopes.jct.compilers.Compiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

import javax.tools.StandardLocation

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamDirectory.newRamDirectory
import static org.assertj.core.api.SoftAssertions.assertSoftly

/**
 * Example integration test that makes use of the Immutables annotation processor.
 *
 * <p>This proves the ability to interoperate between a common and widely used annotation processor
 * library and the JCT library.
 *
 * @author Ashley Scopes
 */
@DisplayName("Immutables Integration test")
@SuppressWarnings("GrUnresolvedAccess")
class ImmutablesIntegrationTest {

  @DisplayName("Immutables @Value produces the expected class")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest
  void immutablesValueProducesTheExpectedClass(Compiler compiler) {
    // Given
    def sources = newRamDirectory("sources")
        .rootDirectory()
        .copyContentsFrom("src", "test", "resources", "code", "flat")

    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    assertThatCompilation(compilation)
        .isSuccessful()

    def animalClass = compilation
        .getFileManager()
        .getClassLoader(StandardLocation.CLASS_OUTPUT)
        .loadClass("org.example.ImmutableAnimal")

    def animal = animalClass
        .builder()
        .name("Cat")
        .legCount(4)
        .age(5)
        .build()

    assertSoftly { softly ->
      softly.assertThatObject(animal.name).isEqualTo("Cat")
      softly.assertThatObject(animal.legCount).isEqualTo(4)
      softly.assertThatObject(animal.age).isEqualTo(5)
    }
  }

  @DisplayName("Immutables @Value produces the expected class for modules")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest(modules = true)
  void immutablesValueProducesTheExpectedClassForModules(Compiler compiler) {
    // Given
    def sources = newRamDirectory("sources")
        .rootDirectory()
        .copyContentsFrom("src", "test", "resources", "code", "jpms")

    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    assertThatCompilation(compilation)
        .isSuccessful()

    def animalClass = compilation
        .getFileManager()
        .getClassLoader(StandardLocation.CLASS_OUTPUT)
        .loadClass("org.example.ImmutableAnimal")

    def animal = animalClass
        .builder()
        .name("Cat")
        .legCount(4)
        .age(5)
        .build()

    assertSoftly { softly ->
      softly.assertThatObject(animal.name).isEqualTo("Cat")
      softly.assertThatObject(animal.legCount).isEqualTo(4)
      softly.assertThatObject(animal.age).isEqualTo(5)
    }
  }
}
