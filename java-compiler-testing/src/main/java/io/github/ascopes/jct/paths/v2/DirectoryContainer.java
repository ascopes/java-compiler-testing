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

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.FileUtils;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A container that wraps a known directory of files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class DirectoryContainer implements Container {

  private final Path root;
  private final String name;

  /**
   * Initialize this container.
   *
   * @param root the root directory to
   */
  public DirectoryContainer(Path root) {
    this.root = requireNonNull(root, "root");
    name = root.toString();
  }

  @Override
  public void close() throws IOException {
    // Do nothing for this implementation. We have nothing to close.
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    var path = fileObject.getPath();
    return path.startsWith(root) && Files.isRegularFile(path);
  }

  @Override
  public Optional<? extends Path> findFile(String path) {
    return Optional
        .of(FileUtils.relativeResourceNameToPath(root, path))
        .filter(Files::isRegularFile);
  }

  @Override
  public Optional<? extends byte[]> getClassBinary(String binaryName) throws IOException {
    var path = FileUtils.binaryNameToPath(root, binaryName, Kind.CLASS);
    return Files.isRegularFile(path)
        ? Optional.of(Files.readAllBytes(path))
        : Optional.empty();
  }

  @Override
  public Optional<? extends PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return Optional
        .of(FileUtils.resourceNameToPath(root, packageName, relativeName))
        .filter(Files::isRegularFile)
        .map(PathFileObject::new);
  }

  @Override
  public Optional<? extends PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    return Optional
        .of(FileUtils.resourceNameToPath(root, packageName, relativeName))
        .map(PathFileObject::new);
  }

  @Override
  public Optional<? extends PathFileObject> getJavaFileForInput(
      String binaryName,
      Kind kind
  ) {
    return Optional
        .of(FileUtils.binaryNameToPath(root, binaryName, kind))
        .filter(Files::isRegularFile)
        .map(PathFileObject::new);
  }

  @Override
  public Optional<? extends PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    return Optional
        .of(FileUtils.binaryNameToPath(root, className, kind))
        .map(PathFileObject::new);
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
  public Optional<? extends URL> getResource(String resourcePath) throws IOException {
    var path = FileUtils.relativeResourceNameToPath(root, resourcePath);
    return Files.isRegularFile(path)
        ? Optional.of(path.toUri().toURL())
        : Optional.empty();
  }

  @Override
  public Optional<? extends String> inferBinaryName(PathFileObject javaFileObject) {
    return Optional
        .of(javaFileObject.getPath())
        .filter(path -> path.startsWith(root))
        .filter(Files::isRegularFile)
        .map(FileUtils::pathToBinaryName);
  }

  @Override
  public Collection<? extends PathFileObject> list(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    var maxDepth = recurse ? Integer.MAX_VALUE : 1;

    try (var walker = Files.walk(root, maxDepth, FileVisitOption.FOLLOW_LINKS)) {
      return walker
          .filter(FileUtils.fileWithAnyKind(kinds))
          .map(PathFileObject::new)
          .collect(Collectors.toList());
    }
  }
}
