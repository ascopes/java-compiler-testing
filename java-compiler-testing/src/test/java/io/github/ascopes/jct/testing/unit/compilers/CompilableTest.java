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
package io.github.ascopes.jct.testing.unit.compilers;

import static io.github.ascopes.jct.testing.helpers.MoreMocks.mockCast;
import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.testing.helpers.TypeRef;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

/**
 * {@link JctCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Compilable tests")
@ExtendWith(MockitoExtension.class)
class CompilableTest {

  @Mock
  JctCompiler<?, ?> compiler;

  @DisplayName("addClassPath(...) tests")
  @TestFactory
  Stream<DynamicTest> addClassPathTests() {
    return addPackagePathTestsFor(
        "addClassPath",
        JctCompiler::addClassPath,
        JctCompiler::addClassPath,
        StandardLocation.CLASS_PATH
    );
  }

  @DisplayName("addModulePath(...) tests")
  @TestFactory
  Stream<DynamicTest> addModulePathTests() {
    return addModulePathTestsFor(
        "addModulePath",
        JctCompiler::addModulePath,
        JctCompiler::addModulePath,
        StandardLocation.MODULE_PATH
    );
  }

  @DisplayName("addSourcePath(...) tests")
  @TestFactory
  Stream<DynamicTest> addSourcePathTests() {
    return addPackagePathTestsFor(
        "addSourcePath",
        JctCompiler::addSourcePath,
        JctCompiler::addSourcePath,
        StandardLocation.SOURCE_PATH
    );
  }

  @DisplayName("addModuleSourcePath(...) tests")
  @TestFactory
  Stream<DynamicTest> addModuleSourcePathTests() {
    return addModulePathTestsFor(
        "addModuleSourcePath",
        JctCompiler::addModuleSourcePath,
        JctCompiler::addModuleSourcePath,
        StandardLocation.MODULE_SOURCE_PATH
    );
  }

  @DisplayName("releaseVersion(int) should call releaseVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void releaseVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.release(anyInt())).willCallRealMethod();
    given(compiler.release(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.release(versionInt);

    // Then
    then(compiler).should().release(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("releaseVersion(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void releaseVersionIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.release(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.release(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a release version less than 0");
  }

  @DisplayName("releaseVersion(SourceVersion) should call releaseVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void releaseVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.release(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.release(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.release(versionEnum);

    // Then
    then(compiler).should().release(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("sourceVersion(int) should call sourceVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.source(anyInt())).willCallRealMethod();
    given(compiler.source(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.source(versionInt);

    // Then
    then(compiler).should().source(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("sourceVersion(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.source(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.source(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a source version less than 0");
  }

  @DisplayName("sourceVersion(SourceVersion) should call sourceVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void sourceVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.source(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.source(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.source(versionEnum);

    // Then
    then(compiler).should().source(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("targetVersion(int) should call targetVersion(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionIntCallsReleaseVersionString(int versionInt) {
    // Given
    var versionString = "" + versionInt;
    given(compiler.target(anyInt())).willCallRealMethod();
    given(compiler.target(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.target(versionInt);

    // Then
    then(compiler).should().target(versionString);
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("targetVersion(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.target(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.target(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a target version less than 0");
  }

  @DisplayName("targetVersion(SourceVersion) should call targetVersion(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void targetVersionSourceVersionCallsReleaseVersionString(
      SourceVersion versionEnum,
      String versionString
  ) {
    // Given
    given(compiler.target(any(SourceVersion.class))).willCallRealMethod();
    given(compiler.target(anyString())).will(ctx -> compiler);

    // When
    var result = compiler.target(versionEnum);

    // Then
    then(compiler).should().target(versionString);
    assertThat(result).isSameAs(compiler);
  }

  static Stream<Arguments> sourceVersions() {
    return Stream
        .of(SourceVersion.values())
        .map(version -> Arguments.of(version, "" + version.ordinal()));
  }

  static Stream<DynamicTest> addPackagePathTestsFor(
      String name,
      AddPackagePathAliasMethod<PathWrapper> pathLikeAdder,
      AddPackagePathAliasMethod<Path> pathAdder,
      Location location
  ) {
    var locName = location.getName();

    var isPackageOriented = dynamicTest(
        "expect location for " + name + " to not be module-oriented",
        () -> {
          // Then
          assertThat(location)
              .withFailMessage("%s is module oriented!", locName)
              .matches(not(Location::isModuleOrientedLocation));
        }
    );

    var isNotOutputLocation = dynamicTest(
        "expect location for " + name + " to not be an output location",
        () -> {
          // Then
          assertThat(location)
              .withFailMessage("%s is an output location!", locName)
              .matches(not(Location::isOutputLocation));
        }
    );

    var pathLikeReturnsCompiler = dynamicTest(
        name + "(PathLike) should return the compiler",
        () -> {
          // Given
          var pathLike = stub(PathWrapper.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(PathWrapper.class))).will(ctx -> compiler);
          // Stub this method to keep results consistent, even though we shouldn't call it.
          // Just keeps the failure test results consistent and meaningful.
          given(compiler.addPath(any(), any(), any(Path.class))).will(ctx -> compiler);
          given(pathLikeAdder.add(compiler, pathLike)).willCallRealMethod();

          // When
          var result = pathLikeAdder.add(compiler, pathLike);

          // Then
          assertThat(result).isSameAs(compiler);
        }
    );

    var pathReturnsCompiler = dynamicTest(
        name + "(Path) should return the compiler",
        () -> {
          // Given
          var path = stub(Path.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(Path.class))).will(ctx -> compiler);
          // Stub this method to keep results consistent, even though we shouldn't call it.
          // Just keeps the failure test results consistent and meaningful.
          given(compiler.addPath(any(), any(), any(Path.class))).will(ctx -> compiler);
          given(pathAdder.add(compiler, path)).willCallRealMethod();

          // When
          var result = pathAdder.add(compiler, path);

          // Then
          assertThat(result).isSameAs(compiler);
        }
    );

    var callsAddPathLike = dynamicTest(
        name + "(PathLike) should delegate to addPath(" + locName + ", PathLike)",
        () -> {
          // Given
          var pathLike = stub(PathWrapper.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(PathWrapper.class))).will(ctx -> compiler);
          given(pathLikeAdder.add(compiler, pathLike)).willCallRealMethod();

          // When
          pathLikeAdder.add(compiler, pathLike);

          // Then
          then(compiler).should().addPath(location, pathLike);
        }
    );

    var callsAddPath = dynamicTest(
        name + "(Path) should delegate to addPath(" + locName + ", Path)",
        () -> {
          // Given
          var path = stub(Path.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(Path.class))).will(ctx -> compiler);
          given(pathAdder.add(compiler, path)).willCallRealMethod();

          // When
          pathAdder.add(compiler, path);

          // Then
          then(compiler).should().addPath(location, path);
        }
    );

    return Stream.of(
        isPackageOriented,
        isNotOutputLocation,
        pathLikeReturnsCompiler,
        pathReturnsCompiler,
        callsAddPathLike,
        callsAddPath
    );
  }

  static Stream<DynamicTest> addModulePathTestsFor(
      String name,
      AddModulePathAliasMethod<PathWrapper> pathLikeAdder,
      AddModulePathAliasMethod<Path> pathAdder,
      Location location
  ) {
    var locName = location.getName();
    var moduleName = "foobar.baz";

    var isModuleOriented = dynamicTest(
        "expect location for " + name + " to be module-oriented",
        () -> {
          // Then
          assertThat(location)
              .withFailMessage("%s is not module-oriented!", locName)
              .matches(Location::isModuleOrientedLocation);
        }
    );

    var isNotOutputLocation = dynamicTest(
        "expect location for " + name + " to not be an output location",
        () -> {
          // Then
          assertThat(location)
              .withFailMessage("%s is an output location!", locName)
              .matches(not(Location::isOutputLocation));
        }
    );

    var pathLikeReturnsCompiler = dynamicTest(
        name + "(String, PathLike) should return the compiler",
        () -> {
          // Given
          var pathLike = stub(PathWrapper.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          // Stub this method to keep results consistent, even though we shouldn't call it.
          // Just keeps the failure test results consistent and meaningful.
          given(compiler.addPath(any(), any(PathWrapper.class))).will(ctx -> compiler);
          given(compiler.addPath(any(), any(), any(PathWrapper.class))).will(ctx -> compiler);
          given(pathLikeAdder.add(compiler, moduleName, pathLike)).willCallRealMethod();

          // When
          var result = pathLikeAdder.add(compiler, moduleName, pathLike);

          // Then
          assertThat(result).isSameAs(compiler);
        }
    );

    var pathReturnsCompiler = dynamicTest(
        name + "(String, Path) should return the compiler",
        () -> {
          // Given
          var path = stub(Path.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          // Stub this method to keep results consistent, even though we shouldn't call it.
          // Just keeps the failure test results consistent and meaningful.
          given(compiler.addPath(any(), any(Path.class))).will(ctx -> compiler);
          given(compiler.addPath(any(), any(), any(Path.class))).will(ctx -> compiler);
          given(pathAdder.add(compiler, moduleName, path)).willCallRealMethod();

          // When
          var result = pathAdder.add(compiler, moduleName, path);

          // Then
          assertThat(result).isSameAs(compiler);
        }
    );

    var callsAddPathLike = dynamicTest(
        name + "(String, PathLike) should delegate to addPath(" + locName + ", String, PathLike)",
        () -> {
          // Given
          var pathLike = stub(PathWrapper.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(PathWrapper.class))).will(ctx -> compiler);
          given(pathLikeAdder.add(compiler, moduleName, pathLike)).willCallRealMethod();

          // When
          pathLikeAdder.add(compiler, moduleName, pathLike);

          // Then
          then(compiler).should().addPath(location, moduleName, pathLike);
        }
    );

    var callsAddPath = dynamicTest(
        name + "(String, Path) should delegate to addPath(" + locName + ", String, Path)",
        () -> {
          // Given
          var path = stub(Path.class);
          JctCompiler<?, ?> compiler = mockCast(
              new TypeRef<>() {},
              withSettings().strictness(Strictness.LENIENT)
          );
          given(compiler.addPath(any(), any(Path.class))).will(ctx -> compiler);
          given(pathAdder.add(compiler, moduleName, path)).willCallRealMethod();

          // When
          pathAdder.add(compiler, moduleName, path);

          // Then
          then(compiler).should().addPath(location, moduleName, path);
        }
    );

    return Stream.of(
        isModuleOriented,
        isNotOutputLocation,
        pathLikeReturnsCompiler,
        pathReturnsCompiler,
        callsAddPathLike,
        callsAddPath
    );
  }

  @FunctionalInterface
  interface AddPackagePathAliasMethod<P> {

    JctCompiler<?, ?> add(JctCompiler<?, ?> compiler, P path);
  }

  @FunctionalInterface
  interface AddModulePathAliasMethod<P> {

    JctCompiler<?, ?> add(JctCompiler<?, ?> compiler, String moduleName, P path);
  }
}
