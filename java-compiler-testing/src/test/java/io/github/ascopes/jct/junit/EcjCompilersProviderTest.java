/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EcjCompilersProvider} tests (Java 11-16 cases).
 */
@DisplayName("EcjCompilersProvider tests (Java 11-16 cases)")
class EcjCompilersProviderTest {

  @DisplayName(".provideArguments() returns an empty stream")
  @Test
  void provideArgumentsReturnsEmptyStream() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java11ClassLoader());

    // When
    var result = provider.provideArguments(mock());

    // Then
    assertThat(result).isEmpty();
  }

  @DisplayName(".initializeNewCompiler() throws NoClassDefFoundError when on Java 11")
  @Test
  void initialiseNewCompilerThrowsNoClassDefFoundErrorWhenOnJava11() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java11ClassLoader());

    // Then
    assertThatExceptionOfType(NoClassDefFoundError.class)
        .isThrownBy(provider::initializeNewCompiler)
        .withMessage("ECJ implementation not found (perhaps you are running on a Java version "
            + "older than Java 17?");
  }

  @DisplayName(".initializeNewCompiler() throws IllegalStateException if reflection fails")
  @Test
  void initialiseNewCompilerThrowsIllegalStateExceptionIfReflectionFails() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java17ClassLoader());

    // Then
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(provider::initializeNewCompiler)
        .withMessage(
            "Failed performing operation \"initialising ECJ frontend classes\". This is a bug.")
        .havingCause()
        .isInstanceOf(InvocationTargetException.class);
  }

  @DisplayName(".minSupportedVersion() throws NoClassDefFoundError when on Java 11")
  @Test
  void minSupportedVersionThrowsNoClassDefFoundErrorWhenOnJava11() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java11ClassLoader());

    // Then
    assertThatExceptionOfType(NoClassDefFoundError.class)
        .isThrownBy(provider::minSupportedVersion)
        .withMessage("ECJ implementation not found (perhaps you are running on a Java version "
            + "older than Java 17?");
  }

  @DisplayName(".minSupportedVersion() throws IllegalStateException if reflection fails")
  @Test
  void minSupportedVersionThrowsIllegalStateExceptionIfReflectionFails() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java17ClassLoader());

    // Then
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(provider::minSupportedVersion)
        .withMessage(
            "Failed performing operation \"get earliest supported language version for ECJ\". "
                + "This is a bug.")
        .havingCause()
        .isInstanceOf(InvocationTargetException.class);
  }

  @DisplayName(".maxSupportedVersion() throws NoClassDefFoundError when on Java 11")
  @Test
  void maxSupportedVersionThrowsNoClassDefFoundErrorWhenOnJava11() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java11ClassLoader());

    // Then
    assertThatExceptionOfType(NoClassDefFoundError.class)
        .isThrownBy(provider::maxSupportedVersion)
        .withMessage("ECJ implementation not found (perhaps you are running on a Java version "
            + "older than Java 17?");
  }

  @DisplayName(".maxSupportedVersion() throws IllegalStateException if reflection fails")
  @Test
  void maxSupportedVersionThrowsIllegalStateExceptionIfReflectionFails() throws Exception {
    // Given
    var provider = new EcjCompilersProvider(java17ClassLoader());

    // Then
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(provider::maxSupportedVersion)
        .withMessage(
            "Failed performing operation \"get latest supported language version for ECJ\". "
                + "This is a bug.")
        .havingCause()
        .isInstanceOf(InvocationTargetException.class);
  }

  ClassLoader java11ClassLoader() throws Exception {
    var classLoader = mock(ClassLoader.class);
    when(classLoader.loadClass(any()))
        .thenThrow(ClassNotFoundException.class);
    return classLoader;
  }

  ClassLoader java17ClassLoader() throws Exception {
    var classLoader = mock(ClassLoader.class);
    when(classLoader.loadClass(any()))
        .thenAnswer(ctx -> EcjJctCompilerProviderStub.class);
    return classLoader;
  }

  static class EcjJctCompilerProviderStub {
    public EcjJctCompilerProviderStub() {
      throw new RuntimeException("bang");
    }

    public static int getEarliestSupportedVersionInt() {
      throw new RuntimeException("bang");
    }

    public static int getLatestSupportedVersionInt() {
      throw new RuntimeException("bang");
    }
  }
}
