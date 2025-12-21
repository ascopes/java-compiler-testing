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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import java.util.Map;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link ModuleContainerGroupAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ModuleContainerGroupAssert tests")
class ModuleContainerGroupAssertTest {

  @DisplayName("ModuleContainerGroupAssert.moduleExists(...) tests")
  @Nested
  class ModuleExistsTest {

    @DisplayName(".moduleExists(...) fails if the module name is null")
    @Test
    void moduleExistsFailsIfModuleNameIsNull() {
      // Given
      var assertions = new ModuleContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.moduleExists(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("module");
    }

    @DisplayName(".moduleExists(...) fails if the container group is null")
    @Test
    void moduleExistsFailsIfContainerGroupIsNull() {
      // Given
      var assertions = new ModuleContainerGroupAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.moduleExists("something"))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".moduleExists(...) fails with fuzzy suggestions when the module does not exist")
    @Test
    void moduleExistsFailsWithFuzzySuggestionsWhenTheModuleDoesNotExist() {
      // Given
      var moduleContainerGroup = mock(ModuleContainerGroup.class);
      when(moduleContainerGroup.getModule(any())).thenReturn(null);
      when(moduleContainerGroup.getModules()).thenReturn(Map.of(
          module("foo.baz"), mock(),
          module("foo.bork"), mock(),
          module("org.example"), mock()
      ));

      var assertions = new ModuleContainerGroupAssert(moduleContainerGroup);

      // Then
      assertThatThrownBy(() -> assertions.moduleExists("foo.bar"))
          .message()
          .isEqualTo(String.join(
              "\n",
              "No module matching foo.bar was found. Maybe you meant:",
              "  - foo.baz",
              "  - foo.bork"
          ));

      verify(moduleContainerGroup).getModule("foo.bar");
      verify(moduleContainerGroup).getModules();
      verifyNoMoreInteractions(moduleContainerGroup);
    }

    @DisplayName(".moduleExists(...) returns assertions on the module when it exists")
    @Test
    void moduleExistsReturnsAssertionsOnTheModuleWhenItExists() {
      // Given
      var moduleContainerGroup = mock(ModuleContainerGroup.class);
      var packageContainerGroup = mock(PackageContainerGroup.class);
      when(moduleContainerGroup.getModule(any())).thenReturn(packageContainerGroup);

      var assertions = new ModuleContainerGroupAssert(moduleContainerGroup);

      // Then
      assertThatCode(() -> assertions.moduleExists("foo.bar").isSameAs(packageContainerGroup))
          .doesNotThrowAnyException();

      verify(moduleContainerGroup).getModule("foo.bar");
      verifyNoMoreInteractions(moduleContainerGroup);
    }

    private ModuleLocation module(String name) {
      return new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, name);
    }
  }

  @DisplayName("ModuleContainerGroupAssert.moduleDoesNotExist(...) tests")
  @Nested
  class ModuleDoesNotExistTest {

    @DisplayName(".moduleDoesNotExist(...) fails if the module name is null")
    @Test
    void moduleDoesNotExistFailsIfModuleNameIsNull() {
      // Given
      var assertions = new ModuleContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.moduleDoesNotExist(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("module");
    }

    @DisplayName(".moduleDoesNotExist(...) fails if the container group is null")
    @Test
    void moduleDoesNotExistFailsIfContainerGroupIsNull() {
      // Given
      var assertions = new ModuleContainerGroupAssert(null);

      // Then
      assertThatThrownBy(() -> assertions.moduleDoesNotExist("something"))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".moduleDoesNotExist(...) fails if the module exists")
    @Test
    void moduleDoesNotExistFailsIfModuleExists() {
      // Given
      var moduleContainerGroup = mock(ModuleContainerGroup.class);
      when(moduleContainerGroup.getModule(any())).thenReturn(mock());
      var assertions = new ModuleContainerGroupAssert(moduleContainerGroup);

      // Then
      assertThatThrownBy(() -> assertions.moduleDoesNotExist("foo.bar"))
          .isInstanceOf(AssertionError.class)
          .hasMessage("Found unexpected module foo.bar");
    }

    @DisplayName(".moduleDoesNotExist(...) succeeds if the module does not exist")
    @Test
    void moduleDoesNotExistSucceedsIfTheModuleDoesNotExist() {
      // Given
      var moduleContainerGroup = mock(ModuleContainerGroup.class);
      when(moduleContainerGroup.getModule(any())).thenReturn(null);
      var assertions = new ModuleContainerGroupAssert(moduleContainerGroup);

      // Then
      assertThatCode(() -> assertions.moduleDoesNotExist("foo.bar").isSameAs(moduleContainerGroup))
          .doesNotThrowAnyException();
    }
  }
}
