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

package io.github.ascopes.jct.jsr199.containers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.jsr199.ModuleLocation;
import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractPackageContainerGroup
    implements PackageContainerGroup {

  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".zip",
      ".jar",
      ".war"
  );

  protected final String release;
  protected final List<Container> containers;
  protected final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param release the release to use for multi-release JARs.
   */
  protected AbstractPackageContainerGroup(String release) {
    this.release = requireNonNull(release, "release");

    containers = new ArrayList<>();
    classLoaderLazy = new Lazy<>(this::createClassLoader);
  }

  @Override
  public final String toString() {
    return new ToStringBuilder(this)
        .attribute("location", getLocation())
        .toString();
  }

  @Override
  public void addPackage(Container container) {
    containers.add(container);
  }

  @Override
  public void addPackage(PathLike path) {
    var actualPath = path.getPath();

    var archive = ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(actualPath.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = archive
        ? new JarContainer(getLocation(), path, release)
        : new DirectoryContainer(getLocation(), path);

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
  public Optional<Path> findFile(String path) {
    return containers
        .stream()
        .flatMap(container -> container.findFile(path).stream())
        .findFirst();
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoaderLazy.access();
  }

  @Override
  public Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForInput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForOutput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForInput(className, kind).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForOutput(className, kind).stream())
        .findFirst();
  }

  @Override
  public final List<? extends Container> getPackages() {
    return Collections.unmodifiableList(containers);
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    var location = getLocation();

    if (location instanceof ModuleLocation) {
      throw new UnsupportedOperationException("Cannot load services from specific modules");
    }

    return ServiceLoader.load(service, classLoaderLazy.access());
  }

  @Override
  public Optional<String> inferBinaryName(PathFileObject fileObject) {
    return containers
        .stream()
        .flatMap(container -> container.inferBinaryName(fileObject).stream())
        .findFirst();
  }

  @Override
  public boolean isEmpty() {
    return containers.isEmpty();
  }

  @Override
  public Collection<? extends PathFileObject> list(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    var items = new ArrayList<PathFileObject>();

    for (var container : containers) {
      items.addAll(container.list(packageName, kinds, recurse));
    }

    return items;
  }

  protected ContainerClassLoader createClassLoader() {
    return new ContainerClassLoader(getLocation(), getPackages());
  }
}
