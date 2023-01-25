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

import java.io.File;
import java.nio.file.Path;
import javax.annotation.concurrent.ThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Chainable builder for creating directories.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
public interface DirectoryBuilder {

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * <p>This uses the default file system.
   *
   * <p>Examples:
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   directoryBuilder.copyContentsFrom("foo/bar/baz");
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   directoryBuilder.copyContentsFrom("foo", "bar", "baz");
   * </code></pre>
   *
   * @param first the first part of the path to the directory to copy from.
   * @param rest  any additional parts of the path.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copyContentsFrom(String first, String... rest);

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * @param dir the directory to copy the contents from.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copyContentsFrom(File dir);

  /**
   * Copy the contents of the directory at the given path recursively into this directory.
   *
   * @param rootDir the directory to copy the contents from.
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory copyContentsFrom(Path rootDir);

  /**
   * Create an empty directory.
   *
   * @return the root managed directory for further configuration.
   */
  ManagedDirectory thatIsEmpty();
}
