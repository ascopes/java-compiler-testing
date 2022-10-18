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
package io.github.ascopes.jct.containers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.annotations.WillCloseWhenClosed;
import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An abstract base implementation for a group of containers that relate to a specific location.
 *
 * <p>This mechanism enables the ability to have locations with more than one path in them,
 * which is needed to facilitate the Java compiler's distributed class path, module handling, and
 * other important features.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public abstract class AbstractPackageContainerGroup
    implements PackageContainerGroup {

  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".zip",
      ".jar",
      ".war"
  );

  protected final Location location;
  protected final String release;
  protected final List<@WillCloseWhenClosed Container> containers;
  protected final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param location the location being represented.
   * @param release  the release to use for multi-release JARs.
   */
  protected AbstractPackageContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");
    this.release = requireNonNull(release, "release");

    containers = new ArrayList<>();
    classLoaderLazy = new Lazy<>(this::createClassLoader);
  }

  @Override
  public final String toString() {
    return new ToStringBuilder(this)
        .attribute("location", getLocation())
        .attribute("containerCount", containers.size())
        .toString();
  }

  @Override
  public void addPackage(Container container) {
    containers.add(container);
  }

  @Override
  public void addPackage(PathWrapper path) {
    var actualPath = path.getPath();

    var archive = ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(actualPath.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = archive
        ? new JarContainerImpl(getLocation(), path, release)
        : new PathWrappingContainerImpl(getLocation(), path);

    addPackage(container);
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    return containers
        .stream()
        .anyMatch(container -> container.contains(fileObject));
  }

  @Override
  public void close() throws IOException {
    // Close everything in a best-effort fashion.
    var exceptions = new ArrayList<Throwable>();

    for (var container : containers) {
      try {
        container.close();
      } catch (IOException ex) {
        exceptions.add(ex);
      }
    }

    try {
      classLoaderLazy.destroy();
    } catch (Exception ex) {
      exceptions.add(ex);
    }

    if (exceptions.size() > 0) {
      var ioEx = new IOException("One or more components failed to close");
      exceptions.forEach(ioEx::addSuppressed);
      throw ioEx;
    }
  }

  @Override
  @Nullable
  public Path findFile(String path) {
    for (var container : containers) {
      var result = container.findFile(path);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoaderLazy.access();
  }

  @Override
  @Nullable
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    for (var container : containers) {
      var file = container.getFileForInput(packageName, relativeName);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Override
  @Nullable
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    for (var container : containers) {
      var file = container.getFileForOutput(packageName, relativeName);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForInput(String className, Kind kind) {
    for (var container : containers) {
      var file = container.getJavaFileForInput(className, kind);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    for (var container : containers) {
      var file = container.getJavaFileForOutput(className, kind);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public final List<Container> getPackages() {
    return List.copyOf(containers);
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    if (location instanceof ModuleLocation) {
      throw new UnsupportedOperationException("Cannot load services from specific modules");
    }

    return ServiceLoader.load(service, classLoaderLazy.access());
  }

  @Override
  @Nullable
  public String inferBinaryName(PathFileObject fileObject) {
    for (var container : containers) {
      var name = container.inferBinaryName(fileObject);
      if (name != null) {
        return name;
      }
    }

    return null;
  }

  @Override
  public boolean isEmpty() {
    return containers.isEmpty();
  }

  @Override
  public void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException {
    for (var container : containers) {
      container.listFileObjects(packageName, kinds, recurse, collection);
    }
  }

  protected ContainerGroupClassLoaderImpl createClassLoader() {
    return new ContainerGroupClassLoaderImpl(getLocation(), getPackages());
  }

}
