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
package io.github.ascopes.jct.containers.impl;

import static io.github.ascopes.jct.fixtures.Fixtures.somePathRoot;
import static io.github.ascopes.jct.fixtures.Fixtures.someRelease;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.utils.ModuleDiscoverer;
import io.github.ascopes.jct.utils.ModuleDiscoverer.ModuleCandidate;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link ContainerGroupRepositoryImpl} tests.
 *
 * <p>This is partially an integration test in nature, but this is only done since there is
 * little benefit in mocking all moving components. Components that perform things like module
 * discovery will still be mocked internally. The idea behind this class is to ensure that the
 * implementation performs the expected behaviour as a pure black box (i.e. an opaque implementation
 * that may be subject to structural change over time).
 *
 * @author Ashley Scopes
 */
@DisplayName("ContainerGroupRepositoryImpl tests")
@SuppressWarnings("resource")
class ContainerGroupRepositoryImplTest {

  String release;
  ContainerGroupRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    release = someRelease();
    repository = new ContainerGroupRepositoryImpl(release);
  }

  @AfterEach
  void tearDown() {
    repository.close();
  }

  @DisplayName("Initialising the repository sets the release")
  @Test
  void initialisingTheRepositorySetsTheRelease() {
    // Given
    var release = someRelease();
    try (var repository = new ContainerGroupRepositoryImpl(release)) {
      // Then
      assertThat(repository.getRelease()).isEqualTo(release);
    }
  }

  @DisplayName(".addPath(...) tests")
  @Nested
  class AddPathTest {

    @DisplayName("adding a module location in a non-existent parent registers the module correctly")
    @Test
    void addingModuleLocationInNonExistentParentRegistersModuleCorrectly() {
      // Given
      var moduleLocation = new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, "foo.bar");
      PathRoot pathRoot = somePathRoot();

      // When
      repository.addPath(moduleLocation, pathRoot);

      // Then
      var groups = repository.getModuleContainerGroups();
      assertThat(groups)
          .as("module oriented groups")
          .hasSize(1);

      var group = groups.iterator().next();
      assertThat(group.getLocation())
          .as("module oriented group location")
          .isEqualTo(StandardLocation.MODULE_SOURCE_PATH);

      var modules = group.getModules();
      assertThat(modules)
          .as("map of modules in module oriented group")
          .hasSize(1)
          .containsKey(moduleLocation);

      var module = modules.get(moduleLocation);
      assertThat(module.getLocation())
          .as("module group location")
          .isEqualTo(moduleLocation);

      var packages = module.getPackages();
      assertThat(packages)
          .as("collection of module group packages")
          .hasSize(1);

      var modulePackage = packages.iterator().next();
      assertThat(modulePackage.getPathRoot())
          .as("module group package path root")
          .isEqualTo(pathRoot);
    }

    @DisplayName("adding a package to a non-existent output group registers it as a package")
    @Test
    void addingPackageToNonExistentOutputGroupRegistersItAsPackage() {
      // Given
      var location = StandardLocation.CLASS_OUTPUT;
      PathRoot pathRoot = somePathRoot();

      // When
      repository.addPath(location, pathRoot);

      // Then
      var groups = repository.getOutputContainerGroups();
      assertThat(groups)
          .as("output container groups")
          .hasSize(1);

      var group = groups.iterator().next();
      assertThat(group.getLocation())
          .as("output container group location")
          .isEqualTo(location);

      assertThat(group.getModules())
          .as("output container group modules")
          .isEmpty();

      var packages = group.getPackages();
      assertThat(packages)
          .as("output container group packages")
          .hasSize(1);

      var outputPackage = packages.iterator().next();
      assertThat(outputPackage.getPathRoot())
          .as("output package path root")
          .isEqualTo(pathRoot);
    }

    @DisplayName(
        "adding an empty directory to a module-oriented group does not register any modules"
    )
    @Test
    void addingEmptyDirectoryToModuleOrientedGroupDoesNotRegisterModules() {
      // Given
      try (var moduleDiscoverer = mockStatic(ModuleDiscoverer.class)) {
        var location = StandardLocation.MODULE_SOURCE_PATH;
        PathRoot pathRoot = somePathRoot();

        moduleDiscoverer.when(() -> ModuleDiscoverer.findModulesIn(any()))
            .thenReturn(Set.of());

        // When
        repository.addPath(location, pathRoot);

        // Then
        var groups = repository.getModuleContainerGroups();
        assertThat(groups)
            .as("module container groups")
            .isEmpty();

        moduleDiscoverer.verify(() -> ModuleDiscoverer.findModulesIn(pathRoot.getPath()));
      }
    }

    @DisplayName(
        "adding a directory of modules to a module-oriented group registers them as modules"
    )
    @Test
    void addingDirectoryOfModulesToModuleOrientedGroupRegistersThemAsModules() {
      // Given
      try (var moduleDiscoverer = mockStatic(ModuleDiscoverer.class)) {
        var location = StandardLocation.MODULE_SOURCE_PATH;
        var pathRoot = somePathRoot();

        var discoveredModules = Stream.of("foo.bar", "baz.bork")
            .map(name -> new ModuleCandidate(
                name,
                pathRoot.getPath().resolve(name),
                mock("some descriptor")
            ))
            .collect(Collectors.toSet());

        moduleDiscoverer.when(() -> ModuleDiscoverer.findModulesIn(any()))
            .thenReturn(discoveredModules);

        // When
        repository.addPath(location, pathRoot);

        // Then
        var groups = repository.getModuleContainerGroups();
        assertThat(groups)
            .as("module container groups")
            .hasSize(1);

        var group = groups.iterator().next();
        assertThat(group.getLocation())
            .as("module container group location")
            .isEqualTo(location);

        var modules = group.getModules();
        assertThat(modules)
            .as("module container group modules")
            .hasSize(2);

        assertThat(modules.get(new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, "foo.bar")))
            .as("module container group module <foo.bar>")
            .extracting(PackageContainerGroup::getPackages, list(Container.class))
            .singleElement()
            .extracting(Container::getPathRoot)
            .extracting(PathRoot::getPath)
            .isEqualTo(pathRoot.getPath().resolve("foo.bar"));

        assertThat(modules.get(new ModuleLocation(StandardLocation.MODULE_SOURCE_PATH, "baz.bork")))
            .as("module container group module <baz.bork>")
            .extracting(PackageContainerGroup::getPackages, list(Container.class))
            .singleElement()
            .extracting(Container::getPathRoot)
            .extracting(PathRoot::getPath)
            .isEqualTo(pathRoot.getPath().resolve("baz.bork"));

        moduleDiscoverer.verify(() -> ModuleDiscoverer.findModulesIn(pathRoot.getPath()));
      }
    }

    @Disabled("todo: continue implementing test cases here...")
    @DisplayName("adding a package root registers the package root")
    @Test
    void addingPackageRootRegistersPackageRoot() {
      // TODO: continue
    }
  }
}
