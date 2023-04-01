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
package io.github.ascopes.jct.tests.unit.workspaces;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someModuleName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.Workspace;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Workspace tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@DisplayName("Workspace tests")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class WorkspaceTest {

  @Mock
  Workspace workspace;

  @DisplayName(".addClassOutputPackage(Path) calls addPackage(CLASS_OUTPUT, Path)")
  @Test
  void addClassOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addClassOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addClassOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.CLASS_OUTPUT, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addClassOutputModule(String, Path) calls addModule(CLASS_OUTPUT, String, Path)")
  @Test
  void addClassOutputModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addClassOutputModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addClassOutputModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.CLASS_OUTPUT, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addSourceOutputPackage(Path) calls addPackage(SOURCE_OUTPUT, Path)")
  @Test
  void addSourceOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addSourceOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addSourceOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.SOURCE_OUTPUT, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addSourceOutputModule(String, Path) calls addModule(SOURCE_OUTPUT, String, Path)")
  @Test
  void addSourceOutputModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addSourceOutputModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addSourceOutputModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.SOURCE_OUTPUT, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addClassPathPackage(Path) calls addPackage(CLASS_PATH, Path)")
  @Test
  void addClassPathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addClassPathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addClassPathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.CLASS_PATH, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addModulePathModule(String, Path) calls addModule(MODULE_PATH, String, Path)")
  @Test
  void addModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.MODULE_PATH, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addSourcePathPackage(Path) calls addPackage(SOURCE_PATH, Path)")
  @Test
  void addSourcePathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addSourcePathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addSourcePathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.SOURCE_PATH, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addSourcePathModule(String, Path) calls addModule(MODULE_SOURCE_PATH, String, Path)"
  )
  @Test
  void addSourcePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addSourcePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addSourcePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.MODULE_SOURCE_PATH, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addAnnotationProcessorPathPackage(Path) calls addPackage(ANNOTATION_PROCESSOR_PATH, Path)"
  )
  @Test
  void addAnnotationProcessorPathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addAnnotationProcessorPathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addAnnotationProcessorPathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addAnnotationProcessorPathModule(String, Path) calls "
          + ".addModule(ANNOTATION_PROCESSOR_MODULE_PATH, String, Path)"
  )
  @Test
  void addAnnotationProcessorPathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addAnnotationProcessorPathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addAnnotationProcessorPathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".addNativeHeaderOutputPackage(Path) calls addPackage(NATIVE_HEADER_OUTPUT, Path)")
  @Test
  @SuppressWarnings("removal")
  void addNativeHeaderOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addNativeHeaderOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addNativeHeaderOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.NATIVE_HEADER_OUTPUT, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addNativeHeaderOutputModule(String, Path) calls addModule(NATIVE_HEADER_OUTPUT, String, "
          + "Path)"
  )
  @Test
  @SuppressWarnings("removal")
  void addNativeHeaderOutputModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addNativeHeaderOutputModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addNativeHeaderOutputModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.NATIVE_HEADER_OUTPUT, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addUpgradeModulePathModule(String, Path) calls addModule(UPGRADE_MODULE_PATH, String, Path)"
  )
  @Test
  @SuppressWarnings("removal")
  void addUpgradeModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addUpgradeModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addUpgradeModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.UPGRADE_MODULE_PATH, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addSystemModulePathModule(String, Path) calls addModule(SYSTEM_MODULES, String, Path)"
  )
  @Test
  @SuppressWarnings("removal")
  void addSystemModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addSystemModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addSystemModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.SYSTEM_MODULES, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".addPatchModulePathModule(String, Path) calls addModule(PATCH_MODULE_PATH, String, Path)"
  )
  @Test
  @SuppressWarnings("removal")
  void addPatchModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addPatchModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addPatchModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.PATCH_MODULE_PATH, module, path);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createClassOutputPackage() calls createPackage(CLASS_OUTPUT)")
  @Test
  void createClassOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createClassOutputPackage();

    // When
    workspace.createClassOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createClassOutputModule(String) calls createModule(CLASS_OUTPUT, String)")
  @Test
  void createClassOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createClassOutputModule(any());
    var module = someText();

    // When
    workspace.createClassOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.CLASS_OUTPUT, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createSourceOutputPackage() calls createPackage(SOURCE_OUTPUT,)")
  @Test
  void createSourceOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createSourceOutputPackage();

    // When
    workspace.createSourceOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createSourceOutputModule(String, Path) calls createModule(SOURCE_OUTPUT, String)")
  @Test
  void createSourceOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createSourceOutputModule(any());
    var module = someText();

    // When
    workspace.createSourceOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.SOURCE_OUTPUT, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createClassPathPackage(Path) calls createPackage(CLASS_PATH)")
  @Test
  void createClassPathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createClassPathPackage();

    // When
    workspace.createClassPathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.CLASS_PATH);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createModulePathModule(String, Path) calls createModule(MODULE_PATH, String)")
  @Test
  void createModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createModulePathModule(any());
    var module = someText();

    // When
    workspace.createModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.MODULE_PATH, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createSourcePathPackage() calls createPackage(SOURCE_PATH)")
  @Test
  void createSourcePathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createSourcePathPackage();

    // When
    workspace.createSourcePathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.SOURCE_PATH);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createSourcePathModule(String) calls createModule(MODULE_SOURCE_PATH, String)"
  )
  @Test
  void createSourcePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createSourcePathModule(any());
    var module = someText();

    // When
    workspace.createSourcePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.MODULE_SOURCE_PATH, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createAnnotationProcessorPathPackage() calls createPackage(ANNOTATION_PROCESSOR_PATH)"
  )
  @Test
  void createAnnotationProcessorPathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createAnnotationProcessorPathPackage();

    // When
    workspace.createAnnotationProcessorPathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createAnnotationProcessorPathModule(String) calls "
          + ".createModule(ANNOTATION_PROCESSOR_MODULE_PATH, String)"
  )
  @Test
  void createAnnotationProcessorPathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createAnnotationProcessorPathModule(any());
    var module = someText();

    // When
    workspace.createAnnotationProcessorPathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".createNativeHeaderOutputPackage(Path) calls createPackage(NATIVE_HEADER_OUTPUT)")
  @Test
  @SuppressWarnings("removal")
  void createNativeHeaderOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createNativeHeaderOutputPackage();

    // When
    workspace.createNativeHeaderOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.NATIVE_HEADER_OUTPUT);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createNativeHeaderOutputModule(String) calls createModule(NATIVE_HEADER_OUTPUT, String)"
  )
  @Test
  @SuppressWarnings("removal")
  void createNativeHeaderOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createNativeHeaderOutputModule(any());
    var module = someText();

    // When
    workspace.createNativeHeaderOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.NATIVE_HEADER_OUTPUT, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createUpgradeModulePathModule(String) calls createModule(UPGRADE_MODULE_PATH, String)"
  )
  @Test
  @SuppressWarnings("removal")
  void createUpgradeModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createUpgradeModulePathModule(any());
    var module = someText();

    // When
    workspace.createUpgradeModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.UPGRADE_MODULE_PATH, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createSystemModulePathModule(String) calls createModule(SYSTEM_MODULES, String)"
  )
  @Test
  @SuppressWarnings("removal")
  void createSystemModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createSystemModulePathModule(any());
    var module = someText();

    // When
    workspace.createSystemModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.SYSTEM_MODULES, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(
      ".createPatchModulePathModule(String) calls createModule(PATCH_MODULE_PATH, String)"
  )
  @Test
  @SuppressWarnings("removal")
  void createPatchModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createPatchModulePathModule(any());
    var module = someText();

    // When
    workspace.createPatchModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.PATCH_MODULE_PATH, module);
    verifyNoMoreInteractions(workspace);
  }

  @DisplayName(".getClassOutputPackages calls getPackages(CLASS_OUTPUT)")
  @Test
  void getClassOutputPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getClassOutputPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getClassOutputPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getClassOutputModule calls getModule(CLASS_OUTPUT, ...)")
  @Test
  void getClassOutputModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getClassOutputModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getClassOutputModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.CLASS_OUTPUT, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getClassOutputModules calls getModules(CLASS_OUTPUT)")
  @Test
  void getClassOutputModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getClassOutputModules()).thenCallRealMethod();

    // When
    var actual = workspace.getClassOutputModules();

    // Then
    verify(workspace).getModules(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourceOutputPackages calls getPackages(SOURCE_OUTPUT)")
  @Test
  void getSourceOutputPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getSourceOutputPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getSourceOutputPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourceOutputModule calls getModule(SOURCE_OUTPUT, ...)")
  @Test
  void getSourceOutputModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getSourceOutputModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getSourceOutputModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.SOURCE_OUTPUT, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourceOutputModules calls getModules(SOURCE_OUTPUT)")
  @Test
  void getSourceOutputModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getSourceOutputModules()).thenCallRealMethod();

    // When
    var actual = workspace.getSourceOutputModules();

    // Then
    verify(workspace).getModules(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getClassPathPackages calls getPackages(CLASS_PATH)")
  @Test
  void getClassPathPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getClassPathPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getClassPathPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.CLASS_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourcePathPackages calls getPackages(SOURCE_PATH)")
  @Test
  void getSourcePathPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getSourcePathPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getSourcePathPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.SOURCE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getAnnotationProcessorPathPackages calls getPackages(ANNOTATION_PROCESSOR_PATH)")
  @Test
  void getAnnotationProcessorPathPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getAnnotationProcessorPathPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getAnnotationProcessorPathPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(
      ".getAnnotationProcessorPathModule calls getModule(ANNOTATION_PROCESSOR_MODULE_PATH, ...)"
  )
  @Test
  void getAnnotationProcessorPathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getAnnotationProcessorPathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getAnnotationProcessorPathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(
      ".getAnnotationProcessorPathModules calls getModules(ANNOTATION_PROCESSOR_MODULE_PATH)"
  )
  @Test
  void getAnnotationProcessorPathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getAnnotationProcessorPathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getAnnotationProcessorPathModules();

    // Then
    verify(workspace).getModules(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getPlatformClassPathPackages calls getPackages(PLATFORM_CLASS_PATH)")
  @Test
  @SuppressWarnings("removal")
  void getPlatformClassPathPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getPlatformClassPathPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getPlatformClassPathPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.PLATFORM_CLASS_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getNativeHeaderOutputPackages calls getPackages(NATIVE_HEADER_OUTPUT)")
  @Test
  @SuppressWarnings("removal")
  void getNativeHeaderOutputPackagesCallsGetPackages() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getPackages(any())).thenReturn(expected);
    when(workspace.getNativeHeaderOutputPackages()).thenCallRealMethod();

    // When
    var actual = workspace.getNativeHeaderOutputPackages();

    // Then
    verify(workspace).getPackages(StandardLocation.NATIVE_HEADER_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getNativeHeaderOutputModule calls getModule(NATIVE_HEADER_OUTPUT, ...)")
  @Test
  @SuppressWarnings("removal")
  void getNativeHeaderOutputModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getNativeHeaderOutputModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getNativeHeaderOutputModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getNativeHeaderOutputModules calls getModules(NATIVE_HEADER_OUTPUT)")
  @Test
  @SuppressWarnings("removal")
  void getNativeHeaderOutputModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getNativeHeaderOutputModules()).thenCallRealMethod();

    // When
    var actual = workspace.getNativeHeaderOutputModules();

    // Then
    verify(workspace).getModules(StandardLocation.NATIVE_HEADER_OUTPUT);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }


  @DisplayName(".getSourcePathModule calls getModule(MODULE_SOURCE_PATH, ...)")
  @Test
  void getSourcePathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getSourcePathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getSourcePathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.MODULE_SOURCE_PATH, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSourcePathModules calls getModules(MODULE_SOURCE_PATH)")
  @Test
  void getSourcePathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getSourcePathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getSourcePathModules();

    // Then
    verify(workspace).getModules(StandardLocation.MODULE_SOURCE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }


  @DisplayName(".getUpgradeModulePathModule calls getModule(UPGRADE_MODULE_PATH, ...)")
  @Test
  @SuppressWarnings("removal")
  void getUpgradeModulePathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getUpgradeModulePathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getUpgradeModulePathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getUpgradeModulePathModules calls getModules(UPGRADE_MODULE_PATH)")
  @Test
  @SuppressWarnings("removal")
  void getUpgradeModulePathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getUpgradeModulePathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getUpgradeModulePathModules();

    // Then
    verify(workspace).getModules(StandardLocation.UPGRADE_MODULE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSystemModulePathModule calls getModule(SYSTEM_MODULES, ...)")
  @Test
  @SuppressWarnings("removal")
  void getSystemModulePathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getSystemModulePathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getSystemModulePathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.SYSTEM_MODULES, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getSystemModulePathModules calls getModules(SYSTEM_MODULES)")
  @Test
  @SuppressWarnings("removal")
  void getSystemModulePathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getSystemModulePathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getSystemModulePathModules();

    // Then
    verify(workspace).getModules(StandardLocation.SYSTEM_MODULES);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getModulePathModule calls getModule(MODULE_PATH, ...)")
  @Test
  void getModulePathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getModulePathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getModulePathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.MODULE_PATH, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getModulePathModules calls getModules(MODULE_PATH)")
  @Test
  void getModulePathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getModulePathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getModulePathModules();

    // Then
    verify(workspace).getModules(StandardLocation.MODULE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getPatchModulePathModule calls getModule(PATCH_MODULE_PATH, ...)")
  @Test
  @SuppressWarnings("removal")
  void getPatchModulePathModuleCallsGetModule() {
    // Given
    List<PathRoot> expected = mock();
    when((List<PathRoot>) workspace.getModule(any(), any())).thenReturn(expected);
    when(workspace.getPatchModulePathModule(any())).thenCallRealMethod();
    var moduleName = someModuleName();

    // When
    var actual = workspace.getPatchModulePathModule(moduleName);

    // Then
    verify(workspace).getModule(StandardLocation.PATCH_MODULE_PATH, moduleName);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }

  @DisplayName(".getPatchModulePathModules calls getModules(PATCH_MODULE_PATH)")
  @Test
  @SuppressWarnings("removal")
  void getPatchModulePathModulesCallsGetModules() {
    // Given
    Map<String, List<? extends PathRoot>> expected = mock();
    when(workspace.getModules(any())).thenReturn(expected);
    when(workspace.getPatchModulePathModules()).thenCallRealMethod();

    // When
    var actual = workspace.getPatchModulePathModules();

    // Then
    verify(workspace).getModules(StandardLocation.PATCH_MODULE_PATH);
    verifyNoMoreInteractions(workspace);
    assertThat(actual).isSameAs(expected);
  }
}
