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
package io.github.ascopes.jct.utils;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Iterable and collection helper utilities.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
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
    return flattenedItems;
  }

  /**
   * Convert variadic arguments with an enforced first element into a list of those elements.
   *
   * <p>This pattern is used to ensure vararg overloads take at least one element, by enforcing
   * this at compile-time.
   *
   * @param first the enforced first element.
   * @param rest  the rest of the elements.
   * @param <T>   the type of the elements.
   * @return the list of the elements.
   */
  @SafeVarargs
  public static <T> List<T> combineOneOrMore(T first, T... rest) {
    var list = new ArrayList<T>();
    list.add(first);
    list.addAll(Arrays.asList(rest));
    return list;
  }

  /**
   * Ensure there are no {@code null} elements in the given collection.
   *
   * <p>This also ensures the iterable itself is not null either.
   *
   * @param collection     the iterable to check.
   * @param collectionName the name to give in the error message if anything is null.
   * @param <T>            the input collection type.
   * @return the input iterable type.
   */
  public static <T extends Iterable<?>> T requireNonNullValues(
      T collection,
      String collectionName
  ) {
    requireNonNull(collection, collectionName);

    var badElements = Stream.<String>builder();

    var index = 0;
    for (@Nullable Object element : collection) {
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

    return collection;
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
  @SuppressWarnings("NullableProblems")
  public static <T> T[] requireNonNullValues(
      T[] array,
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

    return array;
  }
}
