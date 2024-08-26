/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base interface for a managed directory, including the interfaces for creating fluent-style
 * builders.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface ManagedDirectory extends DirectoryBuilder, PathRoot {

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * <p>For example:
   *
   * <pre><code>
   *   thisDirectory
   *       .createFile("foo", "bar", "baz.txt").withContents(...)
   *       .and().also()
   *       .createFile("foo", "bar", "bork.txt").withContents(...);
   * </code></pre>
   *
   * @return this object.
   * @see #and
   * @see #then
   */
  default ManagedDirectory also() {
    return this;
  }

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * <p>For example:
   *
   * <pre><code>
   *   thisDirectory
   *       .createFile("foo", "bar", "baz.txt").withContents(...)
   *       .and()
   *       .createFile("foo", "bar", "bork.txt").withContents(...);
   * </code></pre>
   *
   * @return this object.
   * @see #also
   * @see #then
   */
  default ManagedDirectory and() {
    return this;
  }

  /**
   * Close the resource.
   *
   * <p>This specifically is not provided by implementing {@link Closeable} as to prevent IDEs
   * giving false linting errors about not closing resources.
   *
   * <p>Users should not need to call this directly if they are using instances produced from a
   * {@link Workspace} implementation.
   *
   * @throws IOException if an IO exception occurs.
   */
  void close() throws IOException;

  /**
   * Create a directory builder for the given path in this RAM file system.
   *
   * <p>Examples:
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   dir.createDirectory("foo/bar/baz")...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   dir.createDirectory("foo", "bar", "baz")...;
   * </code></pre>
   *
   * @param fragments the parts of the path.
   * @return the directory builder.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any of the path fragments are {@code null}.
   */
  default DirectoryBuilder createDirectory(String... fragments) {
    return createDirectory(List.of(fragments));
  }

  /**
   * Create a directory builder for the given path in this RAM file system.
   *
   * <p>Examples:
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   dir.createDirectory(List.of("foo/bar/baz"))...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   dir.createDirectory(List.of("foo", "bar", "baz"))...;
   * </code></pre>
   *
   * @param fragments the parts of the path.
   * @return the directory builder.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any of the path fragments are {@code null}.
   * @since 4.0.0
   */
  @API(since = "4.0.0", status = Status.STABLE)
  DirectoryBuilder createDirectory(List<String> fragments);

  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   dir.createFile("foo/bar/baz.txt")...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   dir.createFile("foo", "bar", "baz.txt")...;
   * </code></pre>
   *
   * @param fragments the parts of the path.
   * @return the file builder.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any of the path fragments are {@code null}.
   */
  default FileBuilder createFile(String... fragments) {
    return createFile(List.of(fragments));
  }

  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   dir.createFile(List.of("foo/bar/baz.txt"))...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   dir.createFile(List.of("foo", "bar", "baz.txt"))...;
   * </code></pre>
   *
   * @param fragments the parts of the path.
   * @return the file builder.
   * @throws IllegalArgumentException if no path fragments are provided.
   * @throws NullPointerException     if any of the path fragments are {@code null}.
   * @since 4.0.0
   */
  @API(since = "4.0.0", status = Status.STABLE)
  FileBuilder createFile(List<String> fragments);

  /**
   * Get the identifying name of the temporary file system.
   *
   * @return the identifier string.
   */
  String getName();

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * <p>For example:
   *
   * <pre><code>
   *   thisDirectory
   *       .createFile("foo", "bar", "baz.txt").withContents(...)
   *       .and().then()
   *       .createFile("foo", "bar", "bork.txt").withContents(...);
   * </code></pre>
   *
   * @return this object.
   * @see #and
   * @see #also
   */
  default ManagedDirectory then() {
    return this;
  }

}
