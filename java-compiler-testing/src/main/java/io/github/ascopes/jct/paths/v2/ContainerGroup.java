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

package io.github.ascopes.jct.paths.v2;

import static io.github.ascopes.jct.intern.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.EnumerationAdapter;
import io.github.ascopes.jct.intern.IoExceptionUtils;
import io.github.ascopes.jct.intern.Lazy;
import io.github.ascopes.jct.paths.ModuleLocation;
import io.github.ascopes.jct.paths.RamPath;
import java.io.Closeable;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A group of containers that relate to a specific location.
 *
 * <p>This mechanism enables the ability to have locations with more than one path in them,
 * which is needed to facilitate the Java compiler's distributed class path, module handling, and
 * other important features.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class ContainerGroup implements Closeable {
  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".zip",
      ".jar",
      ".war"
  );

  private final Location location;
  private final List<Container> containers;
  private final String release;
  private final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param location the location of the container group.
   * @param release  the release to use for multi-release JARs.
   */
  public ContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");
    containers = new ArrayList<>();
    this.release = requireNonNull(release, "release");
    classLoaderLazy = new Lazy<>(() -> new ContainerClassLoader(this));
  }

  /**
   * Add a path to this group.
   *
   * <p>Note that this will destroy the {@link #getClassLoader() classloader} if one is already
   * allocated.
   *
   * @param path the path to add.
   */
  public void addPath(Path path) throws IOException {
    var archive = ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(path.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = archive
        ? new JarContainer(path, release)
        : new DirectoryContainer(path);

    containers.add(container);
  }

  /**
   * Add a RAM path to this group.
   *
   * <p>This is the same as {@link #addPath(Path)}, but ensures that the RAM path is kept
   * allocated for at least as long as this group is.
   *
   * <p>Note that this will destroy the {@link #getClassLoader() classloader} if one is already
   * allocated.
   *
   * @param ramPath the RAM path to add.
   */
  public void addRamPath(RamPath ramPath) {
    containers.add(new RamPathContainer(ramPath));
  }

  /**
   * Determine whether this group contains the given file object anywhere.
   *
   * @param fileObject the file object to look for.
   * @return {@code true} if the file object is contained in this group, or {@code false} otherwise.
   */
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

  /**
   * Find the first occurrence of a given path to a file.
   *
   * @param path the path to the file to find.
   * @return the first occurrence of the path in this group, or an empty optional if not found.
   */
  public Optional<? extends Path> findFile(String path) {
    return containers
        .stream()
        .flatMap(container -> container.findFile(path).stream())
        .findFirst();
  }

  /**
   * Get a classloader for this group of paths.
   *
   * @return the classloader.
   */
  public ClassLoader getClassLoader() {
    return classLoaderLazy.access();
  }

  /**
   * Get a {@link FileObject} that can have content read from it.
   *
   * <p>This will return an empty optional if no file is found.
   *
   * @param packageName  the package name of the file to read.
   * @param relativeName the relative name of the file to read.
   * @return the file object, or an empty optional if the file is not found.
   */
  public Optional<? extends PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForInput(packageName, relativeName).stream())
        .findFirst();
  }

  /**
   * Get a {@link FileObject} that can have content written to it for the given file.
   *
   * <p>This will attempt to write to the first writeable path in this group. An empty optional
   * will be returned if no writeable paths exist in this group.
   *
   * @param packageName  the name of the package the file is in.
   * @param relativeName the relative name of the file within the package.
   * @return the {@link FileObject} to write to, or an empty optional if this group has no paths
   *     that can be written to.
   */
  public Optional<? extends PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForOutput(packageName, relativeName).stream())
        .findFirst();
  }

  /**
   * Get a {@link JavaFileObject} that can have content written to it for the given file.
   *
   * <p>This will attempt to write to the first writeable path in this group. An empty optional
   * will be returned if no writeable paths exist in this group.
   *
   * @param className the binary name of the class to read.
   * @param kind      the kind of file to read.
   * @return the {@link JavaFileObject} to write to, or an empty optional if this group has no paths
   *     that can be written to.
   */
  public Optional<? extends PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForInput(className, kind).stream())
        .findFirst();
  }

  /**
   * Get a {@link JavaFileObject} that can have content written to it for the given class.
   *
   * <p>This will attempt to write to the first writeable path in this group. An empty optional
   * will be returned if no writeable paths exist in this group.
   *
   * @param className the name of the class.
   * @param kind      the kind of the class file.
   * @return the {@link JavaFileObject} to write to, or an empty optional if this group has no paths
   *     that can be written to.
   */
  public Optional<? extends PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForOutput(className, kind).stream())
        .findFirst();
  }

  /**
   * Get the location that this group of paths is for.
   *
   * @return the location.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Get a service loader for the given service class.
   *
   * @param service the service class to get.
   * @param <S>     the service class type.
   * @return the service loader.
   */
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    if (location instanceof ModuleLocation) {
      throw new UnsupportedOperationException("Cannot load services from specific modules");
    }

    getClass().getModule().addUses(service);
    if (!location.isModuleOrientedLocation()) {
      return ServiceLoader.load(service, getClassLoader());
    }

    var finders = containers
        .stream()
        .map(Container::getModuleFinder)
        .toArray(ModuleFinder[]::new);

    var composedFinder = ModuleFinder.compose(finders);
    var bootLayer = ModuleLayer.boot();
    var config = bootLayer
        .configuration()
        .resolveAndBind(ModuleFinder.of(), composedFinder, Collections.emptySet());

    var layer = bootLayer
        .defineModulesWithOneLoader(config, ClassLoader.getSystemClassLoader());

    return ServiceLoader.load(layer, service);
  }

  /**
   * Try to infer the binary name of a given file object.
   *
   * @param fileObject the file object to infer the binary name for.
   * @return the binary name if known, or an empty optional otherwise.
   */
  public Optional<? extends String> inferBinaryName(PathFileObject fileObject) {
    return containers
        .stream()
        .flatMap(container -> container.inferBinaryName(fileObject).stream())
        .findFirst();
  }

  /**
   * Determine if this group has no paths registered.
   *
   * @return {@code true} if no paths are registered. {@code false} if paths are registered.
   */
  public boolean isEmpty() {
    return containers.isEmpty();
  }

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @return an iterable of resultant file objects.
   * @throws IOException if the file lookup fails.
   */
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

  private static class ContainerClassLoader extends ClassLoader {
    static {
      ClassLoader.registerAsParallelCapable();
    }

    private final ContainerGroup owner;

    private ContainerClassLoader(ContainerGroup owner) {
      this.owner = owner;
    }

    protected Class<?> findClass(String binaryName) throws ClassNotFoundException {
      for (var container : owner.containers) {
        try {
          var maybeBinary = container.getClassBinary(binaryName);

          if (maybeBinary.isPresent()) {
            var binary = maybeBinary.get();
            return defineClass(null, binary, 0, binary.length);
          }
        } catch (IOException ex) {
          throw new ClassNotFoundException(
              "Failed to load class " + binaryName + " due to an IOException",
              ex
          );
        }
      }

      throw new ClassNotFoundException(binaryName);
    }

    @Override
    protected URL findResource(String resourcePath) {
      try {
        for (var container : owner.containers) {
          var maybeResource = container.getResource(resourcePath);

          if (maybeResource.isPresent()) {
            return maybeResource.get();
          }
        }

      } catch (IOException ex) {
        // We ignore this, according to the spec for how this should be handled.
        // TODO: maybe log this.
      }

      return null;
    }

    @Override
    protected Enumeration<URL> findResources(String resourcePath) throws IOException {
      var resources = new ArrayList<URL>();

      for (var container : owner.containers) {
        container.getResource(resourcePath).ifPresent(resources::add);
      }

      return new EnumerationAdapter<>(resources.iterator());
    }
  }
}
