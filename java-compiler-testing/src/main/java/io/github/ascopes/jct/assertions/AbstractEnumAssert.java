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
package io.github.ascopes.jct.assertions;

import static io.github.ascopes.jct.utils.IterableUtils.requireAtLeastOne;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.assertj.core.api.AbstractAssert;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for an assertion on an {@link Enum}.
 *
 * @param <A> the implementation type.
 * @param <E> the enum type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AbstractEnumAssert<A extends AbstractEnumAssert<A, E>, E extends Enum<E>>
    extends AbstractAssert<A, E> {

  /**
   * Initialize this enum assertion.
   *
   * @param value    the value to assert upon.
   * @param selfType the type of this assertion implementation.
   */
  @SuppressWarnings("DataFlowIssue")
  protected AbstractEnumAssert(@Nullable E value, Class<?> selfType) {
    super(value, selfType);
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param elements the elements to check for.
   * @return this assertion object.
   * @throws NullPointerException     if any of the elements to test against are null.
   * @throws IllegalArgumentException if the elements array is empty.
   * @throws AssertionError           if the actual value is null, or if the value is not in the
   *                                  given group of acceptable values.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final A isAnyOf(E... elements) {
    requireNonNullValues(elements, "elements");
    requireAtLeastOne(elements, "elements");
    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .containsAnyOf(elements);

    return myself;
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param elements the elements to check for.
   * @return this assertion object.
   * @throws NullPointerException     if any of the elements to test against are null.
   * @throws IllegalArgumentException if the elements collection is empty.
   * @throws AssertionError           if the actual value is null, or if the value is not in the
   *                                  given iterable of acceptable values.
   */
  public final A isAnyOfElements(Collection<E> elements) {
    requireNonNullValues(elements, "elements");
    requireAtLeastOne(elements, "elements");
    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .containsAnyElementsOf(elements);

    return myself;
  }

  /**
   * Assert that the value is none of the given values.
   *
   * @param elements any elements to check for.
   * @return this assertion object.
   * @throws NullPointerException     if any of the elements to test against are null.
   * @throws IllegalArgumentException if the elements array is empty.
   * @throws AssertionError           if the actual value is null, or if the value is in the given
   *                                  group of acceptable values.
   */
  @SafeVarargs
  public final A isNoneOf(E... elements) {
    requireNonNullValues(elements, "elements");
    requireAtLeastOne(elements, "elements");
    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .doesNotContain(elements);

    return myself;
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param elements the elements to check for.
   * @return this assertion object.
   * @throws NullPointerException     if any of the elements to test against are null.
   * @throws IllegalArgumentException if the elements collection is empty.
   * @throws AssertionError           if the actual value is null, or if the value is in the given
   *                                  iterable of acceptable values.
   */
  public final A isNoneOfElements(Collection<E> elements) {
    requireNonNullValues(elements, "elements");
    requireAtLeastOne(elements, "elements");
    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .doesNotContainAnyElementsOf(elements);

    return myself;
  }

  private String description() {
    return String.format("%s enum value <%s>", actual.getClass().getSimpleName(), actual);
  }
}
