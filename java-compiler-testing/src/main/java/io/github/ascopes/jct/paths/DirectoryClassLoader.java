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

package io.github.ascopes.jct.paths;

import io.github.ascopes.jct.intern.EnumerationAdapter;
import io.github.ascopes.jct.intern.StringUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * A classloader for multiple {@link Path} types, similar to {@link java.net.URLClassLoader}, but
 * that can only apply to one or more directory trees.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class DirectoryClassLoader extends ClassLoader {

  static {
    registerAsParallelCapable();
  }

  private final Collection<Path> dirs;

  /**
   * Initialize the classloader, using the system classloader as a parent.
   *
   * @param dirs the paths of directories to use.
   */
  public DirectoryClassLoader(Iterable<? extends Path> dirs) {
    Objects.requireNonNull(dirs);

    // Retain insertion order.
    this.dirs = new LinkedHashSet<>();
    dirs.forEach(this.dirs::add);
  }

  @Override
  public String toString() {
    return "DirectoryClassLoader{"
        + "dirs=" + StringUtils.quotedIterable(dirs)
        + "}";
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    var pathName = name.replace('.', '/') + ".class";

    for (var root : dirs) {
      var fullPath = root.resolve(pathName);
      if (Files.isRegularFile(fullPath)) {
        try {
          var classData = Files.readAllBytes(fullPath);
          return defineClass(null, classData, 0, classData.length);
        } catch (IOException ex) {
          throw new ClassNotFoundException("Failed to read resource " + fullPath, ex);
        }
      }
    }

    throw new ClassNotFoundException(name);
  }

  /**
   * Find a resource in the paths.
   *
   * @param name the name of the resource.
   * @return the URL to the resource, or {@code null} if not found.
   */
  @Override
  protected URL findResource(String name) {
    return dirs
        .stream()
        .map(root -> root.resolve(name))
        .filter(Files::isRegularFile)
        .map(this::pathToUrl)
        .findFirst()
        .orElse(null);
  }

  /**
   * Find resources with the given name in the paths.
   *
   * @param name the name of the resource.
   * @return an enumeration of the URLs of any resources found that match the given name.
   */
  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    try {
      var iterator = dirs
          .stream()
          .map(root -> root.resolve(name))
          .filter(Files::isRegularFile)
          .map(this::pathToUrl)
          .iterator();
      return new EnumerationAdapter<>(iterator);
    } catch (IllegalArgumentException ex) {
      throw new IOException("Failed to read one or more resources", ex);
    }
  }

  /**
   * Convert a path to a URL, throwing an unchecked exception if the conversion fails.
   *
   * @param path the path to convert.
   * @return the URL.
   * @throws IllegalArgumentException if the URL conversion fails.
   */
  private URL pathToUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException("Cannot convert path to URL", ex);
    }
  }
}
