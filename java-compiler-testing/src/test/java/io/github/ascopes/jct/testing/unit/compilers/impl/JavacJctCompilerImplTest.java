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
package io.github.ascopes.jct.testing.unit.compilers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.impl.JavacJctCompilerImpl;
import io.github.ascopes.jct.compilers.impl.JavacJctFlagBuilderImpl;
import java.util.Random;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link JavacJctCompilerImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavacJctCompilerImpl tests")
class JavacJctCompilerImplTest {

  JavaCompiler javaCompiler;
  JavacJctCompilerImpl compiler;

  @BeforeEach
  void setUp() {
    javaCompiler = mock(JavaCompiler.class);
    compiler = new JavacJctCompilerImpl(javaCompiler);
  }

  @DisplayName("compilers have the expected default name")
  @Test
  void compilersHaveTheExpectedDefaultName() {
    // Then
    assertThat(compiler.getName()).isEqualTo("JDK Compiler");
  }

  @DisplayName("compilers have the expected JSR-199 compiler implementation")
  @Test
  void compilersHaveTheExpectedCompilerImplementation() {
    // Then
    assertThat(compiler.getJsr199Compiler()).isSameAs(javaCompiler);
  }

  @DisplayName("compilers have the expected flag builder")
  @Test
  void compilersHaveTheExpectedFlagBuilder() {
    // Then
    assertThat(compiler.getFlagBuilder()).isInstanceOf(JavacJctFlagBuilderImpl.class);
  }

  @DisplayName("compilers have the -implicit:class flag set")
  @Test
  void compilersHaveTheImplicitClassFlagSet() {
    // Then
    assertThat(compiler.getCompilerOptions()).contains("-implicit:class");
  }

  @DisplayName("compilers have the expected default release string")
  @Test
  void compilersHaveTheExpectedDefaultRelease() {
    // Given
    try (var compilerClassMock = mockStatic(JavacJctCompilerImpl.class)) {
      var latestSupportedInt = 11 + new Random().nextInt(10);

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

  @DisplayName("the earliest supported version int has the expected value")
  @CsvSource({
      "true, 9",
      "false, 8",
  })
  @ParameterizedTest(name = "expect {1} when modules = {0}")
  void theEarliestSupportedVersionIntHasTheExpectedValue(boolean modules, int expect) {
    // Then
    assertThat(JavacJctCompilerImpl.getEarliestSupportedVersionInt(modules))
        .isEqualTo(expect);
  }

  @DisplayName("the latest supported version int has the expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for modules = {0}")
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void theLatestSupportedVersionIntHasTheExpectedValue(boolean modules) {
    // Given
    try (var sourceVersionMock = mockStatic(SourceVersion.class)) {
      var latestSupportedOrdinal = 11 + new Random().nextInt(10);

      var latestSupportedEnum = mock(SourceVersion.class);
      when(latestSupportedEnum.ordinal())
          .thenReturn(latestSupportedOrdinal);

      sourceVersionMock.when(SourceVersion::latestSupported)
          .thenReturn(latestSupportedEnum);

      // Then
      assertThat(JavacJctCompilerImpl.getLatestSupportedVersionInt(modules))
          .isEqualTo(latestSupportedOrdinal);
    }
  }
}
