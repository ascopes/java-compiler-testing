/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import org.jspecify.annotations.Nullable;

/**
 * Helper for dumping the structure of locations in a workspace for debugging purposes.
 *
 * <p>This is not thread-safe.
 *
 * @author Ashley Scopes
 * @since 5.0.0
 */
final class WorkspaceDumper {

  private final Appendable appendable;

  WorkspaceDumper(Appendable appendable) {
    this.appendable = appendable;
  }

  void dump(String repr, Map<Location, List<PathRoot>> locations) {
    uncheckedIo(() -> appendable.append("Workspace ").append(repr).append(":\n"));
    locations.forEach(this::dumpLocation);
  }

  private void dumpLocation(Location location, List<PathRoot> pathRoots) {
    uncheckedIo(() -> appendable.append("  Location ").append(location.toString()).append(":\n"));
    pathRoots.forEach(this::dumpPath);
  }

  private void dumpPath(PathRoot pathRoot) {
    uncheckedIo(() -> {
      appendable.append("   - ").append(pathRoot.getUri().toString()).append("\n");
      try (var walker = new Walker(7)) {
        Files.walkFileTree(pathRoot.getPath(), walker);
      }
    });
  }

  private final class Walker extends SimpleFileVisitor<Path> implements AutoCloseable {

    private static final int INDENT_SIZE = 2;

    private int index;
    private int indent;

    private Walker(int indent) {
      index = 0;
      this.indent = indent;
    }

    @Override
    public void close() throws IOException {
      // Only 1 index implies an empty directory.
      if (index <= 1) {
        doIndent();
        appendable.append("  [[empty]]\n");
      }
      appendable.append("\n");
    }

    @Override
    public FileVisitResult preVisitDirectory(
        Path dir,
        BasicFileAttributes attrs
    ) throws IOException {
      if (index > 0) {
        doIndent();
        appendable.append(dir.getFileName().toString()).append("/\n");
        indent += INDENT_SIZE;
      }

      ++index;
      return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult postVisitDirectory(
        Path dir,
        @Nullable IOException ex
    ) throws IOException {
      indent -= INDENT_SIZE;
      return super.postVisitDirectory(dir, ex);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      doIndent();
      appendable.append(file.getFileName().toString()).append("\n");
      return super.visitFile(file, attrs);
    }

    private void doIndent() throws IOException {
      appendable.append(" ".repeat(indent));
    }
  }
}
