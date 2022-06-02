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

import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertFactory;


/**
 * An assertion on an optionally present value.
 *
 * @param <I> the inner assertion type to use.
 * @param <A> the inner value to assert on.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class OptionalAssert<I extends AbstractAssert<I, A>, A>
    extends AbstractAssert<OptionalAssert<I, A>, Optional<A>> {

  private final AssertFactory<A, I> assertFactory;

  /**
   * Initialize these assertions.
   *
   * @param actual the actual optional value to assert on.
   * @param assertFactory the assertion factory to use on the value.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public OptionalAssert(
      Optional<A> actual,
      AssertFactory<A, I> assertFactory
  ) {
    super(actual, OptionalAssert.class);
    this.assertFactory = assertFactory;
  }

  /**
   * Assert that a value is present.
   *
   * @return this assertion object.
   */
  public OptionalAssert<I, A> isPresent() {
    if (actual.isEmpty()) {
      throw failure("Expected a value to be present but it was not");
    }

    return this;
  }

  /**
   * Assert that a value is not present.
   */
  public void isEmpty() {
    if (actual.isPresent()) {
      throw failure("Expected no value to be present but got %s", actual);
    }
  }

  /**
   * Get the assertions for the value, asserting that it is present first.
   *
   * @return the assertions on the value.
   */
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public I get() {
    isPresent();
    return assertFactory.createAssert(actual.get());
  }
}
