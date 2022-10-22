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
package io.github.ascopes.jct.acceptancetests.mapstruct

import io.github.ascopes.jct.compilers.Compilable
import io.github.ascopes.jct.junit.JavacCompilerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

import javax.tools.StandardLocation

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static io.github.ascopes.jct.pathwrappers.RamFileSystem.newRamFileSystem
import static org.assertj.core.api.SoftAssertions.assertSoftly

@DisplayName("MapStruct integration tests")
@SuppressWarnings('GrUnresolvedAccess')
class MapStructIntegrationTest {

  @DisplayName("MapStruct generates expected mapping code")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest
  void mapStructGeneratesExpectedMappingCode(Compilable compiler) {
    // Given
    def sources = newRamFileSystem("sources")
        .rootDirectory()
        .copyContentsFrom("src", "test", "resources", "code", "flat")

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()

    def classLoader = compilation.getFileManager().getClassLoader(StandardLocation.CLASS_OUTPUT)
    def packageName = "org.example"

    def carTypeClass = classLoader.loadClass("${packageName}.CarType")
    def carClass = classLoader.loadClass("${packageName}.Car")
    def carMapperClass = classLoader.loadClass("${packageName}.CarMapper")

    def car = carClass.getConstructor().newInstance()

    car.make = "VW Polo"
    car.type = carTypeClass.HATCHBACK
    car.numberOfSeats = 5

    def carMapper = carMapperClass.INSTANCE
    def carDto = carMapper.carToCarDto(car)

    assertSoftly { softly ->
      softly.assertThatObject(carDto.make).isEqualTo("VW Polo")
      softly.assertThatObject(carDto.type).isEqualTo("HATCHBACK")
      softly.assertThatObject(carDto.seatCount).isEqualTo(5)
    }
  }

  @DisplayName("MapStruct generates expected mapping code for modules")
  @Execution(ExecutionMode.CONCURRENT)
  @JavacCompilerTest(modules = true)
  void mapStructGeneratesExpectedMappingCodeForModules(Compilable compiler) {
    // Given
    def sources = newRamFileSystem("sources")
        .rootDirectory()
        .copyContentsFrom("src", "test", "resources", "code", "jpms")

    // When
    def compilation = compiler
        .addSourcePath(sources)
        .compile()

    // Then
    assertThatCompilation(compilation)
        .isSuccessfulWithoutWarnings()

    def classLoader = compilation.getFileManager().getClassLoader(StandardLocation.CLASS_OUTPUT)
    def packageName = "org.example"

    def carTypeClass = classLoader.loadClass("${packageName}.CarType")
    def carClass = classLoader.loadClass("${packageName}.Car")
    def carMapperClass = classLoader.loadClass("${packageName}.CarMapper")

    def car = carClass.getConstructor().newInstance()

    car.make = "VW Polo"
    car.type = carTypeClass.HATCHBACK
    car.numberOfSeats = 5

    def carMapper = carMapperClass.INSTANCE
    def carDto = carMapper.carToCarDto(car)

    assertSoftly { softly ->
      softly.assertThatObject(carDto.make).isEqualTo("VW Polo")
      softly.assertThatObject(carDto.type).isEqualTo("HATCHBACK")
      softly.assertThatObject(carDto.seatCount).isEqualTo(5)
    }
  }
}
