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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.ex.JctNotImplementedException;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.filemanagers.impl.PathFileObjectImpl;
import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(JarContainerImpl.class);

  private static final Lazy<FileSystemProvider> JAR_FS_PROVIDER = new Lazy<>(() -> {
    for (var fsProvider : FileSystemProvider.installedProviders()) {
      if (fsProvider.getScheme().equals("jar")) {
        return fsProvider;
      }
    }

    throw new ProviderNotFoundException("jar");
  });

  private final Location location;
  private final PathRoot jarPath;
  private final String release;
  private final Lazy<PackageFileSystemHolder> holder;

  /**
   * Initialize this JAR container.
   *
   * @param location the location.
   * @param jarPath  the path to the JAR to open.
   * @param release  the release version to use for {@code Multi-Release} JARs.
   */
  public JarContainerImpl(Location location, PathRoot jarPath, String release) {
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
    var path = fileObject.getAbsolutePath();
    var root = holder.access().getPath();
    return path.startsWith(root) && Files.isRegularFile(path);
  }

  @Nullable
  @Override
  public Path getFile(String fragment, String... fragments) {
    var root = holder.access().getPath();
    var fullPath = FileUtils.relativeResourceNameToPath(root, fragment, fragments);
    if (Files.isRegularFile(fullPath)) {
      return fullPath;
    }
    return null;
  }

  @Nullable
  @Override
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    var packageObj = holder.access().getPackage(packageName);

    if (packageObj == null) {
      return null;
    }

    var file = FileUtils.relativeResourceNameToPath(packageObj.getPath(), relativeName);

    if (!Files.isRegularFile(file)) {
      return null;
    }

    return new PathFileObjectImpl(location, file.getRoot(), file);
  }

  @Nullable
  @Override
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    throw new JctNotImplementedException("Cannot handle output files in JARs");
  }

  @Override
  public PathRoot getInnerPathRoot() {
    return holder.access().getPathRoot();
  }

  @Nullable
  @Override
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

    return new PathFileObjectImpl(location, file.getRoot(), file);
  }

  @Nullable
  @Override
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    throw new JctNotImplementedException("Cannot handle output source files in JARs");
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public ModuleFinder getModuleFinder() {
    return ModuleFinder.of(holder.access().getPath());
  }

  @Override
  public String getName() {
    return jarPath.toString();
  }

  @Override
  public PathRoot getPathRoot() {
    return jarPath;
  }

  @Nullable
  @Override
  public String inferBinaryName(PathFileObject javaFileObject) {
    // For some reason, converting a zip entry to a URI gives us a scheme of `jar://file://`, but
    // we cannot then parse the URI back to a path without removing the `file://` bit first. Since
    // we assume we always have instances of PathJavaFileObject here, let's just cast to that and
    // get the correct path immediately.
    var fullPath = javaFileObject.getAbsolutePath();

    if (fullPath.startsWith(holder.access().getPath())) {
      return FileUtils.pathToBinaryName(javaFileObject.getRelativePath());
    }

    return null;
  }

  @Override
  public Collection<Path> listAllFiles() {
    return holder.access().getAllFiles();
  }

  @Override
  public void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException {
    var packageDir = holder.access().getPackage(packageName);

    if (packageDir == null) {
      return;
    }

    var maxDepth = recurse ? Integer.MAX_VALUE : 1;
    var packagePath = packageDir.getPath();

    try (var walker = Files.walk(packagePath, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObjectImpl(location, path.getRoot(), path))
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

    private final FileSystem fileSystem;
    private final PathRoot rootDirectoryPathRoot;
    private final Lazy<Map<String, PathRoot>> packages;
    private final Lazy<Collection<Path>> files;

    private PackageFileSystemHolder() throws IOException {
      var actualJarPath = jarPath.getPath();
      log.trace("Preparing virtual file system holder for JAR {}", actualJarPath);

      // It turns out that we can open more than one ZIP file system pointing to the
      // same file at once, but we cannot do this with the JAR file system itself.
      // This is an issue since it hinders our ability to run tests in parallel where multiple tests
      // might be trying to read the same JAR at once.
      //
      // This means we have to do a little of hacking around to get this to work how we need it to.
      // Remember that JARs are just glorified zip folders.
      //
      // Set the multi-release flag to enable reading META-INF/release/* files correctly if the
      // MANIFEST.MF specifies the Multi-Release entry as true.
      // Turns out the JDK implementation of the ZipFileSystem handles this for us.
      Map<String, Object> env = Map.of(
          "releaseVersion", release,
          "multi-release", release
      );

      // So, for some reason. I cannot make more than one instance of a ZipFileSystem
      // if I pass a URI in here. If I pass a Path in here instead, then I can make
      // multiple copies of it in memory. No idea why this is the way it is, but it
      // appears to be how the JavacFileManager in the JDK can make itself run in parallel
      // safely.
      fileSystem = JAR_FS_PROVIDER.access().newFileSystem(actualJarPath, env);

      // Always expect just one root directory in a ZIP archive.
      var rootDirectory = fileSystem.getRootDirectories().iterator().next();
      rootDirectoryPathRoot = new WrappingDirectoryImpl(rootDirectory);

      // We now compute the package mapping and the file mapping only when we first attempt
      // to access their values. This prevents creating additional overhead by indexing each JAR
      // as soon as we attempt to inspect any attribute within it, since we may not ever need to
      // process the actual values here.
      
      packages = new Lazy<>(() -> uncheckedIo(() -> {
        log.trace("Indexing packages in JAR {}...", actualJarPath);
        try (var walker = Files.walk(rootDirectory)) {
          return walker
              .filter(Files::isDirectory)
              .map(rootDirectory::relativize)
              .collect(Collectors.toUnmodifiableMap(
                  FileUtils::pathToBinaryName,
                  path -> new WrappingDirectoryImpl(rootDirectory.resolve(path)),
                  (a, b) -> {
                    log.trace(
                        "Found duplicate entry {}. This copy will be silently dropped.", 
                        b
                    );
                    return a;
                  }
              ));
        }
      }));

      files = new Lazy<>(() -> uncheckedIo(() -> {
        log.trace("Indexing files in JAR {}...", actualJarPath);
        var allPaths = new ArrayList<Path>(6);

        // We have to do this eagerly all at once as the walker streams must be closed to
        // prevent resource leakage elsewhere.
        try (var walker = Files.walk(rootDirectory)) {
          walker.forEach(allPaths::add);
        }

        return Collections.unmodifiableList(allPaths);
      }));
    }

    private void close() throws IOException {
      log.trace(
          "Closing JAR file system handle ({} @ {})",
          jarPath.getUri(),
          fileSystem.getRootDirectories()
      );
      fileSystem.close();
    }

    @Nullable
    private PathRoot getPackage(String name) {
      return packages.access().get(name);
    }

    private PathRoot getPathRoot() {
      return rootDirectoryPathRoot;
    }

    private Path getPath() {
      return rootDirectoryPathRoot.getPath();
    }

    private Collection<Path> getAllFiles() {
      return files.access();
    }
  }
}
