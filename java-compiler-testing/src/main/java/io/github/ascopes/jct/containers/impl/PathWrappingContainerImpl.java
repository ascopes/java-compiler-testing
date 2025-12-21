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
package io.github.ascopes.jct.containers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.filemanagers.impl.PathFileObjectImpl;
import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container that wraps a known directory of files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class PathWrappingContainerImpl implements Container {

  private static final Logger log = LoggerFactory.getLogger(PathWrappingContainerImpl.class);

  private final Location location;
  private final PathRoot root;

  /**
   * Initialize this container.
   *
   * @param location the location.
   * @param root     the root directory to hold.
   */
  public PathWrappingContainerImpl(Location location, PathRoot root) {
    this.location = requireNonNull(location, "location");
    this.root = requireNonNull(root, "root");
  }

  @Override
  public void close() {
    // Do nothing for this implementation. We have nothing to close here. Anything wrapped in
    // this type will be managed by the Workspace object instead if it needs to be closed.
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var path = fileObject.getAbsolutePath();
    return path.startsWith(root.getPath()) && Files.isRegularFile(path);
  }

  @Nullable
  @Override
  public Path getFile(String... fragments) {
    var realPath = FileUtils.relativeResourceNameToPath(root.getPath(), fragments);

    return Files.isRegularFile(realPath)
        ? realPath
        : null;
  }

  @Nullable
  @Override
  public PathFileObject getFileForInput(String packageName, String relativeName) {
    var path = FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName);

    return Files.isRegularFile(path)
        ? new PathFileObjectImpl(location, root.getPath(), path)
        : null;
  }

  @Override
  public PathFileObject getFileForOutput(String packageName, String relativeName) {
    var path = FileUtils.resourceNameToPath(root.getPath(), packageName, relativeName);
    return new PathFileObjectImpl(location, root.getPath(), path);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the same value as {@link #getPathRoot}, since this
   * implementation is not an opaque wrapper for an archive or serialized resource.
   *
   * @returns the path root.
   */
  @Override
  public PathRoot getInnerPathRoot() {
    return root;
  }

  @Nullable
  @Override
  public PathFileObject getJavaFileForInput(String binaryName, Kind kind) {
    var path = FileUtils.binaryNameToPath(root.getPath(), binaryName, kind);
    return Files.isRegularFile(path)
        ? new PathFileObjectImpl(location, root.getPath(), path)
        : null;
  }

  @Override
  public PathFileObject getJavaFileForOutput(String className, Kind kind) {
    var path = FileUtils.binaryNameToPath(root.getPath(), className, kind);
    return new PathFileObjectImpl(location, root.getPath(), path);
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public ModuleFinder getModuleFinder() {
    return ModuleFinder.of(getPathRoot().getPath());
  }

  @Override
  public String getName() {
    return root.toString();
  }

  @Override
  public PathRoot getPathRoot() {
    return root;
  }

  @Nullable
  @Override
  public String inferBinaryName(PathFileObject javaFileObject) {
    return javaFileObject.getAbsolutePath().startsWith(root.getPath())
        ? FileUtils.pathToBinaryName(javaFileObject.getRelativePath())
        : null;
  }

  @Override
  public Collection<Path> listAllFiles() throws IOException {
    try (var walker = Files.walk(root.getPath(), FileVisitOption.FOLLOW_LINKS)) {
      return walker.toList();
    }
  }

  @Override
  public void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException {
    var maxDepth = recurse ? Integer.MAX_VALUE : 1;
    var basePath = FileUtils.packageNameToPath(root.getPath(), packageName);

    // XXX: Do we want to follow symbolic links here, or can it lead to recursive loops?
    try (var walker = Files.walk(basePath, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(path -> new PathFileObjectImpl(location, root.getPath(), path))
          .forEach(collection::add);
    } catch (NoSuchFileException ex) {
      log.trace("Directory {} does not exist so is being ignored", root.getPath());
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
