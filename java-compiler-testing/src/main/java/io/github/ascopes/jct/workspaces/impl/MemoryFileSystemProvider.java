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

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import io.github.ascopes.jct.workspaces.RamFileSystemProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * RAM file system provider that uses {@code memoryfilesystem} as the underlying file system
 * implementation.
 *
 * @author Ashley Scopes, Philippe Marschall
 * @since 0.7.0
 */
@API(since = "0.7.0", status = Status.INTERNAL)
@SuppressWarnings("removal")
public final class MemoryFileSystemProvider implements RamFileSystemProvider {

  // We could initialise this lazily, but this class has fewer fields and initialisation
  // overhead than a lazy-loaded object would, so it doesn't really make sense to do it
  // here.
  private static final MemoryFileSystemProvider INSTANCE
      = new MemoryFileSystemProvider();

  /**
   * Get the singleton instance of this provider.
   *
   * @return the singleton instance.
   */
  public static MemoryFileSystemProvider getInstance() {
    return INSTANCE;
  }

  private MemoryFileSystemProvider() {
    // Singleton object.
  }

  @Override
  public FileSystem createFileSystem(String name) {
    try {
      return MemoryFileSystemBuilder.newLinux()
          .setCurrentWorkingDirectory("/")
          .build(name);
    } catch (IOException e) {
      throw new UncheckedIOException("could not create file system with name: " + name, e);
    }
  }
}
