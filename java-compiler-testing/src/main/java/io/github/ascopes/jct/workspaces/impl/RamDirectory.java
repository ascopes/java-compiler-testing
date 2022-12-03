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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.utils.FileUtils.assertValidRootName;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.workspaces.TestDirectory;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.CheckReturnValue;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a temporary in-memory file system that exposes the root path of said file
 * system.
 *
 * <p>This provides a utility for constructing complex and dynamic directory tree structures
 * quickly and simply using fluent chained methods.
 *
 * <p>These file systems are integrated into the {@link FileSystem} API, and can be configured to
 * automatically destroy themselves once this RamPath handle is garbage collected.
 *
 * <p>In addition, these paths follow POSIX file system semantics, meaning that files are handled
 * with case-sensitive names, and use forward slashes to separate paths.
 *
 * <p>While this will create a global {@link FileSystem}, it is recommended that you only interact
 * with the file system via this class to prevent potentially confusing behaviour elsewhere.
 *
 * <p>If an underlying system does not support reading files using the NIO path API, you can
 * instead consider using {@link TempDirectory} in place of this class.
 *
 * @author Ashley Scopes
 * @see TempDirectory
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class RamDirectory extends TestDirectory {

  private static final Logger LOGGER = LoggerFactory.getLogger(RamDirectory.class);

  private final String name;
  private final Path rootDirectory;
  private final FileSystem fileSystem;

  private RamDirectory(String name, FileSystem fileSystem, Path rootDirectory, String separator) {
    super(name, rootDirectory, separator);
    this.name = name;
    this.rootDirectory = rootDirectory;
    this.fileSystem = fileSystem;
  }

  @Override
  public void close() throws IOException {
    LOGGER.debug("Closing RAM file system {} ({} @ {})", name, rootDirectory.toUri(), fileSystem);
    fileSystem.close();
  }

  /**
   * Create a new in-memory path.
   *
   * @param name a symbolic name to give the path. This must be a valid POSIX directory name.
   * @return the in-memory path.
   */
  @CheckReturnValue
  public static RamDirectory newRamDirectory(String name) {

    assertValidRootName(name);

    var config = Configuration
        .builder(PathType.unix())
        .setSupportedFeatures(Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL)
        .setAttributeViews("basic", "posix")
        .setRoots("/")
        .setWorkingDirectory("/")
        .setPathEqualityUsesCanonicalForm(true)
        .build();

    var fileSystem = Jimfs.newFileSystem(config);
    var path = fileSystem.getRootDirectories().iterator().next().resolve(name);

    // Ensure the base directory exists.
    uncheckedIo(() -> Files.createDirectories(path));

    var fs = new RamDirectory(name, fileSystem, path,fileSystem.getSeparator());

    LOGGER.debug("Initialized new root '{}' using RAM disk at {}", name, path.toUri());

    return fs;
  }
}
