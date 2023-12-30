/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.tests.unit.assertions;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someBoolean;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.assertions.LocationAssert;
import javax.tools.JavaFileManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link LocationAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("LocationAssert tests")
class LocationAssertTest {

  @DisplayName("LocationAssert.isModuleOrientedLocation(...) tests")
  @Nested
  class IsModuleOrientedLocationTest {

    @DisplayName(".isModuleOrientedLocation() fails if the location is null")
    @Test
    void failsIfTheLocationIsNull() {
      // Given
      var locationAssert = new LocationAssert(null);

      // Then
      assertThatThrownBy(locationAssert::isModuleOrientedLocation)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isModuleOrientedLocation() succeeds if the location is module oriented")
    @Test
    void succeedsIfLocationIsModuleOriented() {
      // Given
      var location = new LocationImpl(someText(), someBoolean(), true);
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatCode(locationAssert::isModuleOrientedLocation)
          .doesNotThrowAnyException();
      assertThat(locationAssert.isModuleOrientedLocation())
          .withFailMessage("Expected the call to return 'this'")
          .isSameAs(locationAssert);
    }

    @DisplayName(".isModuleOrientedLocation() fails if the location is not module oriented")
    @Test
    void failsIfLocationIsNotModuleOriented() {
      // Given
      var location = new LocationImpl(someText(), someBoolean(), false);
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatThrownBy(locationAssert::isModuleOrientedLocation)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected location %s to be module-oriented but it was not",
              location.getName()
          );
    }
  }

  @DisplayName("LocationAssert.isNotModuleOrientedLocation(...) tests")
  @Nested
  class IsNotModuleOrientedLocationTest {

    @DisplayName(".isNotModuleOrientedLocation() fails if the location is null")
    @Test
    void failsIfTheLocationIsNull() {
      // Given
      var locationAssert = new LocationAssert(null);

      // Then
      assertThatThrownBy(locationAssert::isNotModuleOrientedLocation)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isNotModuleOrientedLocation() succeeds if the location is not module oriented")
    @Test
    void succeedsIfLocationIsNotModuleOriented() {
      // Given
      var location = new LocationImpl(someText(), someBoolean(), false);
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatCode(locationAssert::isNotModuleOrientedLocation)
          .doesNotThrowAnyException();

      assertThat(locationAssert.isNotModuleOrientedLocation())
          .withFailMessage("Expected the call to return 'this'")
          .isSameAs(locationAssert);
    }

    @DisplayName(".isNotModuleOrientedLocation() fails if the location is module oriented")
    @Test
    void failsIfLocationIsModuleOriented() {
      // Given
      var location = new LocationImpl(someText(), someBoolean(), true);
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatThrownBy(locationAssert::isNotModuleOrientedLocation)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected location %s to not be module-oriented but it was",
              location.getName()
          );
    }
  }

  @DisplayName("LocationAssert.isOutputLocation(...) tests")
  @Nested
  class IsOutputLocationTest {

    @DisplayName(".isOutputLocation() fails if the location is null")
    @Test
    void failsIfTheLocationIsNull() {
      // Given
      var locationAssert = new LocationAssert(null);

      // Then
      assertThatThrownBy(locationAssert::isOutputLocation)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isOutputLocation() succeeds if the location is an output location")
    @Test
    void succeedsIfLocationIsOutputLocation() {
      // Given
      var location = new LocationImpl(someText(), true, someBoolean());
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatCode(locationAssert::isOutputLocation)
          .doesNotThrowAnyException();

      assertThat(locationAssert.isOutputLocation())
          .withFailMessage("Expected the call to return 'this'")
          .isSameAs(locationAssert);
    }

    @DisplayName(".isOutputLocation() fails if the location is not an output location")
    @Test
    void failsIfLocationIsNotOutputLocation() {
      // Given
      var location = new LocationImpl(someText(), false, someBoolean());
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatThrownBy(locationAssert::isOutputLocation)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected location %s to be an output location but it was not",
              location.getName()
          );
    }
  }

  @DisplayName("LocationAssert.isNotOutputLocation(...) tests")
  @Nested
  class IsNotOutputLocationTest {

    @DisplayName(".isNotOutputLocation() fails if the location is null")
    @Test
    void failsIfTheLocationIsNull() {
      // Given
      var locationAssert = new LocationAssert(null);

      // Then
      assertThatThrownBy(locationAssert::isNotOutputLocation)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".isNotOutputLocation() succeeds if the location is not an output location")
    @Test
    void succeedsIfLocationIsNotOutputLocation() {
      // Given
      var location = new LocationImpl(someText(), false, someBoolean());
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatCode(locationAssert::isNotOutputLocation)
          .doesNotThrowAnyException();

      assertThat(locationAssert.isNotOutputLocation())
          .withFailMessage("Expected the call to return 'this'")
          .isSameAs(locationAssert);
    }

    @DisplayName(".isNotOutputLocation() fails if the location is an output location")
    @Test
    void failsIfLocationIsOutputLocation() {
      // Given
      var location = new LocationImpl(someText(), true, someBoolean());
      var locationAssert = new LocationAssert(location);

      // Then
      assertThatThrownBy(locationAssert::isNotOutputLocation)
          .isInstanceOf(AssertionError.class)
          .hasMessage(
              "Expected location %s to not be an output location but it was",
              location.getName()
          );
    }
  }

  @DisplayName("LocationAssert.name(...) tests")
  @Nested
  class NameTest {

    @DisplayName(".name() fails if the location is null")
    @Test
    void failsIfTheLocationIsNull() {
      // Given
      var locationAssert = new LocationAssert(null);

      // Then
      assertThatThrownBy(locationAssert::name)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".name() returns assertions on the name")
    @Test
    void returnsAssertionsOnTheName() {
      // Given
      var name = someText();
      var location = new LocationImpl(name, someBoolean(), someBoolean());
      var locationAssert = new LocationAssert(location);

      // Then
      locationAssert.name()
          .isNotNull()
          .isNotEmpty()
          .isNotBlank()
          .hasSameSizeAs(name)
          .isEqualTo(name);
    }
  }

  static final class LocationImpl implements JavaFileManager.Location {

    private final String name;
    private final boolean outputLocation;
    private final boolean moduleOrientedLocation;

    public LocationImpl(String name, boolean outputLocation, boolean moduleOrientedLocation) {
      this.name = name;
      this.outputLocation = outputLocation;
      this.moduleOrientedLocation = moduleOrientedLocation;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isOutputLocation() {
      return outputLocation;
    }

    @Override
    public boolean isModuleOrientedLocation() {
      return moduleOrientedLocation;
    }
  }
}
