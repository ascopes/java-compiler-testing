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
package io.github.ascopes.jct.compilers;

import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.workspaces.Workspace;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
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
 * {@link JctCompiler} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompiler tests")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class JctCompilerTest {

  @Mock
  JctCompiler compiler;

  @DisplayName("addAnnotationProcessorOptions(String, String...) should call the expected method")
  @Test
  void addAnnotationProcessorOptionsCallsTheExpectedMethod() {
    // Given
    given(compiler.addAnnotationProcessorOptions(any(String.class), any(String.class)))
        .willCallRealMethod();
    given(compiler.addAnnotationProcessorOptions(any(Iterable.class)))
        .will(ctx -> compiler);

    var first = someText();
    var second = someText();

    // When
    var result = compiler.addAnnotationProcessorOptions(first, second);

    // Then
    then(compiler).should().addAnnotationProcessorOptions(Arrays.asList(first, second));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addAnnotationProcessors(Processor, Processor...) should call the expected method")
  @Test
  void addAnnotationProcessorsCallsTheExpectedMethod() {
    // Given
    given(compiler.addAnnotationProcessors(any(Processor.class), any(Processor.class)))
        .willCallRealMethod();
    given(compiler.addAnnotationProcessors(any(Iterable.class)))
        .will(ctx -> compiler);

    var first = mock(Processor.class);
    var second = mock(Processor.class);

    // When
    var result = compiler.addAnnotationProcessors(first, second);

    // Then
    then(compiler).should().addAnnotationProcessors(Arrays.asList(first, second));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("addCompilerOptions(String, String...) should call the expected method")
  @Test
  void addCompilerOptionsCallsTheExpectedMethod() {
    // Given
    given(compiler.addCompilerOptions(any(String.class), any(String.class)))
        .willCallRealMethod();
    given(compiler.addCompilerOptions(any(Iterable.class)))
        .will(ctx -> compiler);

    var first = someText();
    var second = someText();

    // When
    var result = compiler.addCompilerOptions(first, second);

    // Then
    then(compiler).should().addCompilerOptions(Arrays.asList(first, second));
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("release(int) should call release(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void releaseIntCallsReleaseVersionString(int versionInt) {
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

  @DisplayName("release(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void releaseIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.release(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.release(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a release version less than 0");
  }

  @DisplayName("release(SourceVersion) should call release(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void releaseSourceVersionCallsReleaseVersionString(
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

  @DisplayName(".useRuntimeRelease() should call .release(int) with the runtime feature version")
  @Test
  void useRuntimeReleaseShouldSetTheRuntimeFeatureVersion() {
    // Given
    given(compiler.useRuntimeRelease()).willCallRealMethod();
    given(compiler.release(anyInt())).will(ctx -> compiler);

    // When
    var result = compiler.useRuntimeRelease();

    // Then
    then(compiler).should().release(Runtime.version().feature());
    assertThat(result).isSameAs(compiler);
  }

  @DisplayName("source(int) should call source(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void sourceIntCallsReleaseVersionString(int versionInt) {
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

  @DisplayName("source(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void sourceIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.source(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.source(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a source version less than 0");
  }

  @DisplayName("source(SourceVersion) should call source(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void sourceSourceVersionCallsReleaseVersionString(
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

  @DisplayName("target(int) should call target(String)")
  @ValueSource(ints = {11, 12, 13, 14, 15, 16, 17})
  @ParameterizedTest(name = "for version = {0}")
  void targetIntCallsReleaseVersionString(int versionInt) {
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

  @DisplayName("target(int) throws an IllegalArgumentException for negative versions")
  @ValueSource(ints = {-1, -2, -5, -100_000})
  @ParameterizedTest(name = "for version = {0}")
  void targetIntThrowsIllegalArgumentExceptionForNegativeVersions(int versionInt) {
    // Given
    given(compiler.target(anyInt())).willCallRealMethod();

    // Then
    assertThatThrownBy(() -> compiler.target(versionInt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot provide a target version less than 0");
  }

  @DisplayName("target(SourceVersion) should call target(String)")
  @MethodSource("sourceVersions")
  @ParameterizedTest(name = "for version = {0}")
  void targetSourceVersionCallsReleaseVersionString(
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

  @DisplayName(".compile(Workspace, String, String...) calls .compile(Workspace, Collection)")
  @Test
  void compileStringVarargsCallsCompileCollection() {
    // Given
    var workspace = mock(Workspace.class);

    given(compiler.compile(any(), any(), any(), any()))
        .willCallRealMethod();

    var firstClass = "org.example.Foo";
    var secondClass = "org.example.Bar";
    var thirdClass = "com.organisation.FooBar";

    // When
    var result = compiler.compile(workspace, firstClass, secondClass, thirdClass);

    // Then
    then(compiler).should().compile(workspace, List.of(firstClass, secondClass, thirdClass));
  }

  static Stream<Arguments> sourceVersions() {
    return Stream
        .of(SourceVersion.values())
        .map(version -> Arguments.of(version, "" + version.ordinal()));
  }
}
