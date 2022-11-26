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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.tools.JavaFileManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Extension around a {@link JavaFileManager} that allows adding of {@link PathWrapper} objects to
 * the manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface JctFileManager extends JavaFileManager {

  /**
   * Add a path to a given location.
   *
   * <p>If the file manager is closed, this will raise an exception.
   *
   * @param location the location to use.
   * @param path     the path to add.
   * @throws IllegalStateException if the file manager is {@link #isClosed() closed}.
   */
  void addPath(Location location, PathWrapper path);

  /**
   * Copy all containers from the first location to the second location.
   *
   * @param from the first location.
   * @param to   the second location.
   * @throws IllegalStateException if the file manager is {@link #isClosed() closed}.
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
   * @throws IllegalStateException if the file manager is {@link #isClosed() closed}.
   */
  void ensureEmptyLocationExists(Location location);

  /**
   * Get the container group for the given package-oriented location.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @param location the package oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  PackageContainerGroup getPackageContainerGroup(Location location);

  /**
   * Get a collection of all package container impl in this file manager.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @return the package container impl.
   */
  Collection<PackageContainerGroup> getPackageContainerGroups();

  /**
   * Get the container group for the given module-oriented location.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @param location the module oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  ModuleContainerGroup getModuleContainerGroup(Location location);

  /**
   * Get a collection of all module container impl in this file manager.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @return the module container impl.
   */
  Collection<ModuleContainerGroup> getModuleContainerGroups();

  /**
   * Get the container group for the given output-oriented location.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @param location the output oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  OutputContainerGroup getOutputContainerGroup(Location location);

  /**
   * Get a collection of all output container impl in this file manager.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @return the output container impl.
   */
  Collection<OutputContainerGroup> getOutputContainerGroups();

  /**
   * Determine if the {@link #close()} method has been called on this file manager or not.
   *
   * <p>This can safely be called on a closed file manager.
   *
   * @return {@code false} if the file manager is open, or {@code true} if it is closed.
   */
  boolean isClosed();
}
