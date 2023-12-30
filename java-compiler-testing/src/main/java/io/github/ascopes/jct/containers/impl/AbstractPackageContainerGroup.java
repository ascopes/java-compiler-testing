/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import static java.util.Collections.synchronizedSet;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

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
public abstract class AbstractPackageContainerGroup implements PackageContainerGroup {

  // https://docs.oracle.com/cd/E19830-01/819-4712/ablgz/index.html
  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".ear",
      ".jar",
      ".rar",
      ".war",
      ".zip"
  );

  /**
   * The location of the container group.
   */
  private final Location location;
  private final String release;
  private final Set<Container> containers;
  private final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param location the location being represented.
   * @param release  the release to use for multi-release JARs.
   */
  protected AbstractPackageContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");
    this.release = requireNonNull(release, "release");

    containers = synchronizedSet(new LinkedHashSet<>());
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
  public void addPackage(PathRoot path) {
    var actualPath = path.getPath();

    // Null filename implies the path is the root directory of a file system (
    // like a JIMFS RAM file system we initialize elsewhere).
    var isArchive = actualPath.getFileName() != null && ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(actualPath.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = isArchive
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
    classLoaderLazy.destroy();

    var exceptions = new ArrayList<IOException>();

    for (var container : containers) {
      try {
        container.close();
      } catch (IOException ex) {
        exceptions.add(ex);
      }
    }

    if (!exceptions.isEmpty()) {
      var newEx = new IOException("Containers failed to close in " + location.getName());
      exceptions.forEach(newEx::addSuppressed);
      throw newEx;
    }
  }

  @Nullable
  @Override
  public Path getFile(String fragment, String... fragments) {
    for (var container : containers) {
      var result = container.getFile(fragment, fragments);
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

  @Nullable
  @Override
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    for (var container : containers) {
      var file = container.getFileForInput(packageName, relativeName);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Nullable
  @Override
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    for (var container : containers) {
      var file = container.getFileForOutput(packageName, relativeName);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Nullable
  @Override
  public PathFileObject getJavaFileForInput(String className, Kind kind) {
    for (var container : containers) {
      var file = container.getJavaFileForInput(className, kind);
      if (file != null) {
        return file;
      }
    }

    return null;
  }

  @Nullable
  @Override
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
  public final String getRelease() {
    return release;
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    if (location instanceof ModuleLocation) {
      throw new JctIllegalInputException("Cannot load services from specific modules");
    }

    return ServiceLoader.load(service, classLoaderLazy.access());
  }

  @Nullable
  @Override
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
  public Set<JavaFileObject> listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    // XXX: could this be run in parallel? This probably won't make much difference
    // for in-memory paths, but may improve performance for disk-based paths.
    var collection = new HashSet<JavaFileObject>();
    for (var container : containers) {
      container.listFileObjects(packageName, kinds, recurse, collection);
    }
    return Collections.unmodifiableSet(collection);
  }

  @API(since = "0.6.0", status = Status.STABLE)
  @Override
  public Map<Container, Collection<Path>> listAllFiles() throws IOException {
    var multimap = new LinkedHashMap<Container, Collection<Path>>();
    for (var container : getPackages()) {
      multimap.put(container, container.listAllFiles());
    }
    return Collections.unmodifiableMap(multimap);
  }

  /**
   * Create a classloader and return it.
   *
   * @return the classloader.
   */
  protected ClassLoader createClassLoader() {
    return new PackageContainerGroupUrlClassLoader(this);
  }
}
