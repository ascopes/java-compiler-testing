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

import io.github.ascopes.jct.assertions.repr.LocationRepresentation;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.StringAssert;

/**
 * Assertions for an individual {@link Location location}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class LocationAssert extends AbstractAssert<LocationAssert, Location> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public LocationAssert(Location value) {
    super(value, LocationAssert.class);
    withRepresentation(LocationRepresentation.getInstance());
  }

  /**
   * Assert that the location is module-oriented.
   *
   * @return this assertion object for further call chaining.
   */
  public LocationAssert isModuleOrientedLocation() {
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
   */
  public LocationAssert isNotModuleOrientedLocation() {
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
   */
  public LocationAssert isOutputLocation() {
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
   */
  public LocationAssert isNotOutputLocation() {
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
   */
  public StringAssert name() {
    return new StringAssert(actual.getName());
  }
}
