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

package io.github.ascopes.jct.testing.unit.compilers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;

import io.github.ascopes.jct.compilers.SimpleCompilation;
import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.paths.PathLocationRepository;
import io.github.ascopes.jct.testing.helpers.MoreMocks;
import io.github.ascopes.jct.testing.helpers.TypeRef;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link SimpleCompilation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("SimpleCompilation tests")
class SimpleCompilationTest {

  static Random RANDOM = new Random();

  @DisplayName("isSuccessful returns expected value")
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

  @DisplayName("isFailOnWarnings returns expected value")
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

  @DisplayName("getOutputLines returns expected value")
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

  @DisplayName("getCompilationUnits returns expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for compilationUnitCount = {0}")
  void getCompilationUnitsReturnsExpectedValue(int compilationUnitCount) {
    // Given
    var compilationUnits = Stream
        .generate(() -> MoreMocks.stub(JavaFileObject.class))
        .limit(compilationUnitCount)
        .collect(Collectors.toSet());

    var compilation = filledBuilder()
        .compilationUnits(compilationUnits)
        .build();

    // Then
    assertThat(compilation.getCompilationUnits())
        .asInstanceOf(iterable(JavaFileObject.class))
        .containsExactlyElementsOf(compilationUnits);
  }

  @DisplayName("getDiagnostics returns expected value")
  @ValueSource(ints = {0, 1, 2, 3, 5, 10, 100})
  @ParameterizedTest(name = "for diagnosticCount = {0}")
  void getDiagnosticsReturnsExpectedValue(int diagnosticCount) {
    // Given
    var diagnosticType = new TypeRef<TraceDiagnostic<JavaFileObject>>() {};
    var diagnostics = Stream
        .generate(() -> MoreMocks.stubCast(diagnosticType))
        .limit(diagnosticCount)
        .collect(Collectors.toList());

    var compilation = filledBuilder()
        .diagnostics(diagnostics)
        .build();

    // Then
    assertThat(compilation.getDiagnostics())
        .asInstanceOf(iterable(diagnosticType.getType()))
        .containsExactlyElementsOf(diagnostics);
  }

  @DisplayName("getFileRepository returns expected value")
  @Test
  void getFileRepositoryReturnsExpectedValue() {
    // Given
    var fileRepository = MoreMocks.stub(PathLocationRepository.class);
    var compilation = filledBuilder()
        .pathLocationRepository(fileRepository)
        .build();

    // Then
    Assertions.assertThat(compilation.getFileManager()).isEqualTo(fileRepository);
  }


  @DisplayName("SimpleCompilation.Builder tests")
  @Nested
  class BuilderTest {

    @DisplayName("Building without success set raises a NullPointerException")
    @Test
    void buildingWithoutSuccessSetRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .failOnWarnings(RANDOM.nextBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("success");
    }

    @DisplayName("Building without failOnWarnings set raises a NullPointerException")
    @Test
    void buildingWithoutFailOnWarningsSetRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .success(RANDOM.nextBoolean());

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
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("compilationUnits");
    }

    @DisplayName("Building with null compilation units raises a NullPointerException")
    @Test
    void buildingWithNullCompilationUnitsRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean())
          .compilationUnits(nullableSetOf(
              MoreMocks.stub(JavaFileObject.class),
              MoreMocks.stub(JavaFileObject.class),
              MoreMocks.stub(JavaFileObject.class),
              null,
              MoreMocks.stub(JavaFileObject.class)
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
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnostics");
    }

    @DisplayName("Building with null diagnostics raises a NullPointerException")
    @Test
    void buildingWithNullDiagnosticsRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .compilationUnits(Set.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean())
          .diagnostics(nullableListOf(
              MoreMocks.stubCast(new TypeRef<>() {}),
              null,
              MoreMocks.stubCast(new TypeRef<>() {})
          ));

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("diagnostics[1]");
    }

    @DisplayName("Setting null file repositories raises a NullPointerException")
    @Test
    void settingNullFileRepositoriesRaisesNullPointerException() {
      // Given
      var builder = filledBuilder();

      // Then
      assertThatThrownBy(() -> builder.pathLocationRepository(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("pathLocationRepository");
    }

    @DisplayName("Building without a file repository raises a NullPointerException")
    @Test
    void buildingWithoutFileRepositoryRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .outputLines(List.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("pathLocationRepository");
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
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .diagnostics(List.of())
          .compilationUnits(Set.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean());

      // Then
      assertThatThrownBy(builder::build)
          .isInstanceOf(NullPointerException.class)
          .hasMessage("outputLines");
    }

    @DisplayName("Building with null output lines raises a NullPointerException")
    @Test
    void buildingWithNullOutputLinesRaisesNullPointerException() {
      // Given
      var builder = SimpleCompilation
          .builder()
          .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
          .outputLines(List.of())
          .diagnostics(List.of())
          .success(RANDOM.nextBoolean())
          .failOnWarnings(RANDOM.nextBoolean())
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

  static SimpleCompilation.Builder filledBuilder() {
    return SimpleCompilation
        .builder()
        .compilationUnits(Set.of())
        .diagnostics(List.of())
        .failOnWarnings(RANDOM.nextBoolean())
        .pathLocationRepository(MoreMocks.stub(PathLocationRepository.class))
        .outputLines(List.of())
        .success(RANDOM.nextBoolean());
  }
}
