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

import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.NioPath;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.utils.IoExceptionUtils;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.Nullable;
import io.github.ascopes.jct.utils.StringUtils;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Container that wraps a JAR path and allows reading the contents of the JAR in-memory lazily.
 *
 * <p>Unlike the regular <strong>JAR</strong> file system provider, more than one of these
 * containers can exist pointing to the same physical JAR at once without concurrency issues
 * occurring.
 *
 * <p>The JAR will be opened lazily when needed, and then kept open until {@link #close() closed}
 * explicitly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class JarContainer implements Container {

  private final Location location;
  private final PathLike jarPath;
  private final String release;
  private final Lazy<PackageFileSystemHolder> holder;

  /**
   * Initialize this JAR container.
   *
   * @param location the location.
   * @param jarPath  the path to the JAR to open.
   * @param release  the release version to use for {@code Multi-Release} JARs.
   */
  public JarContainer(Location location, PathLike jarPath, String release) {
    this.location = requireNonNull(location, "location");
    this.jarPath = requireNonNull(jarPath, "jarPath");
    this.release = requireNonNull(release, "release");
    holder = new Lazy<>(() -> IoExceptionUtils.uncheckedIo(PackageFileSystemHolder::new));
  }

  @Override
  public void close() throws IOException {
    holder.ifInitialized(PackageFileSystemHolder::close);
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var path = fileObject.getFullPath();
    for (var root : holder.access().getRootDirectories()) {
      return path.startsWith(root) && Files.isRegularFile(path);
    }
    return false;
  }

  @Override
  public Optional<Path> findFile(String path) {
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Absolute paths are not supported (got '" + path + "')");
    }

    for (var root : holder.access().getRootDirectories()) {
      var fullPath = FileUtils.relativeResourceNameToPath(root, path);
      if (Files.isRegularFile(fullPath)) {
        return Optional.of(fullPath);
      }
    }

    return Optional.empty();
  }

  @Override
  public Optional<byte[]> getClassBinary(String binaryName) throws IOException {
    var packageName = FileUtils.binaryNameToPackageName(binaryName);
    var packageDir = holder.access().getPackage(packageName);

    if (packageDir == null) {
      return Optional.empty();
    }

    var className = FileUtils.binaryNameToClassName(binaryName);
    var classPath = FileUtils.classNameToPath(packageDir.getPath(), className, Kind.CLASS);

    return Files.isRegularFile(classPath)
        ? Optional.of(Files.readAllBytes(classPath))
        : Optional.empty();
  }

  @Override
  public Optional<PathFileObject> getFileForInput(String packageName,
      String relativeName) {
    return Optional
        .ofNullable(holder.access().getPackage(packageName))
        .map(PathLike::getPath)
        .map(packageDir -> FileUtils.relativeResourceNameToPath(packageDir, relativeName))
        .filter(Files::isRegularFile)
        .map(path -> new PathFileObject(location, path.getRoot(), path));
  }

  @Override
  public Optional<PathFileObject> getFileForOutput(String packageName,
      String relativeName) {
    // This JAR is read-only.
    return Optional.empty();
  }

  @Override
  public Optional<PathFileObject> getJavaFileForInput(String binaryName, Kind kind) {
    var packageName = FileUtils.binaryNameToPackageName(binaryName);
    var className = FileUtils.binaryNameToClassName(binaryName);

    return Optional
        .ofNullable(holder.access().getPackage(packageName))
        .map(PathLike::getPath)
        .map(packageDir -> FileUtils.classNameToPath(packageDir, className, kind))
        .filter(Files::isRegularFile)
        .map(path -> new PathFileObject(location, path.getRoot(), path));
  }

  @Override
  public Optional<PathFileObject> getJavaFileForOutput(String className,
      Kind kind) {
    // This JAR is read-only.
    return Optional.empty();
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public ModuleFinder getModuleFinder() {
    var paths = holder
        .access()
        .getRootDirectoriesStream()
        .toArray(Path[]::new);
    return ModuleFinder.of(paths);
  }

  @Override
  public String getName() {
    return jarPath.toString();
  }

  @Override
  public PathLike getPath() {
    return jarPath;
  }

  @Override
  public Optional<URL> getResource(String resourcePath) throws IOException {
    // TODO(ascopes): could we index these resources ahead-of-time in the lazy initializer?
    for (var root : holder.access().getRootDirectories()) {
      var path = FileUtils.relativeResourceNameToPath(root, resourcePath);
      if (Files.isRegularFile(path)) {
        return Optional.of(path.toUri().toURL());
      }
    }

    return Optional.empty();
  }

  @Override
  public Optional<String> inferBinaryName(PathFileObject javaFileObject) {
    // For some reason, converting a zip entry to a URI gives us a scheme of `jar://file://`, but
    // we cannot then parse the URI back to a path without removing the `file://` bit first. Since
    // we assume we always have instances of PathJavaFileObject here, let's just cast to that and
    // get the correct path immediately.
    var fullPath = javaFileObject.getFullPath();

    for (var root : holder.access().getRootDirectories()) {
      if (fullPath.startsWith(root)) {
        return Optional.of(FileUtils.pathToBinaryName(javaFileObject.getRelativePath()));
      }
    }

    return Optional.empty();
  }

  @Override
  public Collection<? extends PathFileObject> list(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    var packageDir = holder.access().getPackages().get(packageName);

    if (packageDir == null) {
      return List.of();
    }

    var maxDepth = recurse ? Integer.MAX_VALUE : 1;

    var items = new ArrayList<PathFileObject>();

    var packagePath = packageDir.getPath();

    try (var walker = Files.walk(packagePath, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObject(location, path.getRoot(), path))
          .forEach(items::add);
    }

    return items;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{uri=" + StringUtils.quoted(jarPath.getUri()) + "}";
  }

  /**
   * Wrapper around a set of packages and a file system that can be opened lazily.
   */
  private class PackageFileSystemHolder {

    private final Map<String, PathLike> packages;
    private final FileSystem fileSystem;

    private PackageFileSystemHolder() throws IOException {
      // It turns out that we can open more than one ZIP file system pointing to the
      // same file at once, but we cannot do this with the JAR file system itself.
      // This is an issue since it hinders our ability to run tests in parallel where multiple tests
      // might be trying to read the same JAR at once.
      //
      // This means we have to do a little of hacking around to get this to work how we need it to.
      // Remember that JARs are just glorified zip folders.

      // Set the multi-release flag to enable reading META-INF/release/* files correctly if the
      // MANIFEST.MF specifies the Multi-Release entry as true.
      // Turns out the JDK implementation of the ZipFileSystem handles this for us.
      packages = new HashMap<>();

      var actualJarPath = jarPath.getPath();

      var env = Map.<String, Object>of(
          "releaseVersion", release,
          "multi-release", release
      );

      // So, for some reason. I cannot make more than one instance of a ZipFileSystem
      // if I pass a URI in here. If I pass a Path in here instead, then I can make
      // multiple copies of it in memory. No idea why this is the way it is, but it
      // appears to be how the JavacFileManager in the JDK can make itself run in parallel
      // safely. While in Rome, I guess.
      fileSystem = getJarFileSystemProvider().newFileSystem(actualJarPath, env);

      // Index packages ahead-of-time to improve performance.
      for (var root : fileSystem.getRootDirectories()) {
        try (var walker = Files.walk(root)) {
          walker
              .filter(Files::isDirectory)
              .map(root::relativize)
              .forEach(path -> packages.put(
                  FileUtils.pathToBinaryName(path),
                  new NioPath(root.resolve(path))
              ));
        }
      }
    }

    private void close() throws IOException {
      packages.clear();
      fileSystem.close();
    }

    private Map<String, PathLike> getPackages() {
      return packages;
    }

    @Nullable
    private PathLike getPackage(String name) {
      return packages.get(name);
    }

    private Iterable<? extends Path> getRootDirectories() {
      return fileSystem.getRootDirectories();
    }

    private Stream<? extends Path> getRootDirectoriesStream() {
      return StreamSupport.stream(fileSystem.getRootDirectories().spliterator(), false);
    }
  }

  private static FileSystemProvider getJarFileSystemProvider() {
    for (var fsProvider : FileSystemProvider.installedProviders()) {
      if (fsProvider.getScheme().equals("jar")) {
        return fsProvider;
      }
    }

    throw new ProviderNotFoundException("jar");
  }
}
