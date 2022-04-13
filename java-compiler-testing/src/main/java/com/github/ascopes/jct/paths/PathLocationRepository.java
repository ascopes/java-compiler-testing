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

package com.github.ascopes.jct.paths;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Repository to store and manage path location managers.
 *
 * <p>This will treat {@link ModuleLocation} as a special case, resolving the parent manager
 * first.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PathLocationRepository implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathLocationRepository.class);

  private static final Comparator<Location> LOCATION_COMPARATOR = Comparator
      .comparing(Location::getName)
      .thenComparing(ModuleLocation.class::isInstance);

  private final Map<Location, ParentPathLocationManager> managers;

  /**
   * Initialize the repository.
   */
  public PathLocationRepository() {
    // We use a navigable map here as there is no concrete guarantee that all implementations of
    // Location will provide consistent equality and hashcode implementations, which may cause
    // us problems when dealing with equivalence.
    managers = new TreeMap<>(LOCATION_COMPARATOR);
  }

  /**
   * Close this repository by destroying any references it holds.
   *
   * <p>This will empty all locations. Any other resources you have opened must be freed
   * separately.
   */
  @Override
  public void close() {
    managers.clear();
  }

  /**
   * Determine if the given location is registered with the manager.
   *
   * @param location the location to look for.
   * @return {@code true} if registered, or {@code false} if not registered.
   */
  public boolean contains(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = ((ModuleLocation) location);
      var moduleName = moduleLocation.getModuleName();
      var manager = managers.get(moduleLocation.getParent());
      return manager != null && manager.hasModuleLocationManager(moduleName);
    }

    return managers.containsKey(location);
  }

  /**
   * Get the manager for a location, if it exists.
   *
   * @param location the location to look up.
   * @return the location manager, if present, or an empty optional if it does not exist.
   */
  public Optional<PathLocationManager> get(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var moduleName = moduleLocation.getModuleName();
      var parentLocation = moduleLocation.getParent();
      return Optional
          .ofNullable(managers.get(parentLocation))
          .flatMap(manager -> manager.getModuleLocationManager(moduleName));
    }

    return Optional.ofNullable(managers.get(location));
  }

  /**
   * Get the manager for a location, creating it first if it does not yet exist.
   *
   * <p>Non-module output locations that do not have a path already configured will automatically
   * have a {@link RamPath} created for them.
   *
   * @param location the location to look up.
   * @return the location manager.
   * @throws IllegalArgumentException if a {@link StandardLocation#MODULE_SOURCE_PATH} has already
   *                                  been created and this operation would create a {@link
   *                                  StandardLocation#SOURCE_PATH} location, or vice-versa.
   */
  public PathLocationManager getOrCreate(Location location) {
    ensureCompatibleLocation(location);

    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var moduleName = moduleLocation.getModuleName();
      var parentLocation = moduleLocation.getParent();
      return managers
          .computeIfAbsent(parentLocation, unused -> new ParentPathLocationManager(location))
          .getOrCreateModuleLocationManager(moduleName);
    }

    return managers
        .computeIfAbsent(location, unused -> createParentPathLocationManager(location));
  }

  /**
   * Get a map of all locations in this repository mapped to their respective paths in the order
   * those paths are considered.
   *
   * @return the map of locations to lists of paths.
   */
  public SortedMap<Location, List<? extends Path>> getAllPaths() {
    return managers
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue().getPaths(),
            (a, b) -> {
              if (Objects.equals(a, b)) {
                throw new IllegalStateException("Unexpected duplicate entries a=" + a + ", b=" + b);
              }
              return a;
            },
            () -> new TreeMap<>(LOCATION_COMPARATOR)
        ));
  }

  @Override
  public String toString() {
    return "PathLocationManagerRepository{}";
  }

  private void ensureCompatibleLocation(Location location) {
    if (location instanceof ModuleLocation) {
      location = ((ModuleLocation) location).getParent();
    }

    if (managers.containsKey(StandardLocation.SOURCE_PATH)
        && location.equals(StandardLocation.MODULE_SOURCE_PATH)) {
      throw new IllegalArgumentException(
          "A source-path location has already been registered, meaning the compiler will run in "
              + "legacy compilation mode. This means you cannot add a module-source-path to this "
              + "configuration as well");
    }

    if (managers.containsKey(StandardLocation.MODULE_SOURCE_PATH)
        && location.equals(StandardLocation.SOURCE_PATH)) {
      throw new IllegalArgumentException(
          "A module-source-path location has already been registered, meaning the compiler will "
              + "run in multi-module compilation mode. This means you cannot add a source-path to "
              + "this configuration as well");
    }
  }

  private ParentPathLocationManager createParentPathLocationManager(Location location) {
    var manager = new ParentPathLocationManager(location);

    if (location.isOutputLocation()) {
      var ramPath = RamPath.createPath(true);
      LOGGER.debug(
          "Implicitly created new in-memory path {} for output location {}",
          ramPath,
          location.getName()
      );
      manager.addRamPath(ramPath);
    }

    return manager;
  }
}
