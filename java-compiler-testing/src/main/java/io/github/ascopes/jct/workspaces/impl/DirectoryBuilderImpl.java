/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.workspaces.DirectoryBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.StringJoiner;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chainable builder for creating directories.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class DirectoryBuilderImpl implements DirectoryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryBuilderImpl.class);

  private final ManagedDirectory parent;
  private final Path targetPath;

  /**
   * Initialise a new directory builder.
   *
   * @param parent the parent managed directory to chain calls back onto.
   * @param first  the first part of the directory path.
   * @param rest   additional parts of the directory path.
   */
  public DirectoryBuilderImpl(ManagedDirectory parent, String first, String... rest) {
    this.parent = parent;
    var targetPath = parent.getPath().resolve(first);
    for (var next : rest) {
      targetPath = targetPath.resolve(next);
    }
    this.targetPath = targetPath;
  }

  @Override
  public ManagedDirectory copyContentsFrom(String first, String... rest) {
    // Path.of is fine here as it is for the default file system.
    return copyContentsFrom(Path.of(first, rest));
  }

  @Override
  public ManagedDirectory copyContentsFrom(File dir) {
    return copyContentsFrom(dir.toPath());
  }

  @Override
  public ManagedDirectory copyContentsFrom(Path rootDir) {
    uncheckedIo(() -> {
      Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {

        @Override
        public FileVisitResult preVisitDirectory(
            Path dir,
            BasicFileAttributes attrs
        ) throws IOException {
          // Fix windows-style separators if needed.
          var targetChildDirectory = targetPath.resolve(collapsePath(rootDir.relativize(dir)));

          LOGGER.trace("making directory {} (existing {})", targetChildDirectory, dir);

          // Ignore if the directory already exists (will occur for the root).
          Files.createDirectories(targetChildDirectory);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(
            Path file,
            BasicFileAttributes attrs
        ) throws IOException {
          // Fix windows-style separators if needed.
          var targetFile = targetPath.resolve(collapsePath(rootDir.relativize(file)));

          LOGGER.trace("copying {} to {}", file, targetFile);

          Files.copy(file, targetFile);
          return FileVisitResult.CONTINUE;
        }
      });
    });

    return parent;
  }

  @Override
  public ManagedDirectory thatIsEmpty() {
    uncheckedIo(() -> Files.createDirectories(targetPath));
    return parent;
  }

  private String collapsePath(Path path) {
    var joiner = new StringJoiner(parent.getPath().getFileSystem().getSeparator());
    for (var part : path) {
      joiner.add(part.toString());
    }
    return joiner.toString();
  }
}
