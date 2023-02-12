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
package io.github.ascopes.jct.tests.unit.junit;

import static io.github.ascopes.jct.compilers.javac.JavacJctCompilerImpl.getEarliestSupportedVersionInt;
import static io.github.ascopes.jct.compilers.javac.JavacJctCompilerImpl.getLatestSupportedVersionInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.support.AnnotationConsumerInitializer.initialize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import io.github.ascopes.jct.compilers.javac.JavacJctCompilerImpl;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.junit.JavacCompilersProvider;
import io.github.ascopes.jct.junit.VersionStrategy;
import java.lang.reflect.AnnotatedElement;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link JavacCompilersProvider} tests.
 */
@DisplayName("JavacCompilersProvider tests")
class JavacCompilersProviderTest {

  @DisplayName("Provider uses the user-provided compiler version bounds when valid")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when modules = {0}")
  void providerUsesTheUserProvidedVersionRangesWhenValid(boolean modules) {
    // Given
    try (var javacMock = mockStatic(JavacJctCompilerImpl.class)) {
      javacMock.when(() -> getEarliestSupportedVersionInt(modules)).thenReturn(8);
      javacMock.when(() -> getLatestSupportedVersionInt(modules)).thenReturn(17);
      var annotation = someAnnotation(10, 15, modules);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new JavacCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (JavacJctCompilerImpl) args.get()[0])
          .collect(Collectors.toList());

      // Then
      assertThat(compilers)
          .as("compilers that were initialised (%s)", compilers)
          .hasSize(6);

      assertSoftly(softly -> {
        for (var i = 0; i < compilers.size(); ++i) {
          var compiler = compilers.get(i);
          softly.assertThat(compiler.getName())
              .as("compilers[%d].getName()", i)
              .isEqualTo("JDK Compiler (release = Java %d)", 10 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 10 + i);
        }
      });
    }
  }

  @DisplayName("Provider uses the minimum compiler version that is allowed if exceeded")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when modules = {0}")
  void providerUsesTheMinCompilerVersionAllowedIfExceeded(boolean modules) {
    // Given
    try (var javacMock = mockStatic(JavacJctCompilerImpl.class)) {
      javacMock.when(() -> getEarliestSupportedVersionInt(modules)).thenReturn(8);
      javacMock.when(() -> getLatestSupportedVersionInt(modules)).thenReturn(17);
      var annotation = someAnnotation(1, 15, modules);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new JavacCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (JavacJctCompilerImpl) args.get()[0])
          .collect(Collectors.toList());

      // Then
      assertThat(compilers)
          .as("compilers that were initialised (%s)", compilers)
          .hasSize(8);

      assertSoftly(softly -> {
        for (var i = 0; i < compilers.size(); ++i) {
          var compiler = compilers.get(i);
          softly.assertThat(compiler.getName())
              .as("compilers[%d].getName()", i)
              .isEqualTo("JDK Compiler (release = Java %d)", 8 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 8 + i);
        }
      });
    }
  }

  @DisplayName("Provider uses the maximum compiler version that is allowed if exceeded")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when modules = {0}")
  void providerUsesTheMaxCompilerVersionAllowedIfExceeded(boolean modules) {
    // Given
    try (var javacMock = mockStatic(JavacJctCompilerImpl.class)) {
      javacMock.when(() -> getEarliestSupportedVersionInt(modules)).thenReturn(8);
      javacMock.when(() -> getLatestSupportedVersionInt(modules)).thenReturn(17);
      var annotation = someAnnotation(10, 17, modules);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new JavacCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (JavacJctCompilerImpl) args.get()[0])
          .collect(Collectors.toList());

      // Then
      assertThat(compilers)
          .as("compilers that were initialised (%s)", compilers)
          .hasSize(8);

      assertSoftly(softly -> {
        for (var i = 0; i < compilers.size(); ++i) {
          var compiler = compilers.get(i);
          softly.assertThat(compiler.getName())
              .as("compilers[%d].getName()", i)
              .isEqualTo("JDK Compiler (release = Java %d)", 10 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 10 + i);
        }
      });
    }
  }

  @SafeVarargs
  @SuppressWarnings("removal")
  final JavacCompilerTest someAnnotation(
      int min,
      int max,
      boolean modules,
      Class<? extends JctCompilerConfigurer<?>>... configurers
  ) {
    var annotation = mock(JavacCompilerTest.class);
    when(annotation.minVersion()).thenReturn(min);
    when(annotation.maxVersion()).thenReturn(max);
    when(annotation.modules()).thenReturn(modules);
    when(annotation.configurers()).thenReturn(configurers);
    when(annotation.versionStrategy()).thenReturn(VersionStrategy.RELEASE);
    when(annotation.annotationType()).thenAnswer(ctx -> JavacCompilerTest.class);
    return annotation;
  }

  AnnotatedElement someAnnotatedElement(JavacCompilerTest annotation) {
    var element = mock(AnnotatedElement.class);
    when(element.getDeclaredAnnotation(JavacCompilerTest.class)).thenReturn(annotation);
    return element;
  }
}
