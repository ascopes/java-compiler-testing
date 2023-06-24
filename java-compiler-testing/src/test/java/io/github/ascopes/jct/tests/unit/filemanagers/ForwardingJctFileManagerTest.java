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

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someBinaryName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someBoolean;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someClassName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someFlag;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someInt;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someJavaFileObject;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someModuleName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someOf;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePackageName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePathRoot;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelativePath;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelease;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ForwardingJctFileManager;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ForwardingJctFileManager} tests.
 *
 * @author Ashley Scopes
 */
@ExtendWith(MockitoExtension.class)
class ForwardingJctFileManagerTest {

  @Mock
  JctFileManager fileManagerImpl;

  @InjectMocks
  ForwardingJctFileManagerImpl forwardingFileManager;

  //////////////////////////////////////////////////////////////////////////////////
  /// Implementations that are provided by ForwardingJavaFileManager in the JDK. ///
  //////////////////////////////////////////////////////////////////////////////////

  @DisplayName(".getClassLoader(...) calls the same method on the implementation")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when method returns null = {0}")
  void getClassLoaderCallsTheSameMethodOnTheImplementation(boolean returnNull) {
    // Given
    var expectedClassLoader = returnNull ? null : mock(ClassLoader.class);
    when(fileManagerImpl.getClassLoader(any())).thenReturn(expectedClassLoader);
    var expectedLocation = someLocation();

    // When
    var actualClassLoader = forwardingFileManager.getClassLoader(expectedLocation);

    // Then
    verify(fileManagerImpl).getClassLoader(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualClassLoader).isSameAs(expectedClassLoader);
  }

  @DisplayName(".inferBinaryName(...) calls the same method on the implementation")
  @Test
  void inferBinaryNameCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedBinaryName = someBinaryName();
    when(fileManagerImpl.inferBinaryName(any(), any())).thenReturn(expectedBinaryName);
    var expectedLocation = someLocation();
    var expectedJavaFileObject = someJavaFileObject();

    // When
    var actualBinaryName = forwardingFileManager
        .inferBinaryName(expectedLocation, expectedJavaFileObject);

    // Then
    verify(fileManagerImpl).inferBinaryName(expectedLocation, expectedJavaFileObject);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualBinaryName).isEqualTo(expectedBinaryName);
  }

  @DisplayName(".isSameFile(...) calls the same method on the implementation")
  @Test
  void isSameFileCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedResult = someBoolean();
    when(fileManagerImpl.isSameFile(any(), any())).thenReturn(expectedResult);
    var expectedFirstFileObject = someJavaFileObject();
    var expectedSecondFileObject = someJavaFileObject();

    // When
    var actualResult = forwardingFileManager
        .isSameFile(expectedFirstFileObject, expectedSecondFileObject);

    // Then
    verify(fileManagerImpl).isSameFile(expectedFirstFileObject, expectedSecondFileObject);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @DisplayName(".handleOption(...) calls the same method on the implementation")
  @Test
  void handleOptionCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedResult = someBoolean();
    when(fileManagerImpl.handleOption(any(), any())).thenReturn(expectedResult);
    var expectedCurrent = someFlag();
    Iterator<String> expectedRemaining = mock();

    // When
    var actualResult = forwardingFileManager.handleOption(expectedCurrent, expectedRemaining);

    // Then
    verify(fileManagerImpl).handleOption(expectedCurrent, expectedRemaining);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @DisplayName(".hasLocation(...) calls the same method on the implementation")
  @Test
  void hasLocationCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedResult = someBoolean();
    when(fileManagerImpl.hasLocation(any())).thenReturn(expectedResult);
    var expectedLocation = someLocation();

    // When
    var actualResult = forwardingFileManager.hasLocation(expectedLocation);

    // Then
    verify(fileManagerImpl).hasLocation(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @DisplayName(".isSupportedOption(...) calls the same method on the implementation")
  @Test
  void isSupportedOptionCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedResult = someInt(1, 5);
    when(fileManagerImpl.isSupportedOption(any())).thenReturn(expectedResult);
    var expectedOption = someFlag();

    // When
    var actualResult = forwardingFileManager.isSupportedOption(expectedOption);

    // Then
    verify(fileManagerImpl).isSupportedOption(expectedOption);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @DisplayName(".getJavaFileForInput(...) calls the same method on the implementation")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when method returns null = {0}")
  void getJavaFileForInputCallsTheSameMethodOnTheImplementation(boolean returnNull)
      throws IOException {

    // Given
    var expectedFile = returnNull ? null : someJavaFileObject();
    when(fileManagerImpl.getJavaFileForInput(any(), any(), any())).thenReturn(expectedFile);
    var expectedLocation = someLocation();
    var expectedClassName = someClassName();
    var expectedKind = oneOf(JavaFileObject.Kind.class);

    // When
    var actualFile = forwardingFileManager
        .getJavaFileForInput(expectedLocation, expectedClassName, expectedKind);

    // Then
    verify(fileManagerImpl).getJavaFileForInput(expectedLocation, expectedClassName, expectedKind);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualFile).isSameAs(expectedFile);
  }

  @DisplayName(".getJavaFileForOutput(...) calls the same method on the implementation")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when method returns null = {0}")
  void getJavaFileForOutputCallsTheSameMethodOnTheImplementation(boolean returnNull)
      throws IOException {

    // Given
    var expectedFile = returnNull ? null : someJavaFileObject();
    when(fileManagerImpl.getJavaFileForOutput(any(), any(), any(), any())).thenReturn(expectedFile);
    var expectedLocation = someLocation();
    var expectedClassName = someClassName();
    var expectedKind = oneOf(JavaFileObject.Kind.class);
    var expectedSibling = returnNull ? null : someJavaFileObject();

    // When
    var actualFile = forwardingFileManager
        .getJavaFileForOutput(expectedLocation, expectedClassName, expectedKind, expectedSibling);

    // Then
    verify(fileManagerImpl)
        .getJavaFileForOutput(expectedLocation, expectedClassName, expectedKind, expectedSibling);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualFile).isSameAs(expectedFile);
  }

  @DisplayName(".getFileForInput(...) calls the same method on the implementation")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when method returns null = {0}")
  void getFileForInputCallsTheSameMethodOnTheImplementation(boolean returnNull)
      throws IOException {

    // Given
    var expectedFile = returnNull ? null : someJavaFileObject();
    when(fileManagerImpl.getFileForInput(any(), any(), any())).thenReturn(expectedFile);
    var expectedLocation = someLocation();
    var expectedPackageName = someClassName();
    var expectedRelativeName = someRelativePath().toString();

    // When
    var actualFile = forwardingFileManager
        .getFileForInput(expectedLocation, expectedPackageName, expectedRelativeName);

    // Then
    verify(fileManagerImpl)
        .getFileForInput(expectedLocation, expectedPackageName, expectedRelativeName);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualFile).isSameAs(expectedFile);
  }

  @DisplayName(".getFileForOutput(...) calls the same method on the implementation")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "when method returns null = {0}")
  void getFileForOutputCallsTheSameMethodOnTheImplementation(boolean returnNull)
      throws IOException {

    // Given
    var expectedFile = returnNull ? null : someJavaFileObject();
    when(fileManagerImpl.getFileForOutput(any(), any(), any(), any())).thenReturn(expectedFile);
    var expectedLocation = someLocation();
    var expectedPackageName = someClassName();
    var expectedRelativeName = someRelativePath().toString();
    var expectedSibling = returnNull ? null : someJavaFileObject();

    // When
    var actualFile = forwardingFileManager.getFileForOutput(
        expectedLocation,
        expectedPackageName,
        expectedRelativeName,
        expectedSibling
    );

    // Then
    verify(fileManagerImpl).getFileForOutput(
        expectedLocation,
        expectedPackageName,
        expectedRelativeName,
        expectedSibling
    );
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualFile).isSameAs(expectedFile);
  }

  @DisplayName(".flush() calls the same method on the implementation")
  @Test
  void flushCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    doNothing().when(fileManagerImpl).flush();

    // When
    forwardingFileManager.flush();

    // Then
    verify(fileManagerImpl).flush();
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(".close() calls the same method on the implementation")
  @Test
  void closeCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    doNothing().when(fileManagerImpl).close();

    // When
    forwardingFileManager.close();

    // Then
    verify(fileManagerImpl).close();
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(
      ".getLocationForModule(Location, String) calls the same method on the implementation"
  )
  @Test
  void getLocationForModuleLocationStringCallsTheSameMethodOnTheImplementation()
      throws IOException {

    // Given
    var expectedLocation = someLocation();
    when(fileManagerImpl.getLocationForModule(any(), any(String.class)))
        .thenReturn(expectedLocation);
    var expectedInputLocation = someLocation();
    var expectedModuleName = someModuleName();

    // When
    var actualLocation = forwardingFileManager
        .getLocationForModule(expectedInputLocation, expectedModuleName);

    // Then
    verify(fileManagerImpl)
        .getLocationForModule(expectedInputLocation, expectedModuleName);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualLocation).isSameAs(expectedLocation);
  }

  @DisplayName(
      ".getLocationForModule(Location, JavaFileObject) calls the same method on the implementation"
  )
  @Test
  void getLocationForModuleLocationJavaFileObjectCallsTheSameMethodOnTheImplementation()
      throws IOException {

    // Given
    var expectedLocation = someLocation();
    when(fileManagerImpl.getLocationForModule(any(), any(JavaFileObject.class)))
        .thenReturn(expectedLocation);
    var expectedInputLocation = someLocation();
    var expectedJavaFileObject = someJavaFileObject();

    // When
    var actualLocation = forwardingFileManager
        .getLocationForModule(expectedInputLocation, expectedJavaFileObject);

    // Then
    verify(fileManagerImpl).getLocationForModule(expectedInputLocation, expectedJavaFileObject);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualLocation).isSameAs(expectedLocation);
  }

  @DisplayName(".getServiceLoader(...) calls the same method on the implementation")
  @Test
  void getServiceLoaderCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    class SomeClass {
      // Nothing to do here.
    }

    ServiceLoader<Object> expectedServiceLoader = mock();
    when(fileManagerImpl.getServiceLoader(any(), any())).thenReturn(expectedServiceLoader);
    var expectedLocation = someLocation();

    // When
    var actualServiceLoader = forwardingFileManager
        .getServiceLoader(expectedLocation, SomeClass.class);

    // Then
    verify(fileManagerImpl).getServiceLoader(expectedLocation, SomeClass.class);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualServiceLoader).isSameAs(expectedServiceLoader);
  }

  @DisplayName(".inferModuleName(...) calls the same method on the implementation")
  @Test
  void inferModuleNameCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    var expectedModuleName = someModuleName();
    when(fileManagerImpl.inferModuleName(any())).thenReturn(expectedModuleName);
    var expectedLocation = someLocation();

    // When
    var actualModuleName = forwardingFileManager.inferModuleName(expectedLocation);

    // Then
    verify(fileManagerImpl).inferModuleName(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualModuleName).isEqualTo(expectedModuleName);
  }

  @DisplayName(".listLocationsForModules(...) calls the same method on the implementation")
  @Test
  void listLocationsForModulesCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    Iterable<Set<JavaFileManager.Location>> expectedIterable = mock();
    when(fileManagerImpl.listLocationsForModules(any())).thenReturn(expectedIterable);
    var expectedLocation = someLocation();

    // When
    var actualIterable = forwardingFileManager.listLocationsForModules(expectedLocation);

    // Then
    verify(fileManagerImpl).listLocationsForModules(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualIterable).isEqualTo(expectedIterable);
  }

  @DisplayName(".contains(...) calls the same method on the implementation")
  @Test
  void containsCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    var expectedResult = someBoolean();
    when(fileManagerImpl.contains(any(), any())).thenReturn(expectedResult);
    var expectedLocation = someLocation();
    var expectedFileObject = someJavaFileObject();

    // When
    var actualResult = forwardingFileManager.contains(expectedLocation, expectedFileObject);

    // Then
    verify(fileManagerImpl).contains(expectedLocation, expectedFileObject);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  //////////////////////////////////////////////////////////////////////
  /// Implementations provided directly by ForwardingJctFileManager. ///
  //////////////////////////////////////////////////////////////////////

  @DisplayName(".addPath(...) calls the same method on the implementation")
  @Test
  void addPathCallsTheSameMethodOnTheImplementation() {
    // Given
    doNothing().when(fileManagerImpl).addPath(any(), any());
    var expectedLocation = someLocation();
    var expectedPathRoot = somePathRoot();

    // When
    forwardingFileManager.addPath(expectedLocation, expectedPathRoot);

    // Then
    verify(fileManagerImpl).addPath(expectedLocation, expectedPathRoot);
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(".addPaths(...) calls the same method on the implementation")
  @Test
  void addPathsCallsTheSameMethodOnTheImplementation() {
    // Given
    doNothing().when(fileManagerImpl).addPaths(any(), any());
    var expectedLocation = someLocation();
    Collection<? extends PathRoot> expectedPathRoots = mock();

    // When
    forwardingFileManager.addPaths(expectedLocation, expectedPathRoots);

    // Then
    verify(fileManagerImpl).addPaths(expectedLocation, expectedPathRoots);
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(".copyContainers(...) calls the same method on the implementation")
  @Test
  void copyContainersCallsTheSameMethodOnTheImplementation() {
    // Given
    doNothing().when(fileManagerImpl).copyContainers(any(), any());
    var expectedFromLocation = someLocation();
    var expectedToLocation = someLocation();

    // When
    forwardingFileManager.copyContainers(expectedFromLocation, expectedToLocation);

    // Then
    verify(fileManagerImpl).copyContainers(expectedFromLocation, expectedToLocation);
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(".createEmptyLocation(...) calls the same method on the implementation")
  @Test
  void createEmptyLocationCallsTheSameMethodOnTheImplementation() {
    // Given
    doNothing().when(fileManagerImpl).createEmptyLocation(any());
    var expectedLocation = someLocation();

    // When
    forwardingFileManager.createEmptyLocation(expectedLocation);

    // Then
    verify(fileManagerImpl).createEmptyLocation(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
  }

  @DisplayName(".getEffectiveRelease() calls the same method on the implementation")
  @Test
  void getEffectiveReleaseCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedResult = someRelease();
    when(fileManagerImpl.getEffectiveRelease()).thenReturn(expectedResult);

    // When
    var actualResult = forwardingFileManager.getEffectiveRelease();

    // Then
    verify(fileManagerImpl).getEffectiveRelease();
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @DisplayName(".getPackageContainerGroup(...) calls the same method on the implementation")
  @Test
  void getPackageContainerGroupCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedContainerGroup = mock(PackageContainerGroup.class);
    when(fileManagerImpl.getPackageContainerGroup(any())).thenReturn(expectedContainerGroup);
    var expectedLocation = someLocation();

    // When
    var actualContainerGroup = forwardingFileManager.getPackageContainerGroup(expectedLocation);

    // Then
    verify(fileManagerImpl).getPackageContainerGroup(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroup).isSameAs(expectedContainerGroup);
  }

  @DisplayName(".getPackageContainerGroups() calls the same method on the implementation")
  @Test
  void getPackageContainerGroupsCallsTheSameMethodOnTheImplementation() {
    // Given
    Collection<PackageContainerGroup> expectedContainerGroups = mock();
    when(fileManagerImpl.getPackageContainerGroups()).thenReturn(expectedContainerGroups);

    // When
    var actualContainerGroups = forwardingFileManager.getPackageContainerGroups();

    // Then
    verify(fileManagerImpl).getPackageContainerGroups();
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroups).isSameAs(expectedContainerGroups);
  }

  @DisplayName(".getModuleContainerGroup(...) calls the same method on the implementation")
  @Test
  void getModuleContainerGroupCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedContainerGroup = mock(ModuleContainerGroup.class);
    when(fileManagerImpl.getModuleContainerGroup(any())).thenReturn(expectedContainerGroup);
    var expectedLocation = someLocation();

    // When
    var actualContainerGroup = forwardingFileManager.getModuleContainerGroup(expectedLocation);

    // Then
    verify(fileManagerImpl).getModuleContainerGroup(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroup).isSameAs(expectedContainerGroup);
  }

  @DisplayName(".getModuleContainerGroups() calls the same method on the implementation")
  @Test
  void getModuleContainerGroupsCallsTheSameMethodOnTheImplementation() {
    // Given
    Collection<ModuleContainerGroup> expectedContainerGroups = mock();
    when(fileManagerImpl.getModuleContainerGroups()).thenReturn(expectedContainerGroups);

    // When
    var actualContainerGroups = forwardingFileManager.getModuleContainerGroups();

    // Then
    verify(fileManagerImpl).getModuleContainerGroups();
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroups).isSameAs(expectedContainerGroups);
  }

  @DisplayName(".getOutputContainerGroup(...) calls the same method on the implementation")
  @Test
  void getOutputContainerGroupCallsTheSameMethodOnTheImplementation() {
    // Given
    var expectedContainerGroup = mock(OutputContainerGroup.class);
    when(fileManagerImpl.getOutputContainerGroup(any())).thenReturn(expectedContainerGroup);
    var expectedLocation = someLocation();

    // When
    var actualContainerGroup = forwardingFileManager.getOutputContainerGroup(expectedLocation);

    // Then
    verify(fileManagerImpl).getOutputContainerGroup(expectedLocation);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroup).isSameAs(expectedContainerGroup);
  }

  @DisplayName(".getOutputContainerGroups() calls the same method on the implementation")
  @Test
  void getOutputContainerGroupsCallsTheSameMethodOnTheImplementation() {
    // Given
    Collection<OutputContainerGroup> expectedContainerGroups = mock();
    when(fileManagerImpl.getOutputContainerGroups()).thenReturn(expectedContainerGroups);

    // When
    var actualContainerGroups = forwardingFileManager.getOutputContainerGroups();

    // Then
    verify(fileManagerImpl).getOutputContainerGroups();
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualContainerGroups).isSameAs(expectedContainerGroups);
  }

  @DisplayName(".list(...) calls the same method on the implementation")
  @Test
  void listCallsTheSameMethodOnTheImplementation() throws IOException {
    // Given
    Set<JavaFileObject> expectedSet = mock();
    when(fileManagerImpl.list(any(), any(), any(), anyBoolean())).thenReturn(expectedSet);
    var expectedLocation = someLocation();
    var expectedPackageName = somePackageName();
    var expectedKinds = someOf(JavaFileObject.Kind.class);
    var expectedRecurse = someBoolean();

    // When
    var actualSet = forwardingFileManager
        .list(expectedLocation, expectedPackageName, expectedKinds, expectedRecurse);

    // Then
    verify(fileManagerImpl)
        .list(expectedLocation, expectedPackageName, expectedKinds, expectedRecurse);
    verifyNoMoreInteractions(fileManagerImpl);
    assertThat(actualSet).isSameAs(expectedSet);
  }

  static final class ForwardingJctFileManagerImpl extends ForwardingJctFileManager<JctFileManager> {

    ForwardingJctFileManagerImpl(JctFileManager fileManager) {
      super(fileManager);
    }
  }
}
