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
package io.github.ascopes.jct.containers.impl;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * A group of containers that relate to a specific output location.
 *
 * <p>These can contain packages <strong>and</strong> modules of packages together, and thus
 * are slightly more complicated internally as a result.
 *
 * <p>Operations on modules should first {@link #getModule(String) get} or
 * {@link #getOrCreateModule(String) create} the module, and then operate on that sub-container
 * group. Operations on non-module packages should operate on this container group directly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class OutputContainerGroupImpl
    extends AbstractPackageContainerGroup
    implements OutputContainerGroup {

  private final Map<ModuleLocation, PackageContainerGroup> modules;

  /**
   * Initialize this container group.
   *
   * @param location the location of the group.
   * @param release  the release version.
   */
  public OutputContainerGroupImpl(Location location, String release) {
    super(location, release);
    modules = new HashMap<>();

    if (location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Cannot use module-oriented locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }

    if (!location.isOutputLocation()) {
      throw new JctIllegalInputException(
          "Cannot use non-output locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }
  }

  @Override
  public void addPackage(PathRoot path) {
    if (super.isEmpty()) {
      super.addPackage(path);
    } else {
      throw packageAlreadySpecified(path);
    }
  }

  @Override
  public void addPackage(Container container) {
    if (super.isEmpty()) {
      super.addPackage(container);
    } else {
      throw packageAlreadySpecified(container.getPathRoot());
    }
  }

  @Override
  public void addModule(String module, Container container) {
    getOrCreateModule(module).addPackage(container);
  }

  @Override
  public void addModule(String module, PathRoot path) {
    getOrCreateModule(module).addPackage(path);
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var location = getLocation();

    if (location instanceof ModuleLocation) {
      var module = modules.get(location);
      return module != null && module.contains(fileObject);
    }

    return super.contains(fileObject);
  }

  @Nullable
  @Override
  public PackageContainerGroup getModule(String module) {
    if (module.isEmpty()) {
      // We are a package container group internally.
      return this;
    }

    return modules
        .keySet()
        .stream()
        .filter(location -> location.getModuleName().equals(module))
        .findFirst()
        .map(modules::get)
        .orElse(null);
  }

  @Override
  public Set<Location> getLocationsForModules() {
    return Set.copyOf(modules.keySet());
  }

  @Override
  public Map<ModuleLocation, PackageContainerGroup> getModules() {
    return Map.copyOf(modules);
  }

  @Override
  public PackageContainerGroup getOrCreateModule(String moduleName) {
    var location = getLocation();
    return modules.computeIfAbsent(new ModuleLocation(location, moduleName), this::newPackageGroup);
  }

  @Override
  public boolean hasLocation(ModuleLocation location) {
    return modules.containsKey(location);
  }

  @SuppressWarnings("resource")
  private PackageContainerGroup newPackageGroup(ModuleLocation moduleLocation) {
    // For output locations, we only need the first root. We then just put a subdirectory
    // in there, as it reduces the complexity of this tenfold and means we don't have to
    // worry about creating more in-memory locations on the fly.
    //
    // The reason we have to do this relates to the fact that we will be provided an output
    // directory to write to regardless of whether we want to write out modules or normal
    // packages.
    var release = getRelease();
    var packages = getPackages();

    if (packages.isEmpty()) {
      // This *shouldn't* be reachable in most cases.
      throw new JctIllegalInputException(
          "Cannot add module " + moduleLocation + " to outputs. No output path has been "
              + "provided for this location! Please register a package path to output generated "
              + "modules to first before running the compiler."
      );
    }

    // Use an anonymous class here to avoid the constraints that the PackageContainerGroupImpl
    // imposes on us.
    var group = new AbstractPackageContainerGroup(moduleLocation, release) {};
    var pathWrapper = new WrappingDirectoryImpl(
        getPackages().iterator().next().getPathRoot(),
        moduleLocation.getModuleName()
    );
    uncheckedIo(() -> Files.createDirectories(pathWrapper.getPath()));
    group.addPackage(pathWrapper);
    return group;
  }

  @SuppressWarnings("resource")
  private JctIllegalInputException packageAlreadySpecified(PathRoot newPathRoot) {
    var existingPathRoot = getPackages().iterator().next().getPathRoot();
    return new JctIllegalInputException(
        "Cannot add a new package (" + newPathRoot + ") to this output container group because "
            + "a package has already been specified (" + existingPathRoot + ")"
    );
  }
}
