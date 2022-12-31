/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Implementation of a workspace to use by default.
 *
 * <p>This is not threadsafe, and should only be used once per test.
 *
 * <p>Care should be taken to use instances of this class in a try-with-resources block
 * to ensure resources get closed correctly at the end of your tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class WorkspaceImpl implements Workspace {

  private final PathStrategy pathStrategy;
  private final Map<Location, List<PathRoot>> paths;

  public WorkspaceImpl(PathStrategy pathStrategy) {
    this.pathStrategy = requireNonNull(pathStrategy, "pathStrategy");
    paths = new HashMap<>();
  }

  @Override
  public void close() {
    // Close everything in a best-effort fashion.
    var exceptions = new ArrayList<Throwable>();

    for (var list : paths.values()) {
      for (var path : list) {
        if (path instanceof AbstractManagedDirectory) {
          try {
            ((AbstractManagedDirectory) path).close();

          } catch (Exception ex) {
            exceptions.add(ex);
          }
        }
      }
    }

    if (exceptions.size() > 0) {
      var newEx = new IllegalStateException("One or more components failed to close");
      exceptions.forEach(newEx::addSuppressed);
      throw newEx;
    }
  }

  @Override
  public void addPackage(Location location, Path path) {
    requireNonNull(location, "location");
    requireNonNull(path, "path");

    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException("Location must not be module-oriented");
    }

    if (!Files.exists(path)) {
      throw new IllegalArgumentException("Path " + path + " does not exist");
    }

    var dir = new WrappingDirectoryImpl(path);
    paths.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
  }

  @Override
  public void addModule(Location location, String moduleName, Path path) {
    requireNonNull(location, "location");
    requireNonNull(location, "moduleName");
    requireNonNull(path, "path");

    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Cannot add a module to a non-module-oriented or non-output location"
      );
    }

    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException("Cannot register a module within a module");
    }

    addPackage(new ModuleLocation(location, moduleName), path);
  }

  @Override
  public ManagedDirectory createPackage(Location location) {
    requireNonNull(location, "location");

    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException("Location must not be module-oriented");
    }

    var dir = pathStrategy.newInstance(location.getName());
    paths.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
    return dir;
  }

  @Override
  public ManagedDirectory createModule(Location location, String moduleName) {
    requireNonNull(location, "location");
    requireNonNull(location, "moduleName");

    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Cannot add a module to a non-module-oriented or non-output location"
      );
    }

    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException("Cannot register a module within a module");
    }

    return createPackage(new ModuleLocation(location, moduleName));
  }

  @Override
  public Map<Location, List<? extends PathRoot>> getAllPaths() {
    // Create an immutable copy.
    var pathsCopy = new HashMap<Location, List<PathRoot>>();
    paths.forEach((location, list) -> pathsCopy.put(location, List.copyOf(list)));
    return Collections.unmodifiableMap(pathsCopy);
  }

  @Override
  public PathStrategy getPathStrategy() {
    return pathStrategy;
  }
}
