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
package io.github.ascopes.jct.tests.unit.utils;

import static io.github.ascopes.jct.utils.IterableUtils.flatten;
import static io.github.ascopes.jct.utils.IterableUtils.requireAtLeastOne;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.IterableUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link IterableUtils} tests.
 *
 * @author Ashley Scopes
 */
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

  @DisplayName("requireAtLeastOne(T[], String) raises an exception if the argument is null")
  @Test
  void requireAtLeastOneArrayRaisesExceptionIfArgumentIsNull() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireAtLeastOne((Object[]) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave");
  }

  @DisplayName("requireAtLeastOne(T[], String) raises an exception if the argument is empty")
  @Test
  void requireAtLeastOneArrayRaisesExceptionIfArgumentIsEmpty() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireAtLeastOne(new Object[0], "dave"))
        .isExactlyInstanceOf(IllegalArgumentException.class)
        .withMessage("dave must not be empty");
  }

  @DisplayName("requireAtLeastOne(T[], String) returns the array if it has elements")
  @Test
  void requireAtLeastOneArrayReturnsArrayIfItHasElements() {
    // Given
    var array = new Object[1];

    // When
    var returnedArray = requireAtLeastOne(array, "dave");

    // Then
    assertThat(returnedArray).isSameAs(array);
  }


  @DisplayName(
      "requireAtLeastOne(Collection<?>, String) raises an exception if the argument is null")
  @Test
  void requireAtLeastOneCollectionRaisesExceptionIfArgumentIsNull() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireAtLeastOne((Collection<?>) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave");
  }

  @DisplayName(
      "requireAtLeastOne(Collection<?>, String) raises an exception if the argument is empty")
  @Test
  void requireAtLeastOneCollectionRaisesExceptionIfArgumentIsEmpty() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireAtLeastOne(List.of(), "dave"))
        .isExactlyInstanceOf(IllegalArgumentException.class)
        .withMessage("dave must not be empty");
  }

  @DisplayName(
      "requireAtLeastOne(Collection<?>, String) returns the collection if it has elements")
  @Test
  void requireAtLeastOneCollectionReturnsCollectionIfItHasElements() {
    // Given
    var collection = List.of("foo");

    // When
    var returnedCollection = requireAtLeastOne(collection, "dave");

    // Then
    assertThat(returnedCollection).isSameAs(collection);
  }

  @DisplayName(
      "requireNonNullValues(Iterable<?>, String) succeeds when no null elements are present")
  @Test
  void requireNonNullValuesIterableSucceedsWhenNoNullElementsArePresent() {
    // Given
    var collection = List.of("foo", "bar", "", "baz", "bork");

    // Then
    assertThatNoException()
        .isThrownBy(() -> requireNonNullValues(collection, "dave"));
  }

  @DisplayName("requireNonNullValues(Iterable<?>, String) fails when the collection is null")
  @Test
  void requireNonNullValuesIterableFailsWhenCollectionIsNull() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues((Iterable<?>) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave");
  }

  @DisplayName(
      "requireNonNullValues(Iterable<?>, String) fails when a single null element is present")
  @Test
  void requireNonNullValuesIterableFailsWhenSingleNullElementIsPresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", "", null, "baz", "bork");

    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave[3]");
  }

  @DisplayName(
      "requireNonNullValues(Iterable<?>. String) fails when multiple null elements are present")
  @Test
  void requireNonNullValuesIterableFailsWhenMultipleNullElementsArePresent() {
    // Given
    var collection = Arrays.asList("foo", "bar", null, "", null, null, "baz", "bork");

    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues(collection, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave[2], dave[4], dave[5]");
  }

  @DisplayName("requireNonNullValues(T[], String) succeeds when no null elements are present")
  @Test
  void requireNonNullValuesArraySucceedsWhenNoNullElementsArePresent() {
    // Given
    var array = new String[]{"foo", "bar", "", "baz", "bork"};

    // Then
    assertThatNoException()
        .isThrownBy(() -> requireNonNullValues(array, "dave"));
  }

  @DisplayName("requireNonNullValues(T[], String) fails when the collection is null")
  @Test
  void requireNonNullValuesArrayFailsWhenCollectionIsNull() {
    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues((String[]) null, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave");
  }

  @DisplayName("requireNonNullValues(T[], String) fails when a single null element is present")
  @SuppressWarnings("DataFlowIssue")
  @Test
  void requireNonNullValuesArrayFailsWhenSingleNullElementIsPresent() {
    // Given
    var array = new String[]{"foo", "bar", "", null, "baz", "bork"};

    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues(array, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave[3]");
  }

  @DisplayName("requireNonNullValues(T[], String) fails when multiple null elements are present")
  @SuppressWarnings("DataFlowIssue")
  @Test
  void requireNonNullValuesArrayFailsWhenMultipleNullElementsArePresent() {
    // Given
    var array = new String[]{"foo", "bar", null, "", null, null, "baz", "bork"};

    // Then
    assertThatException()
        .isThrownBy(() -> requireNonNullValues(array, "dave"))
        .isExactlyInstanceOf(NullPointerException.class)
        .withMessage("dave[2], dave[4], dave[5]");
  }
}
