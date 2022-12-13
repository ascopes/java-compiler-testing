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
package io.github.ascopes.jct.workspaces;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import javax.annotation.CheckReturnValue;
import javax.annotation.WillClose;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base interface for a managed directory, including the interfaces for creating fluent-style
 * builders.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface ManagedDirectory extends PathRoot {

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #and
   * @see #then
   */
  @CheckReturnValue
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
  @CheckReturnValue
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
   * @param first the first path fragment.
   * @param rest  any additional path fragments.
   * @return the directory builder.
   */
  @CheckReturnValue
  DirectoryBuilder createDirectory(String first, String... rest);

  /**
   * Create a directory builder for the given path in this RAM file system.
   *
   * @param path the <strong>relative</strong> path within the RAM file system to use.
   * @return the directory builder.
   */
  @CheckReturnValue
  DirectoryBuilder createDirectory(Path path);

  /**
   * Add contents to the root directory in this RAM file system.
   *
   * @return the directory builder.
   */
  @CheckReturnValue
  DirectoryBuilder rootDirectory();

  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * @param first the first path fragment.
   * @param rest  any additional path fragments.
   * @return the file builder.
   */
  @CheckReturnValue
  FileBuilder createFile(String first, String... rest);


  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * @param path the <strong>relative</strong> path within the RAM file system to use.
   * @return the file builder.
   */
  @CheckReturnValue
  FileBuilder createFile(Path path);

  /**
   * Get the identifying name of the temporary file system.
   *
   * @return the identifier string.
   */
  @CheckReturnValue
  String getName();

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #and
   * @see #also
   */
  @CheckReturnValue
  default ManagedDirectory then() {
    return this;
  }

  /**
   * Chainable builder for creating individual files.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  interface FileBuilder {

    /**
     * Create the file with the given contents.
     *
     * @param lines the lines to write using the default charset.
     * @return the file system for further configuration.
     */
    ManagedDirectory withContents(String... lines);

    /**
     * Create the file with the given contents.
     *
     * @param charset the character encoding to use.
     * @param lines   the lines to write.
     * @return the file system for further configuration.
     */
    ManagedDirectory withContents(Charset charset, String... lines);

    /**
     * Create the file with the given byte contents.
     *
     * @param contents the bytes to write.
     * @return the file system for further configuration.
     */
    ManagedDirectory withContents(byte[] contents);

    /**
     * Copy a resource from the class loader on the current thread into the file system.
     *
     * @param resource the resource to copy.
     * @return the file system for further configuration.
     */
    ManagedDirectory copiedFromClassPath(String resource);

    /**
     * Copy a resource from the given class loader into the file system.
     *
     * @param classLoader the class loader to use.
     * @param resource    the resource to copy.
     * @return the file system for further configuration.
     */
    ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource);

    /**
     * Copy the contents from the given file into the file system.
     *
     * @param file the file to read.
     * @return the file system for further configuration.
     */
    ManagedDirectory copiedFromFile(File file);

    /**
     * Copy the contents from the given path into the file system.
     *
     * @param file the file to read.
     * @return the file system for further configuration.
     */
    ManagedDirectory copiedFromFile(Path file);

    /**
     * Copy the contents from the given URL into the file system.
     *
     * @param url the URL to read.
     * @return the file system for further configuration.
     */
    ManagedDirectory copiedFromUrl(URL url);

    /**
     * Create an empty file with nothing in it.
     *
     * @return the file system for further configuration.
     */
    ManagedDirectory thatIsEmpty();

    /**
     * Copy the contents from the given input stream into the file system.
     *
     * <p>The input stream will be closed when reading completes.
     *
     * @param inputStream the input stream to read.
     * @return the file system for further configuration.
     */
    ManagedDirectory fromInputStream(@WillClose InputStream inputStream);
  }

  /**
   * Chainable builder for creating directories.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  interface DirectoryBuilder {

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * <p>This uses the default file system.
     *
     * @param first the first path fragment of the directory to copy from.
     * @param rest  any additional path fragments to copy from.
     * @return the file system for further configuration.
     */
    ManagedDirectory copyContentsFrom(String first, String... rest);

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param dir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    ManagedDirectory copyContentsFrom(File dir);

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param rootDir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    ManagedDirectory copyContentsFrom(Path rootDir);

    /**
     * Create an empty directory.
     *
     * @return the file system for further configuration.
     */
    ManagedDirectory thatIsEmpty();
  }
}
