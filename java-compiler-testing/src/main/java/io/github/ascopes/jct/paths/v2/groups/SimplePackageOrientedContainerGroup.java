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

import io.github.ascopes.jct.intern.EnumerationAdapter;
import io.github.ascopes.jct.intern.Lazy;
import io.github.ascopes.jct.paths.ModuleLocation;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.paths.v2.PathFileObject;
import io.github.ascopes.jct.paths.v2.containers.Container;
import io.github.ascopes.jct.paths.v2.containers.DirectoryContainer;
import io.github.ascopes.jct.paths.v2.containers.JarContainer;
import io.github.ascopes.jct.paths.v2.containers.RamPathContainer;
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
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SimplePackageOrientedContainerGroup implements PackageOrientedContainerGroup {

  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".zip",
      ".jar",
      ".war"
  );

  protected final Location location;
  protected final String release;
  private final List<Container> containers;
  private final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param location the location of the container group.
   * @param release  the release to use for multi-release JARs.
   */
  public SimplePackageOrientedContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");

    if (location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use output locations with this container group"
      );
    }

    if (location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use module-oriented locations with this container group"
      );
    }

    this.release = requireNonNull(release, "release");

    containers = new ArrayList<>();
    classLoaderLazy = new Lazy<>(() -> new SimpleContainerClassLoader(this));
  }

  @Override
  public void addPath(Path path) throws IOException {
    var archive = ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(path.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = archive
        ? new JarContainer(path, release)
        : new DirectoryContainer(path);

    containers.add(container);
  }

  @Override
  public void addRamPath(RamPath ramPath) {
    containers.add(new RamPathContainer(ramPath));
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
  public Optional<? extends Path> findFile(String path) {
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
  public Optional<? extends PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForInput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<? extends PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForOutput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<? extends PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForInput(className, kind).stream())
        .findFirst();
  }

  @Override
  public Optional<? extends PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForOutput(className, kind).stream())
        .findFirst();
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
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

  @Override
  public Optional<? extends String> inferBinaryName(PathFileObject fileObject) {
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

  private static class SimpleContainerClassLoader extends ClassLoader {

    static {
      ClassLoader.registerAsParallelCapable();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleContainerClassLoader.class);

    private final SimplePackageOrientedContainerGroup owner;

    private SimpleContainerClassLoader(SimplePackageOrientedContainerGroup owner) {
      this.owner = owner;
    }

    protected Class<?> findClass(String binaryName) throws ClassNotFoundException {
      try {
        for (var container : owner.containers) {
          var clazz = container
              .getClassBinary(binaryName)
              .map(data -> defineClass(null, data, 0, data.length));

          if (clazz.isPresent()) {
            return clazz.get();
          }
        }

        throw new ClassNotFoundException("Class not found: " + binaryName);
      } catch (IOException ex) {
        throw new ClassNotFoundException("Class loading aborted for: " + binaryName, ex);
      }
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
        LOGGER.warn(
            "Failed to look up resource {} because {}: {} - this will be ignored",
            resourcePath,
            ex.getClass().getName(),
            ex.getMessage()
        );
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
