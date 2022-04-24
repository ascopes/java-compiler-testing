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

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
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

  private final PathJavaFileObjectFactory factory;

  private static final Comparator<Location> LOCATION_COMPARATOR = Comparator
      .comparing(Location::getName)
      .thenComparing(ModuleLocation.class::isInstance);

  private final Map<Location, ParentPathLocationManager> managers;

  /**
   * Initialize the repository.
   *
   * @param factory the factory to use to create {@link PathJavaFileObject} instances.
   */
  public PathLocationRepository(PathJavaFileObjectFactory factory) {
    // We use a navigable map here as there is no concrete guarantee that all implementations of
    // Location will provide consistent equality and hashcode implementations, which may cause
    // us problems when dealing with equivalence.
    this.factory = requireNonNull(factory);
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
    requireNonNull(location);

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
    requireNonNull(location);

    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var moduleName = moduleLocation.getModuleName();
      var parentLocation = moduleLocation.getParent();
      return get(parentLocation)
          .map(ParentPathLocationManager.class::cast)
          .flatMap(manager -> manager.getModuleLocationManager(moduleName));
    }

    LOGGER.trace("Attempting to get location manager for location {}", location.getName());
    var manager = managers.get(location);
    LOGGER.trace(
        "Location manager for location {} was {}",
        location.getName(),
        manager == null ? "not present" : "present"
    );

    return Optional.ofNullable(manager);
  }

  /**
   * Get the manager for a location, if it exists, or throw an exception if it doesn't.
   *
   * @param location the location to look up.
   * @return the location manager.
   * @throws NoSuchElementException if the manager is not found.
   */
  public PathLocationManager getExpected(Location location) {
    requireNonNull(location);

    return get(location)
        .orElseThrow(() -> new NoSuchElementException(
            "No location manager for location " + location.getName() + " was found"
        ));
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
   *                                  been created and this operation would create a
   *                                  {@link StandardLocation#SOURCE_PATH} location, or vice-versa.
   */
  public PathLocationManager getOrCreate(Location location) {
    requireNonNull(location);
    ensureCompatibleLocation(location);

    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      var moduleName = moduleLocation.getModuleName();
      var parentLocation = moduleLocation.getParent();
      return ((ParentPathLocationManager) getOrCreate(parentLocation))
          .getOrCreateModuleLocationManager(moduleName);
    }

    return managers
        .computeIfAbsent(location, unused -> createParentPathLocationManager(location));
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
    var manager = new ParentPathLocationManager(factory, location);

    if (location.isOutputLocation()) {
      var ramPath = RamPath.createPath(
          location.getName() + "-" + UUID.randomUUID(),
          true
      );
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
