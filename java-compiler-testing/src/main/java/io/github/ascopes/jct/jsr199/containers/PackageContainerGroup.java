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

import io.github.ascopes.jct.annotations.WillCloseWhenClosed;
import io.github.ascopes.jct.annotations.WillNotClose;
import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Interface describing a group of containers.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface PackageContainerGroup extends ContainerGroup {

  /**
   * Add a container to this group.
   *
   * <p>The provided container will be closed when this group is closed.
   *
   * @param container the container to add.
   */
  void addPackage(@WillCloseWhenClosed Container container);

  /**
   * Add a path to this group.
   *
   * <p>Note that this will destroy the {@link #getClassLoader() classloader} if one is already
   * allocated.
   *
   * <p>If the path points to some form of archive (such as a JAR), then this may open that archive
   * in a new resource internally. If this occurs, then the resource will always be freed by this
   * class by calling {@link #close()}.
   *
   * <p>Any other closable resources passed to this function will not be closed by this
   * implementation. You must handle the lifecycle of those objects yourself.
   *
   * @param path the path to add.
   */
  void addPackage(@WillNotClose PathLike path);

  /**
   * Find the first occurrence of a given path to a file.
   *
   * @param path the path to the file to find.
   * @return the first occurrence of the path in this group, or an empty optional if not found.
   */
  Optional<Path> findFile(String path);

  /**
   * Get a {@link FileObject} that can have content read from it.
   *
   * <p>This will return an empty optional if no file is found.
   *
   * @param packageName  the package name of the file to read.
   * @param relativeName the relative name of the file to read.
   * @return the file object, or an empty optional if the file is not found.
   */
  Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  );

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
  Optional<PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  );

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
  Optional<PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  );

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
  Optional<PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  );

  /**
   * Get the package-oriented location that this group of paths is for.
   *
   * @return the package-oriented location.
   */
  Location getLocation();

  /**
   * Get the package containers in this group.
   *
   * @return the containers.
   */
  Iterable<? extends Container> getPackages();

  /**
   * Try to infer the binary name of a given file object.
   *
   * @param fileObject the file object to infer the binary name for.
   * @return the binary name if known, or an empty optional otherwise.
   */
  Optional<String> inferBinaryName(PathFileObject fileObject);

  /**
   * Determine if this group has no paths registered.
   *
   * @return {@code true} if no paths are registered. {@code false} if paths are registered.
   */
  boolean isEmpty();

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * <p>The returned stream must be explicitly closed to prevent resource leaks.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @return a stream of resultant file objects.
   * @throws UncheckedIOException if the file lookup fails due to an IO exception.
   */
  @WillNotClose
  Stream<? extends PathFileObject> listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  );
}
