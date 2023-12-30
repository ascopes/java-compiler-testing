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
package io.github.ascopes.jct.tests.unit.workspaces.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.workspaces.impl.JarFactoryImpl;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link JarFactoryImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JarFactoryImpl tests")
class JarFactoryImplTest {

  @DisplayName("JarFactoryImpl is a singleton")
  @Test
  void jarFactoryImplIsSingleton() {
    // Given
    var first = JarFactoryImpl.getInstance();

    // Then
    assertSoftly(softly -> {
      for (var i = 2; i <= 10; ++i) {
        var instance = JarFactoryImpl.getInstance();

        softly.assertThat(instance)
            .as("instance #%s (%s)", i, instance)
            .isSameAs(first);
      }
    });
  }

  @DisplayName("An exception is raised if the output JAR directory does not exist")
  @Test
  void exceptionRaisedIfOutputJarDirectoryDoesNotExist() throws IOException {
    // Given
    try (var fs = Fixtures.someTemporaryFileSystem()) {
      var classesDir = fs.getRootPath().resolve("target").resolve("classes");
      Files.createDirectories(classesDir);
      Files.createFile(classesDir.resolve("HelloWorld.class"));
      var jarTarget = fs.getRootPath().resolve("some-non-existent-directory")
          .resolve("HelloWorld.jar");

      // Then
      assertThatThrownBy(() -> JarFactoryImpl.getInstance().createJarFrom(jarTarget, classesDir))
          .isInstanceOf(NoSuchFileException.class)
          .message()
          .startsWith("/some-non-existent-directory");
    }
  }

  @DisplayName("An exception is raised if the input directory does not exist")
  @Test
  void exceptionRaisedIfInputDirectoryDoesNotExist() {
    // Given
    try (var fs = Fixtures.someTemporaryFileSystem()) {
      var classesDir = fs.getRootPath().resolve("target").resolve("classes");
      var jarTarget = fs.getRootPath().resolve("HelloWorld.jar");

      // Then
      assertThatThrownBy(() -> JarFactoryImpl.getInstance().createJarFrom(jarTarget, classesDir))
          .isInstanceOf(NoSuchFileException.class)
          .hasMessage("/target/classes");
    }
  }

  @DisplayName("The generated JAR contains the expected files")
  @Test
  void theGeneratedJarContainsTheExpectedFiles() throws IOException {
    // Given
    try (var fs = Fixtures.someTemporaryFileSystem()) {
      var inputsDir = Files.createDirectories(fs.getRootPath().resolve("inputs"));
      var outputsDir = Files.createDirectories(fs.getRootPath().resolve("outputs"));
      var outputJar = outputsDir.resolve("output.jar");

      var inputsFooBarBazTxt = inputsDir.resolve("foo").resolve("bar").resolve("baz.txt");
      Files.createDirectories(inputsFooBarBazTxt.getParent());
      Files.writeString(inputsFooBarBazTxt, "Foo! Bar! Baz! Hello, World!");

      var inputsFooBarBorkTxt = inputsDir.resolve("foo").resolve("bar").resolve("bork.txt");
      Files.writeString(inputsFooBarBorkTxt, "Foo! Bar! Baz! Bork!");

      var inputsPingPongTxt = inputsDir.resolve("pingPong.txt");
      Files.writeString(inputsPingPongTxt, "Ping? Pong!");

      var inputsFooBarTxt = inputsDir.resolve("foo").resolve("bar.txt");
      Files.writeString(inputsFooBarTxt, "Foo! Bar!");

      var inputsLoremIpsumBin = inputsDir.resolve("lorem").resolve("ipsum.bin");
      Files.createDirectories(inputsLoremIpsumBin.getParent());
      Files.write(inputsLoremIpsumBin, new byte[]{0xd, 0xe, 0xa, 0xd, 0xb, 0xe, 0xe, 0xf});

      // When
      JarFactoryImpl.getInstance().createJarFrom(outputJar, inputsDir);

      // Then
      withinJar(outputJar, jarFs -> {
        assertThat(jarFs.getRootDirectories()).hasSize(1);
        var root = jarFs.getRootDirectories().iterator().next();

        assertThat(root.resolve("foo").resolve("bar").resolve("baz.txt"))
            .exists()
            .isRegularFile()
            .hasContent("Foo! Bar! Baz! Hello, World!");

        assertThat(root.resolve("foo").resolve("bar").resolve("bork.txt"))
            .exists()
            .isRegularFile()
            .hasContent("Foo! Bar! Baz! Bork!");

        assertThat(root.resolve("foo").resolve("bar.txt"))
            .exists()
            .isRegularFile()
            .hasContent("Foo! Bar!");

        assertThat(root.resolve("lorem").resolve("ipsum.bin"))
            .exists()
            .isRegularFile()
            .hasBinaryContent(new byte[]{0xd, 0xe, 0xa, 0xd, 0xb, 0xe, 0xe, 0xf});
      });
    }
  }

  private static void withinJar(Path jar, ThrowingFileSystemConsumer consumer) throws IOException {
    var provider = FileSystemProvider
        .installedProviders()
        .stream()
        // For whatever reason, the ZIP file provider uses the jar scheme. Don't ask, I have no idea
        // why they do it like this either.
        .filter(nextProvider -> nextProvider.getScheme().equals("jar"))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Oh no! No ZIP (well... JAR) provider! Panic!!!"));

    try (var fs = provider.newFileSystem(jar, Map.of())) {
      consumer.run(fs);
    }
  }

  @FunctionalInterface
  private interface ThrowingFileSystemConsumer {

    void run(FileSystem fileSystem) throws IOException;
  }
}
