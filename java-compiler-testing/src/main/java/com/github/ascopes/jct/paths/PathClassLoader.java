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

package com.github.ascopes.jct.paths;

import com.github.ascopes.jct.intern.AsyncResourceCloser;
import com.github.ascopes.jct.intern.EnumerationAdapter;
import com.github.ascopes.jct.intern.StringUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;


/**
 * A classloader for multiple {@link Path} types, similar to {@link java.net.URLClassLoader}.
 *
 * <p>This can be applied to any combination of directory trees, JARs, WARs, and ZIP files to
 * allow resources and classes to be loaded from any of them.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class PathClassLoader extends ClassLoader {

  static {
    registerAsParallelCapable();
  }

  private static final Cleaner CLEANER = Cleaner.create();

  private final Collection<Path> paths;

  /**
   * Initialize the classloader, using the system classloader as a parent.
   *
   * @param candidatePaths the candidate paths to use.
   */
  public PathClassLoader(Iterable<? extends Path> candidatePaths) {
    this(candidatePaths, ClassLoader.getSystemClassLoader());
  }

  /**
   * Initialize the classloader.
   *
   * @param candidatePaths the candidate paths to use.
   * @param parent         the parent classloader to use.
   */
  public PathClassLoader(Iterable<? extends Path> candidatePaths, ClassLoader parent) {
    Objects.requireNonNull(candidatePaths);
    Objects.requireNonNull(parent);

    // Retain insertion order.
    paths = new LinkedHashSet<>();

    // When we get garbage collected, make sure to also close any file system resources we opened.
    var fileSystems = new HashMap<String, FileSystem>();

    //noinspection ThisEscapedInObjectConstruction
    CLEANER.register(this, new AsyncResourceCloser(fileSystems));

    for (var path : candidatePaths) {
      if (isJavaArchive(path)) {
        // If we have an archive type, treat it like a file system (Java's file system API will
        // implicitly open it for us).
        try {
          var zipFs = FileSystems.newFileSystem(path, (ClassLoader) null);
          var rootDir = zipFs.getRootDirectories().iterator().next();
          fileSystems.put(path.toUri().toString(), zipFs);
          paths.add(rootDir);
        } catch (IOException ex) {
          throw new UncheckedIOException("Failed to open zip archive " + path, ex);
        }
      } else {
        paths.add(path);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    return "PathClassLoader{"
        + "paths=" + StringUtils.quotedIterable(paths)
        + "}";
  }

  /**
   * {@inheritDoc}
   *
   * @param name the class name to load.
   * @return the loaded class.
   * @throws ClassNotFoundException if the class is not found.
   */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    var path = Path.of(name.replace('.', '/') + ".class");

    for (var root : paths) {
      var fullPath = root.resolve(path);
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
    return paths
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
      var iterator = paths
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

  private boolean isJavaArchive(Path path) {
    try {
      return "application/java-archive".equals(Files.probeContentType(path));
    } catch (IOException ex) {
      return false;
    }
  }
}
