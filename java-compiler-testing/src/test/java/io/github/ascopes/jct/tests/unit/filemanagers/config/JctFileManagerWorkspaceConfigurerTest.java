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
package io.github.ascopes.jct.tests.unit.filemanagers.config;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePathRoot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.filemanagers.config.JctFileManagerWorkspaceConfigurer;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.Workspace;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerWorkspaceConfigurer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerWorkspaceConfigurer tests")
@ExtendWith(MockitoExtension.class)
class JctFileManagerWorkspaceConfigurerTest {

  @Mock
  Workspace workspace;

  @Mock
  JctFileManagerImpl fileManager;

  @InjectMocks
  JctFileManagerWorkspaceConfigurer configurer;

  @DisplayName(".configure(...) will copy all workspace paths to the file manager")
  @Test
  void configureWillCopyAllWorkspacePathsToTheFileManager() {
    // Given
    var paths = Map.<Location, List<? extends PathRoot>>of(
        someLocation(), List.of(somePathRoot()),
        someLocation(), List.of(somePathRoot(), somePathRoot()),
        someLocation(), List.of(somePathRoot(), somePathRoot(), somePathRoot()),
        someLocation(), List.of(somePathRoot())
    );
    when(workspace.getAllPaths()).thenReturn(paths);

    // When
    configurer.configure(fileManager);

    // Then
    paths.forEach((location, roots) -> verify(fileManager).addPaths(location, roots));
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
