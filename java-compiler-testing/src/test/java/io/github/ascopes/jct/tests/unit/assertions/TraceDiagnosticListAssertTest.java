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
package io.github.ascopes.jct.tests.unit.assertions;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someTraceDiagnostic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.assertions.TraceDiagnosticListAssert;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.repr.TraceDiagnosticListRepresentation;
import io.github.ascopes.jct.tests.helpers.ExtraArgumentMatchers;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * {@link TraceDiagnosticListAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticListAssert tests")
class TraceDiagnosticListAssertTest {

  @DisplayName("TraceDiagnosticListAssert.errors(...) tests")
  @Nested
  class ErrorsTest {

    @DisplayName(".errors() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::errors)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".errors() returns assertions on just the errors")
    @Test
    void returnsAssertionsOnJustErrors() {
      // Given
      var error1 = someTraceDiagnostic(Kind.ERROR);
      var error2 = someTraceDiagnostic(Kind.ERROR);
      var error3 = someTraceDiagnostic(Kind.ERROR);

      var list = List.of(
          error1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error2,
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error3
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.errors().containsExactly(error1, error2, error3));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.warnings(...) tests")
  @Nested
  class WarningsTest {

    @DisplayName(".warnings() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::warnings)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".warnings() returns assertions on just the warnings")
    @Test
    void returnsAssertionsOnJustWarnings() {
      // Given
      var warning1 = someTraceDiagnostic(Kind.WARNING);
      var warning2 = someTraceDiagnostic(Kind.WARNING);
      var warning3 = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var warning4 = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var warning5 = someTraceDiagnostic(Kind.WARNING);

      var list = List.of(
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.ERROR),
          warning1,
          warning2,
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.NOTE),
          warning3,
          someTraceDiagnostic(Kind.NOTE),
          warning4,
          someTraceDiagnostic(Kind.ERROR),
          warning5
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.warnings()
              .containsExactly(warning1, warning2, warning3, warning4, warning5));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.customWarnings(...) tests")
  @Nested
  class CustomWarningsTest {

    @DisplayName(".customWarnings() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::warnings)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".customWarnings() returns assertions on just the custom warnings")
    @Test
    void returnsAssertionsOnJustCustomWarnings() {
      // Given
      var warning1 = someTraceDiagnostic(Kind.WARNING);
      var warning2 = someTraceDiagnostic(Kind.WARNING);
      var warning3 = someTraceDiagnostic(Kind.WARNING);

      var list = List.of(
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          warning1,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER),
          warning2,
          warning3,
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.ERROR)
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.customWarnings()
              .containsExactly(warning1, warning2, warning3));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.mandatoryWarnings(...) tests")
  @Nested
  class MandatoryWarningsTest {

    @DisplayName(".mandatoryWarnings() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::warnings)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".mandatoryWarnings() returns assertions on just the mandatory warnings")
    @Test
    void returnsAssertionsOnJustMandatoryWarnings() {
      // Given
      var warning1 = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var warning2 = someTraceDiagnostic(Kind.MANDATORY_WARNING);
      var warning3 = someTraceDiagnostic(Kind.MANDATORY_WARNING);

      var list = List.of(
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.WARNING),
          warning1,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER),
          warning2,
          warning3,
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR)
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.mandatoryWarnings()
              .containsExactly(warning1, warning2, warning3));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.notes(...) tests")
  @Nested
  class NotesTest {

    @DisplayName(".notes() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::notes)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".notes() returns assertions on just the notes")
    @Test
    void returnsAssertionsOnJustNotes() {
      // Given
      var note1 = someTraceDiagnostic(Kind.NOTE);
      var note2 = someTraceDiagnostic(Kind.NOTE);
      var note3 = someTraceDiagnostic(Kind.NOTE);

      var list = List.of(
          note1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          note2,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          note3
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.notes().containsExactly(note1, note2, note3));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.others(...) tests")
  @Nested
  class OthersTest {

    @DisplayName(".others() fails if the list is null")
    @Test
    void failsIfListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatThrownBy(assertions::others)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".others() returns assertions on just the other diagnostics")
    @Test
    void returnsAssertionsOnJustOtherDiagnostics() {
      // Given
      var other1 = someTraceDiagnostic(Kind.OTHER);
      var other2 = someTraceDiagnostic(Kind.OTHER);
      var other3 = someTraceDiagnostic(Kind.OTHER);

      var list = List.of(
          other1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          other2,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          other3
      );

      var assertions = new TraceDiagnosticListAssert(list);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.others().containsExactly(other1, other2, other3));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#filteringByKinds(Kind, Kind...) tests")
  @Nested
  class FilteringByKindsVarargsTest {

    @DisplayName(".filteringByKinds(Kind, Kind...) fails if the first kind is null")
    @Test
    void failsIfFirstKindIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.filteringByKinds(null, new Kind[0]))
          .withMessage("kind");
    }

    @DisplayName(".filteringByKinds(Kind, Kind...) fails if the vararg array is null")
    @Test
    void failsIfVarargArrayIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.filteringByKinds(Kind.NOTE, (Kind[]) null))
          .withMessage("moreKinds");
    }

    @DisplayName(".filteringByKinds(Kind, Kind...) fails if any varargs are null")
    @Test
    void failsIfAnyVarargsAreNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.filteringByKinds(Kind.NOTE, Kind.WARNING, null, Kind.ERROR))
          .withMessage("moreKinds[1]");
    }

    @DisplayName(".filteringByKinds(Kind, Kind...) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.filteringByKinds(Kind.NOTE, Kind.ERROR));
    }

    @DisplayName(".filteringByKinds(Kind, Kind...) filters diagnostics by kind")
    @Test
    void filtersDiagnosticsByKind() {
      // Given
      var note1 = someTraceDiagnostic(Kind.NOTE);
      var note2 = someTraceDiagnostic(Kind.NOTE);
      var error1 = someTraceDiagnostic(Kind.ERROR);
      var error2 = someTraceDiagnostic(Kind.ERROR);
      var diagnostics = Arrays.asList(
          note1,
          error1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.WARNING),
          null,
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error2,
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          note2
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.filteringByKinds(Kind.NOTE, Kind.ERROR)
              .containsExactly(note1, error1, error2, note2));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#filteringByKinds(Iterable<Kind>) tests")
  @Nested
  class FilteringByKindsIterableTest {

    @DisplayName(".filteringByKinds(Iterable<Kind>) fails if the iterable is null")
    @SuppressWarnings("RedundantCast")  // Prevent ambiguity
    @Test
    void failsIfKindIterableIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.filteringByKinds((Iterable<Kind>) null))
          .withMessage("kinds");
    }

    @DisplayName(".filteringByKinds(Iterable<Kind>) fails if any kinds are null")
    @Test
    void failsIfAnyKindsAreNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.filteringByKinds(Arrays.asList(Kind.NOTE, null, Kind.ERROR)))
          .withMessage("kinds[1]");
    }

    @DisplayName(".filteringByKinds(Iterable<Kind>) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.filteringByKinds(List.of(Kind.NOTE, Kind.ERROR)));
    }

    @DisplayName(".filteringByKinds(Iterable<Kind>) filters diagnostics by kind")
    @Test
    void filtersDiagnosticsByKind() {
      // Given
      var note1 = someTraceDiagnostic(Kind.NOTE);
      var note2 = someTraceDiagnostic(Kind.NOTE);
      var error1 = someTraceDiagnostic(Kind.ERROR);
      var error2 = someTraceDiagnostic(Kind.ERROR);
      var diagnostics = Arrays.asList(
          note1,
          error1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.WARNING),
          null,
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error2,
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          note2
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.filteringByKinds(List.of(Kind.NOTE, Kind.ERROR))
              .containsExactly(note1, error1, error2, note2));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#excludingKinds(Kind, Kind...) tests")
  @Nested
  class ExcludingKindsVarargsTest {

    @DisplayName(".excludingKinds(Kind, Kind...) fails if the first kind is null")
    @Test
    void failsIfFirstKindIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.excludingKinds(null, new Kind[0]))
          .withMessage("kind");
    }

    @DisplayName(".excludingKinds(Kind, Kind...) fails if the vararg array is null")
    @Test
    void failsIfVarargArrayIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.excludingKinds(Kind.NOTE, (Kind[]) null))
          .withMessage("moreKinds");
    }

    @DisplayName(".excludingKinds(Kind, Kind...) fails if any varargs are null")
    @Test
    void failsIfAnyVarargsAreNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.excludingKinds(Kind.NOTE, Kind.WARNING, null, Kind.ERROR))
          .withMessage("moreKinds[1]");
    }

    @DisplayName(".excludingKinds(Kind, Kind...) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.excludingKinds(Kind.NOTE, Kind.ERROR));
    }

    @DisplayName(".excludingKinds(Kind, Kind...) filters diagnostics by kind")
    @Test
    void filtersDiagnosticsByKind() {
      // Given
      var note1 = someTraceDiagnostic(Kind.NOTE);
      var note2 = someTraceDiagnostic(Kind.NOTE);
      var error1 = someTraceDiagnostic(Kind.ERROR);
      var error2 = someTraceDiagnostic(Kind.ERROR);
      var diagnostics = Arrays.asList(
          note1,
          error1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.WARNING),
          null,
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error2,
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          note2
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions
              .excludingKinds(Kind.WARNING, Kind.MANDATORY_WARNING, Kind.OTHER)
              .containsExactly(note1, error1, error2, note2));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#excludingKinds(Iterable<Kind>) tests")
  @Nested
  class ExcludingKindsIterableTest {

    @DisplayName(".excludingKinds(Iterable<Kind>) fails if the iterable is null")
    @SuppressWarnings("RedundantCast")  // Prevent ambiguity
    @Test
    void failsIfKindIterableIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.excludingKinds((Iterable<Kind>) null))
          .withMessage("kinds");
    }

    @DisplayName(".excludingKinds(Iterable<Kind>) fails if any kinds are null")
    @Test
    void failsIfAnyKindsAreNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.excludingKinds(Arrays.asList(Kind.NOTE, null, Kind.ERROR)))
          .withMessage("kinds[1]");
    }

    @DisplayName(".excludingKinds(Iterable<Kind>) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.excludingKinds(List.of(Kind.NOTE, Kind.ERROR)));
    }

    @DisplayName(".excludingKinds(Iterable<Kind>) filters diagnostics by kind")
    @Test
    void filtersDiagnosticsByKind() {
      // Given
      var note1 = someTraceDiagnostic(Kind.NOTE);
      var note2 = someTraceDiagnostic(Kind.NOTE);
      var error1 = someTraceDiagnostic(Kind.ERROR);
      var error2 = someTraceDiagnostic(Kind.ERROR);
      var diagnostics = Arrays.asList(
          note1,
          error1,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.WARNING),
          null,
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          error2,
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          note2
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions
              .excludingKinds(Set.of(Kind.WARNING, Kind.MANDATORY_WARNING, Kind.OTHER))
              .containsExactly(note1, error1, error2, note2));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoErrors(...) tests")
  @Nested
  class HasNoErrorsTest {

    @DisplayName(".hasNoErrors() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoErrors)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(".hasNoErrors() fails if the diagnostic list contains errors")
    @Test
    void failsIfTheDiagnosticListContainsErrors() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoErrors)
          .withMessageStartingWith("Expected no error diagnostics.");
    }

    @DisplayName(".hasNoErrors() succeeds if the diagnostic list does not contain errors")
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainErrors() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoErrors().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoErrorsOrWarnings(...) tests")
  @Nested
  class HasNoErrorsOrWarningsTest {

    @DisplayName(".hasNoErrorsOrWarnings() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoErrorsOrWarnings)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(
        ".hasNoErrorsOrWarnings() fails if the diagnostic list contains errors or warnings"
    )
    @EnumSource(value = Kind.class, names = {"MANDATORY_WARNING", "WARNING", "ERROR"})
    @ParameterizedTest(name = "when including diagnostic of kind {0}")
    void failsIfTheDiagnosticListContainsErrorsOrWarnings(Kind unwantedKind) {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(unwantedKind),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoErrorsOrWarnings)
          .withMessageStartingWith(
              "Expected no error, mandatory warning, or warning diagnostics."
          );
    }

    @DisplayName(
        ".hasNoErrorsOrWarnings() succeeds if the diagnostic list does not contain errors or "
            + "warnings"
    )
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainErrorsOrWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoErrorsOrWarnings().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoWarnings(...) tests")
  @Nested
  class HasNoWarningsTest {

    @DisplayName(".hasNoWarnings() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoWarnings)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(".hasNoWarnings() fails if the diagnostic list contains warnings")
    @EnumSource(value = Kind.class, names = {"MANDATORY_WARNING", "WARNING"})
    @ParameterizedTest(name = "when including diagnostic of kind {0}")
    void failsIfTheDiagnosticListContainsWarnings(Kind unwantedKind) {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(unwantedKind),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoWarnings)
          .withMessageStartingWith("Expected no mandatory warning, or warning diagnostics.");
    }

    @DisplayName(".hasNoWarnings() succeeds if the diagnostic list does not contain warnings")
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.ERROR)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoWarnings().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoCustomWarnings(...) tests")
  @Nested
  class HasNoCustomWarningsTest {

    @DisplayName(".hasNoCustomWarnings() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoCustomWarnings)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(
        ".hasNoCustomWarnings() fails if the diagnostic list contains custom warnings"
    )
    @Test
    void failsIfTheDiagnosticListContainsCustomWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoCustomWarnings)
          .withMessageStartingWith("Expected no warning diagnostics.");
    }

    @DisplayName(
        ".hasNoCustomWarnings() succeeds if the diagnostic list does not contain custom warnings"
    )
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainCustomWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.ERROR)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoCustomWarnings().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoMandatoryWarnings(...) tests")
  @Nested
  class HasNoMandatoryWarningsTest {

    @DisplayName(".hasNoMandatoryWarnings() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoMandatoryWarnings)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(
        ".hasNoMandatoryWarnings() fails if the diagnostic list contains mandatory warnings"
    )
    @Test
    void failsIfTheDiagnosticListContainsMandatoryWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoMandatoryWarnings)
          .withMessageStartingWith("Expected no mandatory warning diagnostics.");
    }

    @DisplayName(
        ".hasNoMandatoryWarnings() succeeds if the diagnostic list does not contain mandatory "
            + "warnings"
    )
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainMandatoryWarnings() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoMandatoryWarnings().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoNotes(...) tests")
  @Nested
  class HasNoNotesTest {

    @DisplayName(".hasNoNotes() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoNotes)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(".hasNoNotes() fails if the diagnostic list contains notes")
    @Test
    void failsIfTheDiagnosticListContainsNotes() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoNotes)
          .withMessageStartingWith("Expected no note diagnostics.");
    }

    @DisplayName(".hasNoNotes() succeeds if the diagnostic list does not contain notes")
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainNotes() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoNotes().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.hasNoOtherDiagnostics(...) tests")
  @Nested
  class HasNoOthersTest {

    @DisplayName(".hasNoOtherDiagnostics() fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoOtherDiagnostics)
          .withMessageContaining("Expecting actual not to be null");
    }

    @DisplayName(".hasNoOtherDiagnostics() fails if the diagnostic list contains other diagnostics")
    @Test
    void failsIfTheDiagnosticListContainsOtherDiagnostics() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.OTHER),
          someTraceDiagnostic(Kind.NOTE)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(assertions::hasNoOtherDiagnostics)
          .withMessageStartingWith("Expected no other diagnostics.");
    }

    @DisplayName(
        ".hasNoOtherDiagnostics() succeeds if the diagnostic list does not contain "
            + "other diagnostics"
    )
    @Test
    void succeedsIfTheDiagnosticListDoesNotContainOtherDiagnostics() {
      // Given
      var diagnostics = List.of(
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.NOTE)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions.hasNoOtherDiagnostics().isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#hasNoDiagnosticsOfKinds(Kind, Kind...) tests")
  @Nested
  class HasNoDiagnosticsOfKindsVarargsTest {

    @DisplayName(".hasNoDiagnosticsOfKinds(Kind, Kind...) fails if the first kind is null")
    @Test
    void failsIfFirstKindIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(null, new Kind[0]))
          .withMessage("kind");
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Kind, Kind...) fails if the vararg array is null")
    @Test
    void failsIfVarargArrayIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(Kind.NOTE, (Kind[]) null))
          .withMessage("moreKinds");
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Kind, Kind...) fails if any varargs are null")
    @Test
    void failsIfAnyVarargsAreNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions
              .hasNoDiagnosticsOfKinds(Kind.NOTE, Kind.WARNING, null, Kind.ERROR))
          .withMessage("moreKinds[1]");
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Kind, Kind...) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(Kind.NOTE, Kind.ERROR));
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Kind, Kind...) fails if any provided kinds are present")
    @Test
    void failsIfAnyProvidedKindsArePresent() {
      // Given
      var diagnostics = Arrays.asList(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          null,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          null,
          someTraceDiagnostic(Kind.NOTE)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(Kind.ERROR, Kind.NOTE))
          .withMessage(
              "Expected no error, or note diagnostics.\n\nDiagnostics:\n%s",
              diagnostics
                  .stream()
                  .filter(Objects::nonNull)
                  .filter(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.NOTE)
                  .collect(Collectors.collectingAndThen(
                      Collectors.toList(),
                      TraceDiagnosticListRepresentation.getInstance()::toStringOf
                  ))
          );
    }

    @DisplayName(
        ".hasNoDiagnosticsOfKinds(Kind, Kind...) succeeds if no provided kinds are present"
    )
    @Test
    void succeedsIfNoProvidedKindsArePresent() {
      // Given
      var diagnostics = Arrays.asList(
          someTraceDiagnostic(Kind.OTHER),
          null,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          null
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions
              .hasNoDiagnosticsOfKinds(Kind.ERROR, Kind.NOTE)
              .isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert#hasNoDiagnosticsOfKinds(Iterable<Kind>) tests")
  @Nested
  class HasNoDiagnosticsOfKindsIterableTest {

    @DisplayName(".hasNoDiagnosticsOfKinds(Iterable<Kind>) fails if the iterable is null")
    @SuppressWarnings("RedundantCast")  // Prevent reading ambiguity
    @Test
    void failsIfIterableIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds((Iterable<Kind>) null))
          .withMessage("kinds");
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Iterable<Kind>) fails if the iterable has null members")
    @Test
    void failsIfIterableHasNullMembers() {
      // Given
      var assertions = new TraceDiagnosticListAssert(mock());

      // Then
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> assertions
              .hasNoDiagnosticsOfKinds(Arrays.asList(Kind.ERROR, null, Kind.WARNING)))
          .withMessage("kinds[1]");
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Iterable<Kind>) fails if the diagnostic list is null")
    @Test
    void failsIfDiagnosticListIsNull() {
      // Given
      var assertions = new TraceDiagnosticListAssert(null);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(List.of(Kind.NOTE, Kind.ERROR)));
    }

    @DisplayName(".hasNoDiagnosticsOfKinds(Iterable<Kind>) fails if any provided kinds are present")
    @Test
    void failsIfAnyProvidedKindsArePresent() {
      // Given
      var diagnostics = Arrays.asList(
          someTraceDiagnostic(Kind.NOTE),
          someTraceDiagnostic(Kind.OTHER),
          null,
          someTraceDiagnostic(Kind.ERROR),
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          null,
          someTraceDiagnostic(Kind.NOTE)
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatExceptionOfType(AssertionError.class)
          .isThrownBy(() -> assertions.hasNoDiagnosticsOfKinds(List.of(Kind.ERROR, Kind.NOTE)))
          .withMessage(
              "Expected no error, or note diagnostics.\n\nDiagnostics:\n%s",
              diagnostics
                  .stream()
                  .filter(Objects::nonNull)
                  .filter(d -> d.getKind() == Kind.ERROR || d.getKind() == Kind.NOTE)
                  .collect(Collectors.collectingAndThen(
                      Collectors.toList(),
                      TraceDiagnosticListRepresentation.getInstance()::toStringOf
                  ))
          );
    }

    @DisplayName(
        ".hasNoDiagnosticsOfKinds(Iterable<Kind>) succeeds if no provided kinds are present"
    )
    @Test
    void succeedsIfNoProvidedKindsArePresent() {
      // Given
      var diagnostics = Arrays.asList(
          someTraceDiagnostic(Kind.OTHER),
          null,
          someTraceDiagnostic(Kind.WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          someTraceDiagnostic(Kind.MANDATORY_WARNING),
          null
      );
      var assertions = new TraceDiagnosticListAssert(diagnostics);

      // Then
      assertThatNoException()
          .isThrownBy(() -> assertions
              .hasNoDiagnosticsOfKinds(List.of(Kind.ERROR, Kind.NOTE))
              .isSameAs(diagnostics));
    }
  }

  @DisplayName("TraceDiagnosticListAssert.filteringBy(...) tests")
  @Nested
  class FilteringByTest {

    @DisplayName(".filteringBy(...) calls .filteredOn")
    @Test
    void filteringByCallsFilteredOn() {
      // Given
      var assertions = mock(TraceDiagnosticListAssert.class);
      var expectedResult = mock(TraceDiagnosticListAssert.class);
      when(assertions.filteringBy(any())).thenCallRealMethod();
      when(assertions.filteredOn(anyPredicate())).thenReturn(expectedResult);
      Predicate<TraceDiagnostic<? extends JavaFileObject>> predicate = mock();

      // When
      var actualResult = assertions.filteringBy(predicate);

      // Then
      assertThat(actualResult).isSameAs(expectedResult);
      verify(assertions).filteringBy(predicate);
      verify(assertions).isNotNull();
      verify(assertions).filteredOn(predicate);
      verifyNoMoreInteractions(assertions);
    }

    private Predicate<TraceDiagnostic<? extends JavaFileObject>> anyPredicate() {
      return ExtraArgumentMatchers.hasGenericType();
    }
  }

  @DisplayName("TraceDiagnosticListAssert.toAssert(...) tests")
  @Nested
  class ToAssertTest {

    @DisplayName(".toAssert(...) should create a new assertion correctly")
    @Test
    void toAssertShouldCreateNewAssertionCorrectly() {
      // .singleElement() calls .toAssert() internally.

      // Given
      var description = someText();
      var diagnostics = List.of(someTraceDiagnostic(Kind.ERROR));
      var diagnosticListAssertions = new TraceDiagnosticListAssert(diagnostics)
          .describedAs(description);

      // When
      var diagnosticAssertions = diagnosticListAssertions.singleElement();

      // Then
      assertThatNoException()
          .isThrownBy(() -> diagnosticAssertions.isSameAs(diagnostics.get(0)));

      assertThat(diagnosticAssertions.descriptionText())
          .startsWith(description)
          .contains("check single element");
    }
  }
}
