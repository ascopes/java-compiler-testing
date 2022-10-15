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
package io.github.ascopes.jct.jsr199.containers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.jsr199.ModuleLocation;
import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.paths.SubPath;
import io.github.ascopes.jct.utils.IoExceptionUtils;
import io.github.ascopes.jct.utils.ModulePrefix;
import io.github.ascopes.jct.utils.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A group of containers that relate to a specific output location.
 *
 * <p>These can contain packages <strong>and</strong> modules of packages together, and thus
 * are slightly more complicated internally as a result.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class OutputContainerGroupImpl
    extends AbstractPackageContainerGroup
    implements OutputContainerGroup {

  private final Location location;
  private final Map<ModuleLocation, SimpleOutputModuleContainerGroup> modules;

  /**
   * Initialize this container group.
   *
   * @param location the location of the group.
   * @param release  the release version.
   */
  public OutputContainerGroupImpl(Location location, String release) {
    super(release);
    this.location = requireNonNull(location, "location");
    modules = new HashMap<>();

    if (location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use module-oriented locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }

    if (!location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use non-output locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }
  }

  @Override
  public void addModule(String module, Container container) {
    getOrCreateModule(module).addPackage(container);
  }

  @Override
  public void addModule(String module, PathLike path) {
    getOrCreateModule(module).addPackage(path);
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    if (location instanceof ModuleLocation) {
      return Optional
          .ofNullable(modules.get(location))
          .map(module -> module.contains(fileObject))
          .orElse(false);
    }

    return super.contains(fileObject);
  }

  @Override
  public Optional<Path> findFile(String path) {
    return ModulePrefix
        .tryExtract(path)
        .flatMap(prefix -> getOrCreateModule(prefix.getModuleName())
            .findFile(prefix.getRest()))
        .or(() -> super.findFile(path));
  }

  @Override
  public Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    // TODO(ascopes): can we have modules conceptually in this method call?
    return ModulePrefix
        .tryExtract(packageName)
        .flatMap(prefix -> getOrCreateModule(prefix.getModuleName())
            .getFileForInput(prefix.getModuleName(), relativeName))
        .or(() -> super.getFileForInput(packageName, relativeName));
  }

  @Override
  public Optional<PathFileObject> getFileForOutput(String packageName, String relativeName) {
    // TODO(ascopes): can we have modules conceptually in this method call?
    return ModulePrefix
        .tryExtract(packageName)
        .flatMap(prefix -> getOrCreateModule(prefix.getModuleName())
            .getFileForOutput(prefix.getRest(), relativeName))
        .or(() -> super.getFileForOutput(packageName, relativeName));
  }

  @Override
  public Optional<PathFileObject> getJavaFileForInput(String className, Kind kind) {
    return ModulePrefix
        .tryExtract(className)
        .flatMap(prefix -> getOrCreateModule(prefix.getModuleName())
            .getJavaFileForInput(prefix.getRest(), kind))
        .or(() -> super.getJavaFileForInput(className, kind));
  }

  @Override
  public Optional<PathFileObject> getJavaFileForOutput(String className, Kind kind) {
    return ModulePrefix
        .tryExtract(className)
        .flatMap(prefix -> getOrCreateModule(prefix.getModuleName())
            .getJavaFileForOutput(prefix.getRest(), kind))
        .or(() -> super.getJavaFileForOutput(className, kind));
  }

  @Override
  public PackageContainerGroup getOrCreateModule(String moduleName) {
    return modules.computeIfAbsent(
        new ModuleLocation(location, moduleName),
        moduleLocation -> {
          // For output locations, we only need the first root. We then just put a subdirectory
          // in there, as it reduces the complexity of this tenfold and means we don't have to
          // worry about creating more in-memory locations on the fly.
          var group = new SimpleOutputModuleContainerGroup(moduleLocation);
          var path = new SubPath(getPackages().iterator().next().getPath(), moduleName);
          IoExceptionUtils.uncheckedIo(() -> Files.createDirectories(path.getPath()));
          group.addPackage(path);
          return group;
        });
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public List<Set<Location>> getLocationsForModules() {
    return List.of(Set.copyOf(modules.keySet()));
  }

  @Override
  public Map<ModuleLocation, ? extends PackageContainerGroup> getModules() {
    return null;
  }

  @Override
  public boolean hasLocation(ModuleLocation location) {
    return modules.containsKey(location);
  }

  @Override
  protected ContainerClassLoader createClassLoader() {
    var moduleMapping = modules
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> entry.getKey().getModuleName(),
            entry -> entry.getValue().getPackages()
        ));

    return new ContainerClassLoader(location, getPackages(), moduleMapping);
  }

  /**
   * Wrapper around a location that lacks the constraints that {@link PackageContainerGroupImpl}
   * imposes.
   */
  private class SimpleOutputModuleContainerGroup
      extends AbstractPackageContainerGroup {

    private final Location location;

    private SimpleOutputModuleContainerGroup(Location location) {
      super(OutputContainerGroupImpl.this.release);
      this.location = requireNonNull(location, "location");
    }

    @Override
    public Location getLocation() {
      return location;
    }
  }
}
