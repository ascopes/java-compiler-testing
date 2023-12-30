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
package io.github.ascopes.jct.tests.unit.compilers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.utils.StringUtils;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link JctCompilationImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompilationImpl tests")
class JctCompilationImplTest {

  @DisplayName(".getArguments() returns the expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for argumentCount = {0}")
  void getArgumentsReturnsExpectedValue(int argumentCount) {
    // Given
    var arguments = Stream
        .generate(Fixtures::someFlag)
        .limit(argumentCount)
        .collect(Collectors.toList());

    var compilation = filledBuilder()
        .arguments(arguments)
        .build();

    // Then
    assertThat(compilation.getArguments())
        .asInstanceOf(iterable(String.class))
        .containsExactlyElementsOf(arguments);
  }

  @DisplayName(".isSuccessful() returns the expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for success = {0}")
  void isSuccessfulReturnsExpectedValue(boolean expected) {
    // Given
    var compilation = filledBuilder()
        .success(expected)
        .build();

    // Then
    assertThat(compilation.isSuccessful()).isEqualTo(expected);
  }

  @DisplayName(".isFailOnWarnings() returns the expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for failOnWarnings = {0}")
  void isFailOnWarningsReturnsExpectedValue(boolean expected) {
    // Given
    var compilation = filledBuilder()
        .failOnWarnings(expected)
        .build();

    // Then
    assertThat(compilation.isFailOnWarnings()).isEqualTo(expected);
  }

  @DisplayName(".getOutputLines() returns the expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for lineCount = {0}")
  void getOutputLinesReturnsExpectedValue(int lineCount) {
    // Given
    var lines = Stream
        .generate(UUID::randomUUID)
        .map(Objects::toString)
        .limit(lineCount)
        .collect(Collectors.toList());

    var compilation = filledBuilder()
        .outputLines(lines)
        .build();

    // Then
    assertThat(compilation.getOutputLines())
        .containsExactlyElementsOf(lines);
  }

  @DisplayName(".getCompilationUnits() returns the expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for compilationUnitCount = {0}")
  void getCompilationUnitsReturnsExpectedValue(int compilationUnitCount) {
    // Given
    var compilationUnits = Stream
        .generate(() -> mock(JavaFileObject.class))
        .limit(compilationUnitCount)
        .collect(Collectors.toSet());

    var compilation = filledBuilder()
        .compilationUnits(compilationUnits)
        .build();

    // Then
    assertThat(compilation.getCompilationUnits())
        .asInstanceOf(iterable(JavaFileObject.class))
        .containsExactlyInAnyOrderElementsOf(compilationUnits);
  }

  @DisplayName(".getDiagnostics() returns the expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for diagnosticCount = {0}")
  void getDiagnosticsReturnsExpectedValue(int diagnosticCount) {
    // Given
    var diagnostics = Stream
        .generate(Fixtures::someTraceDiagnostic)
        .limit(diagnosticCount)
        .collect(Collectors.toList());

    var compilation = filledBuilder()
        .diagnostics(diagnostics)
        .build();

    // Then
    assertThat(compilation.getDiagnostics())
        .asInstanceOf(iterable(TraceDiagnostic.class))
        .containsExactlyElementsOf(diagnostics);
  }

  @DisplayName(".getFileManager() returns the expected value")
  @Test
  void getFileManagerReturnsExpectedValue() {
    // Given
    var fileManager = mock(JctFileManager.class);
    var compilation = filledBuilder()
        .fileManager(fileManager)
        .build();

    // Then
    assertThat(compilation.getFileManager()).isEqualTo(fileManager);
  }

  @DisplayName(".toString() returns the expected value")
  @Test
  void toStringReturnsExpectedValue() {
    // Given
    var success = Fixtures.someBoolean();
    var failOnWarnings = Fixtures.someBoolean();
    var fileManager = mock(JctFileManager.class);
    var arguments = Fixtures.someFlags();

    var compilation = filledBuilder()
        .success(success)
        .failOnWarnings(failOnWarnings)
        .fileManager(fileManager)
        .arguments(arguments)
        .build();

    // Then
    assertThat(compilation)
        .asString()
        .as("compilation.toString()")
        .isEqualTo(
            "JctCompilationImpl{success=%s, failOnWarnings=%s, fileManager=%s, arguments=%s}",
            success,
            failOnWarnings,
            fileManager,
            StringUtils.quotedIterable(arguments)
        );
  }

  @DisplayName("CompilationImpl.Builder tests")
  @Nested
  class BuilderTest {

    @DisplayName("Building without arguments raises a NullPointerException")
    @Test
    void buildingWithoutArgumentsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .diagnostics(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("arguments");
    }

    @DisplayName("Building with null arguments raises a NullPointerException")
    @Test
    void buildingWithNullArgumentsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(nullableListOf(Fixtures.someFlag(), null, Fixtures.someFlag()))
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean())
          .diagnostics(List.of());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("arguments[1]");
    }

    @DisplayName("Building without success set raises a NullPointerException")
    @Test
    void buildingWithoutSuccessSetRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("success");
    }

    @DisplayName("Building without failOnWarnings set raises a NullPointerException")
    @Test
    void buildingWithoutFailOnWarningsSetRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("failOnWarnings");
    }

    @DisplayName("Setting null compilation units raises a NullPointerException")
    @Test
    void settingNullCompilationUnitsRaisesNullPointerException() {
      // Given
      var builder = filledBuilder();

      // Then
      assertThatThrownBy(() -> builder.compilationUnits(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("compilationUnits");
    }

    @DisplayName("Building without compilation units raises a NullPointerException")
    @Test
    void buildingWithoutCompilationUnitsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("compilationUnits");
    }

    @DisplayName("Building with null compilation units raises a NullPointerException")
    @Test
    void buildingWithNullCompilationUnitsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean())
          .compilationUnits(nullableSetOf(
              mock(JavaFileObject.class),
              mock(JavaFileObject.class),
              mock(JavaFileObject.class),
              null,
              mock(JavaFileObject.class)
          ));

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("compilationUnits[3]");
    }

    @DisplayName("Setting null diagnostics raises a NullPointerException")
    @Test
    void settingNullDiagnosticsRaisesNullPointerException() {
      // Given
      var builder = filledBuilder();

      // Then
      assertThatThrownBy(() -> builder.diagnostics(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnostics");
    }

    @DisplayName("Building without diagnostics raises a NullPointerException")
    @Test
    void buildingWithoutDiagnosticsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnostics");
    }

    @DisplayName("Building with null diagnostics raises a NullPointerException")
    @Test
    void buildingWithNullDiagnosticsRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean())
          .diagnostics(nullableListOf(
              mock(),
              null,
              mock()
          ));

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnostics[1]");
    }

    @DisplayName("Setting null file managers raises a NullPointerException")
    @Test
    void settingNullFileManagerRaisesNullPointerException() {
      // Given
      var builder = filledBuilder();

      // Then
      assertThatThrownBy(() -> builder.fileManager(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fileManager");
    }

    @DisplayName("Building without a file manager raises a NullPointerException")
    @Test
    void buildingWithoutFileManagerRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .outputLines(List.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fileManager");
    }

    @DisplayName("Setting null output lines raises a NullPointerException")
    @Test
    void settingNullOutputLinesRaisesNullPointerException() {
      // Given
      var builder = filledBuilder();

      // Then
      assertThatThrownBy(() -> builder.outputLines(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("outputLines");
    }

    @DisplayName("Building without output lines raises a NullPointerException")
    @Test
    void buildingWithoutOutputLinesRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("outputLines");
    }

    @DisplayName("Building with null output lines raises a NullPointerException")
    @Test
    void buildingWithNullOutputLinesRaisesNullPointerException() {
      // Given
      var builder = JctCompilationImpl
          .builder()
          .arguments(List.of())
          .fileManager(mock(JctFileManager.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(Fixtures.someBoolean())
          .failOnWarnings(Fixtures.someBoolean())
          .outputLines(nullableListOf("foo", "bar", "baz", "bork", null, "qux"));

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("outputLines[4]");
    }
  }

  @SafeVarargs
  static <T> Set<T> nullableSetOf(T... values) {
    return new LinkedHashSet<>(Arrays.asList(values));
  }

  @SafeVarargs
  static <T> List<T> nullableListOf(T... values) {
    return Arrays.asList(values);
  }

  static JctCompilationImpl.Builder filledBuilder() {
    return JctCompilationImpl
        .builder()
        .arguments(List.of())
        .compilationUnits(Set.of())
        .diagnostics(List.of())
        .failOnWarnings(Fixtures.someBoolean())
        .fileManager(mock(JctFileManager.class))
        .outputLines(List.of())
        .success(Fixtures.someBoolean());
  }
}
