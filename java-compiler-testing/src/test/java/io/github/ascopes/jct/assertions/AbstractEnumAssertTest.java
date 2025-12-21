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

import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AbstractEnumAssert tests")
@SuppressWarnings("DataFlowIssue")
class AbstractEnumAssertTest {

  @DisplayName(".isAnyOf(...) tests")
  @Nested
  class IsAnyOfTest {

    @DisplayName("Expect failure when the element is null and we assert against one element")
    @Test
    void failsIfInputIsNullOnSingleElement() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf(DnbArtist.MADUK))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect failure when the element is null and we assert against multiple elements")
    @Test
    void failsIfInputIsNullOnMultipleElements() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf(DnbArtist.MADUK, DnbArtist.WILKINSON))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect error if we assert against a single null element")
    @Test
    void errorsIfExpectedIsNullOnSingleElement() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf((DnbArtist) null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against a null array")
    @Test
    void errorsIfExpectedIsNullArray() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf((DnbArtist[]) null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we against multiple null elements")
    @Test
    void errorsIfExpectedIsNullOnMultipleElements() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf(DnbArtist.SUB_FOCUS, null, null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against an empty array")
    @Test
    void errorsIfExpectedIsEmptyArray() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(assertions::isAnyOf)
          .isInstanceOf(IllegalArgumentException.class);
    }


    @DisplayName("Expect failure if no match against a single element")
    @Test
    void failsIfNoMatchOnSingleElement() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf(DnbArtist.MADUK))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect failure if no match against multiple elements")
    @Test
    void failsIfNoMatchOnMultipleElements() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOf(
          DnbArtist.MADUK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM
      )).isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect success if the first element matches")
    @Test
    void succeedsIfFirstElementIsMatch() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertions.isAnyOf(
          DnbArtist.CULTURE_SHOCK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM
      );
    }

    @DisplayName("Expect success if another element matches")
    @Test
    void succeedsIfArrayElementIsMatch() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertions.isAnyOf(
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM,
          DnbArtist.CULTURE_SHOCK
      );
    }
  }

  @DisplayName(".isAnyOfElements(...) tests")
  @Nested
  class IsAnyOfElementsTest {

    @DisplayName("Expect failure when the element is null")
    @Test
    void failsIfInputIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOfElements(List.of(DnbArtist.MADUK)))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect error if we assert against a null iterable")
    @Test
    void errorsIfExpectedIsNull() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOfElements(null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if the input is null and we assert against null elements")
    @Test
    void errorsIfExpectedIsNullElement() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOfElements(Arrays.asList(null, null)))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against an empty collection")
    @Test
    void errorsIfExpectedIsEmptyCollection() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOfElements(List.of()))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Expect failure if no match against multiple elements")
    @Test
    void failsIfNoMatchOnMultipleElements() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isAnyOfElements(List.of(
          DnbArtist.MADUK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM
      ))).isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect success if any element matches")
    @Test
    void succeedsIfAnElementIsMatch() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertions.isAnyOfElements(List.of(
          DnbArtist.CULTURE_SHOCK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM
      ));
    }
  }


  @DisplayName(".isNoneOf(...) tests")
  @Nested
  class IsNoneOfTest {

    @DisplayName("Expect failure when the element is null and we assert against one element")
    @Test
    void failsIfInputIsNullOnSingleElement() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf(DnbArtist.MADUK))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect failure when the element is null and we assert against multiple elements")
    @Test
    void failsIfInputIsNullOnMultipleElements() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf(DnbArtist.MADUK, DnbArtist.WILKINSON))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect error if we assert against a single null element")
    @Test
    void errorsIfExpectedIsNullOnSingleElement() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf((DnbArtist) null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against a null array")
    @Test
    void errorsIfExpectedIsNullOnMultipleElementsArray() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf((DnbArtist[]) null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we against multiple null elements")
    @Test
    void errorsIfExpectedIsNullOnMultipleElements() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf(DnbArtist.SUB_FOCUS, null, null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against an empty array")
    @Test
    void errorsIfExpectedIsEmptyArray() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(assertions::isNoneOf)
          .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Expect failure if match against a single element")
    @Test
    void failsIfMatchOnSingleElement() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf(DnbArtist.CULTURE_SHOCK))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect failure if match against multiple elements")
    @Test
    void failsIfMatchOnMultipleElements() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOf(
          DnbArtist.MADUK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM,
          DnbArtist.CULTURE_SHOCK
      )).isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect success if another element matches")
    @Test
    void succeedsIfArrayElementIsNotMatch() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      var result = assertions.isNoneOf(
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM,
          DnbArtist.RAMESES_B
      );

      assertThat(result).isSameAs(assertions);
    }
  }

  @DisplayName(".isNoneOfElements(...) tests")
  @Nested
  class IsNoneOfElementsTest {

    @DisplayName("Expect failure when the element is null")
    @Test
    void failsIfInputIsNull() {
      // Given
      var assertions = new Impl(null);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOfElements(List.of(DnbArtist.MADUK)))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect error if we assert against a null iterable")
    @Test
    void errorsIfExpectedIsNull() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOfElements(null))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if the input is null and we assert against null elements")
    @Test
    void errorsIfExpectedIsNullElement() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOfElements(Arrays.asList(null, null)))
          .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Expect error if we assert against an empty collection")
    @Test
    void errorsIfExpectedIsEmptyCollection() {
      // Given
      var assertions = new Impl(DnbArtist.FEINT);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOfElements(List.of()))
          .isInstanceOf(IllegalArgumentException.class);
    }


    @DisplayName("Expect failure if match against any elements")
    @Test
    void failsIfMatchOnElement() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // Then
      assertThatThrownBy(() -> assertions.isNoneOfElements(List.of(
          DnbArtist.MADUK,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.CULTURE_SHOCK,
          DnbArtist.PENDULUM
      ))).isInstanceOf(AssertionError.class);
    }

    @DisplayName("Expect success if no element matches")
    @Test
    void succeedsIfNoMatches() {
      // Given
      var assertions = new Impl(DnbArtist.CULTURE_SHOCK);

      // When
      var result = assertions.isNoneOfElements(List.of(
          DnbArtist.RAMESES_B,
          DnbArtist.DELTA_HEAVY,
          DnbArtist.PENDULUM
      ));

      assertThat(result).isSameAs(assertions);
    }
  }

  static class Impl extends AbstractEnumAssert<Impl, DnbArtist> {

    Impl(@Nullable DnbArtist value) {
      super(value, Impl.class);
    }
  }

  enum DnbArtist {
    MADUK,
    DELTA_HEAVY,
    WILKINSON,
    PENDULUM,
    FEINT,
    CULTURE_SHOCK,
    RAMESES_B,
    SUB_FOCUS,
  }
}
