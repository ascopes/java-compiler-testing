/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.containers;

import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base interface representing a group of package-oriented paths.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
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
  void addPackage(@WillNotClose PathRoot path);

  /**
   * Get a class loader for this group of containers.
   *
   * <p>Note that adding additional containers to this group after accessing this class loader
   * may result in the class loader being destroyed or re-created.
   *
   * @return the class loader.
   */
  ClassLoader getClassLoader();

  /**
   * Find the first occurrence of a given path to a file in packages or modules.
   *
   * <p>Modules are treated as subdirectories.
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   containerGroup.getFile("foo/bar/baz.txt")...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   containerGroup.getFile("foo", "bar", "baz.txt");
   * </code></pre>
   *
   * @param fragment  the first part of the path.
   * @param fragments any additional parts of the path.
   * @return the first occurrence of the path in this group, or null if not found.
   * @throws IllegalArgumentException if the provided path is absolute.
   */
  @Nullable
  Path getFile(String fragment, String... fragments);

  /**
   * Get a {@link FileObject} that can have content read from it.
   *
   * <p>This will return {@code null} if no file is found matching the criteria.
   *
   * @param packageName  the package name of the file to read.
   * @param relativeName the relative name of the file to read.
   * @return the file object, or null if the file is not found.
   */
  @Nullable
  PathFileObject getFileForInput(String packageName, String relativeName);

  /**
   * Get a {@link FileObject} that can have content written to it for the given file.
   *
   * <p>This will attempt to write to the first writeable path in this group. {@code null}
   * will be returned if no writeable paths exist in this group.
   *
   * @param packageName  the name of the package the file is in.
   * @param relativeName the relative name of the file within the package.
   * @return the {@link FileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getFileForOutput(String packageName, String relativeName);

  /**
   * Get a {@link JavaFileObject} that can have content read from it for the given file.
   *
   * <p>This will return {@code null} if no file is found matching the criteria.
   *
   * @param className the binary name of the class to read.
   * @param kind      the kind of file to read.
   * @return the {@link JavaFileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getJavaFileForInput(String className, Kind kind);

  /**
   * Get a {@link JavaFileObject} that can have content written to it for the given class.
   *
   * <p>This will attempt to write to the first writeable path in this group. {@code null}
   * will be returned if no writeable paths exist in this group.
   *
   * @param className the name of the class.
   * @param kind      the kind of the class file.
   * @return the {@link JavaFileObject} to write to, or null if this group has no paths that can be
   *     written to.
   */
  @Nullable
  PathFileObject getJavaFileForOutput(String className, Kind kind);

  /**
   * Get the package-oriented location that this group of paths is for.
   *
   * @return the package-oriented location.
   */
  Location getLocation();

  /**
   * Get the package containers in this group.
   *
   * <p>Returned packages are presented in the order that they were registered. This is the
   * resolution order that the compiler will use.
   *
   * @return the containers.
   */
  List<Container> getPackages();

  /**
   * Try to infer the binary name of a given file object.
   *
   * @param fileObject the file object to infer the binary name for.
   * @return the binary name if known, or null otherwise.
   */
  @Nullable
  String inferBinaryName(PathFileObject fileObject);

  /**
   * Determine if this group has no paths registered.
   *
   * @return {@code true} if no paths are registered. {@code false} if paths are registered.
   */
  boolean isEmpty();

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @param collection  the collection to fill.
   * @throws IOException if the file lookup fails due to an IO exception.
   */
  void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException;
}
