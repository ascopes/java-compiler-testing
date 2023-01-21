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
package io.github.ascopes.jct.tests.unit.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.assertions.JctCompilationAssert;
import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.repr.TraceDiagnosticListRepresentation;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import java.util.List;
import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link JctCompilationAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctCompilationAssert tests")
class JctCompilationAssertTest {

  @DisplayName("JctCompilationAssert#isSuccessful tests")
  @Nested
  class IsSuccessfulTest {

    @DisplayName(".isSuccessful() fails if the compilation is null")
    @Test
    void isSuccessfulFailsIfCompilationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(assertions::isSuccessful)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(
        ".isSuccessful() fails with diagnostics if the compilation failed (no fail on warnings)"
    )
    @Test
    void isSuccessfulFailsWithDiagnosticsIfCompilationFailedNoFailOnWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(true);
      when(compilation.isFailOnWarnings()).thenReturn(false);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      var expectedRepr = TraceDiagnosticListRepresentation.getInstance().toStringOf(List.of(
          errorDiag1, errorDiag2
      ));

      assertThatThrownBy(assertions::isSuccessful)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected a successful compilation, but it failed.\n\nDiagnostics:\n%s",
              expectedRepr
          );
    }

    @DisplayName(
        ".isSuccessful() fails with diagnostics if the compilation failed (fail on warnings)"
    )
    @Test
    void isSuccessfulFailsWithDiagnosticsIfCompilationFailedFailOnWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(true);
      when(compilation.isFailOnWarnings()).thenReturn(true);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      var expectedRepr = TraceDiagnosticListRepresentation.getInstance().toStringOf(List.of(
          warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      ));

      assertThatThrownBy(assertions::isSuccessful)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected a successful compilation, but it failed.\n\nDiagnostics:\n%s",
              expectedRepr
          );
    }

    @DisplayName(
        ".isSuccessful() succeeds if the compilation succeeded (no fail on warnings)"
    )
    @Test
    void isSuccessfulSucceedsIfCompilationSucceededNoFailOnWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(false);
      when(compilation.isFailOnWarnings()).thenReturn(false);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThat(assertions.isSuccessful())
          .isSameAs(assertions);
    }

    @DisplayName(
        ".isSuccessful succeeds if the compilation succeeded (fail on warnings)"
    )
    @Test
    void isSuccessfulSucceedsIfCompilationSucceededFailOnWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var diagnostics = List.of(noteDiag, otherDiag);

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(false);
      when(compilation.isFailOnWarnings()).thenReturn(true);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThat(assertions.isSuccessful())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JctCompilationAssert#isSuccessfulWithoutWarnings tests")
  @Nested
  class IsSuccessfulWithoutWarningsTest {

    @DisplayName(".isSuccessfulWithoutWarnings() fails if the compilation is null")
    @Test
    void isSuccessfulFailsIfCompilationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(assertions::isSuccessfulWithoutWarnings)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isSuccessfulWithoutWarnings() fails with diagnostics if the compilation failed")
    @Test
    void isSuccessfulFailsWithDiagnosticsIfCompilationFailed() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(true);
      when(compilation.isFailOnWarnings()).thenReturn(false);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      var expectedRepr = TraceDiagnosticListRepresentation.getInstance().toStringOf(List.of(
          errorDiag1, errorDiag2
      ));

      assertThatThrownBy(assertions::isSuccessfulWithoutWarnings)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected a successful compilation, but it failed.\n\nDiagnostics:\n%s",
              expectedRepr
          );
    }

    @DisplayName(
        ".isSuccessfulWithoutWarnings() fails with diagnostics if the compilation succeeded with "
            + "warnings")
    @Test
    void isSuccessfulFailsWithDiagnosticsIfCompilationSucceededWithWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(false);
      when(compilation.isFailOnWarnings()).thenReturn(false);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      var expectedRepr = TraceDiagnosticListRepresentation.getInstance().toStringOf(List.of(
          warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      ));

      assertThatThrownBy(assertions::isSuccessfulWithoutWarnings)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected no error, mandatory warning, or warning diagnostics.\n\n"
                  + "Diagnostics:\n%s",
              expectedRepr
          );
    }

    @DisplayName(".isSuccessfulWithoutWarnings() succeeds if the compilation succeeded")
    @Test
    void isSuccessfulSucceedsIfCompilationSucceededNoFailOnWarnings() {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var diagnostics = List.of(noteDiag, otherDiag);

      var compilation = mock(JctCompilation.class);
      when(compilation.isFailure()).thenReturn(false);
      when(compilation.isFailOnWarnings()).thenReturn(false);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThat(assertions.isSuccessfulWithoutWarnings())
          .isSameAs(assertions);
    }
  }

  @DisplayName("JctCompilationAssert#isFailure tests")
  @Nested
  class IsFailureTest {

    @DisplayName(".isFailure() fails if the compilation is null")
    @Test
    void isFailureFailsIfCompilationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(assertions::isFailure)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isFailure() fails if the compilation is successful")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for failOnWarnings = {0}")
    void isFailureFailsIfTheCompilationIsSuccessfulNoFailOnWarnings(boolean failOnWarnings) {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      // Technically we can have error diagnostics without the compilation failing, so include
      // them in this test case just for clarity.
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isSuccessful()).thenReturn(true);
      when(compilation.isFailOnWarnings()).thenReturn(failOnWarnings);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      var expectedRepr = TraceDiagnosticListRepresentation.getInstance().toStringOf(List.of(
          warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      ));

      assertThatThrownBy(assertions::isFailure)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected compilation to fail, but it succeeded.\n\nDiagnostics:\n%s",
              expectedRepr
          );
    }

    @DisplayName(".isFailure() succeeds if the compilation is a failure")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for failOnWarnings = {0}")
    void isFailureSucceedsIfFailure(boolean failOnWarnings) {
      // Given
      var noteDiag = Fixtures.someTraceDiagnostic(Kind.NOTE);
      var otherDiag = Fixtures.someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = Fixtures.someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = Fixtures.someTraceDiagnostic(Kind.MANDATORY_WARNING);
      // Technically we can have error diagnostics without the compilation failing, so include
      // them in this test case just for clarity.
      var errorDiag1 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = Fixtures.someTraceDiagnostic(Kind.ERROR);
      var diagnostics = List.of(
          noteDiag, otherDiag, warnDiag1, warnDiag2, mandatoryWarnDiag, errorDiag1, errorDiag2
      );

      var compilation = mock(JctCompilation.class);
      when(compilation.isSuccessful()).thenReturn(false);
      when(compilation.isFailOnWarnings()).thenReturn(failOnWarnings);
      when(compilation.getDiagnostics()).thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThat(assertions.isFailure())
          .isSameAs(assertions);
    }
  }
}
