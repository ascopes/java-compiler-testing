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

package com.github.ascopes.jct.unittests.compilations;

import static com.github.ascopes.jct.unittests.helpers.MoreMocks.stub;
import static com.github.ascopes.jct.unittests.helpers.MoreMocks.stubCast;
import static org.assertj.core.api.BDDAssertions.then;

import com.github.ascopes.jct.compilers.impl.CompilationImpl;
import com.github.ascopes.jct.diagnostics.TraceDiagnostic;
import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.unittests.helpers.TypeRef;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link CompilationImpl}.
 *
 * @author Ashley Scopes
 */
@DisplayName("CompilationImpl tests")
class CompilationImplTest {

  @DisplayName("isWarningsAsErrors() is expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void isWarningsAsErrorsIsExpectedValue(boolean warningsAsErrors) {
    // Given
    var compilation = someBuilder()
        .warningsAsErrors(warningsAsErrors)
        .build();

    // Then
    then(compilation.isWarningsAsErrors()).isEqualTo(warningsAsErrors);
  }

  @DisplayName("isSuccessful() is expected value")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest
  void isSuccessfulIsExpectedValue(boolean successful) {
    // Given
    var compilation = someBuilder()
        .success(successful)
        .build();

    // Then
    then(compilation.isSuccessful()).isEqualTo(successful);
  }

  @DisplayName("getOutputLines() returns expected value")
  @Test
  void getOutputLinesReturnsExpectedValue() {
    // Given
    var lines = Stream
        .generate(() -> UUID.randomUUID().toString())
        .limit(10)
        .collect(Collectors.toList());

    var compilation = someBuilder()
        .outputLines(lines)
        .build();

    // Then
    then(compilation.getOutputLines()).isEqualTo(lines);
  }

  @DisplayName("getCompilationUnits() returns expected value")
  @Test
  void getCompilationUnitsReturnsExpectedValue() {
    // Given
    var compilationUnits = Stream
        .generate(() -> stub(JavaFileObject.class))
        .limit(10)
        .collect(Collectors.toSet());

    var compilation = someBuilder()
        .compilationUnits(compilationUnits)
        .build();

    // Then
    then(compilation.getCompilationUnits()).isEqualTo(compilationUnits);
  }

  @DisplayName("getDiagnostics() returns expected value")
  @Test
  void getDiagnosticsReturnsExpectedValue() {
    // Given
    var diagnostics = Stream
        .generate(() -> stubCast(new TypeRef<TraceDiagnostic<? extends JavaFileObject>>() {}))
        .limit(10)
        .collect(Collectors.toList());

    var compilation = someBuilder()
        .diagnostics(diagnostics)
        .build();

    // Then
    then(compilation.getDiagnostics()).isEqualTo(diagnostics);
  }

  @DisplayName("getFileRepository() returns expected value")
  @Test
  void getFileRepositoryReturnsExpectedValue() {
    // Given
    var repo = new PathLocationRepository();

    var compilation = someBuilder()
        .fileRepository(repo)
        .build();

    // Then
    then(compilation.getFileRepository()).isSameAs(repo);
  }

  static CompilationImpl.Builder someBuilder() {
    return CompilationImpl.builder()
        .outputLines(List.of())
        .compilationUnits(Set.of())
        .success(true)
        .warningsAsErrors(false)
        .diagnostics(List.of())
        .fileRepository(new PathLocationRepository());
  }
}
