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
import javax.annotation.CheckReturnValue;
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
public interface ManagedDirectory extends DirectoryBuilder, PathRoot {

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
   * Create a file builder for the given path in this RAM file system.
   *
   * @param first the first path fragment.
   * @param rest  any additional path fragments.
   * @return the file builder.
   */
  @CheckReturnValue
  FileBuilder createFile(String first, String... rest);

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

}
