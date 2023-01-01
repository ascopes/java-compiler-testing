/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
package io.github.ascopes.jct.tests.unit.repr;

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.repr.LocationRepresentation;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link LocationRepresentation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("LocationRepresentation tests")
class LocationRepresentationTest {

  @DisplayName("toStringOf(null) returns \"null\"")
  @Test
  void toStringOfNullReturnsNull() {
    // Given
    var repr = LocationRepresentation.getInstance();

    // When
    var result = repr.toStringOf(null);

    // Then
    assertThat(result).isEqualTo("null");
  }

  @DisplayName("toStringOf(Location) returns the location name")
  @Test
  void toStringOfLocationReturnsName() {
    // Given
    var repr = LocationRepresentation.getInstance();
    var location = oneOf(StandardLocation.values());

    // When
    var result = repr.toStringOf(location);

    // Then
    assertThat(result).isEqualTo(location.getName());
  }
}
