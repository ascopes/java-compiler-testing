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

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.IterableUtils;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;


/**
 * Abstract base class for an assertion on an {@link Enum}.
 *
 * @param <S> the implementation type.
 * @param <E> the enum type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractEnumAssert<S extends AbstractEnumAssert<S, E>, E extends Enum<E>>
    extends AbstractAssert<S, E> {

  private final String friendlyTypeName;

  /**
   * Initialize this enum assertion.
   *
   * @param value            the value to assert upon.
   * @param selfType         the type of this assertion implementation.
   * @param friendlyTypeName the friendly type name to use.
   */
  protected AbstractEnumAssert(
      E value,
      Class<?> selfType,
      String friendlyTypeName
  ) {
    super(value, selfType);
    this.friendlyTypeName = requireNonNull(friendlyTypeName, "friendlyTypeName");
  }

  /**
   * Assert that the value is one of the given values.
   *
   * @param first the first value to check for.
   * @param more  any additional values to check for.
   * @return this assertion object.
   */
  @SafeVarargs
  public final S isOneOf(E first, E... more) {
    requireNonNull(first, "first");
    requireNonNullValues(more, "more");

    var all = IterableUtils.asList(first, more);
    if (!all.contains(actual)) {
      var actualStr = reprName(actual);

      String expectedStr;
      if (all.size() > 1) {
        expectedStr = "one of " + all
            .stream()
            .map(this::reprName)
            .collect(Collectors.joining(", "));
      } else {
        expectedStr = reprName(first);
      }

      throw failureWithActualExpected(
          actualStr,
          expectedStr,
          "Expected %s to be %s, but it was %s",
          friendlyTypeName,
          expectedStr,
          actualStr
      );
    }

    return myself;
  }

  private String reprName(E e) {
    return e == null
        ? "null"
        : "<" + e.name() + ">";
  }
}
