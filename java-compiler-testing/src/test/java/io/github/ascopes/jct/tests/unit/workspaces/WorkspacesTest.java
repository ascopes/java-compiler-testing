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
package io.github.ascopes.jct.tests.unit.workspaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.Workspaces;
import io.github.ascopes.jct.workspaces.impl.WorkspaceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link Workspaces} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("Workspaces tests")
@SuppressWarnings("resource")
class WorkspacesTest {

  @DisplayName("newWorkspace() calls newWorkspacee(PathStrategy.defaultStrategy()")
  @Test
  void newWorkspaceCreatesWorkspaceWithDefaultStrategy() {
    // Given
    try (var staticMock = mockStatic(Workspaces.class)) {
      staticMock.when(Workspaces::newWorkspace).thenCallRealMethod();
      var expectedWorkspace = mock(Workspace.class);
      staticMock.when(() -> Workspaces.newWorkspace(any())).thenReturn(expectedWorkspace);

      // When
      var actualWorkspace = Workspaces.newWorkspace();

      // Then
      staticMock.verify(() -> Workspaces.newWorkspace(PathStrategy.defaultStrategy()));
      assertThat(actualWorkspace).isSameAs(expectedWorkspace);
    }
  }

  @DisplayName("newWorkspace(PathStrategy) returns a new WorkspaceImpl")
  @Test
  void newWorkspaceReturnsNewWorkspaceImpl() {
    // Given
    try (var workspaceImplMock = mockConstruction(WorkspaceImpl.class)) {
      var pathStrategy = mock(PathStrategy.class);

      // When
      var workspace = Workspaces.newWorkspace(pathStrategy);

      // Then
      assertThat(workspace)
          .isInstanceOf(WorkspaceImpl.class);

      assertThat(workspaceImplMock.constructed())
          .singleElement()
          // Nested assertion to swap expected/actual args.
          .satisfies(constructed -> assertThat(workspace).isSameAs(constructed));

    }
  }
}
