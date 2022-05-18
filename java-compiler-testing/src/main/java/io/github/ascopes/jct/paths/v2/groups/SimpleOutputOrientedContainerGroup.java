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
package io.github.ascopes.jct.paths.v2.groups;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.ModulePrefix;
import io.github.ascopes.jct.paths.ModuleLocation;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.paths.v2.PathFileObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
public class SimpleOutputOrientedContainerGroup
    extends AbstractPackageOrientedContainerGroup
    implements OutputOrientedContainerGroup {

  private final Location location;
  private final Map<ModuleLocation, PackageOrientedContainerGroup> modules;

  /**
   * Initialize this container group.
   *
   * @param location the location of the group.
   * @param release  the release version.
   */
  public SimpleOutputOrientedContainerGroup(Location location, String release) {
    super(release);
    this.location = requireNonNull(location, "location");
    modules = new HashMap<>();

    if (location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use module-oriented locations with this container group"
      );
    }

    if (!location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use non-output locations with this container group"
      );
    }
  }

  @Override
  @SuppressWarnings("resource")
  public void addPath(Path path, String module) throws IOException {
    forModule(module).addPath(path);
  }

  @Override
  @SuppressWarnings("resource")
  public void addPath(RamPath ramPath, String module) throws IOException {
    forModule(module).addPath(ramPath);
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
  @SuppressWarnings("resource")
  public Optional<Path> findFile(String path) {
    return ModulePrefix
        .tryExtract(path)
        .flatMap(prefix -> forModule(prefix.getModuleName())
            .findFile(prefix.getRest()))
        .or(() -> super.findFile(path));
  }

  @Override
  @SuppressWarnings("resource")
  public Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    // TODO(ascopes): can we have modules conceptually in this method call?
    return ModulePrefix
        .tryExtract(packageName)
        .flatMap(prefix -> forModule(prefix.getModuleName())
            .getFileForInput(prefix.getModuleName(), relativeName))
        .or(() -> super.getFileForInput(packageName, relativeName));
  }

  @Override
  @SuppressWarnings("resource")
  public Optional<PathFileObject> getFileForOutput(String packageName, String relativeName) {
    // TODO(ascopes): can we have modules conceptually in this method call?
    return ModulePrefix
        .tryExtract(packageName)
        .flatMap(prefix -> forModule(prefix.getModuleName())
            .getFileForOutput(prefix.getRest(), relativeName))
        .or(() -> super.getFileForOutput(packageName, relativeName));
  }

  @Override
  @SuppressWarnings("resource")
  public Optional<PathFileObject> getJavaFileForInput(String className, Kind kind) {
    return ModulePrefix
        .tryExtract(className)
        .flatMap(prefix -> forModule(prefix.getModuleName())
            .getJavaFileForInput(prefix.getRest(), kind))
        .or(() -> super.getJavaFileForInput(className, kind));
  }

  @Override
  @SuppressWarnings("resource")
  public Optional<PathFileObject> getJavaFileForOutput(String className, Kind kind) {
    return ModulePrefix
        .tryExtract(className)
        .flatMap(prefix -> forModule(prefix.getModuleName())
            .getJavaFileForOutput(prefix.getRest(), kind))
        .or(() -> super.getJavaFileForOutput(className, kind));
  }

  @Override
  public PackageOrientedContainerGroup forModule(String moduleName) {
    var location = new ModuleLocation(this.location, moduleName);
    return modules.computeIfAbsent(location, SimpleOutputOrientedModuleContainerGroup::new);
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  protected PackageOrientedClassLoader createClassLoader() {
    throw new UnsupportedOperationException(
        "Getting a classloader for an output location is not yet supported."
    );
  }

  /**
   * Wrapper around a location that lacks the constraints that
   * {@link SimplePackageOrientedContainerGroup} imposes.
   */
  private class SimpleOutputOrientedModuleContainerGroup
      extends AbstractPackageOrientedContainerGroup {

    private final Location location;

    private SimpleOutputOrientedModuleContainerGroup(Location location) {
      super(SimpleOutputOrientedContainerGroup.this.release);
      this.location = requireNonNull(location, "location");
    }

    @Override
    public Location getLocation() {
      return location;
    }
  }
}
