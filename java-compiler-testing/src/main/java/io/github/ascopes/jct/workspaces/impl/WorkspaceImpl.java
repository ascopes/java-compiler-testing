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
package io.github.ascopes.jct.workspaces.impl;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.tools.JavaFileManager.Location;

/**
 * Implementation of a workspace to use by default.
 *
 * <p>This is not thread safe, and should only be used once per test.
 *
 * <p>Care should be taken to use instances of this class in a try-with-resources block
 * to ensure resources get closed correctly at the end of your tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class WorkspaceImpl implements Workspace {

  private volatile boolean closed;
  private final String id;
  private final PathStrategy pathStrategy;
  private final Map<Location, List<PathRoot>> locations;

  /**
   * Initialise this workspace.
   *
   * @param pathStrategy the path strategy to use for creating source and target paths.
   */
  public WorkspaceImpl(PathStrategy pathStrategy) {
    id = UUID.randomUUID().toString();
    closed = false;
    this.pathStrategy = requireNonNull(pathStrategy, "pathStrategy");
    locations = new HashMap<>();
  }

  @Override
  public void close() {
    try {
      // Close everything in a best-effort fashion.
      var exceptions = new ArrayList<Throwable>();

      for (var list : locations.values()) {
        for (var path : list) {
          if (path instanceof AbstractManagedDirectory dir) {
            try {
              dir.close();

            } catch (Exception ex) {
              exceptions.add(ex);
            }
          }
        }
      }

      if (!exceptions.isEmpty()) {
        var newEx = new IllegalStateException("One or more components failed to close");
        exceptions.forEach(newEx::addSuppressed);
        throw newEx;
      }
    } finally {
      closed = true;
    }
  }

  @Override
  public void dump(Appendable appendable) {
    new WorkspaceDumper(appendable).dump(toString(), locations);
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void addPackage(Location location, Path path) {
    requireNonNull(location, "location");
    requireNonNull(path, "path");

    if (location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException("Location must not be module-oriented");
    }

    if (!Files.exists(path)) {
      throw new JctIllegalInputException("Path " + path + " does not exist");
    }

    var dir = new WrappingDirectoryImpl(path);
    locations.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
  }

  @Override
  public void addModule(Location location, String moduleName, Path path) {
    requireNonNull(location, "location");
    requireNonNull(location, "moduleName");
    requireNonNull(path, "path");

    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw new JctIllegalInputException(
          "Cannot add a module to a non-module-oriented or non-output location"
      );
    }

    if (location instanceof ModuleLocation) {
      throw new JctIllegalInputException("Cannot register a module within a module");
    }

    addPackage(new ModuleLocation(location, moduleName), path);
  }

  @Override
  public ManagedDirectory createPackage(Location location) {
    requireNonNull(location, "location");

    if (location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException("Location must not be module-oriented");
    }

    // Needs to be unique.
    var fsName = location.getName().replaceAll("[^A-Za-z0-9]", "")
        + UUID.randomUUID();

    var dir = pathStrategy.newInstance(fsName);
    locations.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
    return dir;
  }

  @Override
  public ManagedDirectory createModule(Location location, String moduleName) {
    requireNonNull(location, "location");
    requireNonNull(location, "moduleName");

    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw new JctIllegalInputException(
          "Cannot add a module to a non-module-oriented or non-output location"
      );
    }

    if (location instanceof ModuleLocation) {
      throw new JctIllegalInputException("Cannot register a module within a module");
    }

    return createPackage(new ModuleLocation(location, moduleName));
  }

  @Override
  public Map<Location, List<? extends PathRoot>> getAllPaths() {
    // Create an immutable copy.
    var locationsCopy = new HashMap<Location, List<PathRoot>>();
    locations.forEach((location, list) -> locationsCopy.put(location, List.copyOf(list)));
    return unmodifiableMap(locationsCopy);
  }

  @Override
  public List<? extends PathRoot> getModule(Location location, String moduleName) {
    if (location instanceof ModuleLocation) {
      throw new JctIllegalInputException("Use .getPackages(ModuleLocation) for module locations");
    }

    if (!location.isOutputLocation() && !location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be module-oriented or an output location"
      );
    }

    var moduleLocation = new ModuleLocation(location, moduleName);
    return getPackages(moduleLocation);
  }

  @Override
  public Map<String, List<? extends PathRoot>> getModules(Location location) {
    if (location instanceof ModuleLocation) {
      throw new JctIllegalInputException("Cannot pass a ModuleLocation to this method");
    }

    if (!location.isOutputLocation() && !location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be module-oriented or an output location"
      );
    }

    var results = new HashMap<String, List<PathRoot>>();

    locations.forEach((pathLocation, pathRoots) -> {
      if (pathLocation instanceof ModuleLocation modulePathLocation) {
        if (modulePathLocation.getParent().equals(location)) {
          results.computeIfAbsent(modulePathLocation.getModuleName(), name -> new ArrayList<>())
              .addAll(pathRoots);
        }
      }
    });

    // Create an immutable view of both dimensions.
    var resultsCopy = new HashMap<String, List<? extends PathRoot>>();
    results.forEach((loc, roots) -> resultsCopy.put(loc, List.copyOf(roots)));
    return Map.copyOf(resultsCopy);
  }

  @Override
  public PathStrategy getPathStrategy() {
    return pathStrategy;
  }

  @Override
  public List<? extends PathRoot> getPackages(Location location) {
    if (location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must not be module-oriented"
      );
    }

    var roots = locations.get(location);
    return roots == null
        ? List.of()
        : List.copyOf(roots);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("id", id)
        .attribute("pathStrategy", pathStrategy)
        .attribute("closed", closed)
        .attribute("numberOfLocations", locations.size())
        .toString();
  }
}
