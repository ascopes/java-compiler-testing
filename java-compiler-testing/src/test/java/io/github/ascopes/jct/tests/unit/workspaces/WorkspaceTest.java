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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.ascopes.jct.workspaces.Workspace;
import java.nio.file.Path;
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
class WorkspaceTest {

  @Mock
  Workspace workspace;

  @DisplayName("addClassOutputPackage(Path) calls addPackage(CLASS_OUTPUT, Path)")
  @Test
  void addClassOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addClassOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addClassOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.CLASS_OUTPUT, path);
  }

  @DisplayName("addClassOutputModule(String, Path) calls addModule(CLASS_OUTPUT, String, Path)")
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
  }

  @DisplayName("addSourceOutputPackage(Path) calls addPackage(SOURCE_OUTPUT, Path)")
  @Test
  void addSourceOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addSourceOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addSourceOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.SOURCE_OUTPUT, path);
  }

  @DisplayName("addSourceOutputModule(String, Path) calls addModule(SOURCE_OUTPUT, String, Path)")
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
  }

  @DisplayName("addClassPathPackage(Path) calls addPackage(CLASS_PATH, Path)")
  @Test
  void addClassPathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addClassPathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addClassPathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.CLASS_PATH, path);
  }

  @DisplayName("addModulePathModule(String, Path) calls addModule(MODULE_PATH, String, Path)")
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
  }

  @DisplayName("addSourcePathPackage(Path) calls addPackage(SOURCE_PATH, Path)")
  @Test
  void addSourcePathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addSourcePathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addSourcePathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.SOURCE_PATH, path);
  }

  @DisplayName(
      "addSourcePathModule(String, Path) calls addModule(MODULE_SOURCE_PATH, String, Path)"
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
  }

  @DisplayName(
      "addAnnotationProcessorPathPackage(Path) calls addPackage(ANNOTATION_PROCESSOR_PATH, Path)"
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
  }

  @DisplayName(
      "addAnnotationProcessorPathModule(String, Path) calls "
          + "addModule(ANNOTATION_PROCESSOR_MODULE_PATH, String, Path)"
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
  }

  @DisplayName("addPlatformClassPathPackage(Path) calls addPackage(PLATFORM_CLASS_PATH, Path)")
  @Test
  void addPlatformClassPathPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addPlatformClassPathPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addPlatformClassPathPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  @DisplayName("addNativeHeaderOutputPackage(Path) calls addPackage(NATIVE_HEADER_OUTPUT, Path)")
  @Test
  void addNativeHeaderOutputPackageCallsAddPackage() {
    // Given
    doCallRealMethod().when(workspace).addNativeHeaderOutputPackage(any());
    var path = mock(Path.class);

    // When
    workspace.addNativeHeaderOutputPackage(path);

    // Then
    verify(workspace).addPackage(StandardLocation.NATIVE_HEADER_OUTPUT, path);
  }

  @DisplayName(
      "addNativeHeaderOutputModule(String, Path) calls addModule(NATIVE_HEADER_OUTPUT, String, "
          + "Path)"
  )
  @Test
  void addNativeHeaderOutputModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addNativeHeaderOutputModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addNativeHeaderOutputModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.NATIVE_HEADER_OUTPUT, module, path);
  }

  @DisplayName(
      "addUpgradeModulePathModule(String, Path) calls addModule(UPGRADE_MODULE_PATH, String, Path)"
  )
  @Test
  void addUpgradeModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addUpgradeModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addUpgradeModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.UPGRADE_MODULE_PATH, module, path);
  }

  @DisplayName(
      "addSystemModulePathModule(String, Path) calls addModule(SYSTEM_MODULES, String, Path)"
  )
  @Test
  void addSystemModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addSystemModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addSystemModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.SYSTEM_MODULES, module, path);
  }

  @DisplayName(
      "addPatchModulePathModule(String, Path) calls addModule(PATCH_MODULE_PATH, String, Path)"
  )
  @Test
  void addPatchModulePathModuleCallsAddModule() {
    // Given
    doCallRealMethod().when(workspace).addPatchModulePathModule(any(), any());
    var module = someText();
    var path = mock(Path.class);

    // When
    workspace.addPatchModulePathModule(module, path);

    // Then
    verify(workspace).addModule(StandardLocation.PATCH_MODULE_PATH, module, path);
  }

  @DisplayName("createClassOutputPackage() calls createPackage(CLASS_OUTPUT)")
  @Test
  void createClassOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createClassOutputPackage();

    // When
    workspace.createClassOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.CLASS_OUTPUT);
  }

  @DisplayName("createClassOutputModule(String) calls createModule(CLASS_OUTPUT, String)")
  @Test
  void createClassOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createClassOutputModule(any());
    var module = someText();

    // When
    workspace.createClassOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.CLASS_OUTPUT, module);
  }

  @DisplayName("createSourceOutputPackage() calls createPackage(SOURCE_OUTPUT,)")
  @Test
  void createSourceOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createSourceOutputPackage();

    // When
    workspace.createSourceOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.SOURCE_OUTPUT);
  }

  @DisplayName("createSourceOutputModule(String, Path) calls createModule(SOURCE_OUTPUT, String)")
  @Test
  void createSourceOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createSourceOutputModule(any());
    var module = someText();

    // When
    workspace.createSourceOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.SOURCE_OUTPUT, module);
  }

  @DisplayName("createClassPathPackage(Path) calls createPackage(CLASS_PATH)")
  @Test
  void createClassPathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createClassPathPackage();

    // When
    workspace.createClassPathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.CLASS_PATH);
  }

  @DisplayName("createModulePathModule(String, Path) calls createModule(MODULE_PATH, String)")
  @Test
  void createModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createModulePathModule(any());
    var module = someText();

    // When
    workspace.createModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.MODULE_PATH, module);
  }

  @DisplayName("createSourcePathPackage() calls createPackage(SOURCE_PATH)")
  @Test
  void createSourcePathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createSourcePathPackage();

    // When
    workspace.createSourcePathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.SOURCE_PATH);
  }

  @DisplayName(
      "createSourcePathModule(String) calls createModule(MODULE_SOURCE_PATH, String)"
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
  }

  @DisplayName(
      "createAnnotationProcessorPathPackage() calls createPackage(ANNOTATION_PROCESSOR_PATH)"
  )
  @Test
  void createAnnotationProcessorPathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createAnnotationProcessorPathPackage();

    // When
    workspace.createAnnotationProcessorPathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  @DisplayName(
      "createAnnotationProcessorPathModule(String) calls "
          + "createModule(ANNOTATION_PROCESSOR_MODULE_PATH, String)"
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
  }

  @DisplayName("createPlatformClassPathPackage() calls createPackage(PLATFORM_CLASS_PATH)")
  @Test
  void createPlatformClassPathPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createPlatformClassPathPackage();

    // When
    workspace.createPlatformClassPathPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.PLATFORM_CLASS_PATH);
  }

  @DisplayName("createNativeHeaderOutputPackage(Path) calls createPackage(NATIVE_HEADER_OUTPUT)")
  @Test
  void createNativeHeaderOutputPackageCallsCreatePackage() {
    // Given
    doCallRealMethod().when(workspace).createNativeHeaderOutputPackage();

    // When
    workspace.createNativeHeaderOutputPackage();

    // Then
    verify(workspace).createPackage(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  @DisplayName(
      "createNativeHeaderOutputModule(String) calls createModule(NATIVE_HEADER_OUTPUT, String)"
  )
  @Test
  void createNativeHeaderOutputModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createNativeHeaderOutputModule(any());
    var module = someText();

    // When
    workspace.createNativeHeaderOutputModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.NATIVE_HEADER_OUTPUT, module);
  }

  @DisplayName(
      "createUpgradeModulePathModule(String) calls createModule(UPGRADE_MODULE_PATH, String)"
  )
  @Test
  void createUpgradeModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createUpgradeModulePathModule(any());
    var module = someText();

    // When
    workspace.createUpgradeModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.UPGRADE_MODULE_PATH, module);
  }

  @DisplayName(
      "createSystemModulePathModule(String) calls createModule(SYSTEM_MODULES, String)"
  )
  @Test
  void createSystemModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createSystemModulePathModule(any());
    var module = someText();

    // When
    workspace.createSystemModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.SYSTEM_MODULES, module);
  }

  @DisplayName(
      "createPatchModulePathModule(String) calls createModule(PATCH_MODULE_PATH, String)"
  )
  @Test
  void createPatchModulePathModuleCallsCreateModule() {
    // Given
    doCallRealMethod().when(workspace).createPatchModulePathModule(any());
    var module = someText();

    // When
    workspace.createPatchModulePathModule(module);

    // Then
    verify(workspace).createModule(StandardLocation.PATCH_MODULE_PATH, module);
  }
}
