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
package io.github.ascopes.jct.jsr199;

import io.github.ascopes.jct.jsr199.groups.ModuleContainerGroup;
import io.github.ascopes.jct.jsr199.groups.OutputContainerGroup;
import io.github.ascopes.jct.jsr199.groups.PackageContainerGroup;
import io.github.ascopes.jct.paths.PathLike;
import java.util.Collection;
import java.util.Optional;
import javax.tools.JavaFileManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Extension around a {@link JavaFileManager} that allows adding of {@link PathLike} objects to the
 * manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface FileManager extends JavaFileManager {

  /**
   * Add a path to a given location.
   *
   * @param location the location to use.
   * @param path     the path to add.
   */
  void addPath(Location location, PathLike path);

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
  void ensureEmptyLocationExists(Location location);

  /**
   * Copy all containers from the first location to the second location.
   *
   * @param from the first location.
   * @param to   the second location.
   */
  void copyContainers(Location from, Location to);

  /**
   * Get the container group for the given package-oriented location.
   *
   * @param location the package oriented location.
   * @return the container group, or an empty optional if one does not exist.
   */
  Optional<PackageContainerGroup> getPackageContainerGroup(Location location);

  /**
   * Get a collection of all package container groups in this file manager.
   *
   * @return the package container groups.
   */
  Collection<PackageContainerGroup> getPackageContainerGroups();


  /**
   * Get the container group for the given module-oriented location.
   *
   * @param location the module oriented location.
   * @return the container group, or an empty optional if one does not exist.
   */
  Optional<ModuleContainerGroup> getModuleContainerGroup(Location location);

  /**
   * Get a collection of all module container groups in this file manager.
   *
   * @return the module container groups.
   */
  Collection<ModuleContainerGroup> getModuleContainerGroups();

  /**
   * Get the container group for the given output-oriented location.
   *
   * @param location the output oriented location.
   * @return the container group, or an empty optional if one does not exist.
   */
  Optional<OutputContainerGroup> getOutputContainerGroup(Location location);

  /**
   * Get a collection of all output container groups in this file manager.
   *
   * @return the output container groups.
   */
  Collection<OutputContainerGroup> getOutputContainerGroups();
}
