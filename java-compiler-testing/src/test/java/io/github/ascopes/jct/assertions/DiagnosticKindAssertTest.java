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
package io.github.ascopes.jct.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.tools.Diagnostic.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;

/**
 * {@link DiagnosticKindAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("DiagnosticKindAssert tests")
class DiagnosticKindAssertTest {

  @DisplayName("DiagnosticKindAssert.isError(...) tests")
  @Nested
  class IsErrorTest {

    @DisplayName(".isError() fails if the kind is not an error")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "ERROR")
    @ParameterizedTest(name = "for {0}")
    void isErrorFailsIfKindIsNotError(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isError)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isError() succeeds if the kind is an error")
    @Test
    void isErrorSucceedsIfTheKindIsAnError() {
      // Given
      var assertions = new DiagnosticKindAssert(Kind.ERROR);

      // Then
      assertThat(assertions.isError())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isWarningOrError(...) tests")
  @Nested
  class IsWarningOrErrorTest {

    @DisplayName(
        ".isWarningOrError() fails if the kind is not an error, warning, or mandatory warning"
    )
    @NullSource
    @EnumSource(
        value = Kind.class,
        mode = Mode.EXCLUDE,
        names = {"ERROR", "WARNING", "MANDATORY_WARNING"}
    )
    @ParameterizedTest(name = "for {0}")
    void isWarningOrErrorFailsIfKindIsNotWarningOrError(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isWarningOrError)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(
        ".isWarningOrError() succeeds if the kind is an error, warning, or mandatory warning"
    )
    @EnumSource(
        value = Kind.class,
        mode = Mode.INCLUDE,
        names = {"ERROR", "WARNING", "MANDATORY_WARNING"}
    )
    @ParameterizedTest(name = "for {0}")
    void isWarningOrErrorSucceedsIfTheKindIsWarningOrError(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThat(assertions.isWarningOrError())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isWarning(...) tests")
  @Nested
  class IsWarningTest {

    @DisplayName(".isWarning() fails if the kind is not a warning or mandatory warning")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = {"WARNING", "MANDATORY_WARNING"})
    @ParameterizedTest(name = "for {0}")
    void isWarningFailsIfKindIsNotWarning(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isWarning)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isWarning() succeeds if the kind is a warning or mandatory warning")
    @EnumSource(value = Kind.class, mode = Mode.INCLUDE, names = {"WARNING", "MANDATORY_WARNING"})
    @ParameterizedTest(name = "for {0}")
    void isWarningSucceedsIfTheKindIsWarning(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThat(assertions.isWarning())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isCustomWarning(...) tests")
  @Nested
  class IsCustomWarningTest {

    @DisplayName(".isCustomWarning() fails if the kind is not WARNING")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "WARNING")
    @ParameterizedTest(name = "for {0}")
    void isCustomWarningFailsIfKindIsNotCustomWarning(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isCustomWarning)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isCustomWarning() succeeds if the kind is WARNING")
    @Test
    void isCustomWarningSucceedsIfTheKindIsCustomWarning() {
      // Given
      var assertions = new DiagnosticKindAssert(Kind.WARNING);

      // Then
      assertThat(assertions.isCustomWarning())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isMandatoryWarning(...) tests")
  @Nested
  class IsMandatoryWarningTest {

    @DisplayName(".isMandatoryWarning() fails if the kind is not a mandatory warning")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "MANDATORY_WARNING")
    @ParameterizedTest(name = "for {0}")
    void isMandatoryWarningFailsIfKindIsNotMandatoryWarning(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isMandatoryWarning)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isMandatoryWarning() succeeds if the kind is a mandatory warning")
    @Test
    void isMandatoryWarningSucceedsIfTheKindIsMandatoryWarning() {
      // Given
      var assertions = new DiagnosticKindAssert(Kind.MANDATORY_WARNING);

      // Then
      assertThat(assertions.isMandatoryWarning())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isNote(...) tests")
  @Nested
  class IsNoteTest {

    @DisplayName(".isNote() fails if the kind is not a note")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "NOTE")
    @ParameterizedTest(name = "for {0}")
    void isNoteFailsIfKindIsNotNote(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isNote)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isNote() succeeds if the kind is a note")
    @Test
    void isNoteSucceedsIfTheKindIsNote() {
      // Given
      var assertions = new DiagnosticKindAssert(Kind.NOTE);

      // Then
      assertThat(assertions.isNote())
          .isSameAs(assertions);
    }
  }

  @DisplayName("DiagnosticKindAssert.isOther(...) tests")
  @Nested
  class IsOtherTest {

    @DisplayName(".isOther() fails if the kind is not OTHER")
    @NullSource
    @EnumSource(value = Kind.class, mode = Mode.EXCLUDE, names = "OTHER")
    @ParameterizedTest(name = "for {0}")
    void isOtherFailsIfKindIsNotOther(Kind kind) {
      // Given
      var assertions = new DiagnosticKindAssert(kind);

      // Then
      assertThatThrownBy(assertions::isOther)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isOther() succeeds if the kind is OTHER")
    @Test
    void isOtherSucceedsIfTheKindIsOther() {
      // Given
      var assertions = new DiagnosticKindAssert(Kind.OTHER);

      // Then
      assertThat(assertions.isOther())
          .isSameAs(assertions);
    }
  }
}
