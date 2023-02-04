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
package io.github.ascopes.jct.containers.impl;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
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
@API(since = "0.0.1", status = Status.INTERNAL)
@ThreadSafe
public final class OutputContainerGroupImpl
    extends AbstractPackageContainerGroup
    implements OutputContainerGroup {

  private final Map<ModuleLocation, OutputPackageContainerGroupImpl> modules;

  /**
   * Initialize this container group.
   *
   * @param location the location of the group.
   * @param release  the release version.
   */
  public OutputContainerGroupImpl(Location location, String release) {
    super(location, release);
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
  public void addModule(String module, @WillCloseWhenClosed Container container) {
    getOrCreateModule(module).addPackage(container);
  }

  @Override
  public void addModule(String module, PathRoot path) {
    getOrCreateModule(module).addPackage(path);
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var location = getLocation();

    if (location instanceof ModuleLocation) {
      var module = modules.get(location);
      return module != null && module.contains(fileObject);
    }

    return super.contains(fileObject);
  }

  @Override
  public PackageContainerGroup getModule(String module) {
    if (module.isEmpty()) {
      // We are a package container group internally.
      return this;
    }

    return modules
        .keySet()
        .stream()
        .filter(location -> location.getModuleName().equals(module))
        .findFirst()
        .map(modules::get)
        .orElse(null);
  }

  @Override
  @Nullable
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    var moduleName = extractModuleName(packageName);

    if (moduleName != null) {
      var module = getModule(moduleName);

      if (module != null) {
        var realPackageName = extractPackageName(packageName);
        var file = module.getFileForInput(realPackageName, relativeName);

        if (file != null) {
          return file;
        }
      }
    }

    return super.getFileForInput(packageName, relativeName);
  }

  @Override
  @Nullable
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    var moduleName = extractModuleName(packageName);

    if (moduleName != null) {
      var module = getOrCreateModule(moduleName);
      var realPackageName = extractPackageName(packageName);
      var file = module.getFileForOutput(realPackageName, relativeName);

      if (file != null) {
        return file;
      }
    }

    return super.getFileForOutput(packageName, relativeName);
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForInput(String className, Kind kind) {
    var moduleName = extractModuleName(className);

    if (moduleName != null) {
      var module = getModule(moduleName);

      if (module != null) {
        var realClassName = extractPackageName(className);
        var file = module.getJavaFileForInput(realClassName, kind);

        if (file != null) {
          return file;
        }
      }
    }

    return super.getJavaFileForInput(className, kind);
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    var moduleName = extractModuleName(className);

    if (moduleName != null) {
      var module = getOrCreateModule(moduleName);
      var realClassName = extractPackageName(className);
      var file = module.getJavaFileForOutput(realClassName, kind);

      if (file != null) {
        return file;
      }
    }

    return super.getJavaFileForOutput(className, kind);
  }

  @Override
  public Set<Location> getLocationsForModules() {
    return Set.copyOf(modules.keySet());
  }

  @Override
  public Map<ModuleLocation, PackageContainerGroup> getModules() {
    return Map.copyOf(modules);
  }

  @Override
  public PackageContainerGroup getOrCreateModule(String moduleName) {
    var location = getLocation();
    return modules.computeIfAbsent(new ModuleLocation(location, moduleName), this::newPackageGroup);
  }

  @Override
  public boolean hasLocation(ModuleLocation location) {
    return modules.containsKey(location);
  }

  @Override
  protected ClassLoader createClassLoader() {
    return new PackageContainerGroupUrlClassLoader(this);
  }

  @SuppressWarnings("resource")
  @WillNotClose
  private OutputPackageContainerGroupImpl newPackageGroup(ModuleLocation moduleLocation) {
    // For output locations, we only need the first root. We then just put a subdirectory
    // in there, as it reduces the complexity of this tenfold and means we don't have to
    // worry about creating more in-memory locations on the fly.
    var release = getRelease();

    var group = new OutputPackageContainerGroupImpl(moduleLocation, release);
    var pathWrapper = new WrappingDirectoryImpl(
        getPackages().iterator().next().getPathRoot(),
        moduleLocation.getModuleName()
    );
    uncheckedIo(() -> Files.createDirectories(pathWrapper.getPath()));
    group.addPackage(pathWrapper);
    return group;
  }

  @Nullable
  private static String extractModuleName(String binaryName) {
    // extractModuleName("foo.bar.Baz") -> null
    // extractModuleName("some.module/foo.bar.Baz") -> "some.module"
    var separatorIndex = binaryName.indexOf('/');
    return separatorIndex == -1 ? null : binaryName.substring(0, separatorIndex);
  }

  private static String extractPackageName(String binaryName) {
    // extractPackageName("foo.bar.Baz") -> "foo.bar.Baz"
    // extractPackageName("some.module/foo.bar.Baz") -> "foo.bar.Baz"
    var separatorIndex = binaryName.indexOf('/');
    return separatorIndex == -1 ? binaryName : binaryName.substring(separatorIndex + 1);
  }

  /**
   * Unlike {@link PackageContainerGroupImpl}, this implementation does not reject output-oriented
   * locations in the constructor.
   *
   * <p>This implementation is only ever expected to hold one of each container in it.
   */
  private static final class OutputPackageContainerGroupImpl
      extends AbstractPackageContainerGroup {

    private OutputPackageContainerGroupImpl(Location location, String release) {
      super(location, release);
    }

    @Override
    protected ClassLoader createClassLoader() {
      return new PackageContainerGroupUrlClassLoader(this);
    }
  }
}
