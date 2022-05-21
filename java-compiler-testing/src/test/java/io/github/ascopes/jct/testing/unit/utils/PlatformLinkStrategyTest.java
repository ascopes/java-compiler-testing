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

package io.github.ascopes.jct.testing.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.utils.PlatformLinkStrategy;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link PlatformLinkStrategy} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PlatformLinkStrategy tests")
class PlatformLinkStrategyTest {

  @DisplayName("Hard links are created on Windows")
  @MethodSource("windowsOses")
  @ParameterizedTest(name = "for os.name = \"{0}\"")
  void hardLinksAreCreatedForWindows(String osName) throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.windows())) {
      var root = fs.getRootDirectories().iterator().next();

      // Given
      var props = new Properties();
      props.put("os.name", osName);

      var target = Files.createFile(root.resolve("target"));
      var content = UUID.randomUUID().toString();
      Files.writeString(target, content);

      var link = root.resolve("link");

      // When
      var result = new PlatformLinkStrategy(props).createLinkOrCopy(link, target);

      // Then
      assertThat(result)
          .isEqualTo(link)
          .isRegularFile()
          .content()
          .isEqualTo(content);

      // Verify the file is a link and not a copy.
      var moreContent = content + UUID.randomUUID();
      Files.writeString(target, moreContent);

      assertThat(result)
          .content()
          .withFailMessage("Expected result to be a link, but it was a copy")
          .isEqualTo(moreContent);
    }
  }

  @DisplayName("Symbolic links are created on other OSes")
  @MethodSource("otherOses")
  @ParameterizedTest(name = "for os.name = \"{0}\"")
  void symbolicLinksAreCreatedForWindows(String osName) throws IOException {
    try (var fs = Jimfs.newFileSystem(Configuration.unix())) {
      var root = fs.getRootDirectories().iterator().next();

      // Given
      var props = new Properties();
      props.put("os.name", osName);

      var target = Files.createFile(root.resolve("target"));
      var content = UUID.randomUUID().toString();
      Files.writeString(target, content);

      var link = root.resolve("link");

      // When
      var result = new PlatformLinkStrategy(props).createLinkOrCopy(link, target);

      // Then
      assertThat(result)
          .isEqualTo(link)
          .isSymbolicLink()
          .content()
          .isEqualTo(content);

      // Verify the file is a link and not a copy.
      var moreContent = content + UUID.randomUUID();
      Files.writeString(target, moreContent);

      assertThat(result)
          .content()
          .withFailMessage("Expected result to be a link, but it was a copy")
          .isEqualTo(moreContent);
    }
  }

  @DisplayName("Copies are created when links are not supported")
  @MethodSource({"windowsOses", "otherOses" })
  @ParameterizedTest(name = "for os.name = \"{0}\"")
  void copiesAreCreatedWhenLinksAreNotSupported(String osName) throws IOException {
    var config = Configuration
        .builder(PathType.unix())
        .setRoots("/")
        .setWorkingDirectory("/work")
        // No features!
        .setSupportedFeatures()
        .setAttributeViews("basic")
        .build();

    try (var fs = Jimfs.newFileSystem(config)) {
      var root = fs.getRootDirectories().iterator().next();

      // Given
      var props = new Properties();
      props.put("os.name", osName);

      var target = Files.createFile(root.resolve("target"));
      var content = UUID.randomUUID().toString();
      Files.writeString(target, content);

      var link = root.resolve("link");

      // When
      var result = new PlatformLinkStrategy(props).createLinkOrCopy(link, target);

      // Then
      assertThat(result)
          .isEqualTo(link)
          .isRegularFile()
          .content()
          .isEqualTo(content);

      // Verify the file is a link and not a copy.
      var moreContent = content + UUID.randomUUID();
      Files.writeString(target, moreContent);

      assertThat(result)
          .content()
          .withFailMessage("Expected result to be a copy, but it was a link")
          .isNotEqualTo(moreContent);
    }
  }

  @DisplayName("Linking logic works for the current platform without exceptions")
  @Test
  void linkingLogicWorksForCurrentPlatform() throws IOException {
    var dir = Files.createTempDirectory("PlatformLinkStrategy-platform");

    try {
      var target = Files.createFile(dir.resolve("target"));
      var content = UUID.randomUUID().toString();
      Files.writeString(target, content);

      var link = dir.resolve("link");

      // When
      var result = new PlatformLinkStrategy(System.getProperties())
          .createLinkOrCopy(link, target);

      // Then
      assertThat(result)
          .isEqualTo(link)
          .content()
          .isEqualTo(content);

    } finally {
      try (var list = Files.list(dir)) {
        for (var file : list.collect(Collectors.toList())) {
          Files.delete(file);
        }
      }
      Files.delete(dir);
    }
  }

  static Stream<String> windowsOses() {
    return Stream.of(
        "Windows",
        "Windows 95",
        "Windows 98",
        "Windows ME",
        "Windows NT",
        "Windows 2000",
        "Windows Server 2000",
        "Windows 2003",
        "Windows Server 2003",
        "Windows XP",
        "Windows Server 2008",
        "Windows Server 2012",
        "Windows Vista",
        "Windows 7",
        "Windows 8",
        "Windows 8.1",
        "Windows 10",
        "Windows 11"
    );
  }

  static Stream<String> otherOses() {
    return Stream.of(
        // POSIX/UNIX-like OSes.
        "AIX",
        "FreeBSD",
        "HP-UX",
        "Irix",
        "LINUX",
        "Linux",
        "Mac",
        "Mac OS X",
        "MINIX",
        "Minix",
        "OpenBSD",
        "NetBSD",
        "Solaris",
        "SunOS",

        // Other non-UNIX OSes.
        "OS/2",
        "OS/400",
        "TempleOS",
        "z/OS"
    );
  }
}
