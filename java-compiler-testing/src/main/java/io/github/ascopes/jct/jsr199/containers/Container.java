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

import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import java.io.Closeable;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Container that wraps a file path source of some description.
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
   * @param path the path to the file to find.
   * @return the path if the file exists, or an empty optional if it does not exist.
   */
  Optional<Path> findFile(String path);

  /**
   * Get the binary data for a class, if it exists.
   *
   * @param binaryName the binary name of the class.
   * @return the binary data, if it exists, otherwise an empty optional.
   * @throws IOException if an IO exception occurs.
   */
  Optional<byte[]> getClassBinary(String binaryName) throws IOException;

  /**
   * Get a {@link FileObject} for reading, if it exists.
   *
   * <p>If the file does not exist, an empty optional is returned.
   *
   * @param packageName  the package name of the file.
   * @param relativeName the relative name of the file in the package.
   * @return the file object, or an empty optional if it does not exist.
   */
  Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  );

  /**
   * Get a {@link FileObject} for writing.
   *
   * <p>If the container is read-only, an empty optional is returned.
   *
   * @param packageName  the package name of the file.
   * @param relativeName the relative name of the file in the package.
   * @return the file object, or an empty optional if this container is read-only.
   */
  Optional<PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  );

  /**
   * Get a {@link JavaFileObject} for reading, if it exists.
   *
   * <p>If the file does not exist, an empty optional is returned.
   *
   * @param className the binary name of the class to open.
   * @param kind      the kind of file to open.
   * @return the file object, or an empty optional if it does not exist.
   */
  Optional<PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  );

  /**
   * Get a {@link JavaFileObject} for writing.
   *
   * <p>If the container is read-only, an empty optional is returned.
   *
   * @param className the binary name of the class to open.
   * @param kind      the kind of file to open.
   * @return the file object, or an empty optional if this container is read-only.
   */
  Optional<PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  );

  /**
   * Get the location of this container.
   *
   * @return the location.
   */
  Location getLocation();

  /**
   * Get a module finder for this container.
   *
   * @return the module finder for this container.
   */
  ModuleFinder getModuleFinder();

  /**
   * Get the name of this container for human consumption.
   *
   * @return the container name.
   */
  String getName();

  /**
   * Get the path of the container.
   *
   * @return the path.
   */
  PathLike getPath();

  /**
   * Get a classpath resource for the given resource path if it exists.
   *
   * <p>If the resource does not exist, then return an empty optional.
   *
   * @param resourcePath the path to the resource.
   * @return the URL to the resource if it exists, or an empty optional if it does not exist.
   * @throws IOException if an IO error occurs looking for the resource.
   */
  Optional<URL> getResource(String resourcePath) throws IOException;

  /**
   * Infer the binary name of a given Java file object.
   *
   * @param javaFileObject the Java file object to infer the binary name of.
   * @return the name, or an empty optional if the file does not exist in this container.
   */
  Optional<String> inferBinaryName(PathFileObject javaFileObject);

  /**
   * List all the file objects that match the given criteria in this group.
   *
   * @param packageName the package name to look in.
   * @param kinds       the kinds of file to look for.
   * @param recurse     {@code true} to recurse subpackages, {@code false} to only consider the
   *                    given package.
   * @return the stream of results.
   */
  Collection<? extends PathFileObject> list(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException;


}
