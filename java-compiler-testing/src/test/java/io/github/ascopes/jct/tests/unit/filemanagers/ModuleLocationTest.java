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
package io.github.ascopes.jct.tests.unit.filemanagers;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someModuleName;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import java.util.Objects;
import java.util.stream.Stream;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link ModuleLocation} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ModuleLocation tests")
class ModuleLocationTest {

  @DisplayName("Passing a null parent to the constructor raises an exception")
  @SuppressWarnings("DataFlowIssue")
  @Test
  void passingNullParentToConstructorRaisesException() {
    // Then
    assertThatThrownBy(() -> new ModuleLocation(null, "something"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("parent");
  }

  @DisplayName("Passing a null module name to the constructor raises an exception")
  @SuppressWarnings("DataFlowIssue")
  @Test
  void passingNullNameToConstructorRaisesException() {
    // Then
    assertThatThrownBy(() -> new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("moduleName");
  }

  @DisplayName("Passing an input package-oriented location to the constructor raises an exception")
  @MethodSource("packageOrientedInputLocations")
  @ParameterizedTest(name = "for StandardLocation.{0}")
  void passingInputPackageOrientedLocationToConstructorRaisesException(StandardLocation location) {
    // Then
    assertThatThrownBy(() -> new ModuleLocation(location, "foo.bar"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "The parent of a module location must be either an output location or be "
                + "module-oriented, but got %s",
            location.getName()
        );
  }

  @DisplayName(
      "Passing a module-oriented or output location to the constructor does not raise an exception"
  )
  @MethodSource({"moduleOrientedLocations", "outputLocations"})
  @ParameterizedTest(name = "for StandardLocation.{0}")
  void passingModuleOrientedOrOutputLocationsToConstructorSucceeds(StandardLocation location) {
    // Then
    assertThatCode(() -> new ModuleLocation(location, "foo.bar"))
        .doesNotThrowAnyException();
  }

  @DisplayName(".getParent() returns the parent location")
  @Test
  void getParentReturnsTheParentLocation() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.getParent())
        .isSameAs(parent);
  }

  @DisplayName(".getModuleName() returns the module name")
  @Test
  void getModuleNameReturnsTheModuleName() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.getModuleName())
        .isEqualTo(moduleName);
  }

  @DisplayName(".getName() returns the friendly name identifier")
  @Test
  void getNameReturnsTheFriendlyNameIdentifier() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.getName())
        .isEqualTo("%s[%s]", parent.getName(), moduleName);
  }

  @DisplayName(".isOutputLocation() returns the expected value")
  @MethodSource({"moduleOrientedLocations", "outputLocations"})
  @ParameterizedTest(name = "for StandardLocation.{0}")
  void isOutputLocationReturnsTheExpectedValue(StandardLocation parent) {
    // Given
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.isOutputLocation())
        .isEqualTo(parent.isOutputLocation());
  }

  @DisplayName(".isModuleOrientedLocation() always returns false")
  @MethodSource({"moduleOrientedLocations", "outputLocations"})
  @ParameterizedTest(name = "for StandardLocation.{0}")
  void isModuleOrientedLocationAlwaysReturnsFalse(StandardLocation parent) {
    // Given
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.isModuleOrientedLocation())
        .isFalse();
  }

  @DisplayName(".equals(...) returns true for the same object")
  @Test
  void equalsReturnsTrueForSameObject() {
    // Given
    var moduleLocation = new ModuleLocation(someValidParentLocation(), someModuleName());

    // Then
    assertThat(moduleLocation).isEqualTo(moduleLocation);
  }

  @DisplayName(".equals(...) returns true for equal objects that are not the same instance")
  @Test
  void equalsReturnsTrueForEqualButNonSameObjects() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();

    var moduleLocation1 = new ModuleLocation(parent, moduleName);
    var moduleLocation2 = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation1).isEqualTo(moduleLocation2);
    assertThat(moduleLocation2).isEqualTo(moduleLocation1);
  }

  @DisplayName(".equals(...) returns false for null values")
  @Test
  void equalsReturnsFalseForNullValues() {
    // Then
    assertThat(new ModuleLocation(someValidParentLocation(), someModuleName()))
        .isNotEqualTo(null);
  }

  @DisplayName(".equals(...) returns false for non-module location objects")
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  @Test
  void equalsReturnsFalseForNonModuleLocationObjects() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();

    // Then
    assertThat(new ModuleLocation(parent, moduleName))
        .isNotEqualTo(parent)
        .isNotEqualTo(moduleName)
        .isNotEqualTo("potato")
        .isNotEqualTo(new ArrayIndexOutOfBoundsException());
  }

  @DisplayName(".equals(...) returns false for different parents with the same module name")
  @Test
  void equalsReturnsFalseForDifferentParentsWithTheSameModuleName() {
    // Given
    var parent1 = someValidParentLocation();
    var parent2 = someValidParentLocation(parent1);
    var moduleName = someModuleName();

    var moduleLocation1 = new ModuleLocation(parent1, moduleName);
    var moduleLocation2 = new ModuleLocation(parent2, moduleName);

    // Then
    assertThat(moduleLocation1)
        .isNotEqualTo(moduleLocation2);
  }

  @DisplayName(".equals(...) returns false for shared parents with differing module names")
  @Test
  void equalsReturnsFalseForSharedParentsWithDifferentModuleNames() {
    // Given
    var parent = someValidParentLocation();
    var moduleName1 = "foo.bar";
    var moduleName2 = "baz.bork";

    var moduleLocation1 = new ModuleLocation(parent, moduleName1);
    var moduleLocation2 = new ModuleLocation(parent, moduleName2);

    // Then
    assertThat(moduleLocation1)
        .isNotEqualTo(moduleLocation2);
  }

  @DisplayName(".hashCode() returns the expected value")
  @Test
  void hashCodeReturnsTheExpectedValue() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.hashCode())
        .isEqualTo(Objects.hash(parent, moduleName));
  }

  @DisplayName(".toString() returns the expected value")
  @Test
  void toStringReturnsTheExpectedValue() {
    // Given
    var parent = someValidParentLocation();
    var moduleName = someModuleName();
    var moduleLocation = new ModuleLocation(parent, moduleName);

    // Then
    assertThat(moduleLocation.toString())
        .isEqualTo("ModuleLocation{parent=%s, moduleName=\"%s\"}", parent, moduleName);
  }

  static Stream<StandardLocation> packageOrientedInputLocations() {
    return Stream.of(StandardLocation.values())
        .filter(not(StandardLocation::isOutputLocation))
        .filter(not(StandardLocation::isModuleOrientedLocation));
  }

  static Stream<StandardLocation> moduleOrientedLocations() {
    return Stream.of(StandardLocation.values())
        .filter(not(StandardLocation::isOutputLocation))
        .filter(StandardLocation::isModuleOrientedLocation);
  }

  static Stream<StandardLocation> outputLocations() {
    return Stream.of(StandardLocation.values())
        .filter(StandardLocation::isOutputLocation);
  }

  static StandardLocation someValidParentLocation(StandardLocation... exclude) {
    return Stream
        .concat(moduleOrientedLocations(), outputLocations())
        .filter(location -> Stream.of(exclude).noneMatch(location::equals))
        .collect(collectingAndThen(toList(), Fixtures::oneOf));
  }
}
