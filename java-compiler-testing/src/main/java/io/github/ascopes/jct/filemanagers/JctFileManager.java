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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Extension around a {@link JavaFileManager} that allows adding of {@link PathRoot} objects to the
 * manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface JctFileManager extends JavaFileManager {

  /**
   * Add a path to a given location.
   *
   * @param location the location to use.
   * @param path     the path to add.
   */
  void addPath(Location location, PathRoot path);

  /**
   * Add a collection of paths to a given location.
   *
   * @param location the location to use.
   * @param paths    the paths to add.
   */
  void addPaths(Location location, Collection<? extends PathRoot> paths);

  /**
   * Copy all containers from the first location to the second location.
   *
   * @param from the first location.
   * @param to   the second location.
   */
  void copyContainers(Location from, Location to);

  /**
   * Register an empty container for the given location to indicate to the compiler that the feature
   * exists, but has no configured paths.
   *
   * <p>This is needed to coerce the behaviour for annotation processing in some cases.
   *
   * <p>If the location already exists, then do not do anything.
   *
   * @param location the location to apply an empty container for.
   */
  void createEmptyLocation(Location location);

  /**
   * Get the associated effective release.
   *
   * @return the effective release.
   * @since 0.0.1 (0.0.1-M8)
   */
  String getEffectiveRelease();

  /**
   * Get the container group for the given package-oriented location.
   *
   * @param location the package oriented location.
   * @return the container group, or null if one does not exist.
   */
  PackageContainerGroup getPackageContainerGroup(Location location);

  /**
   * Get a collection of all package container impl in this file manager.
   *
   * @return the package container impl.
   */
  Collection<PackageContainerGroup> getPackageContainerGroups();

  /**
   * Get the container group for the given module-oriented location.
   *
   * @param location the module oriented location.
   * @return the container group, or null if one does not exist.
   */
  ModuleContainerGroup getModuleContainerGroup(Location location);

  /**
   * Get a collection of all module container impl in this file manager.
   *
   * @return the module container impl.
   */
  Collection<ModuleContainerGroup> getModuleContainerGroups();

  /**
   * Get the container group for the given output-oriented location.
   *
   * @param location the output oriented location.
   * @return the container group, or null if one does not exist.
   */
  OutputContainerGroup getOutputContainerGroup(Location location);

  /**
   * Get a collection of all output container impl in this file manager.
   *
   * @return the output container impl.
   */
  Collection<OutputContainerGroup> getOutputContainerGroups();

  /**
   * List all file objects in the given location matching the given criteria.
   *
   * @param location    the location to search in.
   * @param packageName the package name to search in, or {@code ""} to search in the root
   *                    location.
   * @param kinds       the kinds of file to return, or {@link Set#of} {@code (} {@link Kind#OTHER}
   *                    {@code )} to find all files.
   * @param recurse     {@code true} to recurse into subpackages, {@code false} to only check the
   *                    current package.
   * @return a collection of unique file objects that were found.
   * @throws IOException if an IO error occurs reading the file system.
   */
  @Override
  Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException;
}
