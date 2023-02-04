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
package io.github.ascopes.jct.workspaces;

import java.io.Closeable;
import java.io.IOException;
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
   * @param first the first part of the path.
   * @param rest  any additional parts of the path.
   * @return the directory builder.
   */
  DirectoryBuilder createDirectory(String first, String... rest);

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
   * @param fragment  the first part of the path.
   * @param fragments any additional parts of the path.
   * @return the file builder.
   */
  FileBuilder createFile(String fragment, String... fragments);

  /**
   * Get the identifying name of the temporary file system.
   *
   * @return the identifier string.
   */
  String getName();

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #and
   * @see #also
   */
  default ManagedDirectory then() {
    return this;
  }

}
