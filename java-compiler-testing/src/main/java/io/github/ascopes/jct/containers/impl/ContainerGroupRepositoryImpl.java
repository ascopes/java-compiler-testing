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
package io.github.ascopes.jct.containers.impl;

import io.github.ascopes.jct.containers.ContainerGroup;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.utils.ModuleDiscoverer;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A repository of container groups, accessible via their {@link Location} handle.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
public class ContainerGroupRepositoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerGroupRepositoryImpl.class);

  private final String release;
  private final Map<Location, PackageContainerGroup> packageInputs;
  private final Map<Location, ModuleContainerGroup> moduleInputs;
  private final Map<Location, OutputContainerGroup> outputs;

  public ContainerGroupRepositoryImpl(String release) {
    this.release = release;
    packageInputs = new ConcurrentHashMap<>();
    moduleInputs = new ConcurrentHashMap<>();
    outputs = new ConcurrentHashMap<>();
  }

  /**
   * Add the path root to the registry, marking it as being part of the given location.
   *
   * <p>This location can be output-oriented, module-oriented, package-oriented, or a specific
   * module location. Module-oriented locations (including module-oriented output locations) will
   * have all modules extracted from them and registered individually rather than the provided path
   * being stored.
   *
   * @param location the location to add.
   * @param pathRoot the path root to register with the location.
   */
  public void addPath(Location location, PathRoot pathRoot) {
    if (location instanceof ModuleLocation) {
      // If we are adding a specific module, we should resolve where it needs to live
      // using custom logic so that we know it gets registered in the right place.
      var moduleLocation = (ModuleLocation) location;
      addModulePath(moduleLocation, pathRoot);
    } else if (location.isModuleOrientedLocation()) {
      // If we are adding a module-oriented location of any type, then we should discover
      // the modules within it and add those.
      addModuleRoot(location, pathRoot);
    } else {
      // If we have a regular package-oriented location, then we should just register it
      // directly.
      addPackageRoot(location, pathRoot);
    }
  }

  /**
   * A bulk-style call for {@link #addPath(Location, PathRoot)}.
   *
   * @param location  the location to add.
   * @param pathRoots the path roots to register with the location.
   */
  public void addPaths(Location location, Iterable<? extends PathRoot> pathRoots) {
    pathRoots.forEach(pathRoot -> addPath(location, pathRoot));
  }

  /**
   * Copy all containers from the {@code from} location to the {@code to} location.
   *
   * @param from the location to copy from.
   * @param to   the location to copy to.
   * @throws IllegalArgumentException if either location is a {@link ModuleLocation}, or if the two
   *                                  locations are not the same orientation (i.e. output-oriented,
   *                                  module-oriented).
   */
  public void copyContainers(Location from, Location to) {
    if (from instanceof ModuleLocation || to instanceof ModuleLocation) {
      throw new IllegalArgumentException(
          "Cannot currently transfer individual modules to other locations"
      );
    }

    if (from.isOutputLocation() && !to.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Expected " + from.getName() + " and " + to.getName() + " to both be "
              + "output locations"
      );
    }

    if (from.isModuleOrientedLocation() && !to.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Expected " + from.getName() + " and " + to.getName() + " to both be "
              + "module-oriented locations"
      );
    }

    if (from.isOutputLocation()) {
      copyPackageContainers(from, to);
      copyModuleContainers(from, to);
    } else if (from.isModuleOrientedLocation()) {
      copyModuleContainers(from, to);
    } else {
      copyPackageContainers(from, to);
    }
  }

  /**
   * Create an empty location if it does not already exist.
   *
   * @param location the location to create.
   * @throws IllegalArgumentException if the input is a module location.
   */
  public void createEmptyLocation(Location location) {
    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException("Cannot ensure a module location exists");
    }

    if (location.isOutputLocation()) {
      getOrCreateOutputContainerGroup(location);
    } else if (location.isModuleOrientedLocation()) {
      getOrCreateModuleContainerGroup(location);
    } else {
      getOrCreatePackageContainerGroup(location);
    }
  }

  /**
   * Get a container group.
   *
   * @param location the location to get the container group for.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public ContainerGroup getContainerGroup(Location location) {
    ContainerGroup group = outputs.get(location);
    if (group == null) {
      group = moduleInputs.get(location);
      if (group == null) {
        group = packageInputs.get(location);
      }
    }

    return group;
  }

  /**
   * Get a package container group.
   *
   * @param location the location associated with the group to get.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public PackageContainerGroup getPackageContainerGroup(Location location) {
    return packageInputs.get(location);
  }

  /**
   * Get a snapshot of all the package container groups for inputs.
   *
   * @return the package container groups.
   */
  public Collection<PackageContainerGroup> getPackageContainerGroups() {
    return Set.copyOf(packageInputs.values());
  }

  /**
   * Get a package-oriented container group from the input packages or the outputs.
   *
   * <p>This also accepts {@link ModuleLocation module locations} and will resolve them
   * correctly.
   *
   * @param location the location associated with the group to get.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public PackageContainerGroup getPackageOrientedContainerGroup(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var group = getModuleOrientedContainerGroup(moduleLocation.getParent());
      return group == null
          ? null
          : group.getModule(moduleLocation.getModuleName());
    }

    var group = packageInputs.get(location);
    return group == null
        ? outputs.get(location)
        : group;
  }

  /**
   * Get a module container group.
   *
   * @param location the location associated with the group to get.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public ModuleContainerGroup getModuleContainerGroup(Location location) {
    return moduleInputs.get(location);
  }

  /**
   * Get a snapshot of all the module container groups for inputs.
   *
   * @return the module container groups.
   */
  public Collection<ModuleContainerGroup> getModuleContainerGroups() {
    return Set.copyOf(moduleInputs.values());
  }

  /**
   * Get a module-oriented container group from the input modules or the outputs.
   *
   * @param location the location associated with the group to get.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public ModuleContainerGroup getModuleOrientedContainerGroup(Location location) {
    var group = moduleInputs.get(location);
    return group == null ? outputs.get(location) : group;
  }

  /**
   * Get an output container group.
   *
   * @param location the location associated with the group to get.
   * @return the container group, or {@code null} if no group is associated with the location.
   */
  @Nullable
  public OutputContainerGroup getOutputContainerGroup(Location location) {
    return outputs.get(location);
  }

  /**
   * Get a snapshot of all the output container groups.
   *
   * @return the output container groups.
   */
  public Collection<OutputContainerGroup> getOutputContainerGroups() {
    return Set.copyOf(outputs.values());
  }

  /**
   * Determine if the given location is available in this repository.
   *
   * @param location the location to look up.
   * @return {@code true} if the location exists, or {@code false} if it does not.
   */
  public boolean hasLocation(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var parentLocation = moduleLocation.getParent();
      var group = parentLocation.isOutputLocation()
          ? outputs.get(parentLocation)
          : moduleInputs.get(parentLocation);

      if (group == null) {
        return false;
      }

      return group.hasLocation(moduleLocation);
    }

    if (location.isOutputLocation()) {
      return outputs.containsKey(location);
    }

    if (location.isModuleOrientedLocation()) {
      return moduleInputs.containsKey(location);
    }

    return packageInputs.containsKey(location);
  }

  /**
   * Find all {@link ModuleLocation module location objects} within a given module-oriented or
   * output location.
   *
   * @param location the location to discover modules within.
   * @return the set of module locations that were found.
   */
  public Set<Location> listLocationsForModules(Location location) {
    var group = location.isOutputLocation()
        ? outputs.get(location)
        : moduleInputs.get(location);

    return group == null
        ? Set.of()
        : group.getLocationsForModules();
  }

  private void addModulePath(ModuleLocation moduleLocation, PathRoot pathRoot) {
    var parentLocation = moduleLocation.getParent();

    var group = parentLocation.isOutputLocation()
        ? getOrCreateOutputContainerGroup(parentLocation)
        : getOrCreateModuleContainerGroup(parentLocation);

    group.addModule(moduleLocation.getModuleName(), pathRoot);
  }

  private void addModuleRoot(Location location, PathRoot pathRoot) {
    // Attempt to find the modules in the location.
    var modules = ModuleDiscoverer.findModulesIn(pathRoot.getPath());

    if (modules.isEmpty()) {
      LOGGER.warn(
          "There were no valid modules present in {}, so nothing has been registered. "
              + "If you are sure that there are files, this is probably a bug.",
          modules
      );
    } else {
      var group = location.isOutputLocation()
          ? getOrCreateOutputContainerGroup(location)
          : getOrCreateModuleContainerGroup(location);

      modules.forEach((moduleName, modulePath) -> {
        group.addModule(moduleName, new WrappingDirectoryImpl(modulePath));
      });
    }
  }

  private void addPackageRoot(Location location, PathRoot pathRoot) {
    // Simplest case. We just have a package.
    var group = location.isOutputLocation()
        ? getOrCreateOutputContainerGroup(location)
        : getOrCreatePackageContainerGroup(location);

    group.addPackage(pathRoot);
  }

  private void copyPackageContainers(Location from, Location to) {
    var source = from.isOutputLocation()
        ? outputs.get(from)
        : packageInputs.get(from);

    if (source == null || source.isEmpty()) {
      // Nothing to do.
      return;
    }
    var target = to.isOutputLocation()
        ? getOrCreateOutputContainerGroup(to)
        : getOrCreatePackageContainerGroup(to);

    source.getPackages().forEach(target::addPackage);
  }

  private void copyModuleContainers(Location from, Location to) {
    var source = from.isOutputLocation()
        ? outputs.get(from)
        : moduleInputs.get(from);

    if (source == null) {
      // Nothing to do.
      return;
    }

    var target = to.isOutputLocation()
        ? getOrCreateOutputContainerGroup(to)
        : getOrCreateModuleContainerGroup(to);

    source.getModules().forEach((moduleLocation, containerGroup) -> containerGroup
        .getPackages()
        .forEach(container -> target.addModule(moduleLocation.getModuleName(), container)));
  }

  @WillNotClose
  private PackageContainerGroup getOrCreatePackageContainerGroup(Location location) {
    return packageInputs.computeIfAbsent(
        location,
        packageLocation -> new PackageContainerGroupImpl(packageLocation, release)
    );
  }

  @WillNotClose
  private ModuleContainerGroup getOrCreateModuleContainerGroup(Location location) {
    return moduleInputs.computeIfAbsent(
        location,
        moduleLocation -> new ModuleContainerGroupImpl(moduleLocation, release)
    );
  }

  @WillNotClose
  private OutputContainerGroup getOrCreateOutputContainerGroup(Location location) {
    return outputs.computeIfAbsent(
        location,
        outputLocation -> new OutputContainerGroupImpl(outputLocation, release)
    );
  }

}
