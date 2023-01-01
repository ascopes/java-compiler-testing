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
package io.github.ascopes.jct.tests.unit.utils;

import static io.github.ascopes.jct.utils.IterableUtils.combineOneOrMore;
import static io.github.ascopes.jct.utils.IterableUtils.flatten;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.IterableUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link IterableUtils} tests.
 *
 * @author Ashley Scopes
 */
@SuppressWarnings("DataFlowIssue")
class IterableUtilsTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return IterableUtils.class;
  }

  @DisplayName("flatten(...) returns the expected value")
  @Test
  void flattenReturnsExpectedValue() {
    // Given
    var inputItems = List.<Iterable<String>>of(
        List.of("foo", "bar"),
        Set.of(),
        List.of("baz", "bork", "qux"),
        List.of("eggs"),
        Set.of(),
        List.of("spam")
    );
    var expectedItems = List.of("foo", "bar", "baz", "bork", "qux", "eggs", "spam");

    // When
    var actualItems = flatten(inputItems);

    // Then
    assertThat(actualItems)
        .containsExactlyElementsOf(expectedItems);
  }

  @DisplayName("combineOneOrMore(T, T...) returns the expected value")
  @Test
  void combineOneOrMoreReturnsTheExpectedValue() {
    // Given
    var foo = new Object();
    var bar = new Object();
    var baz = new Object();
    var bork = new Object();

    // Then
    assertThat(combineOneOrMore(foo))
        .isEqualTo(List.of(foo));

    assertThat(combineOneOrMore(foo, bar))
        .isEqualTo(List.of(foo, bar));

    assertThat(combineOneOrMore(foo, bar, baz))
        .isEqualTo(List.of(foo, bar, baz));

    assertThat(combineOneOrMore(foo, bar, baz, bork))
        .isEqualTo(List.of(foo, bar, baz, bork));
  }

  @DisplayName("requireNonNullValues(Iterable<?>) succeeds when no null elements are present")
  @Test
  void requireNonNullValuesIterableSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = List.of("foo", "bar", "", "baz", "bork");

    // Then
    assertThatNoException()
        .isThrownBy(() -> requireNonNullValues(collection, "dave"));
  }

  @DisplayName("requireNonNullValues(Iterable<?>) fails when the collection is null")
  @Test
  void requireNonNullValuesIterableFailsWhenCollectionIsNull() {
    // Then
    assertThatThrownBy(() -> requireNonNullValues((Iterable<?>) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave");
  }

  @DisplayName("requireNonNullValues(Iterable<?>) fails when a single null element is present")
  @Test
  void requireNonNullValuesIterableFailsWhenSingleNullElementIsPresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", "", null, "baz", "bork");

    // Then
    assertThatThrownBy(() -> requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[3]");
  }

  @DisplayName("requireNonNullValues(Iterable<?>) fails when multiple null elements are present")
  @Test
  void requireNonNullValuesIterableFailsWhenMultipleNullElementsArePresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", null, "", null, null, "baz", "bork");

    // Then
    assertThatThrownBy(() -> requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[2], dave[4], dave[5]");
  }

  @DisplayName("requireNonNullValues(T[]) succeeds when no null elements are present")
  @Test
  void requireNonNullValuesArraySucceedsWhenNoNullElementsArePresent() {
    // Given
    var array = new String[]{"foo", "bar", "", "baz", "bork"};

    // Then
    assertThatNoException()
        .isThrownBy(() -> requireNonNullValues(array, "dave"));
  }

  @DisplayName("requireNonNullValues(T[]) fails when the collection is null")
  @Test
  void requireNonNullValuesArrayFailsWhenCollectionIsNull() {
    // Then
    assertThatThrownBy(() -> requireNonNullValues((String[]) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave");
  }

  @DisplayName("requireNonNullValues(T[]) fails when a single null element is present")
  @Test
  void requireNonNullValuesArrayFailsWhenSingleNullElementIsPresent() {
    // Given
    var array = new String[]{"foo", "bar", "", null, "baz", "bork"};

    // Then
    assertThatThrownBy(() -> requireNonNullValues(array, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[3]");
  }

  @DisplayName("requireNonNullValues(T[]) fails when multiple null elements are present")
  @Test
  void requireNonNullValuesArrayFailsWhenMultipleNullElementsArePresent() {
    // Given
    var array = new String[]{"foo", "bar", null, "", null, null, "baz", "bork"};

    // Then
    assertThatThrownBy(() -> requireNonNullValues(array, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[2], dave[4], dave[5]");
  }
}
