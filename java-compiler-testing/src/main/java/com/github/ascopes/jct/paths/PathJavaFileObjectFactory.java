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

import static java.util.Objects.requireNonNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

/**
 * Factory for creating {@link PathJavaFileObject} instances consistently, with a customizable
 * charset.
 *
 * <p>This charset can be adjusted once initialized as needed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class PathJavaFileObjectFactory {

  // Determine this on startup to allow inclusion of new extensions that may be added in the
  // future, potentially.
  private static final SortedMap<String, Kind> EXTENSIONS_TO_KINDS;

  static {
    var kinds = new TreeMap<String, Kind>(String::compareToIgnoreCase);
    for (var kind : Kind.values()) {
      if (kind != Kind.OTHER) {
        kinds.put(kind.extension, kind);
      }
    }
    EXTENSIONS_TO_KINDS = Collections.unmodifiableSortedMap(kinds);
  }

  private volatile Charset charset;

  /**
   * Initialize this factory.
   *
   * @param charset the charset to use.
   */
  public PathJavaFileObjectFactory(Charset charset) {
    this.charset = requireNonNull(charset);
  }

  /**
   * Get the charset being used for this factory to read and write files with.
   *
   * @return the charset.
   */
  public Charset getCharset() {
    return charset;
  }

  /**
   * Change the charset to use for subsequent calls.
   *
   * @param charset the charset to change to.
   */
  public void setCharset(Charset charset) {
    this.charset = requireNonNull(charset);
  }

  /**
   * Create a {@link PathJavaFileObject} without knowing the given name.
   *
   * @param location the location of the object.
   * @param path     the path to the object.
   * @return the Java file object to use.
   */
  public PathJavaFileObject create(Location location, Path path) {
    return create(location, path, path.toString());
  }

  /**
   * Create a {@link PathJavaFileObject}.
   *
   * @param location  the location of the object.
   * @param path      the path to the object.
   * @param givenName the name that the user gave the file.
   * @return the Java file object to use.
   */
  public PathJavaFileObject create(Location location, Path path, String givenName) {
    var kind = guessKind(path);
    return new PathJavaFileObject(location, path, givenName, kind, charset);
  }

  private static Kind guessKind(Path path) {
    var fileName = path.getFileName().toString();
    var dotIndex = Math.max(fileName.lastIndexOf('.'), 0);

    var extension = fileName.substring(dotIndex);

    return Optional
        .ofNullable(EXTENSIONS_TO_KINDS.get(extension))
        .orElse(Kind.OTHER);
  }
}
