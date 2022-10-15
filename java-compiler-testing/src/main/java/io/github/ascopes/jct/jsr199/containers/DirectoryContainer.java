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
package io.github.ascopes.jct.jsr199.containers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.WillNotClose;
import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container that wraps a known directory of files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class DirectoryContainer implements Container {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryContainer.class);

  private final Location location;
  private final @WillNotClose PathLike root;
  private final String name;

  /**
   * Initialize this container.
   *
   * @param location the location.
   * @param root     the root directory to hold.
   */
  public DirectoryContainer(Location location, @WillNotClose PathLike root) {
    this.location = requireNonNull(location, "location");
    this.root = requireNonNull(root, "root");
    name = root.toString();
  }

  @Override
  public void close() throws IOException {
    // Do nothing for this implementation. We have nothing to close.
    // This also has the side effect of not closing RamPaths early, as we treat those as
    // DirectoryContainer types.
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var path = fileObject.getFullPath();
    return path.startsWith(root.getPath()) && Files.isRegularFile(path);
  }

  @Override
  public Optional<Path> findFile(String path) {
    if (path.startsWith("/")) {
      throw new IllegalArgumentException("Absolute paths are not supported (got '" + path + "')");
    }

    return Optional
        .of(FileUtils.relativeResourceNameToPath(root.getPath(), path))
        .filter(Files::isRegularFile);
  }

  @Override
  public Optional<byte[]> getClassBinary(String binaryName) throws IOException {
    var path = FileUtils.binaryNameToPath(root.getPath(), binaryName, Kind.CLASS);
    return Files.isRegularFile(path)
        ? Optional.of(Files.readAllBytes(path))
        : Optional.empty();
  }

  @Override
  public Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return Optional
        .of(FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName))
        .filter(Files::isRegularFile)
        .map(path -> new PathFileObject(location, root.getPath(), path));
  }

  @Override
  public Optional<PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    LOGGER.trace(
        "Output file - this={} packageName={}, relativeName={})",
        this,
        packageName,
        relativeName
    );

    return Optional
        .of(FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName))
        .map(path -> new PathFileObject(location, root.getPath(), path));
  }

  @Override
  public Optional<PathFileObject> getJavaFileForInput(
      String binaryName,
      Kind kind
  ) {
    return Optional
        .of(FileUtils.binaryNameToPath(root.getPath(), binaryName, kind))
        .filter(Files::isRegularFile)
        .map(path -> new PathFileObject(location, root.getPath(), path));
  }

  @Override
  public Optional<PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    LOGGER.trace("Output Java file - this={} className={}, kind={})", this, className, kind);

    return Optional
        .of(FileUtils.binaryNameToPath(root.getPath(), className, kind))
        .map(path -> new PathFileObject(location, root.getPath(), path));
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public ModuleFinder getModuleFinder() {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PathLike getPath() {
    return root;
  }

  @Override
  public Optional<URL> getResource(String resourcePath) throws IOException {
    var path = FileUtils.relativeResourceNameToPath(root.getPath(), resourcePath);
    // Getting a URL of a directory within a JAR breaks the JAR file system implementation
    // completely.
    return Files.isRegularFile(path)
        ? Optional.of(path.toUri().toURL())
        : Optional.empty();
  }

  @Override
  public Optional<String> inferBinaryName(PathFileObject javaFileObject) {
    return javaFileObject.getFullPath().startsWith(root.getPath())
        ? Optional.of(FileUtils.pathToBinaryName(javaFileObject.getRelativePath()))
        : Optional.empty();
  }

  @WillNotClose
  @Override
  @SuppressWarnings("resource")
  public Stream<? extends PathFileObject> listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    var maxDepth = recurse ? Integer.MAX_VALUE : 1;

    var basePath = FileUtils.packageNameToPath(root.getPath(), packageName);

    try {
      return Files
          .walk(basePath, maxDepth, FileVisitOption.FOLLOW_LINKS)
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObject(location, root.getPath(), path));

    } catch (NoSuchFileException ex) {
      return Stream.empty();
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", root.getUri())
        .attribute("location", location)
        .toString();
  }
}
