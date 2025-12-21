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
package io.github.ascopes.jct.filemanagers.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.Workspace;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerRequiredLocationsConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerRequiredLocationsConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerRequiredLocationsConfigurerTest {

  @Mock(answer = Answers.RETURNS_MOCKS, strictness = Strictness.LENIENT)
  Workspace workspace;

  @Mock
  JctFileManagerImpl fileManager;

  @InjectMocks
  JctFileManagerRequiredLocationsConfigurer configurer;

  @DisplayName(".configure(...) will ensure all required locations are present")
  @EnumSource(
      value = StandardLocation.class,
      names = {
          "SOURCE_OUTPUT",
          "CLASS_OUTPUT",
          "NATIVE_HEADER_OUTPUT",
      }
  )
  @ParameterizedTest(name = ".configure(...) will configure location {0}")
  void configureWillCreateAllRequiredLocations(StandardLocation location) {
    // Given
    var managedDirectory = mock(ManagedDirectory.class);
    when(workspace.createPackage(location)).thenReturn(managedDirectory);

    // When
    configurer.configure(fileManager);

    // Then
    verify(workspace).createPackage(location);
    verify(fileManager).addPath(location, managedDirectory);
  }

  @DisplayName(".configure(...) will not configure locations that already exist")
  @EnumSource(
      value = StandardLocation.class,
      names = {
          "SOURCE_OUTPUT",
          "CLASS_OUTPUT",
          "NATIVE_HEADER_OUTPUT",
      }
  )
  @ParameterizedTest(name = ".configure(...) will not configure existing location {0}")
  void configureWillNotConfigureExistingLocation(StandardLocation location) {
    // Given
    when(fileManager.hasLocation(any())).thenReturn(false);
    when(fileManager.hasLocation(location)).thenReturn(true);

    // When
    configurer.configure(fileManager);

    // Then
    verify(fileManager, never()).addPath(eq(location), any());
  }

  @DisplayName(".configure(...) returns the input file manager")
  @Test
  void configureReturnsTheInputFileManager() {
    // When
    var result = configurer.configure(fileManager);

    // Then
    assertThat(result).isSameAs(fileManager);
  }

  @DisplayName(".isEnabled() returns true")
  @Test
  void isEnabledReturnsTrue() {
    // Then
    assertThat(configurer.isEnabled()).isTrue();
  }
}
