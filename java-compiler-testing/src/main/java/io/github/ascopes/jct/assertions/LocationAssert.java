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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.repr.LocationRepresentation;
import javax.tools.JavaFileManager.Location;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for an individual {@link Location location}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class LocationAssert extends AbstractAssert<LocationAssert, Location> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  @SuppressWarnings("DataFlowIssue")
  public LocationAssert(@Nullable Location value) {
    super(value, LocationAssert.class);
    info.useRepresentation(LocationRepresentation.getInstance());
  }

  /**
   * Assert that the location is {@link Location#isModuleOrientedLocation() module-oriented}.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null or if the location is not module-oriented.
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
   * Assert that the location is not {@link Location#isModuleOrientedLocation() module-oriented}.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null or if the location is module-oriented.
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
   * Assert that the location is an {@link Location#isOutputLocation() output location}.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null or is not an output location.
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
   * Assert that the location is not an {@link Location#isOutputLocation() output location}.
   *
   * @return this assertion object for further call chaining.
   * @throws AssertionError if the location is null or is an output location.
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
   * Perform assertions on the {@link Location#getName name} of the location.
   *
   * @return the string assertions to perform.
   * @throws AssertionError if the location is null.
   */
  public AbstractStringAssert<?> name() {
    isNotNull();

    return assertThat(actual.getName());
  }
}
