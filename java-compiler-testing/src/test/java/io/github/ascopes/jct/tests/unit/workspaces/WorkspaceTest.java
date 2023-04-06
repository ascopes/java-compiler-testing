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
}
