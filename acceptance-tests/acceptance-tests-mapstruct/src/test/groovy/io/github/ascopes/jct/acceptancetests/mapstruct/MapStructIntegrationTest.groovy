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
package io.github.ascopes.jct.acceptancetests.mapstruct

import io.github.ascopes.jct.compilers.JctCompiler
import io.github.ascopes.jct.junit.JavacCompilerTest
import io.github.ascopes.jct.workspaces.Workspaces
import org.junit.jupiter.api.DisplayName

import javax.tools.StandardLocation

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation
import static org.assertj.core.api.SoftAssertions.assertSoftly


@SuppressWarnings('GrUnresolvedAccess')
class MapStructIntegrationTest {

  @DisplayName("MapStruct generates expected mapping code")
  @JavacCompilerTest
  void mapStructGeneratesExpectedMappingCode(JctCompiler compiler) {
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "flat")

      // When
      def compilation = compiler.compile(workspace)

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

  @DisplayName("MapStruct generates expected mapping code for modules")
  @JavacCompilerTest(minVersion = 9)
  void mapStructGeneratesExpectedMappingCodeForModules(JctCompiler compiler) {
    // Given
    try (def workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "jpms")

      // When
      def compilation = compiler.compile(workspace)

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
}
