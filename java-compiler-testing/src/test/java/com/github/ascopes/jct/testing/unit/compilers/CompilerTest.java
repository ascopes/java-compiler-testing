/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.testing.unit.compilers;

import static com.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static com.github.ascopes.jct.testing.helpers.MoreMocks.stubCast;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.github.ascopes.jct.compilers.Compiler;
import com.github.ascopes.jct.paths.RamPath;
import com.github.ascopes.jct.testing.helpers.TypeRef;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link Compiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Compiler tests")
@ExtendWith(MockitoExtension.class)
class CompilerTest {

  @Mock
  Compiler<?, ?> compiler;

  @DisplayName("vararg overload for addPaths calls the correct method")
  @Test
  void varargOverloadForAddPathsCallsCorrectMethod() {
    // Given
    given(compiler.addPaths(any(), any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var location = stub(Location.class);
    var path1 = stub(Path.class);
    var path2 = stub(Path.class);
    var path3 = stub(Path.class);

    // When
    var result = compiler.addPaths(location, path1, path2, path3);

    // Then
    then(compiler).should().addPaths(location, List.of(path1, path2, path3));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassPath(path) calls addPath(CLASS_PATH, path)")
  @Test
  void addClassPathCallsAddPath() {
    // Given
    given(compiler.addClassPath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addClassPath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.CLASS_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassPaths(paths) calls addPaths(CLASS_PATH, paths)")
  @Test
  void addClassPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addClassPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addClassPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.CLASS_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addClassPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddClassPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addClassPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addClassPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should().addPaths(StandardLocation.CLASS_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourcePath(path) calls addPath(SOURCE_PATH, path)")
  @Test
  void addSourcePathCallsAddPath() {
    // Given
    given(compiler.addSourcePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addSourcePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.SOURCE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourcePaths(paths) calls addPaths(SOURCE_PATH, paths)")
  @Test
  void addSourcePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSourcePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addSourcePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.SOURCE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addSourcePaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddSourcePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addSourcePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSourcePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.SOURCE_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorPath(path) calls addPath(ANNOTATION_PROCESSOR_PATH, path)")
  @Test
  void addAnnotationProcessorPathCallsAddPath() {
    // Given
    given(compiler.addAnnotationProcessorPath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addAnnotationProcessorPath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addAnnotationProcessorPaths(paths) calls addPaths(ANNOTATION_PROCESSOR_PATH, paths)"
  )
  @Test
  void addAnnotationProcessorPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addAnnotationProcessorPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addAnnotationProcessorPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addAnnotationProcessorPaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddAnnotationProcessorPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addAnnotationProcessorPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addAnnotationProcessorPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorModulePath(path) calls "
      + "addPath(ANNOTATION_PROCESSOR_MODULE_PATH, path)")
  @Test
  void addAnnotationProcessorModulePathCallsAddPath() {
    // Given
    given(compiler.addAnnotationProcessorModulePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addAnnotationProcessorModulePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorModulePaths(paths) calls "
      + "addPaths(ANNOTATION_PROCESSOR_MODULE_PATH, paths)")
  @Test
  void addAnnotationProcessorModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addAnnotationProcessorModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addAnnotationProcessorModulePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addAnnotationProcessorModulePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddAnnotationProcessorModulePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addAnnotationProcessorModulePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addAnnotationProcessorModulePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, firstPath, secondPath,
            thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addPlatformClassPath(path) calls addPath(PLATFORM_CLASS_PATH, path)")
  @Test
  void addPlatformClassPathCallsAddPath() {
    // Given
    given(compiler.addPlatformClassPath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addPlatformClassPath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.PLATFORM_CLASS_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addPlatformClassPaths(paths) calls addPaths(PLATFORM_CLASS_PATH, paths)"
  )
  @Test
  void addPlatformClassPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addPlatformClassPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addPlatformClassPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.PLATFORM_CLASS_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addPlatformClassPaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddPlatformClassPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addPlatformClassPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addPlatformClassPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.PLATFORM_CLASS_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addModuleSourcePath(path) calls addPath(MODULE_SOURCE_PATH, path)")
  @Test
  void addModuleSourcePathCallsAddPath() {
    // Given
    given(compiler.addModuleSourcePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addModuleSourcePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.MODULE_SOURCE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addModuleSourcePaths(paths) calls addPaths(MODULE_SOURCE_PATH, paths)"
  )
  @Test
  void addModuleSourcePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addModuleSourcePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addModuleSourcePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addModuleSourcePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddModuleSourcePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addModuleSourcePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addModuleSourcePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.MODULE_SOURCE_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addUpgradeModulePath(path) calls addPath(UPGRADE_MODULE_PATH, path)")
  @Test
  void addUpgradeModulePathCallsAddPath() {
    // Given
    given(compiler.addUpgradeModulePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addUpgradeModulePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.UPGRADE_MODULE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addUpgradeModulePaths(paths) calls addPaths(UPGRADE_MODULE_PATH, paths)"
  )
  @Test
  void addUpgradeModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addUpgradeModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addUpgradeModulePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addUpgradeModulePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddUpgradeModulePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addUpgradeModulePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addUpgradeModulePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.UPGRADE_MODULE_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSystemModulePath(path) calls addPath(SYSTEM_MODULES, path)")
  @Test
  void addSystemModulePathCallsAddPath() {
    // Given
    given(compiler.addSystemModulePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addSystemModulePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.SYSTEM_MODULES, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addSystemModulePaths(paths) calls addPaths(SYSTEM_MODULES, paths)"
  )
  @Test
  void addSystemModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSystemModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addSystemModulePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.SYSTEM_MODULES, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addSystemModulePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddSystemModulePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addSystemModulePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSystemModulePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.SYSTEM_MODULES, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addModulePath(path) calls addPath(MODULE_PATH, path)")
  @Test
  void addModulePathCallsAddPath() {
    // Given
    given(compiler.addModulePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addModulePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.MODULE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addModulePaths(paths) calls addPaths(MODULE_PATH, paths)"
  )
  @Test
  void addModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addModulePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.MODULE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addModulePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddModulePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addModulePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addModulePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.MODULE_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addPatchModulePath(path) calls addPath(PATCH_MODULE_PATH, path)")
  @Test
  void addPatchModulePathCallsAddPath() {
    // Given
    given(compiler.addPatchModulePath(any())).willCallRealMethod();
    given(compiler.addPath(any(), any())).will(ctx -> compiler);
    var path = stub(Path.class);

    // When
    var result = compiler.addPatchModulePath(path);

    // Then
    then(compiler).should().addPath(StandardLocation.PATCH_MODULE_PATH, path);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addPatchModulePaths(paths) calls addPaths(PATCH_MODULE_PATH, paths)"
  )
  @Test
  void addPatchModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addPatchModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Iterable<Path>>() {});

    // When
    var result = compiler.addPatchModulePaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.PATCH_MODULE_PATH, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addPatchModulePaths calls varargs overload for addPaths"
  )
  @Test
  void varargOverloadForAddPatchModulePathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addPatchModulePaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addPatchModulePaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.PATCH_MODULE_PATH, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addRamPaths calls the correct method")
  @Test
  void varargOverloadForAddRamPathsCallsCorrectMethod() {
    // Given
    given(compiler.addRamPaths(any(), any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var location = stub(Location.class);
    var ramPath1 = stub(RamPath.class);
    var ramPath2 = stub(RamPath.class);
    var ramPath3 = stub(RamPath.class);

    // When
    var result = compiler.addRamPaths(location, ramPath1, ramPath2, ramPath3);

    // Then
    then(compiler).should().addRamPaths(location, List.of(ramPath1, ramPath2, ramPath3));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassRamPath(ramPath) calls addRamPath(CLASS_PATH, ramPath)")
  @Test
  void addClassRamPathCallsAddRamPath() {
    // Given
    given(compiler.addClassRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addClassRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.CLASS_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassRamPaths(ramPaths) calls addRamPaths(CLASS_PATH, ramPaths)")
  @Test
  void addClassRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addClassRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addClassRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.CLASS_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addClassRamPaths calls varargs overload for addRamPaths")
  @Test
  void varargOverloadForAddClassRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addClassRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addClassRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.CLASS_PATH, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourceRamPath(ramPath) calls addRamPath(SOURCE_PATH, ramPath)")
  @Test
  void addSourceRamPathCallsAddRamPath() {
    // Given
    given(compiler.addSourceRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addSourceRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.SOURCE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourceRamPaths(ramPaths) calls addRamPaths(SOURCE_PATH, ramPaths)")
  @Test
  void addSourceRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addSourceRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addSourceRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.SOURCE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addSourceRamPaths calls varargs overload for addRamPaths")
  @Test
  void varargOverloadForAddSourceRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addSourceRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSourceRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.SOURCE_PATH, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorRamPath(ramPath) calls "
      + "addRamPath(ANNOTATION_PROCESSOR_PATH, ramPath)")
  @Test
  void addAnnotationProcessorRamPathCallsAddRamPath() {
    // Given
    given(compiler.addAnnotationProcessorRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addAnnotationProcessorRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorRamPaths(ramPaths) calls "
      + "addRamPaths(ANNOTATION_PROCESSOR_PATH, ramPaths)")
  @Test
  void addAnnotationProcessorRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addAnnotationProcessorRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addAnnotationProcessorRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addAnnotationProcessorRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddAnnotationProcessorRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addAnnotationProcessorRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addAnnotationProcessorRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, firstRamPath, secondRamPath,
            thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorModuleRamPath(ramPath) calls "
      + "addRamPath(ANNOTATION_PROCESSOR_MODULE_PATH, ramPath)")
  @Test
  void addAnnotationProcessorModuleRamPathCallsAddRamPath() {
    // Given
    given(compiler.addAnnotationProcessorModuleRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addAnnotationProcessorModuleRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessorModuleRamPaths(ramPaths) calls "
      + "addRamPaths(ANNOTATION_PROCESSOR_MODULE_PATH, ramPaths)")
  @Test
  void addAnnotationProcessorModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addAnnotationProcessorModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addAnnotationProcessorModuleRamPaths(ramPaths);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addAnnotationProcessorModuleRamPaths calls varargs overload "
          + "for addRamPaths")
  @Test
  void varargOverloadForAddAnnotationProcessorModuleRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addAnnotationProcessorModuleRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addAnnotationProcessorModuleRamPaths(firstRamPath, secondRamPath,
        thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, firstRamPath, secondRamPath,
            thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addPlatformClassRamPath(ramPath) calls addRamPath(PLATFORM_CLASS_PATH, ramPath)")
  @Test
  void addPlatformClassRamPathCallsAddRamPath() {
    // Given
    given(compiler.addPlatformClassRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addPlatformClassRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.PLATFORM_CLASS_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addPlatformClassRamPaths(ramPaths) calls addRamPaths(PLATFORM_CLASS_PATH, ramPaths)"
  )
  @Test
  void addPlatformClassRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addPlatformClassRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addPlatformClassRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addPlatformClassRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddPlatformClassRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addPlatformClassRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addPlatformClassRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, firstRamPath, secondRamPath,
            thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addModuleSourceRamPath(ramPath) calls addRamPath(MODULE_SOURCE_PATH, ramPath)")
  @Test
  void addModuleSourceRamPathCallsAddRamPath() {
    // Given
    given(compiler.addModuleSourceRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addModuleSourceRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.MODULE_SOURCE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addModuleSourceRamPaths(ramPaths) calls addRamPaths(MODULE_SOURCE_PATH, ramPaths)"
  )
  @Test
  void addModuleSourceRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addModuleSourceRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addModuleSourceRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.MODULE_SOURCE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addModuleSourceRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddModuleSourceRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addModuleSourceRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addModuleSourceRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.MODULE_SOURCE_PATH, firstRamPath, secondRamPath,
            thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addUpgradeModuleRamPath(ramPath) calls addRamPath(UPGRADE_MODULE_PATH, ramPath)")
  @Test
  void addUpgradeModuleRamPathCallsAddRamPath() {
    // Given
    given(compiler.addUpgradeModuleRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addUpgradeModuleRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.UPGRADE_MODULE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addUpgradeModuleRamPaths(ramPaths) calls addRamPaths(UPGRADE_MODULE_PATH, ramPaths)"
  )
  @Test
  void addUpgradeModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addUpgradeModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addUpgradeModuleRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addUpgradeModuleRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddUpgradeModuleRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addUpgradeModuleRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addUpgradeModuleRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, firstRamPath, secondRamPath,
            thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSystemModuleRamPath(ramPath) calls addRamPath(SYSTEM_MODULES, ramPath)")
  @Test
  void addSystemModuleRamPathCallsAddRamPath() {
    // Given
    given(compiler.addSystemModuleRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addSystemModuleRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.SYSTEM_MODULES, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addSystemModuleRamPaths(ramPaths) calls addRamPaths(SYSTEM_MODULES, ramPaths)"
  )
  @Test
  void addSystemModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addSystemModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addSystemModuleRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.SYSTEM_MODULES, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addSystemModuleRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddSystemModuleRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addSystemModuleRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSystemModuleRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.SYSTEM_MODULES, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addModuleRamPath(ramPath) calls addRamPath(MODULE_PATH, ramPath)")
  @Test
  void addModuleRamPathCallsAddRamPath() {
    // Given
    given(compiler.addModuleRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addModuleRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.MODULE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addModuleRamPaths(ramPaths) calls addRamPaths(MODULE_PATH, ramPaths)"
  )
  @Test
  void addModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addModuleRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.MODULE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addModuleRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddModuleRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addModuleRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addModuleRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.MODULE_PATH, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addPatchModuleRamPath(ramPath) calls addRamPath(PATCH_MODULE_PATH, ramPath)")
  @Test
  void addPatchModuleRamPathCallsAddRamPath() {
    // Given
    given(compiler.addPatchModuleRamPath(any())).willCallRealMethod();
    given(compiler.addRamPath(any(), any())).will(ctx -> compiler);
    var ramPath = stub(RamPath.class);

    // When
    var result = compiler.addPatchModuleRamPath(ramPath);

    // Then
    then(compiler).should().addRamPath(StandardLocation.PATCH_MODULE_PATH, ramPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "addPatchModuleRamPaths(ramPaths) calls addRamPaths(PATCH_MODULE_PATH, ramPaths)"
  )
  @Test
  void addPatchModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addPatchModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Iterable<RamPath>>() {});

    // When
    var result = compiler.addPatchModuleRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.PATCH_MODULE_PATH, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName(
      "vararg overload for addPatchModuleRamPaths calls varargs overload for addRamPaths"
  )
  @Test
  void varargOverloadForAddPatchModuleRamPathsCallsVarargsOverloadsAddRamPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addPatchModuleRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addPatchModuleRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.PATCH_MODULE_PATH, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addAnnotationProcessorOptions calls the correct method")
  @Test
  void varargOverloadForAddAnnotationProcessorOptionsCallsCorrectMethod() {
    // Given
    given(compiler.addAnnotationProcessorOptions(any(), any())).willCallRealMethod();
    given(compiler.addAnnotationProcessorOptions(any())).will(ctx -> compiler);

    // When
    var result = compiler.addAnnotationProcessorOptions("foo", "bar", "baz");

    // Then
    then(compiler).should().addAnnotationProcessorOptions(List.of("foo", "bar", "baz"));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addAnnotationProcessors calls the correct method")
  @Test
  void varargOverloadForAddAnnotationProcessorsCallsCorrectMethod() {
    // Given
    given(compiler.addAnnotationProcessors(any(), any())).willCallRealMethod();
    given(compiler.addAnnotationProcessors(any())).will(ctx -> compiler);

    var firstProc = stub(Processor.class);
    var secondProc = stub(Processor.class);
    var thirdProc = stub(Processor.class);

    // When
    var result = compiler.addAnnotationProcessors(firstProc, secondProc, thirdProc);

    // Then
    then(compiler).should().addAnnotationProcessors(List.of(firstProc, secondProc, thirdProc));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addCompilerOptions calls the correct method")
  @Test
  void varargOverloadForAddCompilerOptionsCallsCorrectMethod() {
    // Given
    given(compiler.addCompilerOptions(any(), any())).willCallRealMethod();
    given(compiler.addCompilerOptions(any())).will(ctx -> compiler);

    // When
    var result = compiler.addCompilerOptions("neko", "neko", "nii");

    // Then
    then(compiler).should().addCompilerOptions(List.of("neko", "neko", "nii"));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addCompilerOptions calls the correct method")
  @Test
  void varargOverloadForAddRuntimeOptionsCallsCorrectMethod() {
    // Given
    given(compiler.addRuntimeOptions(any(), any())).willCallRealMethod();
    given(compiler.addRuntimeOptions(any())).will(ctx -> compiler);

    // When
    var result = compiler.addRuntimeOptions("super", "user", "do");

    // Then
    then(compiler).should().addRuntimeOptions(List.of("super", "user", "do"));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("releaseVersion(int) should call releaseVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void releaseVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.releaseVersion(anyInt())).willCallRealMethod();
    given(compiler.releaseVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.releaseVersion(versionInt);

    // Then
    then(compiler).should().releaseVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("releaseVersion(SourceVersion) should call releaseVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void releaseVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.releaseVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.releaseVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.releaseVersion(versionEnum);

    // Then
    then(compiler).should().releaseVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("sourceVersion(int) should call sourceVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.sourceVersion(anyInt())).willCallRealMethod();
    given(compiler.sourceVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.sourceVersion(versionInt);

    // Then
    then(compiler).should().sourceVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("sourceVersion(SourceVersion) should call sourceVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.sourceVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.sourceVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.sourceVersion(versionEnum);

    // Then
    then(compiler).should().sourceVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("targetVersion(int) should call targetVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.targetVersion(anyInt())).willCallRealMethod();
    given(compiler.targetVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.targetVersion(versionInt);

    // Then
    then(compiler).should().targetVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("targetVersion(SourceVersion) should call targetVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.targetVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.targetVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.targetVersion(versionEnum);

    // Then
    then(compiler).should().targetVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  static Stream<Arguments> sourceVersions() {
    return Stream
        .of(SourceVersion.values())
        .map(version -> Arguments.of(version, "" + version.ordinal()));
  }
}
