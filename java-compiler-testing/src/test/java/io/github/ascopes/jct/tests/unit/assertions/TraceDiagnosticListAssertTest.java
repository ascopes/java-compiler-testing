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

import io.github.ascopes.jct.assertions.TraceDiagnosticListAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import javax.tools.Diagnostic.Kind;
import java.util.List;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someTraceDiagnostic;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TraceDiagnosticListAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("TraceDiagnosticListAssert tests")
class TraceDiagnosticListAssertTest {

  @DisplayName("TraceDiagnosticListAssert#errors tests")
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

  @DisplayName("TraceDiagnosticListAssert#warnings tests")
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

  @DisplayName("TraceDiagnosticListAssert#customWarnings tests")
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


  @DisplayName("TraceDiagnosticListAssert#mandatoryWarnings tests")
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
}
