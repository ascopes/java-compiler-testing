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
package io.github.ascopes.jct.containers;

import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * A container group implementation that holds zero or more modules.
 *
 * <p>These modules can be accessed by their module name. Each one holds a separate group of
 * containers, and has the ability to produce a custom class loader.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface ModuleContainerGroup extends ContainerGroup {

  /**
   * Add a container to this group.
   *
   * <p>The provided container will be closed when this container group is closed.
   *
   * @param module    the module that the container is for.
   * @param container the container to add.
   */
  void addModule(String module, Container container);

  /**
   * Add a path to this group for a module.
   *
   * @param module the name of the module that this is for.
   * @param path   the path to add.
   */
  void addModule(String module, PathRoot path);

  /**
   * Find the package container group for the given module.
   *
   * @param module the module name.
   * @return the container group, or {@code null} if no module was found by that name.
   */
  @Nullable
  PackageContainerGroup getModule(String module);

  /**
   * Get the {@link PackageContainerGroup} for a given module name, creating it if it does not yet
   * exist.
   *
   * @param moduleName the module name to look up.
   * @return the container group.
   */
  PackageContainerGroup getOrCreateModule(String moduleName);

  /**
   * Get the module-oriented location.
   *
   * @return the module-oriented location.
   */
  @Override
  Location getLocation();

  /**
   * Get all locations that are modules.
   *
   * @return the locations that are modules.
   */
  Set<Location> getLocationsForModules();

  /**
   * Get the module container impl in this group.
   *
   * @return the container impl.
   */
  Map<ModuleLocation, PackageContainerGroup> getModules();

  /**
   * Determine if this group contains a given module.
   *
   * @param location the module location to look for.
   * @return {@code true} if present, or {@code false} if not.
   */
  boolean hasLocation(ModuleLocation location);
}
