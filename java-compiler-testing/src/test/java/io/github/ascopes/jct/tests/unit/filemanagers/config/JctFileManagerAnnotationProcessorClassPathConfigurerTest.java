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
package io.github.ascopes.jct.tests.unit.filemanagers.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.config.JctFileManagerAnnotationProcessorClassPathConfigurer;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerAnnotationProcessorClassPathConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerAnnotationProcessorClassPathConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerAnnotationProcessorClassPathConfigurerTest {

  @Mock
  JctCompiler compiler;

  @Mock
  JctFileManager fileManager;

  @InjectMocks
  JctFileManagerAnnotationProcessorClassPathConfigurer configurer;

  @DisplayName(".configure(...) ensures an empty location exists when discovery is enabled")
  @Test
  void configureEnsuresAnEmptyLocationExistsWhenDiscoveryIsEnabled() {
    // Given
    when(compiler.getAnnotationProcessorDiscovery())
        .thenReturn(AnnotationProcessorDiscovery.ENABLED);

    // When
    configurer.configure(fileManager);

    // Then
    verify(fileManager).createEmptyLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    verifyNoMoreInteractions(fileManager);
  }

  @DisplayName(".configure(...) returns the file manager when discovery is enabled")
  @Test
  void configureReturnsTheFileManagerWhenDiscoveryIsEnabled() {
    // Given
    when(compiler.getAnnotationProcessorDiscovery())
        .thenReturn(AnnotationProcessorDiscovery.ENABLED);

    // When
    var actualFileManager = configurer.configure(fileManager);

    // Then
    assertThat(actualFileManager).isSameAs(fileManager);
  }

  @DisplayName(
      ".configure(...) ensures an empty location exists when discovery is enabled with dependencies"
  )
  @Test
  void configureEnsuresAnEmptyLocationExistsWhenDiscoveryIsEnabledWithDependencies() {
    // Given
    var ordered = inOrder(fileManager);

    when(compiler.getAnnotationProcessorDiscovery())
        .thenReturn(AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES);

    // When
    configurer.configure(fileManager);

    // Then
    ordered.verify(fileManager)
        .copyContainers(StandardLocation.CLASS_PATH, StandardLocation.ANNOTATION_PROCESSOR_PATH);
    ordered.verify(fileManager)
        .createEmptyLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    ordered.verifyNoMoreInteractions();
  }

  @DisplayName(
      ".configure(...) returns the file manager when discovery is enabled with dependencies"
  )
  @Test
  void configureReturnsTheFileManagerWhenDiscoveryIsEnabledWithDependencies() {
    // Given
    when(compiler.getAnnotationProcessorDiscovery())
        .thenReturn(AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES);

    // When
    var actualFileManager = configurer.configure(fileManager);

    // Then
    assertThat(actualFileManager).isSameAs(fileManager);
  }

  @DisplayName(".configure() raises an exception if discovery is disabled")
  @Test
  void configureRaisesAnExceptionIfDiscoveryIsDisabled() {
    // Given
    when(compiler.getAnnotationProcessorDiscovery())
        .thenReturn(AnnotationProcessorDiscovery.DISABLED);

    // Then
    assertThatThrownBy(() -> configurer.configure(fileManager))
        .isInstanceOf(IllegalStateException.class);
  }

  @DisplayName(".isEnabled() returns the expected value")
  @CsvSource({
      "ENABLED, true",
      "INCLUDE_DEPENDENCIES, true",
      "DISABLED, false"
  })
  @ParameterizedTest(name = "expect {1} when annotationProcessorDiscovery is {0}")
  void isEnabledReturnsTheExpectedValue(AnnotationProcessorDiscovery discovery, boolean enabled) {
    // Given
    when(compiler.getAnnotationProcessorDiscovery()).thenReturn(discovery);

    // Then
    assertThat(configurer.isEnabled()).isEqualTo(enabled);
  }
}
