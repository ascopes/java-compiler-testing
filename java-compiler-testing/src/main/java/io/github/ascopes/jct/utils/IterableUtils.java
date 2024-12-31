/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.utils;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Iterable and collection helper utilities.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class IterableUtils extends UtilityClass {

  private IterableUtils() {
    // Disallow initialisation.
  }

  /**
   * Take an iterable of iterables and flatten them into a list of individual items.
   *
   * @param iterableOfItems the items to flatten.
   * @param <T>             the inner type.
   * @return the flattened items.
   */
  public static <T> List<T> flatten(Iterable<? extends Iterable<T>> iterableOfItems) {
    var flattenedItems = new ArrayList<T>();
    for (var items : iterableOfItems) {
      for (var item : items) {
        flattenedItems.add(item);
      }
    }
    return Collections.unmodifiableList(flattenedItems);
  }

  /**
   * Ensure that an array has at least one item, raising an exception if it doesn't.
   *
   * <p>This also fails if the array is {@code null}.
   *
   * @param elements  the array to check.
   * @param arrayName the name of the array to show in the error message if the check fails.
   * @param <T>       the element type within the array.
   * @throws IllegalArgumentException if the argument is empty.
   * @throws NullPointerException     if the array is {@code null}.
   * @since 4.0.0
   */
  public static <T> @Nullable T[] requireAtLeastOne(
      @Nullable T @Nullable [] elements,
      String arrayName
  ) {
    requireNonNull(elements, arrayName);

    if (elements.length == 0) {
      throw new IllegalArgumentException(arrayName + " must not be empty");
    }

    return elements;
  }

  /**
   * Ensure that a collection has at least one item, raising an exception if it doesn't.
   *
   * <p>This also fails if the collection is {@code null}.
   *
   * @param collection     the collection to check.
   * @param collectionName the name of the collection to show in the error message if the check
   *                       fails.
   * @param <T>            the input collection type.
   * @param <U>            the element type within the collection.
   * @throws IllegalArgumentException if the iterable is empty.
   * @throws NullPointerException     if the iterable is {@code null}.
   * @since 4.0.0
   */
  public static <T extends Collection<@Nullable U>, U> T requireAtLeastOne(
      @Nullable T collection,
      String collectionName
  ) {
    requireNonNull(collection, collectionName);

    if (collection.isEmpty()) {
      throw new IllegalArgumentException(collectionName + " must not be empty");
    }

    return collection;
  }

  /**
   * Ensure there are no {@code null} elements in the given iterable.
   *
   * <p>This also ensures the iterable itself is not {@code null} either.
   *
   * @param iterable       the iterable to check.
   * @param collectionName the name of the collection to show in the error message if the check
   *                       fails.
   * @param <T>            the input collection type.
   * @param <U>            the element type within the collection.
   * @return the input iterable type.
   * @throws NullPointerException if any of the values are null or if the collection itself is
   *                              null.
   */
  public static <T extends Iterable<@Nullable U>, U> T requireNonNullValues(
      @Nullable T iterable,
      String collectionName
  ) {
    requireNonNull(iterable, collectionName);

    var badElements = Stream.<String>builder();

    var index = 0;
    for (@Nullable Object element : iterable) {
      if (element == null) {
        badElements.add(collectionName + "[" + index + "]");
      }
      ++index;
    }

    var error = badElements
        .build()
        .collect(Collectors.joining(", "));

    if (!error.isEmpty()) {
      throw new NullPointerException(error);
    }

    return iterable;
  }

  /**
   * Ensure there are no {@code null} elements in the given array.
   *
   * <p>This also ensures the array itself is not null either.
   *
   * @param array     the array to check.
   * @param arrayName the name to give in the error message if anything is null.
   * @param <T>       the input collection type.
   * @return the input array.
   */
  @SuppressWarnings("RedundantSuppression")
  public static <T> T[] requireNonNullValues(
      @Nullable T @Nullable [] array,
      String arrayName
  ) {
    // Duplicate this logic so that we do not have to wrap the array in Arrays.list. This prevents
    // a copy of the entire array each time we do this check.
    requireNonNull(array, arrayName);

    var badElements = Stream.<String>builder();

    var index = 0;
    for (@Nullable Object element : array) {
      if (element == null) {
        badElements.add(arrayName + "[" + index + "]");
      }
      ++index;
    }

    var error = badElements
        .build()
        .collect(Collectors.joining(", "));

    if (!error.isEmpty()) {
      throw new NullPointerException(error);
    }

    //noinspection RedundantCast -- Cast is not redundant as we're casting away any nullability
    // annotations for nullness typecheckers. Don't let IntelliJ tell you otherwise as it is a
    // bug.
    return (T[]) array;
  }
}
