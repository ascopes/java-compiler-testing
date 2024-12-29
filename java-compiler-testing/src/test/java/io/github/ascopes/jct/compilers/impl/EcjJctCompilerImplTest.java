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
package io.github.ascopes.jct.compilers.impl;

import static io.github.ascopes.jct.fixtures.Fixtures.someInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EcjJctCompilerImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("EcjJctCompilerImpl tests")
class EcjJctCompilerImplTest {

  EcjJctCompilerImpl compiler;

  @BeforeEach
  void setUp() {
    compiler = new EcjJctCompilerImpl();
  }

  @DisplayName("Compilers have the expected JSR-199 compiler factory")
  @Test
  void compilersHaveTheExpectedCompilerFactory() {
    // When
    var actualCompiler = compiler.getCompilerFactory().createCompiler();

    // Then
    assertThat(actualCompiler).isInstanceOf(EclipseCompiler.class);
  }

  @DisplayName("Compilers have the expected flag builder factory")
  @Test
  void compilersHaveTheExpectedFlagBuilderFactory() {
    // Given
    try (var flagBuilderMock = mockConstruction(EcjJctFlagBuilderImpl.class)) {
      // When
      var flagBuilder = compiler.getFlagBuilderFactory().createFlagBuilder();

      // Then
      assertThat(flagBuilderMock.constructed()).hasSize(1);
      assertThat(flagBuilder).isSameAs(flagBuilderMock.constructed().get(0));
    }
  }

  @DisplayName("Compilers have the expected default release string")
  @Test
  void compilersHaveTheExpectedDefaultRelease() {
    // Given
    try (var compilerClassMock = mockStatic(EcjJctCompilerImpl.class)) {
      var latestSupportedInt = someInt(17, 21);
      compilerClassMock
          .when(EcjJctCompilerImpl::getLatestSupportedVersionInt)
          .thenReturn(latestSupportedInt);

      // When
      var defaultRelease = compiler.getDefaultRelease();

      // Then
      compilerClassMock
          .verify(EcjJctCompilerImpl::getLatestSupportedVersionInt);

      assertThat(defaultRelease)
          .isEqualTo("%d", latestSupportedInt);
    }
  }

  @DisplayName("Compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    // Then
    assertThat(compiler.getName()).isEqualTo("ECJ");
  }

  @DisplayName("Compilers have no default compiler flags set")
  @Test
  void compilersHaveNoDefaultCompilerFlagsSet() {
    // Then
    assertThat(compiler.getCompilerOptions()).isEmpty();
  }

  @DisplayName("Compilers have no default annotation processor flags set")
  @Test
  void compilersHaveNoDefaultAnnotationProcessorFlagsSet() {
    // Then
    assertThat(compiler.getAnnotationProcessorOptions()).isEmpty();
  }

  @DisplayName("Compilers have no default annotation processors set")
  @Test
  void compilersHaveNoDefaultAnnotationProcessorsSet() {
    // Then
    assertThat(compiler.getAnnotationProcessors()).isEmpty();
  }

  @DisplayName("Compilers have the expected latest release")
  @Test
  void latestSupportedVersionReturnsTheExpectedValue() {
    // Given
    var expected = (int) ((ClassFileConstants.getLatestJDKLevel() >> 16L)
        - ClassFileConstants.MAJOR_VERSION_0);

    // When
    var actual = EcjJctCompilerImpl.getLatestSupportedVersionInt();

    // Then
    assertThat(expected).isEqualTo(actual);
  }

  @DisplayName("Compilers have the expected earliest release")
  @Test
  void earliestSupportedVersionReturnsTheExpectedValue() {
    // Given
    var expected = (int) ((ClassFileConstants.JDK1_8 >> 16L)
        - ClassFileConstants.MAJOR_VERSION_0);

    // When
    var actual = EcjJctCompilerImpl.getEarliestSupportedVersionInt();

    // Then
    assertThat(expected).isEqualTo(actual);
  }
}
