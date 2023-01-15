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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.repr.LocationRepresentation;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;

/**
 * Assertions for an individual {@link Location location}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@NotThreadSafe
public final class LocationAssert extends AbstractAssert<LocationAssert, Location> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  public LocationAssert(@Nullable Location value) {
    super(value, LocationAssert.class);
    info.useRepresentation(LocationRepresentation.getInstance());
  }

  /**
   * Assert that the location is module-oriented.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null.
   */
  public LocationAssert isModuleOrientedLocation() {
    isNotNull();

    if (!actual.isModuleOrientedLocation()) {
      throw failure(
          "Expected location %s to be module-oriented but it was not", actual.getName()
      );
    }

    return this;
  }

  /**
   * Assert that the location is not module-oriented.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null.
   */
  public LocationAssert isNotModuleOrientedLocation() {
    isNotNull();

    if (actual.isModuleOrientedLocation()) {
      throw failure(
          "Expected location %s to not be module-oriented but it was", actual.getName()
      );
    }

    return this;
  }

  /**
   * Assert that the location is an output location.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null.
   */
  public LocationAssert isOutputLocation() {
    isNotNull();

    if (!actual.isOutputLocation()) {
      throw failure(
          "Expected location %s to be an output location but it was not", actual.getName()
      );
    }

    return this;
  }

  /**
   * Assert that the location is not an output location.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null.
   */
  public LocationAssert isNotOutputLocation() {
    isNotNull();

    if (actual.isOutputLocation()) {
      throw failure(
          "Expected location %s to not be an output location but it was", actual.getName()
      );
    }

    return this;
  }

  /**
   * Perform assertions on the name of the location.
   *
   * @return the string assertions to perform.
   * @throws AssertionError if the location is null.
   */
  @CheckReturnValue
  public AbstractStringAssert<?> name() {
    isNotNull();

    return assertThat(actual.getName());
  }
}
