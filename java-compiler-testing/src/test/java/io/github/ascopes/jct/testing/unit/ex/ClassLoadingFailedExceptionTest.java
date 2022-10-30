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

import io.github.ascopes.jct.ex.ClassLoadingFailedException;
import javax.tools.JavaFileManager.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ClassLoadingFailedException}.
 *
 * @author Ashley Scopes
 */
@DisplayName("ClassLoadingFailedException tests")
@SuppressWarnings("ThrowableNotThrown")
class ClassLoadingFailedExceptionTest {

  @DisplayName("Null binary names are not allowed")
  @Test
  void nullBinaryNamesAreNotAllowed() {
    // Given
    String name = null;
    var location = stub(Location.class);
    var cause = stub(Throwable.class);

    when(location.getName()).thenReturn("something");

    // Then
    assertThatThrownBy(() -> new ClassLoadingFailedException(name, location, cause))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("binaryName");
  }

  @DisplayName("Null locations are not allowed")
  @Test
  void nullLocationsAreNotAllowed() {
    // Given
    var name = "something";
    Location location = null;
    var cause = stub(Throwable.class);

    // Then
    assertThatThrownBy(() -> new ClassLoadingFailedException(name, location, cause))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("location");
  }

  @DisplayName("Null causes are not allowed")
  @Test
  void nullCausesAreNotAllowed() {
    // Given
    var name = "something";
    var location = stub(Location.class);
    Throwable cause = null;

    when(location.getName()).thenReturn("something else");

    // Then
    assertThatThrownBy(() -> new ClassLoadingFailedException(name, location, cause))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("cause");
  }

  @DisplayName("The binary name is set")
  @Test
  void binaryNameIsSet() {
    // Given
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    Throwable cause = stub(Throwable.class);

    when(location.getName()).thenReturn("some location");

    // When
    var ex = new ClassLoadingFailedException(name, location, cause);

    // Then
    assertThat(ex.getBinaryName())
        .isEqualTo(name);
  }


  @DisplayName("The location is set")
  @Test
  void locationIsSet() {
    // Given
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    Throwable cause = stub(Throwable.class);

    when(location.getName()).thenReturn("some location");

    // When
    var ex = new ClassLoadingFailedException(name, location, cause);

    // Then
    assertThat(ex.getLocation())
        .isSameAs(location);
  }

  @DisplayName("The cause is set")
  @Test
  void causeIsSet() {
    // Given
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    Throwable cause = stub(Throwable.class);

    when(location.getName()).thenReturn("some location");

    // When
    var ex = new ClassLoadingFailedException(name, location, cause);

    // Then
    assertThat(ex.getCause())
        .isSameAs(cause);
  }

  @DisplayName("The message is the expected value")
  @Test
  void messageIsExpectedValue() {
    // Given
    var name = "foo.bar.Baz";
    var location = stub(Location.class);
    Throwable cause = stub(Throwable.class);

    when(location.getName()).thenReturn("some \"location\" in a place");
    when(cause.getMessage()).thenReturn("something something darkside");

    // When
    var ex = new ClassLoadingFailedException(name, location, cause);

    // Then
    assertThat(ex)
        .hasMessage(
            "Class %s failed to load from location %s: %s",
            "\"foo.bar.Baz\"",
            "\"some \\\"location\\\" in a place\"",
            "something something darkside"
        );
  }

}
