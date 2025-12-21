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
package io.github.ascopes.jct.workspaces;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

/**
 * Chainable builder for creating directories.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public interface DirectoryBuilder {

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * <p>Symbolic links will not be followed.
   *
   * <p>This uses the default file system. If you want to use a different {@link FileSystem}
   * as your source, then use {@link #copyContentsFrom(Path)} instead.
   *
   * <p>Examples:
   *
   * <pre><code>
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   directoryBuilder.copyContentsFrom(List.of("foo", "bar", "baz"));
   *
   *   // Using POSIX platform-specific separators (may cause issues if your tests run on Windows)
   *   directoryBuilder.copyContentsFrom(List.of("foo/bar/baz"));
   *
   *   // Using Windows platform-specific separators (may cause issues if your tests run on POSIX)
   *   directoryBuilder.copyContentsFrom(List.of("foo\\bar\\baz"));
   * </code></pre>
   *
   * @param fragments parts of the path.
   * @return the root managed directory for further configuration.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any null path fragments are provided.
   * @see #copyContentsFrom(Path)
   * @see #copyContentsFrom(String...)
   * @since 4.0.0
   */
  ManagedDirectory copyContentsFrom(List<String> fragments);

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * <p>Symbolic links will not be followed.
   *
   * <p>This uses the default file system. If you want to use a different {@link FileSystem}
   * as your source, then use {@link #copyContentsFrom(Path)} instead.
   *
   * <p>Examples:
   *
   * <pre><code>
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   directoryBuilder.copyContentsFrom("foo", "bar", "baz");
   *
   *   // Using POSIX platform-specific separators (may cause issues if your tests run on Windows)
   *   directoryBuilder.copyContentsFrom("foo/bar/baz");
   *
   *   // Using Windows platform-specific separators (may cause issues if your tests run on POSIX)
   *   directoryBuilder.copyContentsFrom("foo\\bar\\baz");
   * </code></pre>
   *
   * @param fragments parts of the path.
   * @return the root managed directory for further configuration.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any null path fragments are provided.
   * @see #copyContentsFrom(Path)
   * @see #copyContentsFrom(List)
   */
  default ManagedDirectory copyContentsFrom(String... fragments) {
    return copyContentsFrom(List.of(fragments));
  }

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * <p>Symbolic links will not be followed.
   *
   * <pre><code>
   *  directory.copyContentsFrom(new File("code/examples"));
   * </code></pre>
   *
   * @param dir the directory to copy the contents from.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copyContentsFrom(File dir);

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * <p>Symbolic links will not be followed.
   *
   * <pre><code>
   *   directory.copyContentsFrom(Path.of("code", "examples"));
   * </code></pre>
   *
   * @param rootDir the directory to copy the contents from.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copyContentsFrom(Path rootDir);

  /**
   * Create an empty directory.
   *
   * <pre><code>
   *   directory.thatIsEmpty();
   * </code></pre>
   *
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory thatIsEmpty();
}
