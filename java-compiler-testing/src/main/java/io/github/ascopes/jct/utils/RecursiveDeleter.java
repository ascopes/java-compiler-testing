/*
 * Copyright (C) 2022 Ashley Scopes
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

package io.github.ascopes.jct.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper file visitor that will recursively delete files.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public class RecursiveDeleter extends SimpleFileVisitor<Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveDeleter.class);
  private static final RecursiveDeleter INSTANCE = new RecursiveDeleter();

  private RecursiveDeleter() {
    // Do nothing.
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    Files.delete(file);
    return super.visitFile(file, attrs);
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    Files.delete(dir);
    return super.postVisitDirectory(dir, exc);
  }

  /**
   * Delete all files in the given path recursively.
   *
   * @param base the base path to delete within.
   * @throws IOException if anything goes wrong.
   */
  public static void deleteAll(Path base) throws IOException {
    if (Files.exists(base)) {
      Files.walkFileTree(base, INSTANCE);
    } else {
      LOGGER.trace("{} does not exist, so will not be recursively deleted", base);
    }
  }
}
