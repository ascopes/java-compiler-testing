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

import io.github.ascopes.jct.utils.Nullable;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertFactory;


/**
 * An assertion on an optionally present value.
 *
 * <p>Unlike {@link org.assertj.core.api.OptionalAssert}, this implementation has stronger
 * semantics and is designed to be used as the proxy to an optionally present result rather than
 * the target value to perform assertions on.
 *
 * @param <I> the inner assertion type to use.
 * @param <A> the inner value to assert on.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class MaybeAssert<I extends AbstractAssert<I, A>, A>
    extends AbstractAssert<MaybeAssert<I, A>, Optional<A>> {

  private final AssertFactory<A, I> assertFactory;

  /**
   * Initialize these assertions.
   *
   * @param actual the actual nullable value to assert on.
   * @param assertFactory the assertion factory to use on the value.
   */
  public MaybeAssert(@Nullable A actual, AssertFactory<A, I> assertFactory) {
    super(Optional.ofNullable(actual), MaybeAssert.class);
    this.assertFactory = assertFactory;
  }

  /**
   * Get the assertions for the value, asserting that it is present first.
   *
   * @return the assertions on the value.
   */
  public I exists() {
    if (actual.isEmpty()) {
      throw failure("Expected a value to be present but it was not");
    }
    return assertFactory.createAssert(actual.get());
  }

  /**
   * Assert that a value is not present.
   */
  public void doesNotExist() {
    if (actual.isPresent()) {
      throw failure("Expected no value to be present but got %s", actual);
    }
  }
}
