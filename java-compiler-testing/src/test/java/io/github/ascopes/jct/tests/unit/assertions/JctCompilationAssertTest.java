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

import static io.github.ascopes.jct.tests.helpers.Fixtures.someFlags;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someTraceDiagnostic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.assertions.JctCompilationAssert;
import io.github.ascopes.jct.assertions.ModuleContainerGroupAssert;
import io.github.ascopes.jct.assertions.OutputContainerGroupAssert;
import io.github.ascopes.jct.assertions.PackageContainerGroupAssert;
import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.repr.TraceDiagnosticListRepresentation;
import java.util.List;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
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

  @DisplayName("JctCompilationAssert.arguments(...) tests")
  @Nested
  class ArgumentsTest {

    @DisplayName(".arguments() fails if the compilation is null")
    @Test
    void argumentsFailsIfCompilationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(assertions::arguments)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".arguments() returns assertions on the arguments")
    @Test
    void argumentsReturnsAssertionsOnArguments() {
      // Given
      var compilation = mock(JctCompilation.class);
      var arguments = someFlags();
      when(compilation.getArguments())
          .thenReturn(arguments);

      // When
      var assertions = new JctCompilationAssert(compilation).arguments();

      // Then
      assertThatCode(() -> assertions.containsExactlyElementsOf(arguments))
          .doesNotThrowAnyException();

      assertThatCode(() -> assertions.containsExactly("foo", "bar", "baz"))
          .isInstanceOf(AssertionError.class);
    }
  }

  @DisplayName("JctCompilationAssert.isSuccessful(...) tests")
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
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
        ".isSuccessful() succeeds if the compilation succeeded (fail on warnings)"
    )
    @Test
    void isSuccessfulSucceedsIfCompilationSucceededFailOnWarnings() {
      // Given
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
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

  @DisplayName("JctCompilationAssert.isSuccessfulWithoutWarnings(...) tests")
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
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

  @DisplayName("JctCompilationAssert.isFailure(...) tests")
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      // Technically we can have error diagnostics without the compilation failing, so include
      // them in this test case just for clarity.
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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
      var noteDiag = someTraceDiagnostic(Kind.NOTE);
      var otherDiag = someTraceDiagnostic(Kind.OTHER);
      var warnDiag1 = someTraceDiagnostic(Kind.WARNING);
      var warnDiag2 = someTraceDiagnostic(Kind.WARNING);
      var mandatoryWarnDiag = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      // Technically we can have error diagnostics without the compilation failing, so include
      // them in this test case just for clarity.
      var errorDiag1 = someTraceDiagnostic(Kind.ERROR);
      var errorDiag2 = someTraceDiagnostic(Kind.ERROR);
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

  @DisplayName("JctCompilationAssert.diagnostics(...) tests")
  @Nested
  class DiagnosticsTest {
    @DisplayName(".diagnostics() fails if the compilation is null")
    @Test
    void diagnosticsFailsIfCompilationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(assertions::diagnostics)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".diagnostics() returns assertions for the diagnostics")
    @Test
    void diagnosticsReturnsDiagnosticAssertions() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(),
          someTraceDiagnostic(),
          someTraceDiagnostic(),
          someTraceDiagnostic()
      );
      var compilation = mock(JctCompilation.class);
      when(compilation.getDiagnostics())
          .thenReturn(diagnostics);

      var assertions = new JctCompilationAssert(compilation).diagnostics();

      // Then
      assertThatCode(() -> assertions.containsExactlyElementsOf(diagnostics))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("JctCompilationAssert.packageGroup(...) tests")
  @Nested
  class PackageGroupTest {
    @DisplayName(".packageGroup(...) fails if the compilation is null")
    @Test
    void packageGroupFailsIfCompilationIsNull() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(false);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.packageGroup(location))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".packageGroup(...) fails if the compilation is null")
    @Test
    void packageGroupFailsIfLocationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.packageGroup(null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName(".packageGroup(...) fails if no container group is found")
    @Test
    void packageGroupFailsIfNoContainerGroupFound() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      when(compilation.getFileManager()).thenReturn(fileManager);

      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(false);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThatThrownBy(() -> assertions.packageGroup(location))
          .isInstanceOf(AssertionError.class)
          .hasMessage("No location named %s exists", location.getName());
    }

    @DisplayName(".packageGroup(...) fails if the location is module oriented")
    @Test
    void packageGroupFailsIfLocationIsModuleOriented() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(true);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.packageGroup(location))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expected location %s to not be module-oriented", location);
    }

    @DisplayName(".packageGroup(...) fails if the location is an output location")
    @Test
    void packageGroupFailsIfLocationIsAnOutputLocation() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(false);
      when(location.isOutputLocation()).thenReturn(true);

      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.packageGroup(location))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expected location %s to not be an output location", location);
    }

    @DisplayName(".packageGroup(...) returns assertions for the package group")
    @Test
    void packageGroupReturnsContainerGroupAssertions() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      var containerGroup = mock(PackageContainerGroup.class);
      when(compilation.getFileManager()).thenReturn(fileManager);
      when(fileManager.getPackageContainerGroup(any())).thenReturn(containerGroup);

      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(false);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(compilation).packageGroup(location);

      // Then
      verify(fileManager).getPackageContainerGroup(location);
      verifyNoMoreInteractions(fileManager);

      assertThat(assertions)
          .isInstanceOf(PackageContainerGroupAssert.class);

      assertThatCode(() -> assertions.isSameAs(containerGroup))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("JctCompilationAssert.moduleGroup(...) tests")
  @Nested
  class ModuleGroupTest {
    @DisplayName(".moduleGroup(...) fails if the compilation is null")
    @Test
    void moduleGroupFailsIfCompilationIsNull() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(true);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.moduleGroup(location))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".moduleGroup(...) fails if the compilation is null")
    @Test
    void moduleGroupFailsIfLocationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.moduleGroup(null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName(".moduleGroup(...) fails if no container group is found")
    @Test
    void moduleGroupFailsIfNoContainerGroupFound() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      when(compilation.getFileManager()).thenReturn(fileManager);

      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(true);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThatThrownBy(() -> assertions.moduleGroup(location))
          .isInstanceOf(AssertionError.class)
          .hasMessage("No location named %s exists", location.getName());
    }

    @DisplayName(".moduleGroup(...) fails if the location is package oriented")
    @Test
    void moduleGroupFailsIfLocationIsModuleOriented() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(false);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.moduleGroup(location))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expected location %s to be module-oriented", location);
    }

    @DisplayName(".moduleGroup(...) fails if the location is an output location")
    @Test
    void moduleGroupFailsIfLocationIsAnOutputLocation() {
      // Given
      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(true);
      when(location.isOutputLocation()).thenReturn(true);

      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.moduleGroup(location))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expected location %s to not be an output location", location);
    }

    @DisplayName(".moduleGroup(...) returns assertions for the module group")
    @Test
    void moduleGroupReturnsContainerGroupAssertions() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      var containerGroup = mock(ModuleContainerGroup.class);
      when(compilation.getFileManager()).thenReturn(fileManager);
      when(fileManager.getModuleContainerGroup(any())).thenReturn(containerGroup);

      var location = someLocation();
      when(location.isModuleOrientedLocation()).thenReturn(true);
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(compilation).moduleGroup(location);

      // Then
      verify(fileManager).getModuleContainerGroup(location);
      verifyNoMoreInteractions(fileManager);

      assertThat(assertions)
          .isInstanceOf(ModuleContainerGroupAssert.class);

      assertThatCode(() -> assertions.isSameAs(containerGroup))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("JctCompilationAssert.outputGroup(...) tests")
  @Nested
  class OutputGroupTest {
    @DisplayName(".outputGroup(...) fails if the compilation is null")
    @Test
    void outputGroupFailsIfCompilationIsNull() {
      // Given
      var location = someLocation();
      when(location.isOutputLocation()).thenReturn(true);

      var assertions = new JctCompilationAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.outputGroup(location))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".outputGroup(...) fails if the compilation is null")
    @Test
    void outputGroupFailsIfLocationIsNull() {
      // Given
      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.outputGroup(null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName(".outputGroup(...) fails if no container group is found")
    @Test
    void outputGroupFailsIfNoContainerGroupFound() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      when(compilation.getFileManager()).thenReturn(fileManager);

      var location = someLocation();
      when(location.isOutputLocation()).thenReturn(true);

      var assertions = new JctCompilationAssert(compilation);

      // Then
      assertThatThrownBy(() -> assertions.outputGroup(location))
          .isInstanceOf(AssertionError.class)
          .hasMessage("No location named %s exists", location.getName());
    }

    @DisplayName(".outputGroup(...) fails if the location is not an output location")
    @Test
    void outputGroupFailsIfLocationIsAnOutputLocation() {
      // Given
      var location = someLocation();
      when(location.isOutputLocation()).thenReturn(false);

      var assertions = new JctCompilationAssert(mock(JctCompilation.class));

      // Then
      assertThatThrownBy(() -> assertions.outputGroup(location))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expected location %s to be an output location", location);
    }

    @DisplayName(".outputGroup(...) returns assertions for the output group")
    @Test
    void outputGroupReturnsContainerGroupAssertions() {
      // Given
      var compilation = mock(JctCompilation.class);
      var fileManager = mock(JctFileManager.class);
      var containerGroup = mock(OutputContainerGroup.class);
      when(compilation.getFileManager()).thenReturn(fileManager);
      when(fileManager.getOutputContainerGroup(any())).thenReturn(containerGroup);

      var location = someLocation();
      when(location.isOutputLocation()).thenReturn(true);

      var assertions = new JctCompilationAssert(compilation).outputGroup(location);

      // Then
      verify(fileManager).getOutputContainerGroup(location);
      verifyNoMoreInteractions(fileManager);

      assertThat(assertions)
          .isInstanceOf(OutputContainerGroupAssert.class);

      assertThatCode(() -> assertions.isSameAs(containerGroup))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName(".classOutputPackages() performs the expected operations")
  @Test
  void classOutputPackagesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(PackageContainerGroupAssert.class);
    var expectedOutputAssertions = mock(OutputContainerGroupAssert.class);
    when(expectedOutputAssertions.packages()).thenReturn(expectedAssertions);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.classOutputPackages()).thenCallRealMethod();
    when(assertions.outputGroup(any())).thenReturn(expectedOutputAssertions);

    // When
    var actualAssertions = assertions.classOutputPackages();

    // Then
    verify(assertions).classOutputPackages();
    verify(assertions).outputGroup(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(assertions);
    verify(expectedOutputAssertions).packages();
    verifyNoMoreInteractions(expectedOutputAssertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".classOutputModules() performs the expected operations")
  @Test
  void classOutputModulesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(ModuleContainerGroupAssert.class);
    var expectedOutputAssertions = mock(OutputContainerGroupAssert.class);
    when(expectedOutputAssertions.modules()).thenReturn(expectedAssertions);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.classOutputModules()).thenCallRealMethod();
    when(assertions.outputGroup(any())).thenReturn(expectedOutputAssertions);

    // When
    var actualAssertions = assertions.classOutputModules();

    // Then
    verify(assertions).classOutputModules();
    verify(assertions).outputGroup(StandardLocation.CLASS_OUTPUT);
    verifyNoMoreInteractions(assertions);
    verify(expectedOutputAssertions).modules();
    verifyNoMoreInteractions(expectedOutputAssertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".sourceOutputPackages() performs the expected operations")
  @Test
  void sourceOutputPackagesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(PackageContainerGroupAssert.class);
    var expectedOutputAssertions = mock(OutputContainerGroupAssert.class);
    when(expectedOutputAssertions.packages()).thenReturn(expectedAssertions);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.sourceOutputPackages()).thenCallRealMethod();
    when(assertions.outputGroup(any())).thenReturn(expectedOutputAssertions);

    // When
    var actualAssertions = assertions.sourceOutputPackages();
    
    // Then
    verify(assertions).sourceOutputPackages();
    verify(assertions).outputGroup(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(assertions);
    verify(expectedOutputAssertions).packages();
    verifyNoMoreInteractions(expectedOutputAssertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".sourceOutputModules() performs the expected operations")
  @Test
  void sourceOutputModulesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(ModuleContainerGroupAssert.class);
    var expectedOutputAssertions = mock(OutputContainerGroupAssert.class);
    when(expectedOutputAssertions.modules()).thenReturn(expectedAssertions);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.sourceOutputModules()).thenCallRealMethod();
    when(assertions.outputGroup(any())).thenReturn(expectedOutputAssertions);

    // When
    var actualAssertions = assertions.sourceOutputModules();

    // Then
    verify(assertions).sourceOutputModules();
    verify(assertions).outputGroup(StandardLocation.SOURCE_OUTPUT);
    verifyNoMoreInteractions(assertions);
    verify(expectedOutputAssertions).modules();
    verifyNoMoreInteractions(expectedOutputAssertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".classPathPackages() performs the expected operations")
  @Test
  void classPathPackagesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(PackageContainerGroupAssert.class);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.classPathPackages()).thenCallRealMethod();
    when(assertions.packageGroup(any())).thenReturn(expectedAssertions);

    // When
    var actualAssertions = assertions.classPathPackages();

    // Then
    verify(assertions).classPathPackages();
    verify(assertions).packageGroup(StandardLocation.CLASS_PATH);
    verifyNoMoreInteractions(assertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".sourcePathPackages() performs the expected operations")
  @Test
  void sourcePathPackagesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(PackageContainerGroupAssert.class);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.sourcePathPackages()).thenCallRealMethod();
    when(assertions.packageGroup(any())).thenReturn(expectedAssertions);

    // When
    var actualAssertions = assertions.sourcePathPackages();

    // Then
    verify(assertions).sourcePathPackages();
    verify(assertions).packageGroup(StandardLocation.SOURCE_PATH);
    verifyNoMoreInteractions(assertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".moduleSourcePathModules() performs the expected operations")
  @Test
  void moduleSourcePathModulesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(ModuleContainerGroupAssert.class);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.moduleSourcePathModules()).thenCallRealMethod();
    when(assertions.moduleGroup(any())).thenReturn(expectedAssertions);

    // When
    var actualAssertions = assertions.moduleSourcePathModules();

    // Then
    verify(assertions).moduleSourcePathModules();
    verify(assertions).moduleGroup(StandardLocation.MODULE_SOURCE_PATH);
    verifyNoMoreInteractions(assertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }

  @DisplayName(".modulePathModules() performs the expected operations")
  @Test
  void modulePathModulesPerformsTheExpectedOperations() {
    // Given
    var expectedAssertions = mock(ModuleContainerGroupAssert.class);

    var assertions = mock(JctCompilationAssert.class);
    when(assertions.modulePathModules()).thenCallRealMethod();
    when(assertions.moduleGroup(any())).thenReturn(expectedAssertions);

    // When
    var actualAssertions = assertions.modulePathModules();

    // Then
    verify(assertions).modulePathModules();
    verify(assertions).moduleGroup(StandardLocation.MODULE_PATH);
    verifyNoMoreInteractions(assertions);
    assertThat(actualAssertions).isSameAs(expectedAssertions);
  }
}
