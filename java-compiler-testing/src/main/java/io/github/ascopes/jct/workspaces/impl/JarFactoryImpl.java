/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

/**
 * A special singleton factory class that is used to create JAR files in memory on the fly.
 *
 * @author Ashley Scopes
 * @since 0.3.0
 */
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
    try (var outputStream = new BufferedOutputStream(Files.newOutputStream(outputFile))) {
      createJarFrom(outputStream, sourceDirectory);
    }
  }

  /**
   * Create a JAR and stream it into the given output stream, using a given source directory.
   *
   * <p>This JAR will not be compressed in a special way.
   *
   * @param outputStream    the output stream to write to.
   * @param sourceDirectory the source directory to read from recursively.
   * @throws IOException if an IO exception occurs anywhere.
   */
  public void createJarFrom(OutputStream outputStream, Path sourceDirectory) throws IOException {
    try (var jarStream = new JarOutputStream(outputStream)) {
      Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

          // File names should be forward-slash delimited in ZIP files.
          var fileName = StreamSupport
              .stream(sourceDirectory.relativize(file).spliterator(), false)
              .map(Path::getFileName)
              .map(Path::toString)
              .collect(Collectors.joining("/"));

          jarStream.putNextEntry(new ZipEntry(fileName));
          jarStream.write(Files.readAllBytes(file));
          jarStream.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });

      jarStream.finish();
    }
  }
}
