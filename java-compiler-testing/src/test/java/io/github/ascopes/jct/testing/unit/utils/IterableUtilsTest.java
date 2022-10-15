/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.testing.unit.utils;

import static io.github.ascopes.jct.utils.IterableUtils.combineOneOrMore;
import static io.github.ascopes.jct.utils.IterableUtils.nonNullUnmodifiableList;
import static io.github.ascopes.jct.utils.IterableUtils.nonNullUnmodifiableSet;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import io.github.ascopes.jct.utils.IterableUtils;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link IterableUtils} tests.
 *
 * @author Ashley Scopes
 */
class IterableUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return IterableUtils.class;
  }

  @DisplayName("combineOneOrMore(T, T...) returns the expected value")
  @Test
  void combineOneOrMoreReturnsTheExpectedValue() {
    var foo = new Object();
    var bar = new Object();
    var baz = new Object();
    var bork = new Object();

    assertThat(combineOneOrMore(foo))
        .isEqualTo(List.of(foo));

    assertThat(combineOneOrMore(foo, bar))
        .isEqualTo(List.of(foo, bar));

    assertThat(combineOneOrMore(foo, bar, baz))
        .isEqualTo(List.of(foo, bar, baz));

    assertThat(combineOneOrMore(foo, bar, baz, bork))
        .isEqualTo(List.of(foo, bar, baz, bork));
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) succeeds when no null elements are present")
  @Test
  void nonNullUnmodifiableListSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = List.of("foo", "bar", "", "baz", "bork");

    // When
    var result = nonNullUnmodifiableList(collection, "geoff");

    // Then
    assertThat(result)
        .isNotNull()
        .isEqualTo(collection)
        .isNotSameAs(collection)
        .isUnmodifiable();
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) fails when the list is null")
  @Test
  void nonNullUnmodifiableListFailsWhenTheListIsNull() {
    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableList(null, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff");
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) fails when one element is null")
  @Test
  void nonNullUnmodifiableListFailsWhenOneElementIsNull() {
    // Given
    var list = Arrays.asList("foo", "bar", "baz", null, "bork");

    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableList(list, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff[3]");
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) fails when multiple elements are null")
  @Test
  void nonNullUnmodifiableListFailsWhenMultipleElementsAreNull() {
    // Given
    var list = Arrays.asList("foo", "bar", "baz", "bork", null, "qux", null);

    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableList(list, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff[4], geoff[6]");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) succeeds when no null elements are present")
  @Test
  void nonNullUnmodifiableSetSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = Set.of("foo", "bar", "", "baz", "bork");

    // When
    var result = nonNullUnmodifiableSet(collection, "pete");

    // Then
    assertThat(result)
        .isNotNull()
        .isEqualTo(collection)
        .isNotSameAs(collection)
        .isUnmodifiable();
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) fails when the set is null")
  @Test
  void nonNullUnmodifiableSetFailsWhenTheSetIsNull() {
    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableSet(null, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("pete");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) fails when one element is null")
  @Test
  void nonNullUnmodifiableSetFailsWhenOneElementIsNull() {
    // Given
    var set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "bar", "baz", null, "bork"));

    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableSet(set, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("pete[3]");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) fails when multiple elements are null")
  @Test
  void nonNullUnmodifiableSetFailsWhenMultipleElementsAreNull() {
    // Given
    var set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "bar", null, "baz", null, "bork"));

    // Then
    assertThatThrownBy(() -> nonNullUnmodifiableSet(set, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        // Duplicates get removed, duplicate nulls get removed, set is left in insertion order
        // as ["foo", "bar", null, "baz", "bork"]
        // Thus index = 2.
        .hasMessage("pete[2]");
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
