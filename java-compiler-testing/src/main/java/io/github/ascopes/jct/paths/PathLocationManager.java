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

package io.github.ascopes.jct.paths;

import static io.github.ascopes.jct.intern.IoExceptionUtils.uncheckedIo;
import static io.github.ascopes.jct.intern.IoExceptionUtils.wrapWithUncheckedIoException;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.AsyncResourceCloser;
import io.github.ascopes.jct.intern.Lazy;
import io.github.ascopes.jct.intern.RecursiveDeleter;
import io.github.ascopes.jct.intern.StringSlicer;
import io.github.ascopes.jct.intern.StringUtils;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.lang.ref.Cleaner;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manager of paths for a specific compiler location.
 *
 * <p>This provides access to file objects within any of the paths, as well as the ability
 * to construct classloaders for the paths as needed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PathLocationManager implements Iterable<Path> {

  private static final Cleaner CLEANER = Cleaner.create();
  private static final Logger LOGGER = LoggerFactory.getLogger(PathLocationManager.class);
  private static final StringSlicer PACKAGE_SPLITTER = new StringSlicer(".");

  private static final Set<String> JAR_FILE_EXTENSIONS = Set.of(
      ".jar",
      ".war"
  );

  private final PathJavaFileObjectFactory factory;
  private final Location location;
  private final Set<Path> roots;
  private final Lazy<ClassLoader> classLoader;

  // We use this to keep the references alive while the manager is alive, but we persist these
  // outside this context, as the user may wish to reuse these file systems across multiple tests
  // or otherwise. When the last reference to a RamPath is destroyed, and the RamPath becomes 
  // phantom-reachable, the garbage collector will reclaim the file system resource held within.
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Set<RamPath> inMemoryDirectories;  // lgtm [java/unused-container]

  // We open JARs as additional file systems as needed, and discard them when this manager gets
  // discarded. To prevent parallel tests failing to open the same JAR file system at the same time,
  // we use symbolic links to vary the name of the JAR first.
  private final Map<String, JarHandle> jarFileSystems;

  /**
   * Initialize the manager.
   *
   * @param factory  the {@link PathJavaFileObject} factory to use.
   * @param location the location to represent.
   */
  @SuppressWarnings("ThisEscapedInObjectConstruction")
  public PathLocationManager(PathJavaFileObjectFactory factory, Location location) {
    LOGGER.trace("Initializing for location {} with factory {}", location, factory);

    this.factory = requireNonNull(factory);
    this.location = requireNonNull(location);
    roots = new LinkedHashSet<>();
    classLoader = new Lazy<>(() -> new DirectoryClassLoader(roots));
    inMemoryDirectories = new HashSet<>();
    jarFileSystems = new HashMap<>();
    CLEANER.register(this, new AsyncResourceCloser(jarFileSystems));
  }

  /**
   * Iterate over the paths to assert on.
   *
   * @return the iterator.
   */
  @Override
  public Iterator<Path> iterator() {
    return roots.iterator();
  }

  /**
   * Add a path to the manager, if it has not already been added.
   *
   * <p>This will destroy the existing classloader, if it already exists.
   *
   * @param path the path to add.
   */
  public void addPath(Path path) {
    LOGGER.debug(
        "Adding paths {} to {} for location {}",
        path,
        getClass().getSimpleName(),
        location
    );
    registerPath(path);
    destroyClassLoader();
  }

  // !!! BUG REGRESSION WARNING FOR THIS API !!!:
  // DO NOT REPLACE COLLECTION<PATH>  WITH ITERABLE<PATH>! THIS WOULD MAKE DIFFERENCES BETWEEN
  // PATH AND COLLECTIONS OF PATHS DIFFICULT TO DISTINGUISH, SINCE PATHS ARE THEMSELVES
  // ITERABLES OF PATHS!

  /**
   * Add multiple paths to the manager, if they have not already been added.
   *
   * <p>This will destroy the existing classloader, if it already exists.
   *
   * @param paths the paths to add.
   */
  public void addPaths(Collection<? extends Path> paths) {
    // Don't expand paths if this was incorrectly called with a single path, since paths themselves
    // are iterables of paths.
    LOGGER.debug(
        "Adding paths {} to {} for location {}",
        paths,
        getClass().getSimpleName(),
        location
    );
    for (var path : paths) {
      registerPath(path);
    }
    destroyClassLoader();
  }

  /**
   * Add an in-memory directory to the manager, if it has not already been added.
   *
   * <p>This will destroy the existing classloader, if it already exists.
   *
   * @param path the path to add.
   */
  public void addRamPath(RamPath path) {
    LOGGER.debug(
        "Registering {} to {} for location {}",
        path,
        getClass().getSimpleName(),
        location
    );
    registerPath(path.getPath());

    // Keep the reference alive.
    inMemoryDirectories.add(path);
    destroyClassLoader();
  }

  /**
   * Add in-memory directories to the manager, if it has not already been added.
   *
   * <p>This will destroy the existing classloader, if it already exists.
   *
   * @param paths the paths to add.
   */
  public void addRamPaths(Collection<? extends RamPath> paths) {
    LOGGER.debug(
        "Registering {} to {} for location {}",
        paths,
        getClass().getSimpleName(),
        location
    );
    for (var path : paths) {
      registerPath(path.getPath());
      // Keep the reference alive.
      inMemoryDirectories.add(path);
    }
    destroyClassLoader();
  }

  /**
   * Determine if this manager contains the given file object anywhere.
   *
   * @param fileObject the file object to look for.
   * @return {@code true} if present, {@code false} otherwise.
   */
  public boolean contains(FileObject fileObject) {
    // TODO(ascopes): can we get non-path file objects here?
    var path = ((PathJavaFileObject) fileObject).getPath();

    // While we could just return `Files.isRegularFile` from the start,
    // we need to make sure the path is one of the roots in the location.
    // Otherwise, we could give a false-positive.
    for (var root : roots) {
      if (path.startsWith(root)) {
        return Files.isRegularFile(path);
      }
    }

    return false;
  }

  /**
   * Get the full path for a given string path to a file by finding the first occurrence where the
   * given path exists as a file.
   *
   * @param path the path to resolve.
   * @return the first full path that ends with the given path that is an existing file, or an empty
   *     optional if no results were found.
   * @throws IllegalArgumentException if an absolute-style path is provided.
   */
  public Optional<? extends Path> findFile(String path) {
    for (var root : roots) {
      var fullPath = root.resolve(path);

      if (Files.exists(fullPath)) {
        return Optional.of(fullPath);
      }
    }

    return Optional.empty();
  }

  /**
   * Get a classloader for this location manager.
   *
   * <p>This will initialize a classloader if it has not been initialized since the last path was
   * added.
   *
   * @return the class loader.
   */
  public ClassLoader getClassLoader() {
    return classLoader.access();
  }

  /**
   * Find an existing file object with the given package and relative file name.
   *
   * @param packageName  the package name.
   * @param relativeName the relative file name.
   * @return the file, or an empty optional if not found.
   */
  public Optional<FileObject> getFileForInput(String packageName, String relativeName) {
    var relativePathParts = packageNameToRelativePathParts(packageName);
    for (var root : roots) {
      var path = resolveNested(root, relativePathParts).resolve(relativeName);
      if (Files.isRegularFile(path)) {
        return Optional.of(factory.create(location, path, root.relativize(path).toString()));
      }
    }

    return Optional.empty();
  }

  /**
   * Get or create a file for output.
   *
   * <p>This will always use the first path that was registered.
   *
   * @param packageName  the package to create the file in.
   * @param relativeName the relative name of the file to create.
   * @return the file object for output, or an empty optional if no paths existed to place it in.
   */
  public Optional<FileObject> getFileForOutput(String packageName, String relativeName) {
    return roots
        .stream()
        .findFirst()
        .flatMap(root -> {
          var relativePathParts = packageNameToRelativePathParts(packageName);
          var path = resolveNested(root, relativePathParts).resolve(relativeName);
          return Optional.of(factory.create(location, path, root.relativize(path).toString()));
        });
  }

  /**
   * Find an existing file object with the given class name and kind.
   *
   * @param className the class name.
   * @param kind      the kind.
   * @return the file, or an empty optional if not found.
   */
  public Optional<JavaFileObject> getJavaFileForInput(String className, Kind kind) {
    var relativePathParts = classNameToRelativePathParts(className, kind.extension);
    for (var root : roots) {
      var path = resolveNested(root, relativePathParts);
      if (Files.isRegularFile(path)) {
        return Optional.of(factory.create(location, path, className));
      }
    }

    return Optional.empty();
  }

  /**
   * Get or create a file for output.
   *
   * <p>This will always use the first path that was registered.
   *
   * @param className the class name of the file to create.
   * @param kind      the kind of file to create.
   * @return the file object for output, or an empty optional if no paths existed to place it in.
   */
  public Optional<JavaFileObject> getJavaFileForOutput(String className, Kind kind) {
    return roots
        .stream()
        .findFirst()
        .flatMap(root -> {
          var relativePathParts = classNameToRelativePathParts(className, kind.extension);
          var path = resolveNested(root, relativePathParts);
          return Optional.of(factory.create(location, path, className));
        });
  }

  /**
   * Get the corresponding {@link Location} handle for this manager.
   *
   * @return the location.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Get a snapshot of the iterable of the paths in this location.
   *
   * @return the list of the paths that were loaded at the time the method was called, in the order
   *     they are considered.
   */
  public List<? extends Path> getRoots() {
    return List.copyOf(roots);
  }

  /**
   * Get a service loader for this location.
   *
   * @param service the service type.
   * @param <S>     the service type.
   * @return the service loader.
   * @throws UnsupportedOperationException if this manager is for a module location.
   */
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    if (location instanceof ModuleLocation) {
      throw new UnsupportedOperationException("Cannot load services from specific modules");
    }

    getClass().getModule().addUses(service);
    if (location.isModuleOrientedLocation()) {
      var finder = ModuleFinder.of(roots.toArray(new Path[0]));
      var bootLayer = ModuleLayer.boot();
      var config = bootLayer
          .configuration()
          .resolveAndBind(ModuleFinder.of(), finder, Collections.emptySet());
      var layer = bootLayer
          .defineModulesWithOneLoader(config, ClassLoader.getSystemClassLoader());
      return ServiceLoader.load(layer, service);
    } else {
      return ServiceLoader.load(service, classLoader.access());
    }
  }

  /**
   * Infer the binary name of the given file object.
   *
   * <p>This will attempt to find the corresponding root path for the file object, and then
   * output the relative path, converted to a class name-like string. If the file object does not
   * have a root in this manager, expect an empty optional to be returned instead.
   *
   * @param fileObject the file object to infer the binary name for.
   * @return the binary name of the object, or an empty optional if unknown.
   */
  public Optional<String> inferBinaryName(JavaFileObject fileObject) {
    // For some reason, converting a zip entry to a URI gives us a scheme of `jar://file://`, but
    // we cannot then parse the URI back to a path without removing the `file://` bit first. Since
    // we assume we always have instances of PathJavaFileObject here, let's just cast to that and
    // get the correct path immediately.
    var path = ((PathJavaFileObject) fileObject).getPath();

    for (var root : roots) {
      var resolvedPath = root.resolve(path.toString());
      if (path.startsWith(root) && Files.isRegularFile(resolvedPath)) {
        var relativePath = root.relativize(resolvedPath);
        return Optional.of(pathToObjectName(relativePath, fileObject.getKind().extension));
      }
    }

    return Optional.empty();
  }

  /**
   * Determine if the manager is empty or not.
   *
   * @return {@code true} if empty, {@code false} otherwise.
   */
  public boolean isEmpty() {
    return roots.isEmpty();
  }

  /**
   * List the {@link JavaFileObject} objects in the given package.
   *
   * @param packageName the package name.
   * @param kinds       the kinds to allow.
   * @param recurse     {@code true} to search recursively, {@code false} to only consider the given
   *                    package directly.
   * @return an iterable of file objects that were found.
   * @throws IOException if any of the paths cannot be read due to an IO error.
   */
  public Iterable<JavaFileObject> list(
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException {
    var relativePathParts = packageNameToRelativePathParts(packageName);
    var maxDepth = walkDepth(recurse);
    var results = new ArrayList<JavaFileObject>();

    for (var root : roots) {
      var path = resolveNested(root, relativePathParts);

      if (!Files.exists(path)) {
        continue;
      }

      try (var stream = Files.walk(path, maxDepth)) {
        stream
            .filter(hasAnyKind(kinds).and(Files::isRegularFile))
            .map(nextFile -> factory.create(location, nextFile))
            .peek(fileObject -> LOGGER.trace(
                "Found file object {} in root {} for list on package={}, kinds={}, recurse={}",
                fileObject,
                root,
                packageName,
                kinds,
                recurse
            ))
            .forEach(results::add);
      }
    }

    if (results.isEmpty()) {
      LOGGER.trace("No files found in any roots for package {}", packageName);
    }

    return results;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{location=" + StringUtils.quoted(location.getName())
        + "}";
  }

  /**
   * Perform {@link Files#walk(Path, FileVisitOption...)} across each root path in this location
   * manager, and return all results in a single stream.
   *
   * @param fileVisitOptions the options for file visiting within each root.
   * @return the stream of paths.
   */
  public Stream<? extends Path> walk(FileVisitOption... fileVisitOptions) {
    return walk(Integer.MAX_VALUE, fileVisitOptions);
  }

  /**
   * Perform {@link Files#walk(Path, int, FileVisitOption...)} across each root path in this
   * location manager, and return all results in a single stream.
   *
   * @param maxDepth         the max depth to recurse in each root.
   * @param fileVisitOptions the options for file visiting within each root.
   * @return the stream of paths.
   */
  public Stream<? extends Path> walk(int maxDepth, FileVisitOption... fileVisitOptions) {
    return getRoots()
        .stream()
        .flatMap(root -> uncheckedIo(() -> Files.walk(root, maxDepth, fileVisitOptions)));
  }

  /**
   * Get the factory for creating {@link PathJavaFileObject} instances with.
   *
   * @return the factory.
   */
  protected PathJavaFileObjectFactory getPathJavaFileObjectFactory() {
    return factory;
  }

  /**
   * Register the given path to the roots of this manager.
   *
   * @param path the path to register.
   */
  @SuppressWarnings("resource")
  protected void registerPath(Path path) {
    if (!Files.exists(path)) {
      throw wrapWithUncheckedIoException(new FileNotFoundException(path.toString()));
    }

    var absolutePath = path.toAbsolutePath();

    if (Files.isDirectory(absolutePath)) {
      registerDirectory(absolutePath);
      return;
    }

    // I previously used Files.probeContentType here, but it turns out that this is buggy on
    // some JREs on MacOS and Windows, where it will always provide a null result.
    // See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=8080369
    // Therefore, I am now just using a file extension check instead until I can think of a better
    // way of probing this without opening each file and checking the header.
    // TODO(ascopes): reconsider how I do this.
    var fileName = path.getFileName().toString();

    for (var extension : JAR_FILE_EXTENSIONS) {
      if (fileName.endsWith(extension)) {
        jarFileSystems
            .computeIfAbsent(absolutePath.toString(), ignored -> openJarHandle(absolutePath));
        return;
      }
    }

    throw new UnsupportedOperationException(
        "File at URI " + absolutePath.toUri() + " is not supported by this implementation."
    );
  }

  private void registerDirectory(Path absolutePath) {
    LOGGER.trace("Adding root {} to {}", absolutePath, this);
    roots.add(absolutePath);
  }

  private JarHandle openJarHandle(Path path) {
    return uncheckedIo(() -> {
      // When we open a JAR, we want to have a unique file name to work with.
      // The reason behind this is that the ZipFileSystemProvider used to open the JARs only
      // allows one open instance of each JAR at a time. If any tests using JCT run in parallel,
      // then we end up with a potential race condition where tests reusing the same JAR cannot
      // open a file system handle. This could also impact other unrelated unit tests that access
      // a JAR file in the classpath directly, as it would conflict with this. Unfortunately,
      // without reimplementing a random-access JAR reader (which the public JarInputStream API
      // does not appear to provide us), we either have to load the entire JAR into RAM ahead of
      // time, which is very slow, or we have to force all tests to run in series. If we do not
      // close the JAR file system, we'd get a memory leak too.
      var fileName = path.getFileName().toString();
      var tempDir = Files.createTempDirectory(fileName);
      try {
        var link = tempDir.resolve(fileName);

        try {
          // Symbolic linking is much more space efficient and faster than making a full copy.
          Files.createSymbolicLink(link, path);
          LOGGER.trace("Created symlink to {} at {}", path, link);
        } catch (FileSystemException ex) {
          // Windows helpfully does not allow creating symbolic links without root.
          Files.copy(path, link);
          LOGGER.trace("Created copy of {} at {} (fs did not allow creation of symlink)", path, ex);
        } catch (UnsupportedOperationException ex) {
          // We can't create symbolic links on the file system. Create a copy instead (slower).
          Files.copy(path, link);
          LOGGER.trace("Created copy of {} at {} (fs does not support symlinks)", path, link);
        }

        for (var provider : FileSystemProvider.installedProviders()) {
          if (provider.getScheme().equals("jar")) {
            var fs = provider.newFileSystem(link, Map.of());
            roots.add(fs.getRootDirectories().iterator().next());
            return new JarHandle(link, fs);
          }
        }

        throw new FileSystemNotFoundException("jar");
      } catch (IOException ex) {
        // Ensure we do not leak resources.
        RecursiveDeleter.deleteAll(tempDir);
        throw ex;
      }
    });
  }

  private void destroyClassLoader() {
    classLoader.destroy();
  }

  private String pathToObjectName(Path path, String extension) {
    assert path.getNameCount() != 0 : "Got an empty path somehow";

    var parts = new ArrayList<String>();
    for (var part : path) {
      parts.add(part.toString());
    }

    // Remove file extension on the last element.
    var lastIndex = parts.size() - 1;
    var fileName = parts.get(lastIndex);
    parts.set(lastIndex, fileName.substring(0, fileName.length() - extension.length()));

    // Join into a package name.
    return String.join(".", parts);
  }

  private String[] packageNameToRelativePathParts(String packageName) {
    // First arg has to be empty to be able to accept variadic arguments properly.
    return PACKAGE_SPLITTER.splitToArray(packageName);
  }

  private String[] classNameToRelativePathParts(String className, String extension) {
    var parts = PACKAGE_SPLITTER.splitToArray(className);
    assert parts.length > 0 : "did not expect an empty classname";
    parts[parts.length - 1] += extension;
    return parts;
  }

  private Path resolveNested(Path base, String[] parts) {
    for (var part : parts) {
      base = base.resolve(part);
    }
    return base.normalize();
  }

  private Predicate<Path> hasAnyKind(Iterable<Kind> kinds) {
    return path -> {
      var fileName = path.getFileName().toString();
      for (var kind : kinds) {
        if (fileName.endsWith(kind.extension)) {
          return true;
        }
      }
      return false;
    };
  }

  private int walkDepth(boolean recurse) {
    return recurse ? Integer.MAX_VALUE : 1;
  }

  private static final class JarHandle implements Closeable {

    private final Path link;
    private final FileSystem fileSystem;

    private JarHandle(Path link, FileSystem fileSystem) {
      this.link = link;
      this.fileSystem = fileSystem;
    }

    @Override
    public void close() throws IOException {
      fileSystem.close();
      RecursiveDeleter.deleteAll(link);
    }
  }
}
