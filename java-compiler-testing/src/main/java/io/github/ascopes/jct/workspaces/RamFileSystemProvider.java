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

import io.github.ascopes.jct.workspaces.impl.MemoryFileSystemProvider;
import java.nio.file.FileSystem;
import java.util.ServiceLoader;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Service provider interface for a RAM-based file system provider.
 *
 * <p>This enables swapping out the default RAM file system implementation with a custom
 * implementation by using Java's {@link ServiceLoader} mechanism.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @deprecated this feature should never be needed by users of this library, and so has been
 * deprecated for removal in v1.0.0.
 */
@API(since = "0.0.1", status = Status.STABLE)
@Deprecated(since = "0.6.0", forRemoval = true)
public interface RamFileSystemProvider {

  /**
   * Create the new file system.
   *
   * @param name the file system name to use.
   * @return the file system object.
   */
  FileSystem createFileSystem(String name);

  /**
   * Get the service provider implementation to use.
   *
   * @return the first service provider implementation, or a default implementation if not provided.
   */
  static RamFileSystemProvider getInstance() {
    return ServiceLoader
        .load(RamFileSystemProvider.class)
        .findFirst()
        .orElseGet(MemoryFileSystemProvider::getInstance);
  }
}
