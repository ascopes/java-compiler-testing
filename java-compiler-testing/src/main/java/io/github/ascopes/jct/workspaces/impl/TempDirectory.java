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

import io.github.ascopes.jct.utils.RecursiveDeleter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.CheckReturnValue;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporary file system location that can be created by users to store sources, inputs, and outputs
 * for compilation within.
 *
 * <p>This implementation uses a temporary directory provided by the default file system on the
 * platform you are using, and is thus compatible with older IO-based facilities such as
 * {@link java.io.File}, {@link java.io.FileInputStream}, etc.
 *
 * <p>The downside to this is that if the JVM crashes, the directory may not be deleted. It may
 * also be slower than {@link RamDirectory}, and does not provide isolation from the environment
 * that the tests are running in.
 *
 * @author Ashley Scopes
 * @see RamDirectory
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class TempDirectory extends AbstractManagedDirectory {

  private static final Logger LOGGER = LoggerFactory.getLogger(TempDirectory.class);

  private final Path rootDirectory;

  private TempDirectory(String name, Path rootDirectory, String separator) {
    super(name, rootDirectory, separator);
    this.rootDirectory = rootDirectory;
  }

  @Override
  public void close() throws IOException {
    LOGGER.debug(
        "Deleting temporary directory ({} @ {})",
        rootDirectory.toUri(),
        rootDirectory.getFileSystem()
    );
    RecursiveDeleter.deleteAll(rootDirectory);
  }

  /**
   * Create a new temporary directory on the root file system somewhere.
   *
   * @param name a symbolic name to give the path. This must be a valid directory name for the
   *             environment you are using.
   * @return the temporary directory.
   */
  @CheckReturnValue
  public static TempDirectory newTempDirectory(String name) {
    assertValidRootName(name);
    var tempDir = uncheckedIo(() -> Files.createTempDirectory("jct-" + name + "_"));
    LOGGER.debug("Initialized new root '{}' using temporary path at {}", name, tempDir);
    return new TempDirectory(name, tempDir, tempDir.getFileSystem().getSeparator());
  }
}
