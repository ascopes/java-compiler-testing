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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.annotations.WillCloseWhenClosed;
import io.github.ascopes.jct.annotations.WillNotClose;
import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.pathwrappers.BasicPathWrapperImpl;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.ToStringBuilder;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
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
 * explicitly. If something goes wrong in this lazy-loading process, then methods may throw an
 * undocumented {@link java.io.UncheckedIOException}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JarContainerImpl implements Container {

  private final Location location;
  private final PathWrapper jarPath;
  private final String release;
  private final Lazy<@WillCloseWhenClosed PackageFileSystemHolder> holder;

  /**
   * Initialize this JAR container.
   *
   * @param location the location.
   * @param jarPath  the path to the JAR to open.
   * @param release  the release version to use for {@code Multi-Release} JARs.
   */
  public JarContainerImpl(Location location, @WillNotClose PathWrapper jarPath, String release) {
    this.location = requireNonNull(location, "location");
    this.jarPath = requireNonNull(jarPath, "jarPath");
    this.release = requireNonNull(release, "release");

    // This will throw if, for example, the file doesn't exist or if the system encounters an IO
    // error of some description. Both of these cases should be unexpected, however.
    holder = new Lazy<>(() -> uncheckedIo(PackageFileSystemHolder::new));
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
  public Path findFile(String path) {
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Absolute paths are not supported (got '" + path + "')");
    }

    for (var root : holder.access().getRootDirectories()) {
      var fullPath = FileUtils.relativeResourceNameToPath(root, path);
      if (Files.isRegularFile(fullPath)) {
        return fullPath;
      }
    }

    return null;
  }

  @Override
  public byte[] getClassBinary(String binaryName) throws IOException {
    var packageName = FileUtils.binaryNameToPackageName(binaryName);
    var packageDir = holder.access().getPackage(packageName);

    if (packageDir == null) {
      return null;
    }

    var className = FileUtils.binaryNameToSimpleClassName(binaryName);
    var classPath = FileUtils.simpleClassNameToPath(packageDir.getPath(), className, Kind.CLASS);

    return Files.isRegularFile(classPath)
        ? Files.readAllBytes(classPath)
        : null;
  }

  @Override
  @Nullable
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    var packageObj = holder.access().getPackage(packageName);

    if (packageObj == null) {
      return null;
    }

    var file = FileUtils.relativeResourceNameToPath(packageObj.getPath(), relativeName);

    if (!Files.isRegularFile(file)) {
      return null;
    }

    return new PathFileObject(location, file.getRoot(), file);
  }

  @Override
  @Nullable
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    throw new UnsupportedOperationException("Cannot handle output files in JARs");
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForInput(String binaryName, Kind kind) {
    var packageName = FileUtils.binaryNameToPackageName(binaryName);
    var className = FileUtils.binaryNameToSimpleClassName(binaryName);

    var packageObj = holder.access().getPackage(packageName);

    if (packageObj == null) {
      return null;
    }

    var file = FileUtils.simpleClassNameToPath(packageObj.getPath(), className, kind);

    if (!Files.isRegularFile(file)) {
      return null;
    }

    return new PathFileObject(location, file.getRoot(), file);
  }

  @Override
  @Nullable
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    throw new UnsupportedOperationException("Cannot handle output source files in JARs");
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
  public PathWrapper getPathWrapper() {
    return jarPath;
  }

  @Override
  @Nullable
  public URL getResource(String resourcePath) throws IOException {
    for (var root : holder.access().getRootDirectories()) {
      var path = FileUtils.relativeResourceNameToPath(root, resourcePath);
      if (Files.isRegularFile(path)) {
        return path.toUri().toURL();
      }
    }

    return null;
  }

  @Override
  @Nullable
  public String inferBinaryName(PathFileObject javaFileObject) {
    // For some reason, converting a zip entry to a URI gives us a scheme of `jar://file://`, but
    // we cannot then parse the URI back to a path without removing the `file://` bit first. Since
    // we assume we always have instances of PathJavaFileObject here, let's just cast to that and
    // get the correct path immediately.
    var fullPath = javaFileObject.getFullPath();

    for (var root : holder.access().getRootDirectories()) {
      if (fullPath.startsWith(root)) {
        return FileUtils.pathToBinaryName(javaFileObject.getRelativePath());
      }
    }

    return null;
  }

  @Override
  public Collection<Path> listAllFiles() throws IOException {
    return holder.access().getAllFiles();
  }

  @Override
  public void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException {
    var packageDir = holder.access().getPackages().get(packageName);

    if (packageDir == null) {
      return;
    }

    var maxDepth = recurse ? Integer.MAX_VALUE : 1;
    var packagePath = packageDir.getPath();

    try (var walker = Files.walk(packagePath, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObject(location, path.getRoot(), path))
          .forEach(collection::add);
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", jarPath.getUri())
        .attribute("location", location)
        .toString();
  }

  /**
   * Wrapper around a set of packages and a file system that can be opened lazily.
   */
  private final class PackageFileSystemHolder {

    private final Map<String, PathWrapper> packages;
    private final @WillCloseWhenClosed FileSystem fileSystem;

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
                  new BasicPathWrapperImpl(root.resolve(path))
              ));
        }
      }
    }

    private void close() throws IOException {
      packages.clear();
      fileSystem.close();
    }

    private Map<String, PathWrapper> getPackages() {
      return packages;
    }

    @Nullable
    private PathWrapper getPackage(String name) {
      return packages.get(name);
    }

    private Iterable<? extends Path> getRootDirectories() {
      return fileSystem.getRootDirectories();
    }

    private Stream<? extends Path> getRootDirectoriesStream() {
      return StreamSupport.stream(fileSystem.getRootDirectories().spliterator(), false);
    }

    private Collection<Path> getAllFiles() throws IOException {
      var allPaths = new ArrayList<Path>();

      // TODO: think of a faster way of doing this without returning a closeable stream.
      for (var root : fileSystem.getRootDirectories()) {
        try (var walker = Files.walk(root)) {
          walker.forEach(allPaths::add);
        }
      }

      return allPaths;
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
