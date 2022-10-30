/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.testing.unit.ex;

import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.ex.ClassMissingException;
import javax.tools.JavaFileManager.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ClassMissingException}.
 *
 * @author Ashley Scopes
 */
@DisplayName("ClassMissingException tests")
@SuppressWarnings("ThrowableNotThrown")
class ClassMissingExceptionTest {

  @DisplayName("Null binary names are not allowed")
  @Test
  void nullBinaryNamesAreNotAllowed() {
    // Given
    var name = "something";
    Location location = null;

    // Then
    assertThatThrownBy(() -> new ClassMissingException(name, location))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("location");
  }

  @DisplayName("Null locations are not allowed")
  @Test
  void nullLocationsAreNotAllowed() {
    // Given
    var name = "something";
    Location location = null;

    // Then
    assertThatThrownBy(() -> new ClassMissingException(name, location))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("location");
  }

  @DisplayName("The binary name is set")
  @Test
  void binaryNameIsSet() {
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    when(location.getName()).thenReturn("some \"location\" in a place");

    // When
    var ex = new ClassMissingException(name, location);

    // Then
    assertThat(ex.getBinaryName())
        .isEqualTo(name);
  }


  @DisplayName("The location is set")
  @Test
  void locationIsSet() {
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    when(location.getName()).thenReturn("some \"location\" in a place");

    // When
    var ex = new ClassMissingException(name, location);

    // Then
    assertThat(ex.getLocation())
        .isSameAs(location);
  }

  @DisplayName("The message is the expected value")
  @Test
  void messageIsExpectedValue() {
    // Given
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    when(location.getName()).thenReturn("some \"location\" in a place");

    // When
    var ex = new ClassMissingException(name, location);

    // Then
    assertThat(ex)
        .hasMessage(
            "Class %s was not found in location %s",
            "\"foo.bar.Baz\"",
            "\"some \\\"location\\\" in a place\""
        );
  }

}
