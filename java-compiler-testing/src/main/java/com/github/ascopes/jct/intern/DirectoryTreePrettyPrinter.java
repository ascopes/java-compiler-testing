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

package com.github.ascopes.jct.intern;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * File visitor that will pretty-print the file tree to a string.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class DirectoryTreePrettyPrinter implements FileVisitor<Path> {

  private final StringBuilder representation;
  private int level;

  private DirectoryTreePrettyPrinter() {
    representation = new StringBuilder();
    level = 0;
  }

  @Override
  public String toString() {
    return representation.toString();
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    addLine(dir);
    ++level;
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    addLine(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    --level;
    return FileVisitResult.CONTINUE;
  }

  private void addLine(Path path) {
    var isDir = Files.isDirectory(path);

    representation
        .append("┃  ".repeat(Math.max(0, level)))
        .append("┣━━")
        .append(isDir ? "┓ " : "╸ ")
        .append(path.getFileName())
        .append(isDir ? "/" : "")
        .append("\n");
  }

  /**
   * Pretty-print the given directory tree.
   *
   * @param base the base of the tree.
   * @return the string representation of the tree.
   * @throws IOException if an IO error occurs anywhere.
   */
  public static String prettyPrint(Path base) throws IOException {
    var printer = new DirectoryTreePrettyPrinter();
    Files.walkFileTree(base, printer);
    return printer.toString();
  }
}
