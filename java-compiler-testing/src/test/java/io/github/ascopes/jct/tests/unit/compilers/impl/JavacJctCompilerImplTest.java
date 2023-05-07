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
package io.github.ascopes.jct.tests.unit.compilers.impl;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.impl.JavacJctCompilerImpl;
import io.github.ascopes.jct.compilers.impl.JavacJctFlagBuilderImpl;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * {@link JavacJctCompilerImpl} tests.
 *
 * @author Ashley Scopes
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@DisplayName("JavacJctCompilerImpl tests")
class JavacJctCompilerImplTest {

  JavacJctCompilerImpl compiler;

  @BeforeEach
  void setUp() {
    compiler = new JavacJctCompilerImpl();
  }

  @DisplayName("Compilers have the expected JSR-199 compiler factory")
  @Test
  void compilersHaveTheExpectedCompilerFactory() {
    // Given
    try (var toolProviderMock = mockStatic(ToolProvider.class)) {
      var jsr199Compiler = mock(JavaCompiler.class);
      toolProviderMock.when(ToolProvider::getSystemJavaCompiler).thenReturn(jsr199Compiler);

      // When
      var actualCompiler = compiler.getCompilerFactory().createCompiler();

      // Then
      toolProviderMock.verify(ToolProvider::getSystemJavaCompiler);
      assertThat(actualCompiler).isSameAs(jsr199Compiler);
    }
  }

  @DisplayName("Compilers have the expected flag builder factory")
  @Test
  void compilersHaveTheExpectedFlagBuilderFactory() {
    // Given
    try (var flagBuilderMock = mockConstruction(JavacJctFlagBuilderImpl.class)) {
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
    try (var compilerClassMock = mockStatic(JavacJctCompilerImpl.class)) {
      var latestSupportedInt = someInt(11, 21);
      compilerClassMock
          .when(() -> JavacJctCompilerImpl.getLatestSupportedVersionInt(anyBoolean()))
          .thenReturn(latestSupportedInt);

      // When
      var defaultRelease = compiler.getDefaultRelease();

      // Then
      compilerClassMock
          .verify(() -> JavacJctCompilerImpl.getLatestSupportedVersionInt(false));

      assertThat(defaultRelease)
          .isEqualTo("%d", latestSupportedInt);
    }
  }

  @DisplayName("Compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    // Then
    assertThat(compiler.getName()).isEqualTo("JDK Compiler");
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

  @DisplayName("the earliest supported version int has the expected value")
  @CsvSource(useHeadersInDisplayName = true, value = {
      "SourceVersion.latest(), modules, expectedResult",
      "                     9,   false,              8",
      "                     9,    true,              9",
      "                    10,   false,              8",
      "                    10,    true,              9",
      "                    11,   false,              8",
      "                    11,    true,              9",
      "                    12,   false,              8",
      "                    12,    true,              9",
      "                    13,   false,              8",
      "                    13,    true,              9",
      "                    14,   false,              8",
      "                    14,    true,              9",
      "                    15,   false,              8",
      "                    15,    true,              9",
      "                    16,   false,              8",
      "                    16,    true,              9",
      "                    17,   false,              8",
      "                    17,    true,              9",
      "                    18,   false,              8",
      "                    18,    true,              9",
      "                    19,   false,              8",
      "                    19,    true,              9",
      // JDK-20 marks Java 8 as obsolete, so we do not support it.
      // We default to Java 9 regardless in this case.
      "                    20,   false,              9",
      "                    20,    true,              9",
      // JDK-21 still considers Java 8 to be the minimum version.
      "                    21,   false,              9",
      "                    21,    true,              9",
  })
  @ParameterizedTest(name = "expect {2} when {0} and {1}")
  void earliestSupportedVersionReturnsTheExpectedValue(
      int latest, boolean modules, int expectedResult
  ) {
    // Given
    try (var sourceVersionMock = mockStatic(SourceVersion.class)) {
      sourceVersionMock.when(SourceVersion::latestSupported)
          .then(ctx -> {
            var sourceVersion = mock(SourceVersion.class);
            when(sourceVersion.ordinal()).thenReturn(latest);
            return sourceVersion;
          });

      // When
      var actualResult = JavacJctCompilerImpl.getEarliestSupportedVersionInt(modules);

      // Then
      assertThat(actualResult).isEqualTo(expectedResult);
    }
  }

  @DisplayName("the earliest supported version int has the expected value")
  @CsvSource(useHeadersInDisplayName = true, value = {
      "SourceVersion.latest(), modules, expectedResult",
      "                     9,   false,              9",
      "                     9,    true,              9",
      "                    10,   false,             10",
      "                    10,    true,             10",
      "                    11,   false,             11",
      "                    11,    true,             11",
      "                    12,   false,             12",
      "                    12,    true,             12",
      "                    13,   false,             13",
      "                    13,    true,             13",
      "                    14,   false,             14",
      "                    14,    true,             14",
      "                    15,   false,             15",
      "                    15,    true,             15",
      "                    16,   false,             16",
      "                    16,    true,             16",
      "                    17,   false,             17",
      "                    17,    true,             17",
      "                    18,   false,             18",
      "                    18,    true,             18",
      "                    19,   false,             19",
      "                    19,    true,             19",
      "                    20,   false,             20",
      "                    20,    true,             20",
      "                    21,   false,             21",
      "                    21,    true,             21",
  })
  @ParameterizedTest(name = "expect {2} when {0} and {1}")
  void latestSupportedVersionReturnsTheExpectedValue(
      int latest, boolean modules, int expectedResult
  ) {
    // Given
    try (var sourceVersionMock = mockStatic(SourceVersion.class)) {
      sourceVersionMock.when(SourceVersion::latestSupported)
          .then(ctx -> {
            var sourceVersion = mock(SourceVersion.class);
            when(sourceVersion.ordinal()).thenReturn(latest);
            return sourceVersion;
          });

      // When
      var actualResult = JavacJctCompilerImpl.getLatestSupportedVersionInt(modules);

      // Then
      assertThat(actualResult).isEqualTo(expectedResult);
    }
  }
}
