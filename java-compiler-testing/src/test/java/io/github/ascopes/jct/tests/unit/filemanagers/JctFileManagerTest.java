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
package io.github.ascopes.jct.tests.unit.filemanagers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.util.stream.Stream;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManager tests}.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManager tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerTest {

  @Mock
  JctFileManager fileManager;

  @DisplayName(".getClassOutputGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getClassOutputGroupMakesTheExpectedCall(OutputContainerGroup expected) {
    // Given
    when(fileManager.getClassOutputGroup()).thenCallRealMethod();
    when(fileManager.getOutputContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getClassOutputGroup();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourceOutputGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getSourceOutputGroupMakesTheExpectedCall(OutputContainerGroup expected) {
    // Given
    when(fileManager.getSourceOutputGroup()).thenCallRealMethod();
    when(fileManager.getOutputContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getSourceOutputGroup();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getClassPathGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getClassPathGroupMakesTheExpectedCall(PackageContainerGroup expected) {
    // Given
    when(fileManager.getClassPathGroup()).thenCallRealMethod();
    when(fileManager.getPackageContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getClassPathGroup();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.CLASS_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourcePathGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getSourcePathGroupMakesTheExpectedCall(PackageContainerGroup expected) {
    // Given
    when(fileManager.getSourcePathGroup()).thenCallRealMethod();
    when(fileManager.getPackageContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getSourcePathGroup();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.SOURCE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getAnnotationProcessorPathGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getAnnotationProcessorPathGroupMakesTheExpectedCall(PackageContainerGroup expected) {
    // Given
    when(fileManager.getAnnotationProcessorPathGroup()).thenCallRealMethod();
    when(fileManager.getPackageContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getAnnotationProcessorPathGroup();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getAnnotationProcessorModulePathGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getAnnotationProcessorModulePathGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getAnnotationProcessorModulePathGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getAnnotationProcessorModulePathGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getPlatformClassPathGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  @SuppressWarnings("removal")
  void getPlatformClassPathGroupMakesTheExpectedCall(PackageContainerGroup expected) {
    // Given
    when(fileManager.getPlatformClassPathGroup()).thenCallRealMethod();
    when(fileManager.getPackageContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getPlatformClassPathGroup();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.PLATFORM_CLASS_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getNativeHeaderOutputGroup() makes the expected call")
  @MethodSource("outputContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getNativeHeaderOutputGroupMakesTheExpectedCall(OutputContainerGroup expected) {
    // Given
    when(fileManager.getNativeHeaderOutputGroup()).thenCallRealMethod();
    when(fileManager.getOutputContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getNativeHeaderOutputGroup();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getModuleSourcePathGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getModuleSourcePathGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getModuleSourcePathGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getModuleSourcePathGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getUpgradeModulePathGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getUpgradeModulePathGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getUpgradeModulePathGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getUpgradeModulePathGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.UPGRADE_MODULE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSystemModulesGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getSystemModulesGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getSystemModulesGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getSystemModulesGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.SYSTEM_MODULES);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getModulePathGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getModulePathGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getModulePathGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getModulePathGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.MODULE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getPatchModulePathGroup() makes the expected call")
  @MethodSource("moduleContainerGroupResults")
  @ParameterizedTest(name = "when internal getter returns {0}")
  void getPatchModulePathGroupMakesTheExpectedCall(ModuleContainerGroup expected) {
    // Given
    when(fileManager.getPatchModulePathGroup()).thenCallRealMethod();
    when(fileManager.getModuleContainerGroup(any())).thenReturn(expected);

    // When
    var actual = fileManager.getPatchModulePathGroup();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH);
    verifyNoMoreInteractions(fileManager);
    assertThat(actual).isSameAs(expected);
  }


  static Stream<ModuleContainerGroup> moduleContainerGroupResults() {
    return Stream.of(
        mock(ModuleContainerGroup.class, "a module container group"),
        null
    );
  }

  static Stream<OutputContainerGroup> outputContainerGroupResults() {
    return Stream.of(
        mock(OutputContainerGroup.class, "an output container group"),
        null
    );
  }

  static Stream<PackageContainerGroup> packageContainerGroupResults() {
    return Stream.of(
        mock(PackageContainerGroup.class, "a package container group"),
        null
    );
  }
}
