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

import static io.github.ascopes.jct.utils.FileUtils.assertValidRootName;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
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
 * also be slower than {@link RamDirectoryImpl}, and does not provide isolation from the environment
 * that the tests are running in.
 *
 * @author Ashley Scopes
 * @see RamDirectoryImpl
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@Immutable
@ThreadSafe
public final class TempDirectoryImpl extends AbstractManagedDirectory {

  private static final Logger LOGGER = LoggerFactory.getLogger(TempDirectoryImpl.class);

  private final Path rootDirectory;

  private TempDirectoryImpl(String name, Path rootDirectory) {
    super(name, rootDirectory);
    this.rootDirectory = rootDirectory;
  }

  @Override
  public void close() throws IOException {
    LOGGER.debug(
        "Deleting temporary directory ({} @ {})",
        rootDirectory.toUri(),
        rootDirectory.getFileSystem()
    );

    Files.walkFileTree(rootDirectory, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        LOGGER.trace("Deleted {}", file);
        return super.visitFile(file, attrs);
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        LOGGER.trace("Deleted {}", dir);
        return super.postVisitDirectory(dir, exc);
      }
    });
  }

  /**
   * Create a new temporary directory on the root file system somewhere.
   *
   * @param name a symbolic name to give the path. This must be a valid directory name for the
   *             environment you are using.
   * @return the temporary directory.
   */
  @CheckReturnValue
  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  public static TempDirectoryImpl newTempDirectory(String name) {
    // TODO(ascopes): are MS-DOS file name length limits a potential issue here?
    assertValidRootName(name);
    var tempDir = uncheckedIo(() -> Files.createTempDirectory("jct-" + name + "_"));
    LOGGER.debug("Initialized new root '{}' using temporary path at {}", name, tempDir);
    return new TempDirectoryImpl(name, tempDir);
  }
}
