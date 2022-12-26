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
package io.github.ascopes.jct.containers;

import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.Closeable;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Container that wraps a file path source of some description.
 *
 * <p>Closing this resource will close any internally held resources that were opened internally.
 * Already-opened resources passed to the implementation will not be closed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface Container extends Closeable {

  /**
   * Determine if this container contains the path file object.
   *
   * @param fileObject the file object to look up.
   * @return {@code true} if the object exists in this container, or {@code false} otherwise.
   */
  boolean contains(PathFileObject fileObject);

  /**
   * Find the physical path to a given string file path.
   *
   * @param fragment  the first fragment to the file to find.
   * @param fragments any additional fragments to the file to find.
   * @return the path if the file exists, or null if it does not exist.
   */
  @Nullable
  Path getFile(String fragment, String... fragments);

  /**
   * Get a {@link FileObject} for reading, if it exists.
   *
   * <p>If the file does not exist, {@code null} is returned.
   *
   * @param packageName  the package name of the file.
   * @param relativeName the relative name of the file in the package.
   * @return the file object, or null if it does not exist.
   */
  @Nullable
  PathFileObject getFileForInput(String packageName, String relativeName);

  /**
   * Get a {@link FileObject} for writing.
   *
   * <p>If the container is read-only, {@code null} is returned.
   *
   * @param packageName  the package name of the file.
   * @param relativeName the relative name of the file in the package.
   * @return the file object, or null if this container is read-only.
   */
  @Nullable
  PathFileObject getFileForOutput(String packageName, String relativeName);

  /**
   * Get a {@link JavaFileObject} for reading, if it exists.
   *
   * <p>If the file does not exist, {@code null} is returned.
   *
   * @param className the binary name of the class to open.
   * @param kind      the kind of file to open.
   * @return the file object, or null if it does not exist.
   */
  @Nullable
  PathFileObject getJavaFileForInput(String className, Kind kind);

  /**
   * Get a {@link JavaFileObject} for writing.
   *
   * <p>If the container is read-only, {@code null} is returned.
   *
   * @param className the binary name of the class to open.
   * @param kind      the kind of file to open.
   * @return the file object, or null if this container is read-only.
   */
  @Nullable
  PathFileObject getJavaFileForOutput(String className, Kind kind);

  /**
   * Get the location of this container.
   *
   * @return the location.
   */
  Location getLocation();

  /**
   * Get a module finder for this container.
   *
   * @return the module finder for this container, or {@code null} if not relevant to the
   *     implementation.
   */
  @Nullable
  ModuleFinder getModuleFinder();

  /**
   * Get the name of this container for human consumption.
   *
   * @return the container name.
   */
  String getName();

  /**
   * Get the path root of the container.
   *
   * @return the path root.
   */
  PathRoot getPathRoot();

  /**
   * Infer the binary name of a given Java file object.
   *
   * @param javaFileObject the Java file object to infer the binary name of.
   * @return the name, or null if the file does not exist in this container.
   */
  @Nullable
  String inferBinaryName(PathFileObject javaFileObject);

  /**
   * List all files within this container.
   *
   * @return all files within the container, relative to the container base path.
   * @throws IOException if the file system failed to be read.
   */
  Collection<Path> listAllFiles() throws IOException;

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @param collection  the collection to fill.
   * @throws IOException if the file system failed to be read.
   */
  void listFileObjects(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse,
      Collection<JavaFileObject> collection
  ) throws IOException;
}
