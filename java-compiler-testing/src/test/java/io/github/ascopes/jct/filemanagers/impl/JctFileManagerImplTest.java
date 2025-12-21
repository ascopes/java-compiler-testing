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
package io.github.ascopes.jct.filemanagers.impl;

import static io.github.ascopes.jct.fixtures.Fixtures.oneOf;
import static io.github.ascopes.jct.fixtures.Fixtures.someAbsolutePath;
import static io.github.ascopes.jct.fixtures.Fixtures.someBinaryName;
import static io.github.ascopes.jct.fixtures.Fixtures.someBoolean;
import static io.github.ascopes.jct.fixtures.Fixtures.someClassName;
import static io.github.ascopes.jct.fixtures.Fixtures.someFlags;
import static io.github.ascopes.jct.fixtures.Fixtures.someInt;
import static io.github.ascopes.jct.fixtures.Fixtures.someJavaFileObject;
import static io.github.ascopes.jct.fixtures.Fixtures.someLocation;
import static io.github.ascopes.jct.fixtures.Fixtures.someModuleName;
import static io.github.ascopes.jct.fixtures.Fixtures.somePackageName;
import static io.github.ascopes.jct.fixtures.Fixtures.somePathRoot;
import static io.github.ascopes.jct.fixtures.Fixtures.someRelativePath;
import static io.github.ascopes.jct.fixtures.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.collection;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.ContainerGroup;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.containers.impl.ContainerGroupRepositoryImpl;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.ex.JctNotFoundException;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.fixtures.Fixtures;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link JctFileManagerImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctFileManagerImpl tests")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"DataFlowIssue", "resource"})
class JctFileManagerImplTest {

  JctFileManagerImpl fileManager;
  ContainerGroupRepositoryImpl repository;
  InOrder order;

  @BeforeEach
  void setUp() {
    // Mock the construction so that we can access the internally created container group repository
    // object.
    try (var construction = mockConstruction(ContainerGroupRepositoryImpl.class)) {
      fileManager = new JctFileManagerImpl("some-release");
      repository = construction.constructed().iterator().next();
      order = inOrder(repository);
    }
  }

  @DisplayName("Constructor disallows null releases")
  @SuppressWarnings("resource")
  @Test
  void constructorDisallowsNullReleases() {
    // Then
    assertThatThrownBy(() -> new JctFileManagerImpl(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("release");
  }

  @DisplayName(".addPath(...) delegates to the repository")
  @Test
  void addPathDelegatesToRepository() {
    // Given
    var location = someLocation();
    var pathRoot = somePathRoot();

    // When
    fileManager.addPath(location, pathRoot);

    // Then
    verify(repository).addPath(location, pathRoot);
    verifyNoMoreInteractions(repository);
  }

  @DisplayName(".addPaths(...) delegates multiple calls to the repository")
  @Test
  void addPathsDelegatesMultipleCallsToTheRepository() {
    // Given
    var location = someLocation();
    var pathRoots = Stream
        .generate(Fixtures::somePathRoot)
        .limit(10)
        .toList();

    // When
    fileManager.addPaths(location, pathRoots);

    // Then
    pathRoots.forEach(pathRoot -> order.verify(repository).addPath(location, pathRoot));
    order.verifyNoMoreInteractions();
  }

  @DisplayName(".close() closes the repository")
  @Test
  void closeDelegatesToTheRepository() {
    // When
    fileManager.close();

    // Then
    verify(repository).close();
    verifyNoMoreInteractions(repository);
  }

  @DisplayName(".contains(...) tests")
  @Nested
  class ContainsTest {

    @DisplayName(".contains(...) returns false if the file object is not a PathFileObject")
    @Test
    void containsReturnsFalseIfFileObjectIsNotPathFileObject() {
      // Given
      var location = someLocation();
      FileObject fileObject = mock();

      // When
      final var result = fileManager.contains(location, fileObject);

      // Then
      assertThat(result).isFalse();
      verifyNoInteractions(repository);
    }

    @DisplayName(".contains(...) returns false if the location is not in the repository")
    @Test
    void containsReturnsFalseIfTheLocationIsNotInTheRepository() {
      var location = someLocation();
      PathFileObject fileObject = mock();

      when(repository.getContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.contains(location, fileObject);

      // Then
      verify(repository).getContainerGroup(location);
      assertThat(result).isFalse();
      verifyNoMoreInteractions(repository);
    }

    @DisplayName(".contains(...) checks the expected container group")
    @ValueSource(booleans = {true, false})
    @ParameterizedTest(name = "for group.contains(...) = {0}")
    void containsChecksTheExpectedContainerGroup(boolean contained) {
      var location = someLocation();
      PathFileObject fileObject = mock();
      ContainerGroup containerGroup = mock();
      when(repository.getContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.contains(any()))
          .thenReturn(contained);

      // When
      final var result = fileManager.contains(location, fileObject);

      // Then
      verify(repository).getContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).contains(fileObject);
      verifyNoMoreInteractions(containerGroup);

      assertThat(result).isEqualTo(contained);
    }
  }

  @DisplayName(".copyContainers(...) delegates to the repository")
  @Test
  void copyContainersDelegatesToTheRepository() {
    // Given
    var from = someLocation();
    var to = someLocation();

    // When
    fileManager.copyContainers(from, to);

    // Then
    verify(repository).copyContainers(from, to);
    verifyNoMoreInteractions(repository);
  }

  @DisplayName(".createEmptyLocation(...) delegates to the repository")
  @Test
  void createEmptyLocationDelegatesToTheRepository() {
    // Given
    var location = someLocation();

    // When
    fileManager.createEmptyLocation(location);

    // Then
    verify(repository).createEmptyLocation(location);
    verifyNoMoreInteractions(repository);
  }

  @DisplayName(".flush() flushes the repository")
  @Test
  void flushDelegatesToTheRepository() {
    // When
    fileManager.flush();

    // Then
    verify(repository).flush();
    verifyNoMoreInteractions(repository);
  }

  @DisplayName(".getClassLoader(...) tests")
  @Nested
  class GetClassLoaderTest {

    @DisplayName(".getClassLoader(...) returns null if the group does not exist")
    @Test
    void getClassLoaderReturnsNullIfGroupDoesNotExist() {
      // Given
      var location = someLocation();
      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getClassLoader(location);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isNull();
    }

    @DisplayName(".getClassLoader(...) calls .getClassLoader() on the container group")
    @Test
    void getClassLoaderDelegatesToTheContainerGroup() {
      // Given
      var location = someLocation();
      PackageContainerGroup containerGroup = mock();
      ClassLoader classLoader = mock();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.getClassLoader())
          .thenReturn(classLoader);

      // When
      final var result = fileManager.getClassLoader(location);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).getClassLoader();
      verifyNoMoreInteractions(containerGroup);

      assertThat(result).isSameAs(classLoader);
    }
  }

  @DisplayName(".getEffectiveRelease() returns the effective release")
  @ValueSource(strings = {"10", "11", "12", "foobar"})
  @ParameterizedTest(name = "for effective release = {0}")
  void getEffectiveReleaseReturnsTheEffectiveRelease(String effectiveRelease) {
    // Given
    try (var fileManager = new JctFileManagerImpl(effectiveRelease)) {
      // Then
      assertThat(fileManager.getEffectiveRelease()).isEqualTo(effectiveRelease);
    }
  }

  @DisplayName(".getFileForInput(...) tests")
  @Nested
  class GetFileForInputTest {

    @DisplayName(".getFileForInput(...) returns null if the group does not exist")
    @Test
    void getFileForInputReturnsNullIfGroupDoesNotExist() {
      // Given
      var location = someLocation();
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getFileForInput(location, packageName, relativeName);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isNull();
    }

    @DisplayName(".getFileForInput(...) calls .getFileForInput(...) on the container group")
    @Test
    void getFileForInputCallsGetFileForInputOnTheContainerGroup() {
      // Given
      var location = someLocation();
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();
      PackageContainerGroup containerGroup = mock();
      PathFileObject fileObject = mock();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.getFileForInput(any(), any()))
          .thenReturn(fileObject);

      // When
      final var result = fileManager.getFileForInput(location, packageName, relativeName);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).getFileForInput(packageName, relativeName);
      verifyNoMoreInteractions(containerGroup);

      assertThat(result).isSameAs(fileObject);
    }
  }

  @DisplayName(".getFileForOutput(...) tests")
  @Nested
  class GetFileForOutputTest {

    @DisplayName(".getFileForOutput(...) throws an exception if the location isn't output-oriented")
    @Test
    void throwsExceptionIfLocationIsNotOutputOriented() {
      // Given
      var name = someText();
      Location location = mock(someText());
      when(location.isOutputLocation()).thenReturn(false);
      when(location.getName()).thenReturn(name);

      // Then
      assertThatThrownBy(() -> fileManager
          .getFileForOutput(
              location,
              somePackageName(),
              someRelativePath().toString(),
              someJavaFileObject()
          ))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be an output location", name);
    }

    @DisplayName(".getFileForOutput(ModuleLocation, ...) returns null if the group does not exist")
    @Test
    void moduleLocationReturnsNullIfGroupDoesNotExist() {
      // Given
      var parentLocation = StandardLocation.CLASS_OUTPUT;
      var moduleLocation = new ModuleLocation(parentLocation, someModuleName());
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();
      var sibling = someJavaFileObject();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager
          .getFileForOutput(moduleLocation, packageName, relativeName, sibling);

      // Then
      verify(repository).getOutputContainerGroup(parentLocation);
      verifyNoMoreInteractions(repository);

      assertThat(result).isNull();
    }

    @DisplayName(
        ".getFileForOutput(ModuleLocation, ...) creates the new location and "
            + "returns the file object")
    @Test
    void moduleLocationCreatesTheNewLocationAndReturnsTheFileObject() {
      // Given
      var parentLocation = StandardLocation.SOURCE_OUTPUT;
      var moduleName = someModuleName();
      var moduleLocation = new ModuleLocation(parentLocation, moduleName);
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();
      var sibling = someJavaFileObject();
      OutputContainerGroup outputContainerGroup = mock();
      PackageContainerGroup moduleGroup = mock();
      PathFileObject fileForOutput = mock();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(outputContainerGroup);
      when(outputContainerGroup.getOrCreateModule(any()))
          .thenReturn(moduleGroup);
      when(moduleGroup.getFileForOutput(any(), any()))
          .thenReturn(fileForOutput);

      // When
      final var result = fileManager
          .getFileForOutput(moduleLocation, packageName, relativeName, sibling);

      // Then
      verify(repository).getOutputContainerGroup(parentLocation);
      verifyNoMoreInteractions(repository);

      verify(outputContainerGroup).getOrCreateModule(moduleName);
      verifyNoMoreInteractions(outputContainerGroup);

      verify(moduleGroup).getFileForOutput(packageName, relativeName);
      verifyNoMoreInteractions(moduleGroup);

      assertThat(result).isSameAs(fileForOutput);
    }

    @DisplayName(".getFileForOutput(Location, ...) returns null if the group does not exist")
    @Test
    void locationReturnsNullIfGroupDoesNotExist() {
      // Given
      var location = StandardLocation.SOURCE_OUTPUT;
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();
      var sibling = someJavaFileObject();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getFileForOutput(location, packageName, relativeName, sibling);

      // Then
      verify(repository).getOutputContainerGroup(location);
      verifyNoMoreInteractions(repository);

      assertThat(result).isNull();
    }

    @DisplayName(".getFileForOutput(Location, ...) returns the file object")
    @Test
    void locationReturnsTheFileObject() {
      // Given
      var location = StandardLocation.NATIVE_HEADER_OUTPUT;
      var packageName = somePackageName();
      var relativeName = someRelativePath().toString();
      var sibling = someJavaFileObject();
      OutputContainerGroup outputContainerGroup = mock();
      PathFileObject fileForOutput = mock();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(outputContainerGroup);
      when(outputContainerGroup.getFileForOutput(any(), any()))
          .thenReturn(fileForOutput);

      // When
      final var result = fileManager.getFileForOutput(location, packageName, relativeName, sibling);

      // Then
      verify(repository).getOutputContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(outputContainerGroup).getFileForOutput(packageName, relativeName);
      verifyNoMoreInteractions(outputContainerGroup);

      assertThat(result).isSameAs(fileForOutput);
    }
  }

  @DisplayName(".getJavaFileForInput(...) tests")
  @Nested
  class GetJavaFileForInputTest {

    @DisplayName(".getJavaFileForInput(...) returns null if the group does not exist")
    @Test
    void getJavaFileForInputReturnsNullIfGroupDoesNotExist() {
      // Given
      var location = someLocation();
      var className = someClassName();
      var kind = oneOf(Kind.class);

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getJavaFileForInput(location, className, kind);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isNull();
    }

    @DisplayName(".getJavaFileForInput(...) calls .getJavaFileForInput(...) on the container group")
    @Test
    void getJavaFileForInputCallsGetJavaFileForInputOnTheContainerGroup() {
      // Given
      var location = someLocation();
      PackageContainerGroup containerGroup = mock();
      PathFileObject fileObject = mock();
      var className = someClassName();
      var kind = oneOf(Kind.class);

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.getJavaFileForInput(any(), any()))
          .thenReturn(fileObject);

      // When
      final var result = fileManager.getJavaFileForInput(location, className, kind);

      // Then
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).getJavaFileForInput(className, kind);
      verifyNoMoreInteractions(containerGroup);

      assertThat(result).isSameAs(fileObject);
    }
  }


  @DisplayName(".getJavaFileForOutput(...) tests")
  @Nested
  class GetJavaFileForOutputTest {

    @DisplayName(
        ".getJavaFileForOutput(...) throws an exception if the location isn't output-oriented"
    )
    @Test
    void throwsExceptionIfLocationIsNotOutputOriented() {
      // Given
      var name = someText();
      Location location = mock(someText());
      var kind = oneOf(Kind.class);
      when(location.isOutputLocation()).thenReturn(false);
      when(location.getName()).thenReturn(name);

      // Then
      assertThatThrownBy(() -> fileManager
          .getJavaFileForOutput(
              location,
              somePackageName(),
              kind,
              someJavaFileObject()
          ))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be an output location", name);
    }

    @DisplayName(
        ".getJavaFileForOutput(ModuleLocation, ...) returns null if the group does not exist"
    )
    @Test
    void moduleLocationReturnsNullIfGroupDoesNotExist() {
      // Given
      var parentLocation = StandardLocation.CLASS_OUTPUT;
      var moduleLocation = new ModuleLocation(parentLocation, someModuleName());
      var className = someClassName();
      var kind = oneOf(Kind.class);
      var sibling = someJavaFileObject();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getJavaFileForOutput(moduleLocation, className, kind, sibling);

      // Then
      verify(repository).getOutputContainerGroup(parentLocation);
      verifyNoMoreInteractions(repository);

      assertThat(result).isNull();
    }

    @DisplayName(
        ".getJavaFileForOutput(ModuleLocation, ...) creates the new location and "
            + "returns the file object")
    @Test
    void moduleLocationCreatesTheNewLocationAndReturnsTheFileObject() {
      // Given
      var parentLocation = StandardLocation.SOURCE_OUTPUT;
      var moduleName = someModuleName();
      var moduleLocation = new ModuleLocation(parentLocation, moduleName);
      var className = someClassName();
      var kind = oneOf(Kind.class);
      var sibling = someJavaFileObject();
      OutputContainerGroup outputContainerGroup = mock();
      PackageContainerGroup moduleGroup = mock();
      PathFileObject javaFileForOutput = mock();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(outputContainerGroup);
      when(outputContainerGroup.getOrCreateModule(any()))
          .thenReturn(moduleGroup);
      when(moduleGroup.getJavaFileForOutput(any(), any()))
          .thenReturn(javaFileForOutput);

      // When
      final var result = fileManager.getJavaFileForOutput(moduleLocation, className, kind, sibling);

      // Then
      verify(repository).getOutputContainerGroup(parentLocation);
      verifyNoMoreInteractions(repository);

      verify(outputContainerGroup).getOrCreateModule(moduleName);
      verifyNoMoreInteractions(outputContainerGroup);

      verify(moduleGroup).getJavaFileForOutput(className, kind);
      verifyNoMoreInteractions(moduleGroup);

      assertThat(result).isSameAs(javaFileForOutput);
    }

    @DisplayName(".getJavaFileForOutput(Location, ...) returns null if the group does not exist")
    @Test
    void locationReturnsNullIfGroupDoesNotExist() {
      // Given
      var location = StandardLocation.SOURCE_OUTPUT;
      var className = someClassName();
      var kind = oneOf(Kind.class);
      var sibling = someJavaFileObject();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.getJavaFileForOutput(location, className, kind, sibling);

      // Then
      verify(repository).getOutputContainerGroup(location);
      verifyNoMoreInteractions(repository);

      assertThat(result).isNull();
    }

    @DisplayName(".getJavaFileForOutput(Location, ...) returns the file object")
    @Test
    void locationReturnsTheFileObject() {
      // Given
      var location = StandardLocation.NATIVE_HEADER_OUTPUT;
      var className = someClassName();
      var kind = oneOf(Kind.class);
      var sibling = someJavaFileObject();
      OutputContainerGroup outputContainerGroup = mock();
      PathFileObject javaFileForOutput = mock();

      when(repository.getOutputContainerGroup(any()))
          .thenReturn(outputContainerGroup);
      when(outputContainerGroup.getJavaFileForOutput(any(), any()))
          .thenReturn(javaFileForOutput);

      // When
      final var result = fileManager.getJavaFileForOutput(location, className, kind, sibling);

      // Then
      verify(repository).getOutputContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(outputContainerGroup).getJavaFileForOutput(className, kind);
      verifyNoMoreInteractions(outputContainerGroup);

      assertThat(result).isSameAs(javaFileForOutput);
    }
  }

  @DisplayName(".getLocationForModule(...) tests")
  @Nested
  class GetLocationForModuleTest {

    @DisplayName(
        ".getLocationForModule(Location, String) throws an exception for package locations"
    )
    @Test
    void getLocationForModuleStringThrowsExceptionForPackageLocations() {
      // Given
      var location = StandardLocation.CLASS_PATH;
      var moduleName = someModuleName();

      // Then
      assertThatThrownBy(() -> fileManager.getLocationForModule(location, moduleName))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be output or module-oriented", location.getName());
    }

    @DisplayName(".getLocationForModule(Location, String) returns the module location")
    @EnumSource(
        value = StandardLocation.class,
        names = {"CLASS_OUTPUT", "SOURCE_OUTPUT", "MODULE_SOURCE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void getLocationForModuleStringReturnsModuleLocation(Location location) {
      // Given
      var moduleName = someModuleName();

      // When
      var moduleLocation = fileManager.getLocationForModule(location, moduleName);

      // Then
      assertThat(moduleLocation.getModuleName()).isEqualTo(moduleName);
      assertThat(moduleLocation.getParent()).isEqualTo(location);
    }

    @DisplayName(
        ".getLocationForModule(Location, FileObject) throws an exception for package locations"
    )
    @Test
    void getLocationForModuleFileObjectThrowsExceptionForPackageLocations() {
      // Given
      var location = StandardLocation.CLASS_PATH;
      var fileObject = someJavaFileObject();

      // Then
      assertThatThrownBy(() -> fileManager.getLocationForModule(location, fileObject))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be output or module-oriented", location.getName());
    }

    @DisplayName(".getLocationForModule(Location, FileObject) throws an exception")
    @Test
    void getLocationForModuleFileObjectThrowsException() {
      // Given
      var location = StandardLocation.MODULE_SOURCE_PATH;
      var fileObject = someJavaFileObject();

      // Then
      assertThatThrownBy(() -> fileManager.getLocationForModule(location, fileObject))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("File object %s is not compatible with this file manager", fileObject);
    }

    @DisplayName(
        ".getLocationForModule(Location, PathFileObject) returns null for non-module file objects"
    )
    @Test
    void getLocationForModulePathFileObjectReturnsNullForNonModuleFileObjects() {
      // Given
      var location = StandardLocation.MODULE_PATH;
      PathFileObject fileObject = mock();
      when(fileObject.getLocation()).thenReturn(StandardLocation.SOURCE_PATH);

      // When
      var moduleLocation = fileManager.getLocationForModule(location, fileObject);

      // Then
      assertThat(moduleLocation).isNull();
    }

    @DisplayName(
        ".getLocationForModule(Location, PathFileObject) returns the module "
            + "location for file objects"
    )
    @Test
    void getLocationForModulePathFileObjectReturnsTheModuleLocationForFileObjects() {
      // Given
      var location = StandardLocation.MODULE_SOURCE_PATH;
      var expectedModuleLocation = new ModuleLocation(location, someModuleName());
      PathFileObject fileObject = mock();
      when(fileObject.getLocation()).thenReturn(expectedModuleLocation);

      // When
      var returnedModuleLocation = fileManager.getLocationForModule(location, fileObject);

      // Then
      assertThat(returnedModuleLocation).isSameAs(expectedModuleLocation);
    }
  }

  @DisplayName(".getModuleContainerGroup(...) tests")
  @Nested
  class GetModuleContainerGroupTest {

    @DisplayName(
        ".getModuleContainerGroup(...) throws an exception for non module-oriented container groups"
    )
    @EnumSource(
        value = StandardLocation.class,
        names = {"SOURCE_PATH", "CLASS_OUTPUT"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void getModuleContainerGroupThrowsExceptionForNonModuleOrientedContainerGroups(
        Location location
    ) {
      // Then
      assertThatThrownBy(() -> fileManager.getModuleContainerGroup(location))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be module-oriented", location.getName());

      verifyNoInteractions(repository);
    }

    @DisplayName(".getModuleContainerGroup(...) delegates to the repository")
    @Test
    void getModuleContainerGroupDelegatesToTheRepository() {
      // Given
      var location = StandardLocation.MODULE_SOURCE_PATH;
      ModuleContainerGroup containerGroup = mock();
      when(repository.getModuleContainerGroup(any()))
          .thenReturn(containerGroup);

      // When
      final var result = fileManager.getModuleContainerGroup(location);

      // Then
      verify(repository).getModuleContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isSameAs(containerGroup);
    }
  }

  @DisplayName(".getModuleContainerGroups() delegates to the repository")
  @Test
  void getModuleContainerGroupsDelegatesToTheRepository() {
    // Given
    Collection<ModuleContainerGroup> moduleContainerGroups = mock();
    when(repository.getModuleContainerGroups())
        .thenReturn(moduleContainerGroups);

    // When
    final var result = fileManager.getModuleContainerGroups();

    // Then
    verify(repository).getModuleContainerGroups();
    verifyNoMoreInteractions(repository);
    assertThat(result).isSameAs(moduleContainerGroups);
  }

  @DisplayName(".getOutputContainerGroup(...) tests")
  @Nested
  class GetOutputContainerGroupTest {

    @DisplayName(
        ".getOutputContainerGroup(...) throws an exception for non output-oriented container groups"
    )
    @EnumSource(
        value = StandardLocation.class,
        names = {"SOURCE_PATH", "MODULE_SOURCE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void getOutputContainerGroupThrowsExceptionForNonOutputOrientedContainerGroups(
        Location location
    ) {
      // Then
      assertThatThrownBy(() -> fileManager.getOutputContainerGroup(location))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be an output location", location.getName());

      verifyNoInteractions(repository);
    }

    @DisplayName(".getOutputContainerGroup(...) delegates to the repository")
    @Test
    void getOutputContainerGroupDelegatesToTheRepository() {
      // Given
      var location = StandardLocation.CLASS_OUTPUT;
      OutputContainerGroup containerGroup = mock();
      when(repository.getOutputContainerGroup(any()))
          .thenReturn(containerGroup);

      // When
      final var result = fileManager.getOutputContainerGroup(location);

      // Then
      verify(repository).getOutputContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isSameAs(containerGroup);
    }
  }


  @DisplayName(".getOutputContainerGroups() delegates to the repository")
  @Test
  void getOutputContainerGroupsDelegatesToTheRepository() {
    // Given
    Collection<OutputContainerGroup> outputContainerGroups = mock();
    when(repository.getOutputContainerGroups())
        .thenReturn(outputContainerGroups);

    // When
    final var result = fileManager.getOutputContainerGroups();

    // Then
    verify(repository).getOutputContainerGroups();
    verifyNoMoreInteractions(repository);
    assertThat(result).isSameAs(outputContainerGroups);
  }

  @DisplayName(".getPackageContainerGroup(...) tests")
  @Nested
  class GetPackageContainerGroupTest {

    @DisplayName(
        ".getPackageContainerGroup(...) throws an exception for non package container groups"
    )
    @EnumSource(
        value = StandardLocation.class,
        names = {"CLASS_OUTPUT", "MODULE_SOURCE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void getPackageContainerGroupThrowsExceptionForNonPackageContainerGroups(Location location) {
      // Then
      assertThatThrownBy(() -> fileManager.getPackageContainerGroup(location))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be an input package location", location.getName());

      verifyNoInteractions(repository);
    }

    @DisplayName(".getPackageContainerGroup(...) delegates to the repository")
    @Test
    void getPackageContainerGroupDelegatesToTheRepository() {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      PackageContainerGroup containerGroup = mock();
      when(repository.getPackageContainerGroup(any()))
          .thenReturn(containerGroup);

      // When
      final var result = fileManager.getPackageContainerGroup(location);

      // Then
      verify(repository).getPackageContainerGroup(location);
      verifyNoMoreInteractions(repository);
      assertThat(result).isSameAs(containerGroup);
    }
  }

  @DisplayName(".getPackageContainerGroups() delegates to the repository")
  @Test
  void getPackageContainerGroupsDelegatesToTheRepository() {
    // Given
    Collection<PackageContainerGroup> packageContainerGroups = mock();
    when(repository.getPackageContainerGroups())
        .thenReturn(packageContainerGroups);

    // When
    final var result = fileManager.getPackageContainerGroups();

    // Then
    verify(repository).getPackageContainerGroups();
    verifyNoMoreInteractions(repository);
    assertThat(result).isSameAs(packageContainerGroups);
  }

  @DisplayName(".getServiceLoader(...) tests")
  @Nested
  class GetServiceLoaderTest {

    @DisplayName(".getServiceLoader(...) throws an exception if the location does not exist")
    @Test
    void getServiceLoaderThrowsExceptionIfLocationDoesNotExist() {
      // Given
      class Some {}

      var location = someLocation();
      when(repository.getContainerGroup(any()))
          .thenReturn(null);

      // Then
      assertThatThrownBy(() -> fileManager.getServiceLoader(location, Some.class))
          .isInstanceOf(JctNotFoundException.class)
          .hasMessage("No container group for location %s exists in this file manager",
              location.getName());

      verify(repository).getContainerGroup(location);
      verifyNoMoreInteractions(repository);
    }

    @DisplayName(".getServiceLoader(...) delegates to the group")
    @Test
    void getServiceLoaderDelegatesToTheGroup() {
      // Given
      class Some {}

      var location = someLocation();
      ContainerGroup containerGroup = mock();
      ServiceLoader<Some> serviceLoader = mock();
      when(repository.getContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.getServiceLoader(any()))
          .thenAnswer(ctx -> serviceLoader);

      // When
      final var result = fileManager.getServiceLoader(location, Some.class);

      // Then
      verify(repository).getContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).getServiceLoader(Some.class);
      verifyNoMoreInteractions(containerGroup);

      assertThat(result).isSameAs(serviceLoader);
    }
  }

  @DisplayName(".handleOption(...) always returns false")
  @RepeatedTest(10)
  void handleOptionAlwaysReturnsFalse() {
    // Given
    var originalFlagIterator = someFlags().iterator();
    var flagIterator = spy(originalFlagIterator);
    var flag = originalFlagIterator.next();

    // When
    final var result = fileManager.handleOption(flag, flagIterator);

    // Then
    assertThat(result).isFalse();
    verifyNoInteractions(flagIterator);
  }

  @DisplayName(".hasLocation(...) delegates to the repository")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for repository.hasLocation(...) = {0}")
  void hasLocationDelegatesToTheRepository(boolean hasLocation) {
    // Given
    var location = someLocation();
    when(repository.hasLocation(any())).thenReturn(hasLocation);

    // When
    final var result = fileManager.hasLocation(location);

    // Then
    verify(repository).hasLocation(location);
    verifyNoMoreInteractions(repository);
    assertThat(result).isEqualTo(hasLocation);
  }

  @DisplayName(".inferBinaryName(...) tests")
  @Nested
  class InferBinaryNameTest {

    @DisplayName(".inferBinaryName(...) throws an exception for module-oriented locations")
    @EnumSource(
        value = StandardLocation.class,
        names = {"MODULE_SOURCE_PATH", "MODULE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void inferBinaryNameThrowsExceptionForModuleOrientedLocations(Location location) {
      // Then
      assertThatThrownBy(() -> fileManager.inferBinaryName(location, someJavaFileObject()))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be package-oriented", location.getName());

      verifyNoInteractions(repository);
    }

    @DisplayName(".inferBinaryName(...) returns null for non-PathFileObject objects")
    @Test
    void inferBinaryNameReturnsNullForNonPathFileObjects() {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      JavaFileObject fileObject = mock();

      // When
      final var result = fileManager.inferBinaryName(location, fileObject);

      // Then
      assertThat(result).isNull();
      verifyNoInteractions(repository);
    }

    @DisplayName(".inferBinaryName(...) returns null if the location does not exist")
    @Test
    void inferBinaryNameReturnsNullIfTheLocationDoesNotExist() {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      PathFileObject fileObject = mock();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.inferBinaryName(location, fileObject);

      // Then
      assertThat(result).isNull();
      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);
    }

    @DisplayName(".inferBinaryName(...)  infers the binary name from the group")
    @Test
    void inferBinaryNameInfersTheBinaryNameFromTheGroup() {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      PathFileObject fileObject = mock();
      PackageContainerGroup containerGroup = mock();
      var binaryName = someBinaryName();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.inferBinaryName(any()))
          .thenReturn(binaryName);

      // When
      final var result = fileManager.inferBinaryName(location, fileObject);

      // Then
      assertThat(result).isEqualTo(binaryName);

      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).inferBinaryName(fileObject);
      verifyNoMoreInteractions(containerGroup);
    }
  }

  @DisplayName(".inferModuleName(...) tests")
  @Nested
  class InferModuleNameTest {

    @DisplayName(
        ".inferModuleName(...) throws an exception if the location is not package oriented"
    )
    @EnumSource(
        value = StandardLocation.class,
        names = {"MODULE_SOURCE_PATH", "MODULE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void inferModuleNameThrowsAnExceptionIfLocationIsNotPackageOriented(Location location) {
      // Then
      assertThatThrownBy(() -> fileManager.inferModuleName(location))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be package-oriented", location.getName());
    }

    @DisplayName(".inferModuleName(...) returns null for non-module locations")
    @Test
    void inferModuleNameReturnsNullForNonModuleLocations() {
      // Given
      var location = StandardLocation.SOURCE_PATH;

      // When
      final var result = fileManager.inferModuleName(location);

      // Then
      assertThat(result).isNull();
    }

    @DisplayName(".inferModuleName(...) returns the module name for module locations")
    @Test
    void inferModuleNameReturnsModuleNameForModuleLocations() {
      // Given
      var parentLocation = StandardLocation.MODULE_SOURCE_PATH;
      var moduleName = someModuleName();
      var moduleLocation = new ModuleLocation(parentLocation, moduleName);

      // When
      final var result = fileManager.inferModuleName(moduleLocation);

      // Then
      assertThat(result).isEqualTo(moduleName);
    }
  }

  @DisplayName(".isSameFile(...) tests")
  @Nested
  class IsSameFileTest {

    @DisplayName(".isSameFile(...) returns false if the first argument is null")
    @Test
    void isSameFileReturnsFalseIfFirstArgumentIsNull() {
      // Then
      assertThat(fileManager.isSameFile(null, mock())).isFalse();
    }

    @DisplayName(".isSameFile(...) returns false if the second argument is null")
    @Test
    void isSameFileReturnsFalseIfSecondArgumentIsNull() {
      // Then
      assertThat(fileManager.isSameFile(mock(), null)).isFalse();
    }

    @DisplayName(".isSameFile(...) returns false if both arguments are null")
    @Test
    void isSameFileReturnsFalseIfBothArgumentsAreNull() {
      // Then
      assertThat(fileManager.isSameFile(null, null)).isFalse();
    }

    @DisplayName(".isSameFile(...) returns false if both files have different URIs")
    @Test
    void isSameFileReturnsFalseIfBothFilesHaveDifferentUris() {
      // Given
      FileObject first = mock();
      FileObject second = mock();

      URI firstUri = someAbsolutePath().toUri();
      URI secondUri = someAbsolutePath().toUri();

      when(first.toUri()).thenReturn(firstUri);
      when(second.toUri()).thenReturn(secondUri);

      // Then
      assertThat(fileManager.isSameFile(first, second)).isFalse();
    }

    @DisplayName(".isSameFile(...) returns true if both files have the same URI")
    @Test
    void isSameFileReturnsTrueIfBothFilesHaveTheSameUri() {
      // Given
      FileObject first = mock();
      FileObject second = mock();

      URI uri = someAbsolutePath().toUri();

      when(first.toUri()).thenReturn(uri);
      when(second.toUri()).thenReturn(uri);

      // Then
      assertThat(fileManager.isSameFile(first, second)).isTrue();
    }
  }

  @DisplayName(".isSupportedOption(...) always returns -1")
  @RepeatedTest(10)
  void isSupportedOptionAlwaysReturnsFalse() {
    // Given
    var flag = someFlags().iterator().next();

    // Then
    assertThat(fileManager.isSupportedOption(flag)).isEqualTo(-1);
  }

  @DisplayName(".list(...) tests")
  @Nested
  class ListTests {

    @DisplayName(".list(...) throws an exception if the location is not package oriented")
    @EnumSource(
        value = StandardLocation.class,
        names = {"MODULE_SOURCE_PATH", "MODULE_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void listThrowsAnExceptionIfLocationIsNotPackageOriented(Location location) {
      // Given
      var packageName = somePackageName();
      var kinds = Stream.generate(() -> oneOf(Kind.class))
          .limit(someInt(1, 4))
          .collect(Collectors.toSet());
      var recurse = someBoolean();

      // Then
      assertThatThrownBy(() -> fileManager.list(location, packageName, kinds, recurse))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be package-oriented", location.getName());
    }

    @DisplayName(".list(...) returns an empty set if the location does not exist")
    @Test
    void listReturnsEmptySetIfLocationDoesNotExist() throws IOException {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      var packageName = somePackageName();
      var kinds = Stream.generate(() -> oneOf(Kind.class))
          .limit(someInt(1, 4))
          .collect(Collectors.toSet());
      var recurse = someBoolean();

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(null);

      // When
      final var result = fileManager.list(location, packageName, kinds, recurse);

      // Then
      assertThat(result)
          .isInstanceOf(Set.class)
          .isEmpty();

      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);
    }

    @DisplayName(".list(...) returns the file listing for the location")
    @Test
    void listReturnsFileListingForTheLocation() throws IOException {
      // Given
      var location = StandardLocation.SOURCE_PATH;
      var packageName = somePackageName();
      var kinds = Stream.generate(() -> oneOf(Kind.class))
          .limit(someInt(1, 4))
          .collect(Collectors.toSet());

      var recurse = someBoolean();
      PackageContainerGroup containerGroup = mock();
      var files = Set.of(someJavaFileObject(), someJavaFileObject(), someJavaFileObject());

      when(repository.getPackageOrientedContainerGroup(any()))
          .thenReturn(containerGroup);
      when(containerGroup.listFileObjects(any(), any(), anyBoolean()))
          .thenReturn(files);

      // When
      final var result = fileManager.list(location, packageName, kinds, recurse);

      // Then
      assertThat(result)
          .isInstanceOf(Set.class)
          .containsExactlyInAnyOrderElementsOf(files);

      verify(repository).getPackageOrientedContainerGroup(location);
      verifyNoMoreInteractions(repository);

      verify(containerGroup).listFileObjects(packageName, kinds, recurse);
      verifyNoMoreInteractions(containerGroup);
    }
  }

  @DisplayName(".listLocationsForModules(...) tests")
  @Nested
  class ListLocationsForModulesTest {

    @DisplayName(
        ".listLocationsForModules(...) throws an exception if the location is package oriented"
    )
    @EnumSource(
        value = StandardLocation.class,
        names = {"SOURCE_PATH", "CLASS_PATH"}
    )
    @ParameterizedTest(name = "for location StandardLocation.{0}")
    void listLocationsForModulesThrowsExceptionIfLocationIsPackageOriented(Location location) {
      // Then
      assertThatThrownBy(() -> fileManager.listLocationsForModules(location))
          .isInstanceOf(JctIllegalInputException.class)
          .hasMessage("Location %s must be output or module-oriented", location.getName());
    }

    @DisplayName(".listLocationsForModules(...) returns the results in a single-element list")
    @Test
    void listLocationsForModulesReturnsTheResultsInSingleElementList() {
      // Given
      var location = StandardLocation.MODULE_SOURCE_PATH;
      var expectedLocations = Stream.generate(() -> new ModuleLocation(location, someModuleName()))
          .limit(someInt(1, 10))
          .map(Location.class::cast)
          .collect(Collectors.toSet());

      when(repository.listLocationsForModules(location))
          .thenReturn(expectedLocations);

      // When
      final var result = fileManager.listLocationsForModules(location);

      // Then
      assertThat(result)
          .singleElement(collection(Location.class))
          .containsExactlyInAnyOrderElementsOf(expectedLocations);
    }
  }

  @DisplayName(".toString() returns the expected value")
  @Test
  void toStringReturnsExpectedValue() {
    // Then
    assertThat(fileManager.toString())
        .isEqualTo("JctFileManagerImpl{repository=%s}", repository);
  }
}
