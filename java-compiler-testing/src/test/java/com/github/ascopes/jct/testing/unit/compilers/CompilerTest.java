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
import java.util.Collection;
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

  @DisplayName("addClassOutputPaths(paths) calls addPaths(CLASS_OUTPUT, paths)")
  @Test
  void addClassOutputPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addClassOutputPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

    // When
    var result = compiler.addClassOutputPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.CLASS_OUTPUT, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addClassOutputPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddClassOutputPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addClassOutputPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addClassOutputPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.CLASS_OUTPUT, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourceOutputPaths(paths) calls addPaths(SOURCE_OUTPUT, paths)")
  @Test
  void addSourceOutputPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSourceOutputPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

    // When
    var result = compiler.addSourceOutputPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.SOURCE_OUTPUT, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addSourceOutputPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddSourceOutputPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addSourceOutputPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSourceOutputPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.SOURCE_OUTPUT, firstPath, secondPath, thirdPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassPaths(paths) calls addPaths(CLASS_PATH, paths)")
  @Test
  void addClassPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addClassPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName("addSourcePaths(paths) calls addPaths(SOURCE_PATH, paths)")
  @Test
  void addSourcePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSourcePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addAnnotationProcessorPaths(paths) calls addPaths(ANNOTATION_PROCESSOR_PATH, paths)"
  )
  @Test
  void addAnnotationProcessorPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addAnnotationProcessorPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName("addAnnotationProcessorModulePaths(paths) calls "
      + "addPaths(ANNOTATION_PROCESSOR_MODULE_PATH, paths)")
  @Test
  void addAnnotationProcessorModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addAnnotationProcessorModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addPlatformClassPaths(paths) calls addPaths(PLATFORM_CLASS_PATH, paths)"
  )
  @Test
  void addPlatformClassPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addPlatformClassPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName("addNativeHeaderOutputPaths(paths) calls addPaths(NATIVE_HEADER_OUTPUT, paths)")
  @Test
  void addNativeHeaderOutputPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addNativeHeaderOutputPaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

    // When
    var result = compiler.addNativeHeaderOutputPaths(paths);

    // Then
    then(compiler).should().addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, paths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addNativeHeaderOutputPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddNativeHeaderOutputPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstPath = stub(Path.class);
    var secondPath = stub(Path.class);
    var thirdPath = stub(Path.class);

    given(compiler.addNativeHeaderOutputPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addPaths(any(), eq(firstPath), eq(secondPath), eq(thirdPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addNativeHeaderOutputPaths(firstPath, secondPath, thirdPath);

    // Then
    then(compiler).should()
        .addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, firstPath, secondPath, thirdPath);
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
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addUpgradeModulePaths(paths) calls addPaths(UPGRADE_MODULE_PATH, paths)"
  )
  @Test
  void addUpgradeModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addUpgradeModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addSystemModulePaths(paths) calls addPaths(SYSTEM_MODULES, paths)"
  )
  @Test
  void addSystemModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSystemModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addModulePaths(paths) calls addPaths(MODULE_PATH, paths)"
  )
  @Test
  void addModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName(
      "addPatchModulePaths(paths) calls addPaths(PATCH_MODULE_PATH, paths)"
  )
  @Test
  void addPatchModulePathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addPatchModulePaths(any())).willCallRealMethod();
    given(compiler.addPaths(any(), any())).will(ctx -> compiler);
    var paths = stubCast(new TypeRef<Collection<Path>>() {});

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

  @DisplayName("addClassOutputRamPaths(paths) calls addPaths(CLASS_OUTPUT, paths)")
  @Test
  void addClassOutputRamPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addClassOutputRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

    // When
    var result = compiler.addClassOutputRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.CLASS_OUTPUT, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addClassOutputRamPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddClassOutputRamPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addClassOutputRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addClassOutputRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.CLASS_OUTPUT, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addSourceOutputRamPaths(paths) calls addPaths(SOURCE_OUTPUT, paths)")
  @Test
  void addSourceOutputRamPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addSourceOutputRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

    // When
    var result = compiler.addSourceOutputRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.SOURCE_OUTPUT, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addSourceOutputRamPaths calls varargs overload for addPaths")
  @Test
  void varargOverloadForAddSourceOutputRamPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addSourceOutputRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addSourceOutputRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should()
        .addRamPaths(StandardLocation.SOURCE_OUTPUT, firstRamPath, secondRamPath, thirdRamPath);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addClassRamPaths(ramPaths) calls addRamPaths(CLASS_PATH, ramPaths)")
  @Test
  void addClassRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addClassRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName("addSourceRamPaths(ramPaths) calls addRamPaths(SOURCE_PATH, ramPaths)")
  @Test
  void addSourceRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addSourceRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName("addAnnotationProcessorRamPaths(ramPaths) calls "
      + "addRamPaths(ANNOTATION_PROCESSOR_PATH, ramPaths)")
  @Test
  void addAnnotationProcessorRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addAnnotationProcessorRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName("addAnnotationProcessorModuleRamPaths(ramPaths) calls "
      + "addRamPaths(ANNOTATION_PROCESSOR_MODULE_PATH, ramPaths)")
  @Test
  void addAnnotationProcessorModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addAnnotationProcessorModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName(
      "addPlatformClassRamPaths(ramPaths) calls addRamPaths(PLATFORM_CLASS_PATH, ramPaths)"
  )
  @Test
  void addPlatformClassRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addPlatformClassRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName("addNativeHeaderOutputRamPaths(paths) calls addPaths(NATIVE_HEADER_OUTPUT, paths)")
  @Test
  void addNativeHeaderOutputRamPathsWithIterableCallsAddPaths() {
    // Given
    given(compiler.addNativeHeaderOutputRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

    // When
    var result = compiler.addNativeHeaderOutputRamPaths(ramPaths);

    // Then
    then(compiler).should().addRamPaths(StandardLocation.NATIVE_HEADER_OUTPUT, ramPaths);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("vararg overload for addNativeHeaderOutputRamPaths calls "
      + "varargs overload for addPaths")
  @Test
  void varargOverloadForAddNativeHeaderOutputRamPathsCallsVarargsOverloadsAddPaths() {
    // Given
    var firstRamPath = stub(RamPath.class);
    var secondRamPath = stub(RamPath.class);
    var thirdRamPath = stub(RamPath.class);

    given(compiler.addNativeHeaderOutputRamPaths(any(), any(), any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), eq(firstRamPath), eq(secondRamPath), eq(thirdRamPath)))
        .will(ctx -> compiler);

    // When
    var result = compiler.addNativeHeaderOutputRamPaths(firstRamPath, secondRamPath, thirdRamPath);

    // Then
    then(compiler).should().addRamPaths(
        StandardLocation.NATIVE_HEADER_OUTPUT, firstRamPath, secondRamPath, thirdRamPath);
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
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName(
      "addUpgradeModuleRamPaths(ramPaths) calls addRamPaths(UPGRADE_MODULE_PATH, ramPaths)"
  )
  @Test
  void addUpgradeModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addUpgradeModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName(
      "addSystemModuleRamPaths(ramPaths) calls addRamPaths(SYSTEM_MODULES, ramPaths)"
  )
  @Test
  void addSystemModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addSystemModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName(
      "addModuleRamPaths(ramPaths) calls addRamPaths(MODULE_PATH, ramPaths)"
  )
  @Test
  void addModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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

  @DisplayName(
      "addPatchModuleRamPaths(ramPaths) calls addRamPaths(PATCH_MODULE_PATH, ramPaths)"
  )
  @Test
  void addPatchModuleRamPathsWithIterableCallsAddRamPaths() {
    // Given
    given(compiler.addPatchModuleRamPaths(any())).willCallRealMethod();
    given(compiler.addRamPaths(any(), any())).will(ctx -> compiler);
    var ramPaths = stubCast(new TypeRef<Collection<RamPath>>() {});

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
    given(compiler.withReleaseVersion(anyInt())).willCallRealMethod();
    given(compiler.withReleaseVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withReleaseVersion(versionInt);

    // Then
    then(compiler).should().withReleaseVersion(versionString);
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
    given(compiler.withReleaseVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.withReleaseVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withReleaseVersion(versionEnum);

    // Then
    then(compiler).should().withReleaseVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("sourceVersion(int) should call sourceVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.withSourceVersion(anyInt())).willCallRealMethod();
    given(compiler.withSourceVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withSourceVersion(versionInt);

    // Then
    then(compiler).should().withSourceVersion(versionString);
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
    given(compiler.withSourceVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.withSourceVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withSourceVersion(versionEnum);

    // Then
    then(compiler).should().withSourceVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("targetVersion(int) should call targetVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.withTargetVersion(anyInt())).willCallRealMethod();
    given(compiler.withTargetVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withTargetVersion(versionInt);

    // Then
    then(compiler).should().withTargetVersion(versionString);
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
    given(compiler.withTargetVersion(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.withTargetVersion(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.withTargetVersion(versionEnum);

    // Then
    then(compiler).should().withTargetVersion(versionString);
    assertThat(result).isSameAs(compiler);
  }

  static Stream<Arguments> sourceVersions() {
    return Stream
        .of(SourceVersion.values())
        .map(version -> Arguments.of(version, "" + version.ordinal()));
  }
}
