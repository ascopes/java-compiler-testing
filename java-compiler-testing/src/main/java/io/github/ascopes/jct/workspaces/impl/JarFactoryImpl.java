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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A special singleton factory class that is used to create JAR files in memory on the fly.
 *
 * @author Ashley Scopes
 * @since 0.3.0
 */
@API(since = "0.3.0", status = Status.STABLE)
public final class JarFactoryImpl {

  private static final JarFactoryImpl INSTANCE = new JarFactoryImpl();

  /**
   * Get an instance of this factory.
   *
   * @return the instance.
   */
  public static JarFactoryImpl getInstance() {
    return INSTANCE;
  }

  private JarFactoryImpl() {
    // Do nothing.
  }

  /**
   * Create a JAR in the given output file from everything recursively in the given source
   * directory.
   *
   * <p>This JAR will not be compressed in a special way.
   *
   * @param outputFile      the output file to write to.
   * @param sourceDirectory the source directory to read from recursively.
   * @throws IOException if an IO exception occurs anywhere.
   */
  public void createJarFrom(Path outputFile, Path sourceDirectory) throws IOException {
    var outputStream = new BufferedOutputStream(Files.newOutputStream(outputFile));

    try (var jarStream = new JarOutputStream(outputStream)) {
      Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          var entry = fileToZipEntry(sourceDirectory, file);
          jarStream.putNextEntry(entry);
          jarStream.write(Files.readAllBytes(file));
          jarStream.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });

      jarStream.finish();
    }
  }

  private ZipEntry fileToZipEntry(Path sourceDirectory, Path file) {
    // Calculate the file name to use in the zip (forward-slash separated)
    var fileName = StreamSupport
        .stream(sourceDirectory.relativize(file).spliterator(), false)
        .map(Path::getFileName)
        .map(Path::toString)
        .collect(Collectors.joining("/"));
    return new ZipEntry(fileName);
  }
}
