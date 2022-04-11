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

import com.github.ascopes.jct.intern.StringUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A path location manager that also supports having nested modules.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class ParentPathLocationManager extends PathLocationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParentPathLocationManager.class);

  private final Map<String, PathLocationManager> modules;

  /**
   * Initialize the manager.
   *
   * @param location the location to represent.
   */
  public ParentPathLocationManager(Location location) {
    super(location);
    modules = new TreeMap<>();
  }

  /**
   * Get a collection of all module managers in this location.
   *
   * <p>If not applicable, this will be an empty collection.
   *
   * @return the collection of module managers.
   */
  public Collection<PathLocationManager> getModuleManagers() {
    return modules.values();
  }

  /**
   * Get the module location for the given module name.
   *
   * @param moduleName the module name.
   * @return the module location.
   */
  public ModuleLocation getModuleLocationFor(String moduleName) {
    // These are lightweight opaque descriptors, so duplicates should not really matter.
    // We only create the backing file trees for these as-needed, too.
    return new ModuleLocation(getLocation(), moduleName);
  }

  /**
   * Get the module location for the given file object, if it exists in any of the given roots.
   *
   * @param fileObject the file object to get the module location for.
   * @return the module location if known, or an empty optional otherwise.
   */
  public Optional<ModuleLocation> getModuleLocationFor(FileObject fileObject) {
    var path = Path.of(fileObject.toUri());

    return roots
        .stream()
        .filter(path::startsWith)
        .map(root -> root.relativize(path))
        .map(Path::iterator)
        .map(Iterator::next)
        .map(Path::toString)
        .filter(modules::containsKey)
        .map(this::getModuleLocationFor)
        .findFirst();
  }

  /**
   * Get or create the module location manager for the given module name.
   *
   * @param moduleName the module name.
   * @return the manager if found, or an empty optional if it does not exist.
   */
  public Optional<PathLocationManager> getModuleLocationManager(String moduleName) {
    return Optional.ofNullable(modules.get(moduleName));
  }

  /**
   * Get or create the module location manager for the given module name.
   *
   * @param moduleName the module name.
   * @return the manager.
   * @throws IllegalArgumentException if this object is already for a module.
   */
  public PathLocationManager getOrCreateModuleLocationManager(String moduleName) {
    return modules.computeIfAbsent(
        moduleName,
        this::buildLocationManagerForModule
    );
  }

  /**
   * Determine if this manager has a nested module manager.
   *
   * @param moduleName the name of the module.
   * @return {@code true} if the nested manager is present, or {@code false} otherwise.
   */
  public boolean hasModuleLocationManager(String moduleName) {
    return modules.containsKey(moduleName);
  }

  /**
   * Get all module locations within this location.
   *
   * @return the module locations as a set.
   */
  public Set<Location> listLocationsForModules() {
    return modules
        .values()
        .stream()
        .map(PathLocationManager::getLocation)
        .collect(Collectors.toSet());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{"
        + "location=" + StringUtils.quoted(location.getName()) + ", "
        + "modules=" + StringUtils.quotedIterable(modules.keySet())
        + "}";
  }


  @Override
  protected void registerPath(Path path) {
    if (isPathCapableOfHoldingModules(path)) {
      // If this path can contain modules, then we should convert that to a module location
      // and store the path there. This just prevent any mistakes where we accidentally put a
      // module in a module oriented location rather than in the corresponding submodule.
      try {
        Files
            .list(path)
            .peek(next -> LOGGER.trace("Checking if {} is a source module", next))
            .filter(Files::isDirectory)
            .filter(next -> Files.isRegularFile(next.resolve("module-info.java")))
            .forEach(module -> {
              var name = module.getFileName().toString();
              LOGGER.debug("Found candidate module {} at {}", name, module);
              var manager = getOrCreateModuleLocationManager(name);
              // Register the first path.
              manager.addPath(module);
            });
      } catch (IOException ex) {
        throw new UncheckedIOException("Failed to read files in " + path, ex);
      }
    }

    super.registerPath(path);
  }

  private PathLocationManager buildLocationManagerForModule(
      String moduleName
  ) {
    var moduleLocation = new ModuleLocation(location, moduleName);
    var moduleManager = new PathLocationManager(moduleLocation);

    roots
        .stream()
        .map(root -> root.resolve(moduleName))
        .forEach(moduleManager::addPath);

    return moduleManager;
  }

  private boolean isPathCapableOfHoldingModules(Path path) {
    // TODO(ascopes): do we want to consider output locations as modules?
    return (location.isModuleOrientedLocation() || location.isOutputLocation())
        && Files.isDirectory(path);
  }
}
