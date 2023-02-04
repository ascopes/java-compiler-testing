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
package io.github.ascopes.jct.assertions;

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.utils.IterableUtils;
import java.util.List;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
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
@API(since = "0.0.1", status = Status.STABLE)
public abstract class AbstractEnumAssert<A extends AbstractEnumAssert<A, E>, E extends Enum<E>>
    extends AbstractAssert<A, E> {

  /**
   * Initialize this enum assertion.
   *
   * @param value    the value to assert upon.
   * @param selfType the type of this assertion implementation.
   */
  protected AbstractEnumAssert(@Nullable E value, Class<?> selfType) {
    super(value, selfType);
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param first the first value to check for.
   * @param more  any additional values to check for.
   * @return this assertion object.
   * @throws NullPointerException if any of the elements to test against are null.
   * @throws AssertionError       if the actual value is null, or if the value is not in the given
   *                              group of acceptable values.
   */
  @SafeVarargs
  public final A isAnyOf(E first, E... more) {
    requireNonNull(first, "first must not be null");
    requireNonNullValues(more, "more");

    return isAnyOfElements(IterableUtils.combineOneOrMore(first, more));
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param elements the elements to check for.
   * @return this assertion object.
   * @throws NullPointerException if any of the elements to test against are null.
   * @throws AssertionError       if the actual value is null, or if the value is not in the given
   *                              iterable of acceptable values.
   */
  public final A isAnyOfElements(Iterable<E> elements) {
    requireNonNullValues(elements, "elements");

    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .containsAnyElementsOf(elements);

    return myself;
  }

  /**
   * Assert that the value is none of the given values.
   *
   * @param first the first value to check for.
   * @param more  any additional values to check for.
   * @return this assertion object.
   * @throws NullPointerException if any of the elements to test against are null.
   * @throws AssertionError       if the actual value is null, or if the value is in the given group
   *                              of acceptable values.
   */
  @SafeVarargs
  public final A isNoneOf(E first, E... more) {
    requireNonNull(first, "first must not be null");
    requireNonNullValues(more, "more");

    return isNoneOfElements(IterableUtils.combineOneOrMore(first, more));
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param elements the elements to check for.
   * @return this assertion object.
   * @throws NullPointerException if any of the elements to test against are null.
   * @throws AssertionError       if the actual value is null, or if the value is in the given
   *                              iterable of acceptable values.
   */
  public final A isNoneOfElements(Iterable<E> elements) {
    requireNonNullValues(elements, "elements");

    isNotNull();

    assertThat(List.of(actual))
        .as(description())
        .doesNotContainAnyElementsOf(elements);

    return myself;
  }

  private Supplier<String> description() {
    return () -> String.format("%s enum value <%s>", actual.getClass().getSimpleName(), actual);
  }
}
