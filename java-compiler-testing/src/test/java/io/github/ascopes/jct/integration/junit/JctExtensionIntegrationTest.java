/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.integration.junit;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.junit.Managed;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Integration tests for {@link JctExtension}.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctExtension integration tests")
class JctExtensionIntegrationTest {

  @DisplayName("Static workspaces are initialized and closed for all tests")
  @Test
  void staticWorkspacesAreInitializedAndClosedOnceForAllTests() {
    var workspace = mock(Workspace.class);

    try (var workspacesMock = mockStatic(Workspaces.class)) {
      workspacesMock.when(() -> Workspaces.newWorkspace(any())).thenReturn(workspace);

      var results = testKit()
          .selectors(selectClass(StaticLifecycleTestCase.class))
          .execute();

      assertThat(results.testEvents().succeeded().count()).isEqualTo(7);
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES));
      verify(workspace).close();
      workspacesMock.verifyNoMoreInteractions();
      verifyNoMoreInteractions(workspace);
    }
  }

  @Disabled("This is just test data")
  @ExtendWith(JctExtension.class)
  static class StaticLifecycleTestCase {

    @Managed
    static Workspace workspace;

    @Test
    void testWorkspaceIsInitialised() {
      assertThat(workspace).isNotNull();
    }

    @Test
    void testWorkspaceIsInitialisedAgain() {
      assertThat(workspace).isNotNull();
    }

    @RepeatedTest(5)
    void testWorkspaceIsInitialisedRepeatedly() {
      assertThat(workspace).isNotNull();
    }
  }

  @DisplayName("Instance workspaces are initialized and closed for each test case")
  @Test
  void instanceWorkspacesAreInitializedAndClosedForEachTest() {
    var workspaces = new ArrayList<Workspace>();

    try (var workspacesMock = mockStatic(Workspaces.class)) {
      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .then(ctx -> {
            var workspace = mock(Workspace.class);
            workspaces.add(workspace);
            return workspace;
          });

      var results = testKit()
          .selectors(selectClass(InstanceLifecycleTestCase.class))
          .execute();

      assertThat(results.testEvents().succeeded().count()).isEqualTo(7);
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(21));
      workspacesMock.verifyNoMoreInteractions();

      assertThat(workspaces)
          .hasSize(21)
          .allSatisfy(ws -> verify(ws).close())
          .allSatisfy(ws -> verifyNoMoreInteractions(ws));
    }
  }

  @Disabled("This is just test data")
  @ExtendWith(JctExtension.class)
  static class InstanceLifecycleTestCase {

    @Managed
    Workspace workspace1;

    @Managed
    Workspace workspace2;

    @Managed
    Workspace workspace3;

    @Test
    void testWorkspaceIsInitialised() {
      assertThat(workspace1).isNotNull();
      assertThat(workspace2).isNotNull();
      assertThat(workspace3).isNotNull();
    }

    @Test
    void testWorkspaceIsInitialisedAgain() {
      assertThat(workspace1).isNotNull();
      assertThat(workspace2).isNotNull();
      assertThat(workspace3).isNotNull();
    }

    @RepeatedTest(5)
    void testWorkspaceIsInitialisedRepeatedly() {
      assertThat(workspace1).isNotNull();
      assertThat(workspace2).isNotNull();
      assertThat(workspace3).isNotNull();
    }
  }

  @DisplayName("Instance workspaces are initialized and closed for each parameterized test case")
  @Test
  void instanceWorkspacesAreInitializedAndClosedForEachParameterizedTest() {
    var workspaces = new ArrayList<Workspace>();

    try (var workspacesMock = mockStatic(Workspaces.class)) {
      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .then(ctx -> {
            var workspace = mock(Workspace.class);
            workspaces.add(workspace);
            return workspace;
          });

      var results = testKit()
          .selectors(selectClass(ParameterizedLifecycleTestCase.class))
          .execute();

      assertThat(results.testEvents().succeeded().count()).isEqualTo(10);
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(10));
      workspacesMock.verifyNoMoreInteractions();

      assertThat(workspaces)
          .hasSize(10)
          .allSatisfy(ws -> verify(ws).close())
          .allSatisfy(ws -> verifyNoMoreInteractions(ws));
    }
  }

  @Disabled("This is just test data")
  @ExtendWith(JctExtension.class)
  static class ParameterizedLifecycleTestCase {

    @Managed
    Workspace workspace;

    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    @ParameterizedTest
    void testWorkspaceIsInitialised(int iteration) {
      assertThat(workspace)
          .as("workspace " + workspace + " for iteration " + iteration)
          .isNotNull();
    }
  }

  @DisplayName("Instance workspaces are initialized and closed once for test factories")
  @Test
  void instanceWorkspacesAreInitializedAndClosedOnceForTestFactories() {
    var workspaces = new ArrayList<Workspace>();

    try (var workspacesMock = mockStatic(Workspaces.class)) {
      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .then(ctx -> {
            var workspace = mock(Workspace.class);
            workspaces.add(workspace);
            return workspace;
          });

      var results = testKit()
          .selectors(selectClass(DynamicLifecycleTestCase.class))
          .execute();

      assertThat(results.testEvents().succeeded().count()).isEqualTo(10);
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(1));
      workspacesMock.verifyNoMoreInteractions();

      assertThat(workspaces)
          .hasSize(1)
          .allSatisfy(ws -> verify(ws).close())
          .allSatisfy(ws -> verifyNoMoreInteractions(ws));
    }
  }

  @Disabled("This is just test data")
  @ExtendWith(JctExtension.class)
  static class DynamicLifecycleTestCase {

    @Managed
    Workspace workspace;

    @TestFactory
    Stream<DynamicTest> testWorkspaceIsInitialised() {
      return IntStream
          .rangeClosed(1, 10)
          .mapToObj(iteration -> dynamicTest("for iteration " + iteration, () -> {
            assertThat(workspace)
                .as("workspace " + workspace + " for iteration " + iteration)
                .isNotNull();
          }));
    }
  }

  @DisplayName("Explicit path strategies for workspaces are handled")
  @Test
  void explicitPathStrategiesAreHandled() {
    try (var workspacesMock = mockStatic(Workspaces.class)) {
      workspacesMock.when(() -> Workspaces.newWorkspace(any())).thenCallRealMethod();

      var results = testKit()
          .selectors(selectClass(CustomPathStrategyTestCase.class))
          .execute();

      assertThat(results.testEvents().succeeded().count()).isEqualTo(1);
      workspacesMock
          .verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(2));
      workspacesMock
          .verify(() -> Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES), times(2));
      workspacesMock.verifyNoMoreInteractions();
    }
  }

  @Disabled("This is just test data")
  @ExtendWith(JctExtension.class)
  static class CustomPathStrategyTestCase {

    @Managed(pathStrategy = PathStrategy.RAM_DIRECTORIES)
    static Workspace staticRamDirectoriesWorkspace;

    @Managed(pathStrategy = PathStrategy.TEMP_DIRECTORIES)
    static Workspace staticTempDirectoriesWorkspace;

    @Managed(pathStrategy = PathStrategy.RAM_DIRECTORIES)
    Workspace ramDirectoriesWorkspace;

    @Managed(pathStrategy = PathStrategy.TEMP_DIRECTORIES)
    Workspace tempDirectoriesWorkspace;

    @Test
    void testWorkspaceIsInitialisedWithCorrectPathStrategy() {
      assertThat(staticRamDirectoriesWorkspace.getPathStrategy())
          .isSameAs(PathStrategy.RAM_DIRECTORIES);
      assertThat(staticTempDirectoriesWorkspace.getPathStrategy())
          .isSameAs(PathStrategy.TEMP_DIRECTORIES);
      assertThat(ramDirectoriesWorkspace.getPathStrategy())
          .isSameAs(PathStrategy.RAM_DIRECTORIES);
      assertThat(tempDirectoriesWorkspace.getPathStrategy())
          .isSameAs(PathStrategy.TEMP_DIRECTORIES);
    }
  }

  private static EngineTestKit.Builder testKit() {
    return EngineTestKit.engine("junit-jupiter")
        .configurationParameter("junit.jupiter.conditions.deactivate", "*DisabledCondition");
  }
}
