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

package com.github.ascopes.jct.testing.unit.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.ascopes.jct.intern.CollectionUtils;
import com.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CollectionUtils} tests.
 *
 * @author Ashley Scopes
 */
class CollectionUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return CollectionUtils.class;
  }

  @DisplayName("combineOneOrMore(T, T...) returns the expected value")
  @Test
  void combineOneOrMoreReturnsTheExpectedValue() {
    var foo = new Object();
    var bar = new Object();
    var baz = new Object();
    var bork = new Object();

    assertThat(CollectionUtils.combineOneOrMore(foo))
        .isEqualTo(List.of(foo));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar))
        .isEqualTo(List.of(foo, bar));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar, baz))
        .isEqualTo(List.of(foo, bar, baz));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar, baz, bork))
        .isEqualTo(List.of(foo, bar, baz, bork));
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) succeeds when no null elements are present")
  @Test
  void nonNullUnmodifiableListSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = List.of("foo", "bar", "", "baz", "bork");

    // When
    var result = CollectionUtils.nonNullUnmodifiableList(collection, "geoff");

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
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableList(null, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff");
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) fails when one element is null")
  @Test
  void nonNullUnmodifiableListFailsWhenOneElementIsNull() {
    // Given
    var list = Arrays.asList("foo", "bar", "baz", null, "bork");
    
    // Then
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableList(list, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff[3]");
  }

  @DisplayName("nonNullUnmodifiableList(List<T>) fails when multiple elements are null")
  @Test
  void nonNullUnmodifiableListFailsWhenMultipleElementsAreNull() {
    // Given
    var list = Arrays.asList("foo", "bar", "baz", "bork", null, "qux", null);

    // Then
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableList(list, "geoff"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("geoff[4], geoff[6]");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) succeeds when no null elements are present")
  @Test
  void nonNullUnmodifiableSetSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = Set.of("foo", "bar", "", "baz", "bork");

    // When
    var result = CollectionUtils.nonNullUnmodifiableSet(collection, "pete");

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
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableSet(null, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("pete");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) fails when one element is null")
  @Test
  void nonNullUnmodifiableSetFailsWhenOneElementIsNull() {
    // Given
    var set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "bar", "baz", null, "bork"));

    // Then
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableSet(set, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("pete[3]");
  }

  @DisplayName("nonNullUnmodifiableSet(Set<T>) fails when multiple elements are null")
  @Test
  void nonNullUnmodifiableSetFailsWhenMultipleElementsAreNull() {
    // Given
    var set = new LinkedHashSet<>(Arrays.asList("foo", "bar", "bar", null, "baz", null, "bork"));

    // Then
    assertThatThrownBy(() -> CollectionUtils.nonNullUnmodifiableSet(set, "pete"))
        .isExactlyInstanceOf(NullPointerException.class)
        // Duplicates get removed, duplicate nulls get removed, set is left in insertion order
        // as ["foo", "bar", null, "baz", "bork"]
        // Thus index = 2.
        .hasMessage("pete[2]");
  }

  @DisplayName("requireNonNullValues(Collection<?>) succeeds when no null elements are present")
  @Test
  void requireNonNullValuesSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = List.of("foo", "bar", "", "baz", "bork");

    // Then
    assertThatNoException()
        .isThrownBy(() -> CollectionUtils.requireNonNullValues(collection, "dave"));
  }

  @DisplayName("requireNonNullValues(Collection<?>) fails when the collection is null")
  @Test
  void requireNonNullValuesFailsWhenCollectionIsNull() {
    // Then
    assertThatThrownBy(() -> CollectionUtils.requireNonNullValues(null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave");
  }

  @DisplayName("requireNonNullValues(Collection<?>) fails when a single null element is present")
  @Test
  void requireNonNullValuesFailsWhenSingleNullElementIsPresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", "", null, "baz", "bork");

    // Then
    assertThatThrownBy(() -> CollectionUtils.requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[3]");
  }

  @DisplayName("requireNonNullValues(Collection<?>) fails when multiple null elements are present")
  @Test
  void requireNonNullValuesFailsWhenMultipleNullElementsArePresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", null, "", null, null, "baz", "bork");

    // Then
    assertThatThrownBy(() -> CollectionUtils.requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("dave[2], dave[4], dave[5]");
  }
}
