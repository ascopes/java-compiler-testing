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
package io.github.ascopes.jct.tests.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompilation;
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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctCompilation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompilation tests")
@ExtendWith(MockitoExtension.class)
class JctCompilationTest {

  @Mock
  JctCompilation compilation;

  @DisplayName("isFailure() returns opposite of isSuccessful()")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for isSuccessful() = {0}")
  void isFailureReturnsOppositeOfIsSuccessful(boolean successful) {
    // Given
    when(compilation.isSuccessful()).thenReturn(successful);
    when(compilation.isFailure()).thenCallRealMethod();

    // When
    var failure = compilation.isFailure();

    // Then
    assertThat(failure).isEqualTo(!successful);
    verify(compilation).isSuccessful();
  }

  @DisplayName("getClassOutputs returns the class outputs from the file manager")
  @NullSource
  @MethodSource("outputContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getClassOutputsReturnsClassOutputsFromTheFileManager(OutputContainerGroup containerGroup) {
    // Given
    when(compilation.getClassOutputs()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getOutputContainerGroup(StandardLocation.CLASS_OUTPUT))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getClassOutputs();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.CLASS_OUTPUT);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getSourceOutputs returns the source outputs from the file manager")
  @NullSource
  @MethodSource("outputContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getSourceOutputsReturnsSourceOutputsFromTheFileManager(OutputContainerGroup containerGroup) {
    // Given
    when(compilation.getSourceOutputs()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getSourceOutputs();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getClassPath returns the class path from the file manager")
  @NullSource
  @MethodSource("packageContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getClassPathReturnsClassPathFromTheFileManager(PackageContainerGroup containerGroup) {
    // Given
    when(compilation.getClassPath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getPackageContainerGroup(StandardLocation.CLASS_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getClassPath();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.CLASS_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getSourcePath returns the source path from the file manager")
  @NullSource
  @MethodSource("packageContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getSourcePathReturnsSourcePathFromTheFileManager(PackageContainerGroup containerGroup) {
    // Given
    when(compilation.getSourcePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getPackageContainerGroup(StandardLocation.SOURCE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getSourcePath();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.SOURCE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getAnnotationProcessorPath returns the annotation processor path from the file "
      + "manager")
  @NullSource
  @MethodSource("packageContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getAnnotationProcessorPathReturnsAnnotationProcessorPathFromTheFileManager(
      PackageContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getAnnotationProcessorPath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getAnnotationProcessorPath();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getAnnotationProcessorModulePath returns the annotation processor module path "
      + "from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getAnnotationProcessorModulePathReturnsAnnotationProcessorModulePathFromTheFileManager(
      ModuleContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getAnnotationProcessorModulePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getAnnotationProcessorModulePath();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getPlatformClassPath returns the platform class path from the file manager")
  @NullSource
  @MethodSource("packageContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getPlatformClassPathReturnsPlatformPathFromTheFileManager(
      PackageContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getPlatformClassPath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getPackageContainerGroup(StandardLocation.PLATFORM_CLASS_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getPlatformClassPath();

    // Then
    verify(fileManager).getPackageContainerGroup(StandardLocation.PLATFORM_CLASS_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getNativeHeaderOutputs returns the native header outputs from the file manager")
  @NullSource
  @MethodSource("outputContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getNativeHeaderOutputsReturnsNativeHeaderOutputsFromTheFileManager(
      OutputContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getNativeHeaderOutputs()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getOutputContainerGroup(StandardLocation.NATIVE_HEADER_OUTPUT))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getNativeHeaderOutputs();

    // Then
    verify(fileManager).getOutputContainerGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getModuleSourcePath returns the module source path from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getModuleSourcePathReturnsModuleSourcePathFromTheFileManager(
      ModuleContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getModuleSourcePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getModuleSourcePath();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getUpgradeModulePath returns the upgrade module path from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getUpgradeModulePathReturnsUpgradeModulePathFromTheFileManager(
      ModuleContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getUpgradeModulePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.UPGRADE_MODULE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getUpgradeModulePath();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.UPGRADE_MODULE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getSystemModules returns the system modules from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getSystemModulesReturnsSystemModulesFromTheFileManager(ModuleContainerGroup containerGroup) {
    // Given
    when(compilation.getSystemModules()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.SYSTEM_MODULES))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getSystemModules();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.SYSTEM_MODULES);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getModulePath returns the module path from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getModulePathReturnsModulePathFromTheFileManager(ModuleContainerGroup containerGroup) {
    // Given
    when(compilation.getModulePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.MODULE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getModulePath();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.MODULE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  @DisplayName("getPatchModulePath returns the patch module path from the file manager")
  @NullSource
  @MethodSource("moduleContainerGroup")
  @ParameterizedTest(name = "for {0}")
  void getPatchModulePathReturnsPatchModulePathFromTheFileManager(
      ModuleContainerGroup containerGroup
  ) {
    // Given
    when(compilation.getPatchModulePath()).thenCallRealMethod();

    var fileManager = mock(JctFileManager.class);
    when(fileManager.getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH))
        .thenReturn(containerGroup);

    when(compilation.getFileManager())
        .thenReturn(fileManager);

    // When
    var actualGroup = compilation.getPatchModulePath();

    // Then
    verify(fileManager).getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH);
    assertThat(actualGroup).isSameAs(containerGroup);
  }

  static Stream<PackageContainerGroup> packageContainerGroup() {
    return Stream.of(mock(PackageContainerGroup.class, "existing package container group"));
  }

  static Stream<ModuleContainerGroup> moduleContainerGroup() {
    return Stream.of(mock(ModuleContainerGroup.class, "existing module container group"));
  }

  static Stream<OutputContainerGroup> outputContainerGroup() {
    return Stream.of(mock(OutputContainerGroup.class, "existing output container group"));
  }
}
