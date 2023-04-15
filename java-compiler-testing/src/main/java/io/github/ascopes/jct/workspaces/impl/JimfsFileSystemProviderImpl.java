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
package io.github.ascopes.jct.workspaces.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.workspaces.RamFileSystemProvider;
import java.nio.file.FileSystem;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * RAM file system provider that uses JIMFS as the underlying file system implementation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@SuppressWarnings("removal")
public final class JimfsFileSystemProviderImpl implements RamFileSystemProvider {

  // We could initialise this lazily, but this class has fewer fields and initialisation
  // overhead than a lazy-loaded object would, so it doesn't really make sense to do it
  // here.
  private static final JimfsFileSystemProviderImpl INSTANCE
      = new JimfsFileSystemProviderImpl();

  /**
   * Get the singleton instance of this provider.
   *
   * @return the singleton instance.
   */
  public static JimfsFileSystemProviderImpl getInstance() {
    return INSTANCE;
  }

  private JimfsFileSystemProviderImpl() {
    // Singleton object.
  }

  @Override
  public FileSystem createFileSystem(String name) {
    var config = Configuration
        .builder(PathType.unix())
        .setSupportedFeatures(
            Feature.LINKS,
            Feature.SYMBOLIC_LINKS,
            Feature.FILE_CHANNEL,
            Feature.SECURE_DIRECTORY_STREAM
        )
        .setAttributeViews("basic", "posix")
        .setRoots("/")
        .setWorkingDirectory("/")
        .setPathEqualityUsesCanonicalForm(true)
        .build();

    return Jimfs.newFileSystem(config);
  }
}
