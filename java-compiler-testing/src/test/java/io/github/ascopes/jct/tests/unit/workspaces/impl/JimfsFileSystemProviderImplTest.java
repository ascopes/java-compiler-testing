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
package io.github.ascopes.jct.tests.unit.workspaces.impl;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.workspaces.impl.JimfsFileSystemProviderImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * {@link JimfsFileSystemProviderImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JimfsFileSystemProviderImpl tests")
class JimfsFileSystemProviderImplTest {

  @DisplayName("The class is a singleton")
  @Test
  void theClassIsSingleton() {
    // When
    var instances = Stream
        .generate(JimfsFileSystemProviderImpl::getInstance)
        .limit(10)
        .collect(Collectors.toList());

    // Then
    assertThat(instances)
        .withFailMessage("One or more calls provided a different object")
        .allMatch(JimfsFileSystemProviderImpl.getInstance()::equals);
  }

  @DisplayName("The provider will create a JIMFS file system")
  @Test
  void theProviderWillCreateJimfsFileSystem() throws IOException {
    // When
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      // Then
      assertThat(fileSystem.getClass().getSimpleName())
          .as("file system implementation class")
          .isEqualTo("MemoryFileSystem");
      assertThat(fileSystem.getRootDirectories())
          .as("file system root directories")
          .hasSize(1);
    }
  }

  @DisplayName("The created file system supports hard links")
  @Test
  void theCreatedFileSystemSupportsHardLinks() throws IOException {
    // Given
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      var root = fileSystem.getRootDirectories().iterator().next();
      var fooTxt = root.resolve("foo.txt");
      var barTxt = root.resolve("bar.txt");
      Files.writeString(fooTxt, "Hello, World!");

      // When
      Files.createLink(barTxt, fooTxt);

      // Then
      assertThat(barTxt)
          .exists()
          .isRegularFile()
          .hasContent("Hello, World!");
    }
  }

  @DisplayName("The created file system supports symbolic links")
  @Test
  void theCreatedFileSystemSupportsSymbolicLinks() throws IOException {
    // Given
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      var root = fileSystem.getRootDirectories().iterator().next();
      var fooTxt = root.resolve("foo.txt");
      var barTxt = root.resolve("bar.txt");
      Files.writeString(fooTxt, "Hello, World!");

      // When
      Files.createSymbolicLink(barTxt, fooTxt);

      // Then
      assertThat(barTxt)
          .exists()
          .isSymbolicLink()
          .hasContent("Hello, World!");
    }
  }

  @DisplayName("The created file system supports file channels")
  @Test
  void theCreatedFileSystemSupportsFileChannels() throws IOException {
    // Given
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      var root = fileSystem.getRootDirectories().iterator().next();
      var fooTxt = root.resolve("foo.txt");
      Files.writeString(fooTxt, "Hello, World!");

      // When
      var buff = ByteBuffer.allocate(4);
      var baos = new ByteArrayOutputStream();
      try (var channel = Files.newByteChannel(fooTxt, StandardOpenOption.READ)) {
        while (channel.read(buff) != -1) {
          baos.write(buff.array(), 0, buff.position());
          buff.clear();
        }
      }

      // Then
      assertThat(baos.toString(StandardCharsets.UTF_8))
          .isEqualTo("Hello, World!");
    }
  }

  @DisplayName("The created file system supports directory streams")
  @Test
  void theCreatedFileSystemSupportsDirectoryStreams() throws IOException {
    // Given
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      var root = fileSystem.getRootDirectories().iterator().next();
      Files.createDirectories(root.resolve("foo").resolve("bar").resolve("baz"));
      Files.createDirectories(root.resolve("do").resolve("ray").resolve("me"));

      // When
      var dirs = new ArrayList<Path>();

      try (var dirStream = Files.newDirectoryStream(root)) {
        assertThat(dirStream).isInstanceOf(SecureDirectoryStream.class);
        dirStream.forEach(dirs::add);
      }

      // Then
      assertThat(dirs)
          .containsExactlyInAnyOrder(root.resolve("foo"), root.resolve("do"));
    }
  }

  @DisplayName("The created file system supports URLs")
  @Test
  void theCreatedFileSystemSupportsUrls() throws IOException {
    // Given
    var instance = JimfsFileSystemProviderImpl.getInstance();
    var fsName = someText();
    try (var fileSystem = instance.createFileSystem(fsName)) {
      var root = fileSystem.getRootDirectories().iterator().next();
      var fooTxt = root.resolve("foo.txt");
      Files.writeString(fooTxt, "Hello, World!");

      // When
      var url = fooTxt.toUri().toURL();

      // Then
      try (var is = url.openStream()) {
        var baos = new ByteArrayOutputStream();
        is.transferTo(baos);

        assertThat(baos.toString(StandardCharsets.UTF_8))
            .isEqualTo("Hello, World!");
      }
    }
  }
}
