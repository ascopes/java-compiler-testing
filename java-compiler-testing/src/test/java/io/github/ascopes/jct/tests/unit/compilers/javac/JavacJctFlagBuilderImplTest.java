/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.compilers.javac.JavacJctFlagBuilderImpl;
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
  }

  @DisplayName("Setting .annotationProcessorOptions(List<String>) adds the options")
  @Test
  void addsAnnotationProcessorOptions() {
    // Given
    var options = Stream
        .generate(() -> someText())
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

  @DisplayName("Setting .compilerOptions(List<String>) adds the options")
  @Test
  void addsCompilerOptions() {
    // Given
    var options = Stream
        .generate(() -> someText())
        .limit(5)
        .collect(Collectors.toList());

    // When
    flagBuilder.compilerOptions(options);

    // Then
    assertThat(flagBuilder.build())
        .containsSequence(options);
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
  @SuppressWarnings("ConstantConditions")
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
