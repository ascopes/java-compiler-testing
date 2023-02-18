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
package io.github.ascopes.jct.tests.unit.compilers.javac;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someBoolean;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelease;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.compilers.CompilationMode;
import io.github.ascopes.jct.compilers.javac.JavacJctFlagBuilderImpl;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link JavacJctFlagBuilderImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JavacJctFlagBuilderImpl tests")
@TestMethodOrder(OrderAnnotation.class)
class JavacJctFlagBuilderImplTest {

  JavacJctFlagBuilderImpl flagBuilder;

  @BeforeEach
  void setUp() {
    flagBuilder = new JavacJctFlagBuilderImpl();
  }

  @DisplayName(".verbose(boolean) tests")
  @Nested
  class VerboseFlagTest {

    @DisplayName("Setting .verbose(true) adds the '-verbose' flag")
    @Test
    void addsFlagIfTrue() {
      // When
      flagBuilder.verbose(true);

      // Then
      assertThat(flagBuilder.build()).contains("-verbose");
    }

    @DisplayName("Setting .verbose(false) does not add the '-verbose' flag")
    @Test
    void doesNotAddFlagIfFalse() {
      // When
      flagBuilder.verbose(false);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-verbose");
    }

    @DisplayName(".verbose(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.verbose(someBoolean()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".previewFeatures(boolean) tests")
  @Nested
  class PreviewFeaturesFlagTest {

    @DisplayName("Setting .previewFeatures(true) adds the '--enable-preview' flag")
    @Test
    void addsFlagIfTrue() {
      // When
      flagBuilder.previewFeatures(true);

      // Then
      assertThat(flagBuilder.build()).contains("--enable-preview");
    }

    @DisplayName("Setting .previewFeatures(false) does not add the '--enable-preview'  flag")
    @Test
    void doesNotAddFlagIfFalse() {
      // When
      flagBuilder.previewFeatures(false);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("--enable-preview");
    }

    @DisplayName(".previewFeatures(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.previewFeatures(someBoolean()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".showWarnings(boolean) tests")
  @Nested
  class ShowWarningsFlagTest {

    @DisplayName("Setting .showWarnings(true) does not add the '-nowarn' flag")
    @Test
    void doesNotAddFlagIfTrue() {
      // When
      flagBuilder.showWarnings(true);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-nowarn");
    }

    @DisplayName("Setting .showWarnings(false) adds the '-nowarn'  flag")
    @Test
    void addsFlagIfFalse() {
      // When
      flagBuilder.showWarnings(false);

      // Then
      assertThat(flagBuilder.build()).contains("-nowarn");
    }

    @DisplayName(".showWarnings(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.showWarnings(someBoolean()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".failOnWarnings(boolean) tests")
  @Nested
  class FailOnWarningsFlagTest {

    @DisplayName("Setting .failOnWarnings(true) adds the '-Werror' flag")
    @Test
    void addsFlagIfTrue() {
      // When
      flagBuilder.failOnWarnings(true);

      // Then
      assertThat(flagBuilder.build()).contains("-Werror");
    }

    @DisplayName("Setting .failOnWarnings(false) does not add the '-Werror'  flag")
    @Test
    void doesNotAddFlagIfFalse() {
      // When
      flagBuilder.failOnWarnings(false);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-Werror");
    }

    @DisplayName(".failOnWarnings(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.failOnWarnings(someBoolean()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".compilationMode(CompilationMode) tests")
  @Nested
  class CompilationModeFlagTest {

    @DisplayName(".compilationMode(COMPILATION_AND_ANNOTATION_PROCESSING) adds no flags")
    @Test
    void compilationAndAnnotationProcessingAddsNoFlags() {
      // When
      flagBuilder.compilationMode(CompilationMode.COMPILATION_AND_ANNOTATION_PROCESSING);

      // Then
      assertThat(flagBuilder.build()).isEmpty();
    }

    @DisplayName(".compilationMode(COMPILATION_ONLY) adds -proc:none")
    @Test
    void compilationOnlyAddsProcNone() {
      // When
      flagBuilder.compilationMode(CompilationMode.COMPILATION_ONLY);

      // Then
      assertThat(flagBuilder.build()).containsExactly("-proc:none");
    }

    @DisplayName(".compilationMode(ANNOTATION_PROCESSING_ONLY) adds -proc:only")
    @Test
    void annotationProcessingOnlyAddsProcOnly() {
      // When
      flagBuilder.compilationMode(CompilationMode.ANNOTATION_PROCESSING_ONLY);

      // Then
      assertThat(flagBuilder.build()).containsExactly("-proc:only");
    }

    @DisplayName(".compilationMode(...) returns the flag builder")
    @EnumSource(CompilationMode.class)
    @ParameterizedTest(name = "for compilationMode = {0}")
    void returnsFlagBuilder(CompilationMode mode) {
      // Then
      assertThat(flagBuilder.compilationMode(mode))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".showDeprecationWarnings(boolean) tests")
  @Nested
  class ShowDeprecationWarningsFlagTest {

    @DisplayName("Setting .showDeprecationWarnings(true) adds the '-deprecation' flag")
    @Test
    void addsFlagIfTrue() {
      // When
      flagBuilder.showDeprecationWarnings(true);

      // Then
      assertThat(flagBuilder.build()).contains("-deprecation");
    }

    @DisplayName("Setting .showDeprecationWarnings(false) does not add the '-deprecation'  flag")
    @Test
    void doesNotAddFlagIfFalse() {
      // When
      flagBuilder.showDeprecationWarnings(false);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-deprecation");
    }

    @DisplayName(".showDeprecationWarnings(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.showDeprecationWarnings(someBoolean()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".release(String) tests")
  @Nested
  class ReleaseFlagTest {

    @DisplayName("Setting .release(String) adds the '--release <version>' flag")
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "Setting .release(String) adds the '--release {0}' flag")
    void addsFlagIfPresent(String version) {
      // When
      flagBuilder.release(version);

      // Then
      assertThat(flagBuilder.build()).containsSequence("--release", version);
    }

    @DisplayName("Setting .release(null) does not add the '--release' flag")
    @Test
    void doesNotAddFlagIfNotPresent() {
      // When
      flagBuilder.release(null);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("--release");
    }

    @DisplayName(".release(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.release(someRelease()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".source(String) tests")
  @Nested
  class SourceFlagTest {

    @DisplayName("Setting .source(String) adds the '-source <version>' flag")
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "Setting .source(String) adds the '-source {0}' flag")
    void addsFlagIfPresent(String version) {
      // When
      flagBuilder.source(version);

      // Then
      assertThat(flagBuilder.build()).containsSequence("-source", version);
    }

    @DisplayName("Setting .source(null) does not add the '-source' flag")
    @Test
    void doesNotAddFlagIfNotPresent() {
      // When
      flagBuilder.source(null);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-source");
    }


    @DisplayName(".source(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.source(someRelease()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".target(String) tests")
  @Nested
  class TargetFlagTest {

    @DisplayName("Setting .target(String) adds the '-target <version>' flag")
    @ValueSource(strings = {"8", "11", "17"})
    @ParameterizedTest(name = "Setting .target(String) adds the '-target {0}' flag")
    void addsFlagIfPresent(String version) {
      // When
      flagBuilder.target(version);

      // Then
      assertThat(flagBuilder.build()).containsSequence("-target", version);
    }

    @DisplayName("Setting .target(null) does not add the '-target' flag")
    @Test
    void doesNotAddFlagIfNotPresent() {
      // When
      flagBuilder.target(null);

      // Then
      assertThat(flagBuilder.build()).doesNotContain("-target");
    }

    @DisplayName(".target(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Then
      assertThat(flagBuilder.target(someRelease()))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".addAnnotationProcessorOptions(List<String>) tests")
  @Nested
  class AnnotationProcessorOptionsTest {

    @DisplayName("Setting .annotationProcessorOptions(List<String>) adds the options")
    @Test
    void addsAnnotationProcessorOptions() {
      // Given
      var options = Stream
          .generate(Fixtures::someText)
          .limit(5)
          .collect(Collectors.toList());

      // When
      flagBuilder.annotationProcessorOptions(options);

      // Then
      assertThat(flagBuilder.build())
          .containsSequence(options.stream()
              .map("-A"::concat)
              .collect(Collectors.toList()));
    }

    @DisplayName(".annotationProcessorOptions(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Given
      var options = Stream
          .generate(Fixtures::someText)
          .limit(5)
          .collect(Collectors.toList());

      // Then
      assertThat(flagBuilder.annotationProcessorOptions(options))
          .isSameAs(flagBuilder);
    }
  }

  @DisplayName(".compilerOptions(List<String>) tests")
  @Nested
  class CompilerOptionsTest {

    @DisplayName("Setting .compilerOptions(List<String>) adds the options")
    @Test
    void addsCompilerOptions() {
      // Given
      var options = Stream
          .generate(Fixtures::someText)
          .limit(5)
          .collect(Collectors.toList());

      // When
      flagBuilder.compilerOptions(options);

      // Then
      assertThat(flagBuilder.build())
          .containsSequence(options);
    }

    @DisplayName(".compilerOptions(...) returns the flag builder")
    @Test
    void returnsFlagBuilder() {
      // Given
      var options = Stream
          .generate(Fixtures::someText)
          .limit(5)
          .collect(Collectors.toList());

      // Then
      assertThat(flagBuilder.compilerOptions(options))
          .isSameAs(flagBuilder);
    }
  }

  @Order(Integer.MAX_VALUE - 1)
  @DisplayName("The flag builder adds multiple flags correctly")
  @Test
  void addsMultipleFlagsCorrectly() {
    // When
    var flags = flagBuilder
        .compilerOptions(List.of("--foo", "--bar"))
        .release("15")
        .annotationProcessorOptions(List.of("--baz", "--bork"))
        .build();

    // Then
    assertThat(flags)
        .containsExactly("--foo", "--bar", "--release", "15", "-A--baz", "-A--bork");
  }

  @Order(Integer.MAX_VALUE)
  @DisplayName("The flag builder produces an immutable list as the result")
  @Test
  void resultIsImmutable() {
    // When
    var flags = flagBuilder
        .compilerOptions(List.of("--foo", "--bar"))
        .release("15")
        .annotationProcessorOptions(List.of("--baz", "--bork"))
        .build();

    // Then
    assertThatThrownBy(() -> flags.add("something"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
