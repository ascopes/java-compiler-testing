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

package io.github.ascopes.jct.testing.unit.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.jimfs.Jimfs;
import io.github.ascopes.jct.intern.RecursiveDeleter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * {@link RecursiveDeleter} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("RecursiveDeleter tests")
class RecursiveDeleterTest {

  FileSystem fs;
  Path root;

  @BeforeEach
  void setUp() {
    fs = Jimfs.newFileSystem();
    root = fs.getRootDirectories().iterator().next();
  }

  @AfterEach
  void tearDown() throws IOException {
    fs.close();
  }

  @DisplayName("A non-existent directory gets ignored")
  @Order(0)
  @Test
  void nonExistentDirectoryGetsIgnored() throws IOException {
    // Given
    final var dir = someDirectory("foo");
    Files.delete(dir);

    // Then
    assertThatCode(() -> RecursiveDeleter.deleteAll(dir))
        .doesNotThrowAnyException();
  }

  @DisplayName("An empty directory gets deleted")
  @Order(1)
  @Test
  void anEmptyDirectoryGetsDeleted() throws IOException {
    // Given
    final var dir = someDirectory("foo");

    // When
    RecursiveDeleter.deleteAll(dir);

    // Then
    assertThat(dir).doesNotExist();
  }

  @DisplayName("A directory with some files in it gets deleted")
  @Order(2)
  @Test
  void directoryWithFilesInItGetsDeleted() throws IOException {
    // Given
    final var dir = someDirectory("foo");
    final var fooTxt = someFile(dir, "foo.txt");
    final var barTxt = someFile(dir, "bar.txt");
    final var bazTxt = someFile(dir, "baz.txt");

    // When
    RecursiveDeleter.deleteAll(dir);

    // Then
    assertThat(fooTxt).doesNotExist();
    assertThat(barTxt).doesNotExist();
    assertThat(bazTxt).doesNotExist();
    assertThat(dir).doesNotExist();
  }

  @DisplayName("A directory with nested directories and files in it gets deleted")
  @Order(3)
  @Test
  void directoryWithNestedFilesAndDirectoriesInItGetsDeleted() throws IOException {
    // Given
    final var dir = someDirectory("foo");
    final var fooTxt = someFile(dir, "foo.txt");
    final var barTxt = someFile(dir, "bar.txt");
    final var bazTxt = someFile(dir, "baz.txt");
    final var subDir = someDirectory(dir, "aaaaaaaa");
    final var subSubDir = someDirectory(subDir, "bbbbbbb");
    final var nestedFile = someFile(subSubDir, "foobarbaz.txt");
    final var subSubSubDir = someDirectory(subDir, "cccccccccc");
    final var anotherNestedFile = someFile(subSubSubDir, "bleep.txt");

    // When
    RecursiveDeleter.deleteAll(dir);

    // Then
    assertThat(anotherNestedFile).doesNotExist();
    assertThat(subSubSubDir).doesNotExist();
    assertThat(nestedFile).doesNotExist();
    assertThat(subSubDir).doesNotExist();
    assertThat(subDir).doesNotExist();
    assertThat(fooTxt).doesNotExist();
    assertThat(barTxt).doesNotExist();
    assertThat(bazTxt).doesNotExist();
    assertThat(dir).doesNotExist();
  }

  Path someDirectory(String... name) throws IOException {
    return someDirectory(root, name);
  }

  Path someDirectory(Path root, String... name) throws IOException {
    final var dir = Files.createDirectory(resolveEach(root, name));
    assertThat(dir).exists().isDirectory();
    return dir;
  }

  Path someFile(Path root, String... name) throws IOException {
    final var file = Files.createFile(resolveEach(root, name));
    assertThat(file).exists().isRegularFile();
    return file;
  }

  Path resolveEach(Path root, String... name) {
    var head = root;
    for (final var part : name) {
      head = head.resolve(part);
    }

    return head.normalize();
  }
}
