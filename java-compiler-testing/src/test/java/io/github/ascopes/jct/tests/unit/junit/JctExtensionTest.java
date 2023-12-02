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
package io.github.ascopes.jct.tests.unit.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.junit.JctExtension;
import io.github.ascopes.jct.junit.Managed;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Answers;

/**
 * {@link JctExtension} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctExtension tests")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("This modifies global state in some test cases")
class JctExtensionTest {

  ExtensionContext extensionContext;
  JctExtension extension;

  @BeforeEach
  void setUp() {
    extensionContext = mock();
    extension = new JctExtension();
  }

  @DisplayName("The beforeAll hook initialises annotated static workspace fields")
  @Test
  void beforeAllHookInitialisesAnnotatedStaticWorkspaceFields() {
    // Given
    try (var workspacesMock = mockStatic(Workspaces.class, Answers.RETURNS_MOCKS)) {
      StaticWorkspaceTestCase.staticWorkspace1 = null;
      StaticWorkspaceTestCase.staticWorkspace2 = null;
      StaticWorkspaceTestCase.staticWorkspace3 = null;
      StaticWorkspaceTestCase.someInvalidStaticWorkspace = null;
      StaticWorkspaceTestCase.someIgnoredStaticWorkspace = null;

      var expectedWorkspace1 = mock(Workspace.class);
      var expectedWorkspace2 = mock(Workspace.class);
      var expectedWorkspace3 = mock(Workspace.class);
      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .thenReturn(expectedWorkspace1, expectedWorkspace2, expectedWorkspace3);
      when(extensionContext.getRequiredTestClass()).thenAnswer(
          ctx -> StaticWorkspaceTestCase.class);

      // When
      assertThatCode(() -> extension.beforeAll(extensionContext))
          .doesNotThrowAnyException();

      // Then
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(2));
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES));
      workspacesMock.verifyNoMoreInteractions();

      assertThat(List.of(
          StaticWorkspaceTestCase.staticWorkspace1,
          StaticWorkspaceTestCase.staticWorkspace2,
          StaticWorkspaceTestCase.staticWorkspace3
      )).containsExactlyInAnyOrder(expectedWorkspace1, expectedWorkspace2, expectedWorkspace3);

      assertThat(StaticWorkspaceTestCase.someInvalidStaticWorkspace).isNull();
      assertThat(StaticWorkspaceTestCase.someIgnoredStaticWorkspace).isNull();
    }
  }

  @DisplayName("The afterAll hook closes annotated static workspace fields")
  @Test
  void afterAllHookClosesAnnotatedStaticWorkspaceFields() {
    // Given
    StaticWorkspaceTestCase.staticWorkspace1 = mock(Workspace.class);
    StaticWorkspaceTestCase.staticWorkspace2 = mock(Workspace.class);
    StaticWorkspaceTestCase.staticWorkspace3 = mock(Workspace.class);
    StaticWorkspaceTestCase.someInvalidStaticWorkspace = mock(Workspace.class);
    StaticWorkspaceTestCase.someIgnoredStaticWorkspace = mock();

    when(extensionContext.getRequiredTestClass()).thenAnswer(ctx -> StaticWorkspaceTestCase.class);

    // When
    assertThatCode(() -> extension.afterAll(extensionContext))
        .doesNotThrowAnyException();

    // Then
    verify(StaticWorkspaceTestCase.staticWorkspace1).close();
    verifyNoMoreInteractions(StaticWorkspaceTestCase.staticWorkspace1);
    verify(StaticWorkspaceTestCase.staticWorkspace2).close();
    verifyNoMoreInteractions(StaticWorkspaceTestCase.staticWorkspace2);
    verify(StaticWorkspaceTestCase.staticWorkspace3).close();
    verifyNoMoreInteractions(StaticWorkspaceTestCase.staticWorkspace3);
    verifyNoInteractions(StaticWorkspaceTestCase.someInvalidStaticWorkspace);
    verifyNoInteractions(StaticWorkspaceTestCase.someIgnoredStaticWorkspace);
  }

  @Disabled("This is just test data")
  static class StaticWorkspaceTestCase {

    @Managed
    static Workspace staticWorkspace1;

    @Managed(pathStrategy = PathStrategy.RAM_DIRECTORIES)
    static Workspace staticWorkspace2;

    @Managed(pathStrategy = PathStrategy.TEMP_DIRECTORIES)
    static Workspace staticWorkspace3;

    // These all get ignored because they don't match the typing/annotation criteria.
    static Workspace someIgnoredStaticWorkspace;

    @Managed
    static Object someInvalidStaticWorkspace;

    // These should be ignored because they are not static, so no instance exists to apply them on.
    @Managed
    Object someInvalidInstanceWorkspace;

    @Managed
    Workspace someIgnoredInstanceWorkspace;

    @Test
    void testSomething() {
      // ...
    }

    @Test
    void testSomethingAgain() {
      // ...
    }
  }

  @DisplayName("The beforeEach hook initialises annotated instance workspace fields")
  @Test
  void beforeEachHookInitialisesAnnotatedInstanceWorkspaceFields() {
    // Given
    try (var workspacesMock = mockStatic(Workspaces.class, Answers.RETURNS_MOCKS)) {
      InstanceWorkspaceTestCase.someIgnoredStaticWorkspace = null;
      InstanceWorkspaceTestCase.someInvalidStaticWorkspace = null;

      var instance1 = new InstanceWorkspaceTestCase();
      var instance2 = new InstanceWorkspaceTestCase();
      var instance3 = new InstanceWorkspaceTestCase();
      var testInstances = mock(TestInstances.class);
      when(testInstances.getAllInstances()).thenReturn(List.of(instance1, instance2, instance3));

      var expectedWorkspace1 = mock(Workspace.class);
      var expectedWorkspace2 = mock(Workspace.class);
      var expectedWorkspace3 = mock(Workspace.class);
      var expectedWorkspace4 = mock(Workspace.class);
      var expectedWorkspace5 = mock(Workspace.class);
      var expectedWorkspace6 = mock(Workspace.class);
      var expectedWorkspace7 = mock(Workspace.class);
      var expectedWorkspace8 = mock(Workspace.class);
      var expectedWorkspace9 = mock(Workspace.class);
      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .thenReturn(
              expectedWorkspace1, expectedWorkspace2, expectedWorkspace3, expectedWorkspace4,
              expectedWorkspace5, expectedWorkspace6, expectedWorkspace7, expectedWorkspace8,
              expectedWorkspace9
          );
      when(extensionContext.getRequiredTestInstances())
          .thenReturn(testInstances);

      // When
      assertThatCode(() -> extension.beforeEach(extensionContext))
          .doesNotThrowAnyException();

      // Then
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.RAM_DIRECTORIES), times(6));
      workspacesMock.verify(() -> Workspaces.newWorkspace(PathStrategy.TEMP_DIRECTORIES), times(3));
      workspacesMock.verifyNoMoreInteractions();

      assertThat(List.of(
          instance1.workspace1,
          instance1.workspace2,
          instance1.workspace3
      )).containsExactlyInAnyOrder(expectedWorkspace1, expectedWorkspace2, expectedWorkspace3);

      assertThat(List.of(
          instance2.workspace1,
          instance2.workspace2,
          instance2.workspace3
      )).containsExactlyInAnyOrder(expectedWorkspace4, expectedWorkspace5, expectedWorkspace6);

      assertThat(List.of(
          instance3.workspace1,
          instance3.workspace2,
          instance3.workspace3
      )).containsExactlyInAnyOrder(expectedWorkspace7, expectedWorkspace8, expectedWorkspace9);

      assertThat(InstanceWorkspaceTestCase.someIgnoredStaticWorkspace).isNull();
      assertThat(InstanceWorkspaceTestCase.someInvalidStaticWorkspace).isNull();
    }
  }

  @DisplayName("The afterEach hook closes annotated instance workspace fields")
  @Test
  void afterEachHookClosesAnnotatedInstanceWorkspaceFields() {
    // Given
    InstanceWorkspaceTestCase.someIgnoredStaticWorkspace = mock();
    InstanceWorkspaceTestCase.someInvalidStaticWorkspace = mock();
    var instance1 = new InstanceWorkspaceTestCase();
    var instance2 = new InstanceWorkspaceTestCase();
    var instance3 = new InstanceWorkspaceTestCase();
    var testInstances = mock(TestInstances.class);
    when(testInstances.getAllInstances()).thenReturn(List.of(instance1, instance2, instance3));

    instance1.workspace1 = mock();
    instance1.workspace2 = mock();
    instance1.workspace3 = mock();
    instance1.someIgnoredInstanceWorkspace = mock();
    instance1.someInvalidInstanceWorkspace = mock();
    instance2.workspace1 = mock();
    instance2.workspace2 = mock();
    instance2.workspace3 = mock();
    instance2.someIgnoredInstanceWorkspace = mock();
    instance2.someInvalidInstanceWorkspace = mock();
    instance3.workspace1 = mock();
    instance3.workspace2 = mock();
    instance3.workspace3 = mock();
    instance3.someIgnoredInstanceWorkspace = mock();
    instance3.someInvalidInstanceWorkspace = mock();

    when(extensionContext.getRequiredTestInstances())
        .thenReturn(testInstances);

    // When
    assertThatCode(() -> extension.afterEach(extensionContext))
        .doesNotThrowAnyException();

    // Then
    verify(instance1.workspace1).close();
    verifyNoMoreInteractions(instance1.workspace1);
    verify(instance1.workspace2).close();
    verifyNoMoreInteractions(instance1.workspace2);
    verify(instance1.workspace3).close();
    verifyNoMoreInteractions(instance1.workspace3);
    verify(instance2.workspace1).close();
    verifyNoMoreInteractions(instance2.workspace1);
    verify(instance2.workspace2).close();
    verifyNoMoreInteractions(instance2.workspace2);
    verify(instance2.workspace3).close();
    verifyNoMoreInteractions(instance2.workspace3);
    verify(instance3.workspace1).close();
    verifyNoMoreInteractions(instance3.workspace1);
    verify(instance3.workspace2).close();
    verifyNoMoreInteractions(instance3.workspace2);
    verify(instance3.workspace3).close();
    verifyNoMoreInteractions(instance3.workspace3);

    verifyNoInteractions(instance1.someIgnoredInstanceWorkspace);
    verifyNoInteractions(instance1.someInvalidInstanceWorkspace);
    verifyNoInteractions(instance2.someIgnoredInstanceWorkspace);
    verifyNoInteractions(instance2.someInvalidInstanceWorkspace);
    verifyNoInteractions(instance3.someIgnoredInstanceWorkspace);
    verifyNoInteractions(instance3.someInvalidInstanceWorkspace);
    verifyNoInteractions(InstanceWorkspaceTestCase.someIgnoredStaticWorkspace);
    verifyNoInteractions(InstanceWorkspaceTestCase.someInvalidStaticWorkspace);
    verifyNoInteractions(InstanceWorkspaceTestCase.someIgnoredStaticWorkspace);
  }

  @Disabled(value = "This is just test data")
  static class InstanceWorkspaceTestCase {

    @Managed
    Workspace workspace1;

    @Managed(pathStrategy = PathStrategy.RAM_DIRECTORIES)
    Workspace workspace2;

    @Managed(pathStrategy = PathStrategy.TEMP_DIRECTORIES)
    Workspace workspace3;

    // These all get ignored because they don't match the typing/annotation criteria.
    Workspace someIgnoredInstanceWorkspace;

    @Managed
    Object someInvalidInstanceWorkspace;

    // These should be ignored because they are static.
    @Managed
    static Object someInvalidStaticWorkspace;

    @Managed
    static Workspace someIgnoredStaticWorkspace;

    @Test
    void testSomething() {
      // ...
    }

    @Test
    void testSomethingAgain() {
      // ...
    }
  }

  @DisplayName("The beforeEach hook will initialise workspaces in any superclasses")
  @Test
  void beforeEachHookWillInitialiseWorkspacesInAnySuperClasses() {
    // Given
    try (var workspacesMock = mockStatic(Workspaces.class, Answers.RETURNS_MOCKS)) {
      var instance = new TestCaseImpl();
      var testInstances = mock(TestInstances.class);
      when(testInstances.getAllInstances()).thenReturn(List.of(instance));
      when(extensionContext.getRequiredTestInstances()).thenReturn(testInstances);

      var expectedWorkspace1 = mock(Workspace.class);
      var expectedWorkspace2 = mock(Workspace.class);
      var expectedWorkspace3 = mock(Workspace.class);
      var expectedWorkspace4 = mock(Workspace.class);

      workspacesMock.when(() -> Workspaces.newWorkspace(any()))
          .thenReturn(expectedWorkspace1, expectedWorkspace2, expectedWorkspace3,
              expectedWorkspace4);

      // When
      assertThatCode(() -> extension.beforeEach(extensionContext))
          .doesNotThrowAnyException();

      // Then
      assertThat(instance.testCaseImplWorkspace).isSameAs(expectedWorkspace1);
      assertThat(instance.testCaseBase3Workspace).isSameAs(expectedWorkspace2);
      assertThat(instance.testCaseBase2Workspace).isSameAs(expectedWorkspace3);
      assertThat(instance.testCaseBase1Workspace).isSameAs(expectedWorkspace4);
    }
  }

  @DisplayName("The afterEach hook will close workspaces in any superclasses")
  @Test
  void afterEachHookWillCloseWorkspacesInAnySuperClasses() {
    // Given
    var instance = new TestCaseImpl();
    var testInstances = mock(TestInstances.class);
    when(testInstances.getAllInstances()).thenReturn(List.of(instance));
    when(extensionContext.getRequiredTestInstances()).thenReturn(testInstances);

    instance.testCaseImplWorkspace = mock();
    instance.testCaseBase3Workspace = mock();
    instance.testCaseBase2Workspace = mock();
    instance.testCaseBase1Workspace = mock();

    // When
    assertThatCode(() -> extension.afterEach(extensionContext))
        .doesNotThrowAnyException();

    // Then
    verify(instance.testCaseImplWorkspace).close();
    verifyNoMoreInteractions(instance.testCaseImplWorkspace);
    verify(instance.testCaseBase1Workspace).close();
    verifyNoMoreInteractions(instance.testCaseBase1Workspace);
    verify(instance.testCaseBase2Workspace).close();
    verifyNoMoreInteractions(instance.testCaseBase2Workspace);
    verify(instance.testCaseBase3Workspace).close();
    verifyNoMoreInteractions(instance.testCaseBase3Workspace);
  }

  static class TestCaseBase1 {

    @Managed
    Workspace testCaseBase1Workspace;
  }

  static class TestCaseBase2 extends TestCaseBase1 {

    @Managed
    Workspace testCaseBase2Workspace;
  }

  static class TestCaseBase3 extends TestCaseBase2 {

    @Managed
    Workspace testCaseBase3Workspace;
  }

  static class TestCaseImpl extends TestCaseBase3 {

    @Managed
    Workspace testCaseImplWorkspace;
  }
}
