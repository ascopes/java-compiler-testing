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
package io.github.ascopes.jct.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.params.support.AnnotationConsumerInitializer.initialize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import io.github.ascopes.jct.compilers.impl.ecj.EcjJctCompilerImpl;
import java.lang.reflect.AnnotatedElement;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link EcjCompilersProvider} tests (Java 17+ cases).
 *
 * <p>These have to be run under Java 17 and newer only otherwise builds will fail in CI for
 * Java 11-16 runners. This is because the ECJ implementation is only compatible with Java 17 and
 * newer.
 */
@DisplayName("EcjCompilersProvider tests (Java 17+ cases)")
class EcjCompilersProviderJava17Test {

  @DisplayName("Provider uses the user-provided compiler version bounds when valid")
  @Test
  void providerUsesTheUserProvidedVersionRangesWhenValid() {
    // Given
    try (var ecjMock = mockStatic(EcjJctCompilerImpl.class)) {
      ecjMock.when(EcjJctCompilerImpl::getEarliestSupportedVersionInt).thenReturn(8);
      ecjMock.when(EcjJctCompilerImpl::getLatestSupportedVersionInt).thenReturn(17);
      var annotation = someAnnotation(10, 15);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new EcjCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (EcjJctCompilerImpl) args.get()[0])
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
              .isEqualTo("ECJ (release = Java %d)", 10 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 10 + i);
        }
      });
    }
  }

  @DisplayName("Provider uses the minimum compiler version that is allowed if exceeded")
  @Test
  void providerUsesTheMinCompilerVersionAllowedIfExceeded() {
    // Given
    try (var ecjMock = mockStatic(EcjJctCompilerImpl.class)) {
      ecjMock.when(EcjJctCompilerImpl::getEarliestSupportedVersionInt).thenReturn(8);
      ecjMock.when(EcjJctCompilerImpl::getLatestSupportedVersionInt).thenReturn(17);
      var annotation = someAnnotation(1, 15);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new EcjCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (EcjJctCompilerImpl) args.get()[0])
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
              .isEqualTo("ECJ (release = Java %d)", 8 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 8 + i);
        }
      });
    }
  }

  @DisplayName("Provider uses the maximum compiler version that is allowed if exceeded")
  @Test
  void providerUsesTheMaxCompilerVersionAllowedIfExceeded() {
    // Given
    try (var ecjMock = mockStatic(EcjJctCompilerImpl.class)) {
      ecjMock.when(EcjJctCompilerImpl::getEarliestSupportedVersionInt).thenReturn(8);
      ecjMock.when(EcjJctCompilerImpl::getLatestSupportedVersionInt).thenReturn(17);
      var annotation = someAnnotation(10, 17);
      var test = someAnnotatedElement(annotation);
      var context = mock(ExtensionContext.class);

      // When
      var consumer = initialize(test, new EcjCompilersProvider());
      var compilers = consumer.provideArguments(context)
          .map(args -> (EcjJctCompilerImpl) args.get()[0])
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
              .isEqualTo("ECJ (release = Java %d)", 10 + i);
          softly.assertThat(compiler.getRelease())
              .as("compilers[%d].getRelease()", i)
              .isEqualTo("%d", 10 + i);
        }
      });
    }
  }

  @SafeVarargs
  final EcjCompilerTest someAnnotation(
      int min,
      int max,
      Class<? extends JctCompilerConfigurer<?>>... configurers
  ) {
    var annotation = mock(EcjCompilerTest.class);
    when(annotation.minVersion()).thenReturn(min);
    when(annotation.maxVersion()).thenReturn(max);
    when(annotation.configurers()).thenReturn(configurers);
    when(annotation.versionStrategy()).thenReturn(VersionStrategy.RELEASE);
    when(annotation.annotationType()).thenAnswer(ctx -> EcjCompilerTest.class);
    return annotation;
  }

  AnnotatedElement someAnnotatedElement(EcjCompilerTest annotation) {
    var element = mock(AnnotatedElement.class);
    when(element.getDeclaredAnnotation(EcjCompilerTest.class)).thenReturn(annotation);
    return element;
  }
}
