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

import static io.github.ascopes.jct.testing.helpers.MoreMocks.stub;
import static io.github.ascopes.jct.utils.FileUtils.binaryNameToPackageName;
import static io.github.ascopes.jct.utils.FileUtils.binaryNameToPath;
import static io.github.ascopes.jct.utils.FileUtils.binaryNameToSimpleClassName;
import static io.github.ascopes.jct.utils.FileUtils.fileWithAnyKind;
import static io.github.ascopes.jct.utils.FileUtils.packageNameToPath;
import static io.github.ascopes.jct.utils.FileUtils.pathToBinaryName;
import static io.github.ascopes.jct.utils.FileUtils.pathToKind;
import static io.github.ascopes.jct.utils.FileUtils.relativeResourceNameToPath;
import static io.github.ascopes.jct.utils.FileUtils.resourceNameToPath;
import static io.github.ascopes.jct.utils.FileUtils.simpleClassNameToPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;

import com.google.common.jimfs.Jimfs;
import io.github.ascopes.jct.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import javax.tools.JavaFileObject.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link FileUtils}.
 *
 * @author Ashley Scopes
 */
@DisplayName("FileUtils tests")
class FileUtilsTest {

  @DisplayName("pathToBinaryName throws an IllegalArgumentException for absolute paths")
  @Test
  void pathToBinaryNameThrowsIllegalArgumentExceptionForAbsolutePaths() {
    // Given
    var path = stub(Path.class);
    given(path.isAbsolute()).willReturn(true);
    given(path.toString()).willReturn("/foo/bar/baz");

    // Then
    assertThatThrownBy(() -> pathToBinaryName(path))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Path cannot be absolute (got /foo/bar/baz)");
  }

  @DisplayName("pathToBinaryName converts relative paths as expected")
  @CsvSource({
      "java/util/List.java, java.util.List",
      "java/util/List.class, java.util.List",
      "kotlin/collections/List.kt, kotlin.collections.List",
      "scala/immutable/List.scala, scala.immutable.List",
      "org/example/foo/bar/Baz$Bork.class, org.example.foo.bar.Baz$Bork",
      "org/example/foo/bar/Baz$Bork, org.example.foo.bar.Baz$Bork",
  })
  @ParameterizedTest(name = "pathToBinaryName(\"{0}\") should return \"{1}\"")
  void pathToBinaryNameConvertsRelativePathsAsExpected(String input, String expected)
      throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var path = fs.getPath(input);

      // When
      var actual = pathToBinaryName(path);

      // Then
      assertThat(actual).isEqualTo(expected);
    }
  }

  @DisplayName("binaryNameToPackageName should remove class names as expected")
  @CsvSource({
      "java.lang.Integer, java.lang",
      "java.util.HashMap$Node, java.util",
      "SomeClassInTheUnnamedPackage, ''",
      "SomeClassInTheUnnamedPackage$InnerClass, ''"
  })
  @ParameterizedTest(name = "binaryNameToPackageName(\"{0}\") should return \"{1}\"")
  void binaryNameToPackageNameShouldRemoveClassNamesAsExpected(String input, String expected) {
    // When
    var actual = binaryNameToPackageName(input);

    // Then
    assertThat(actual)
        .isEqualTo(expected);
  }

  @DisplayName("binaryNameToSimpleClassName should extract class names as expected")
  @CsvSource({
      "java.lang.Integer, Integer",
      "java.util.HashMap$Node, HashMap$Node",
      "SomeClassInTheUnnamedPackage, SomeClassInTheUnnamedPackage",
      "SomeClassInTheUnnamedPackage$InnerClass, SomeClassInTheUnnamedPackage$InnerClass"
  })
  @ParameterizedTest(name = "binaryNameToSimpleClassName(\"{0}\") should return \"{1}\"")
  void binaryNameToSimpleClassNameShouldExtractClassNamesAsExpected(String input, String expected) {
    // When
    var actual = binaryNameToSimpleClassName(input);

    // Then
    assertThat(actual)
        .isEqualTo(expected);
  }

  @DisplayName("binaryNameToPath should return the expected output")
  @CsvSource({
      "/usr/jdk/shared, java.util.ArrayList, CLASS, /usr/jdk/shared/java/util/ArrayList.class",
      "src/jdk/shared, java.util.ArrayList, SOURCE, src/jdk/shared/java/util/ArrayList.java",
      "./doc/jdk/shared, java.util.ArrayList, HTML, doc/jdk/shared/java/util/ArrayList.html",
      "../src/jdk/shared, java.util.ArrayList, SOURCE, ../src/jdk/shared/java/util/ArrayList.java",
      "idk/jdk/shared, java.util.ArrayList, OTHER, idk/jdk/shared/java/util/ArrayList",
      "/, foo.bar.Baz, CLASS, /foo/bar/Baz.class",
      "'', foo.bar.Baz, CLASS, foo/bar/Baz.class",
      "/com/example/me, ClassInUnnamedPackage, SOURCE, /com/example/me/ClassInUnnamedPackage.java",
      "com/example/me, UnnamedPackage, SOURCE, com/example/me/UnnamedPackage.java",
      "./com/example/me, Foo, SOURCE, com/example/me/Foo.java",
      "/, SomeClassInTheUnnamedPackage, SOURCE, /SomeClassInTheUnnamedPackage.java",
      "'', SomeClassInTheUnnamedPackage, SOURCE, SomeClassInTheUnnamedPackage.java"
  })
  @ParameterizedTest(name = "binaryNameToPath(\"{0}\", \"{1}\", {2}) should return \"{3}\"")
  void binaryNameToPathShouldReturnTheExpectedOutput(
      String directory,
      String binaryName,
      Kind kind,
      String expected
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var directoryPath = fs.getPath(directory);
      var expectedPath = fs.getPath(expected);

      // When
      var actualPath = binaryNameToPath(directoryPath, binaryName, kind);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(actualPath).hasSameFileSystemAs(directoryPath);
        softly.assertThat(actualPath).isEqualTo(expectedPath);
      });
    }
  }

  @DisplayName("packageNameToPath should return the expected output")
  @CsvSource({
      "/usr/jdk/shared, java.util, /usr/jdk/shared/java/util",
      "src/jdk/shared, java.lang, src/jdk/shared/java/lang",
      "./doc/jdk/shared, java.time, ./doc/jdk/shared/java/time",
      "../idk/jdk/shared, java.sql, ../idk/jdk/shared/java/sql",
      "/, foo.bar, /foo/bar",
      "'', foo.bar, foo/bar",
  })
  @ParameterizedTest(name = "packageNameToPath(\"{0}\", \"{1}\") should return \"{2}\"")
  void packageNameToPathShouldReturnTheExpectedOutput(
      String directory,
      String packageName,
      String expected
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var directoryPath = fs.getPath(directory);
      var expectedPath = fs.getPath(expected);

      // When
      var actualPath = packageNameToPath(directoryPath, packageName);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(actualPath).hasSameFileSystemAs(directoryPath);
        softly.assertThat(actualPath).isEqualTo(expectedPath);
      });
    }
  }

  @DisplayName("simpleClassNameToPath should return the expected output")
  @CsvSource({
      "/usr/jdk/shared/java/util, ArrayList, SOURCE, /usr/jdk/shared/java/util/ArrayList.java",
      "src/jdk/shared/java/lang, Integer, CLASS, src/jdk/shared/java/lang/Integer.class",
      "./doc/jdk/shared/java/time, Temporal, HTML, doc/jdk/shared/java/time/Temporal.html",
      "doc/jdk/shared/java/time, Temporal, HTML, doc/jdk/shared/java/time/Temporal.html",
      "'', ArrayList, SOURCE, ArrayList.java",
      "/idk/jdk/shared/java/util, ArrayList, OTHER, /idk/jdk/shared/java/util/ArrayList",
  })
  @ParameterizedTest(name = "simpleClassNameToPath(\"{0}\", \"{1}\", {2}) should return \"{3}\"")
  void simpleClassNameToPathShouldReturnTheExpectedOutput(
      String packageDirectory,
      String className,
      Kind kind,
      String expected
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var packageDirectoryPath = fs.getPath(packageDirectory);
      var expectedPath = fs.getPath(expected);

      // When
      var actualPath = simpleClassNameToPath(packageDirectoryPath, className, kind);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(actualPath).hasSameFileSystemAs(packageDirectoryPath);
        softly.assertThat(actualPath).isEqualTo(expectedPath);
      });
    }
  }

  @DisplayName("resourceNameToPath should return the expected output")
  @CsvSource({
      // Relative resources
      "/foo/bar, org.example.bazbork, cat.txt, /foo/bar/org/example/bazbork/cat.txt",
      "/foo/bar, org.example.bazbork, cats/cat.txt, /foo/bar/org/example/bazbork/cats/cat.txt",
      "/foo/bar, org.example.bazbork, ../cat.txt, /foo/bar/org/example/cat.txt",
      "/foo/bar, '', cat.txt, /foo/bar/cat.txt",
      "/foo/bar, '', cats/cat.txt, /foo/bar/cats/cat.txt",
      "/foo/bar, '', ../cats/cat.txt, /foo/cats/cat.txt",
      "foo/bar, net.qux.eggs.spam, dog.gif, foo/bar/net/qux/eggs/spam/dog.gif",
      "./foo/bar, com.google.something, capybara.kek, foo/bar/com/google/something/capybara.kek",
      "../foo/bar, com.something, capybara.meow, ../foo/bar/com/something/capybara.meow",

      // Absolute resources (we ignore the path and the directory).
      "/do/ray/me, foo.bar.baz, /this/is/absolute/resource.png, /this/is/absolute/resource.png",
  })
  @ParameterizedTest(name = "resourceNameToPath(\"{0}\", \"{1}\", \"{2}\") should return \"{3}\"")
  void resourceNameToPathShouldReturnTheExpectedOutput(
      String directory,
      String packageName,
      String relativeName,
      String expected
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var directoryPath = fs.getPath(directory);
      var expectedPath = fs.getPath(expected);

      // When
      var actualPath = resourceNameToPath(directoryPath, packageName, relativeName);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(actualPath).hasSameFileSystemAs(directoryPath);
        softly.assertThat(actualPath).isEqualTo(expectedPath);
      });
    }
  }

  @DisplayName("relativeResourceNameToPath should return the expected output")
  @CsvSource({
      "/foo/bar, com/example/something-cool.txt, /foo/bar/com/example/something-cool.txt",
      "foo/bar, com/example/something-cool.txt, foo/bar/com/example/something-cool.txt",
      "./foo/bar, com/example/something-cool.txt, foo/bar/com/example/something-cool.txt",
      "/, com/example/something-cool.txt, /com/example/something-cool.txt",
      "'', com/example/something-cool.txt, com/example/something-cool.txt",
  })
  @ParameterizedTest(name = "relativeResourceNameToPath(\"{0}\", \"{1}\") should return \"{2}\"")
  void relativeResourceNameToPathShouldReturnTheExpectedOutput(
      String directory,
      String relativeName,
      String expected
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var directoryPath = fs.getPath(directory);
      var expectedPath = fs.getPath(expected);

      // When
      var actualPath = relativeResourceNameToPath(directoryPath, relativeName);

      // Then
      assertSoftly(softly -> {
        softly.assertThat(actualPath).hasSameFileSystemAs(directoryPath);
        softly.assertThat(actualPath).isEqualTo(expectedPath);
      });
    }
  }

  @DisplayName("pathToKind returns the expected output")
  @CsvSource({
      "/foo/bar/baz/bork.java, SOURCE",
      "foo/bar/baz/bork.java, SOURCE",
      "./foo/bar/baz/bork.java, SOURCE",
      "/foo.java, SOURCE",
      "/foo/bar/baz/bork.class, CLASS",
      "foo/bar/baz/bork.class, CLASS",
      "./foo/bar/baz/bork.class, CLASS",
      "/foo.class, CLASS",
      "/foo/bar/baz/bork.html, HTML",
      "foo/bar/baz/bork.html, HTML",
      "./foo/bar/baz/bork.html, HTML",
      "/foo.html, HTML",
      "/foo/bar/baz/bork.JAVA, OTHER",
      "foo/bar/baz/bork.HTML, OTHER",
      "./foo/bar/baz/bork.CLASS, OTHER",
      "/foo/bar/baz/bork.scala, OTHER",
      "foo/bar/baz/bork.rs, OTHER",
      "./foo/bar/baz/bork.kotlin, OTHER",
      "/foo.clj, OTHER",
      "/foo/bar/baz/bork, OTHER",
      "foo/bar/baz/bork, OTHER",
      "./foo/bar/baz/bork, OTHER",
      "/foo, OTHER",
      "/, OTHER",
      "'', OTHER",
  })
  @ParameterizedTest(name = "pathToKind(\"{0}\") should return Kind.{1}")
  void pathToKindReturnsTheExpectedOutput(String pathName, Kind expectedKind) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var path = fs.getPath(pathName);

      // When
      var actualKind = pathToKind(path);

      // Then
      assertThat(actualKind).isSameAs(expectedKind);
    }
  }

  @DisplayName("fileWithAnyKind should fail if the path does not exist")
  @CsvSource({
      "SOURCE, /foo/bar.java",
      "HTML, /foo/bar.html",
      "CLASS, /foo/bar.class",
      "OTHER, /foo/bar.potato",
  })
  @ParameterizedTest(
      name = "fileWithAnyKind([{0}])'s predicate should return false for non-existing path \"{1}\""
  )
  void fileWithAnyKindShouldFailIfThePathDoesNotExist(Kind kind, String pathName)
      throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var path = fs.getPath(pathName);
      assertThat(path).doesNotExist();
      var kindSet = set(kind);
      var predicate = fileWithAnyKind(kindSet);

      // Then
      assertThat(predicate).rejects(path);
    }
  }

  @DisplayName("fileWithAnyKind should fail if the path is a directory")
  @CsvSource({
      "SOURCE, /foo/bar.java",
      "HTML, /foo/bar.html",
      "CLASS, /foo/bar.class",
      "OTHER, /foo/bar.potato",
  })
  @ParameterizedTest(
      name = "fileWithAnyKind([{0}])'s predicate should return false for a directory at \"{1}\""
  )
  void fileWithAnyKindShouldFailIfThePathIsDirectory(Kind kind, String pathName)
      throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var dir = fs.getPath(pathName);
      makeParentDirs(dir);
      Files.createDirectory(dir);
      var kindSet = set(kind);
      var predicate = fileWithAnyKind(kindSet);

      // Then
      assertThat(predicate).rejects(dir);
    }
  }

  @DisplayName("fileWithAnyKind should produce a predicate with the expected behaviour")
  @MethodSource("fileWithAnyKindExistingFileTestCases")
  @ParameterizedTest(
      name = "fileWithAnyKind({0}) predicate should return {2} for existing path \"{1}\""
  )
  void fileWithAnyKindShouldProducePredicateWithTheExpectedBehaviour(
      Set<? extends Kind> kinds,
      String pathName,
      boolean isMatch
  ) throws IOException {
    try (var fs = Jimfs.newFileSystem()) {
      // Given
      var path = fs.getPath(pathName);
      makeParentDirs(path);
      Files.createFile(path);

      var predicate = fileWithAnyKind(kinds);

      // Then
      if (isMatch) {
        assertThat(predicate).accepts(path);
      } else {
        assertThat(predicate).rejects(path);
      }
    }
  }

  static Stream<Arguments> fileWithAnyKindExistingFileTestCases() {
    return Stream.of(
        Arguments.of(set(Kind.SOURCE), "/foo/bar/baz.java", true),
        Arguments.of(set(Kind.CLASS), "/foo/bar/baz.class", true),
        Arguments.of(set(Kind.HTML), "/foo/bar/baz.html", true),

        Arguments.of(set(Kind.SOURCE, Kind.CLASS, Kind.HTML), "/foo/bar/baz.java", true),
        Arguments.of(set(Kind.SOURCE, Kind.CLASS, Kind.HTML), "/foo/bar/baz.class", true),
        Arguments.of(set(Kind.SOURCE, Kind.CLASS, Kind.HTML), "/foo/bar/baz.html", true),

        Arguments.of(set(Kind.SOURCE), "/foo/bar/baz.JAVA", false),
        Arguments.of(set(Kind.CLASS), "/foo/bar/baz.CLASS", false),
        Arguments.of(set(Kind.HTML), "/foo/bar/baz.HTML", false),

        Arguments.of(set(Kind.SOURCE), "/foo/bar/baz.class", false),
        Arguments.of(set(Kind.CLASS), "/foo/bar/baz.html", false),
        Arguments.of(set(Kind.HTML), "/foo/bar/baz.java", false),

        Arguments.of(set(Kind.SOURCE, Kind.HTML), "/foo/bar/baz.class", false),
        Arguments.of(set(Kind.CLASS, Kind.SOURCE), "/foo/bar/baz.html", false),
        Arguments.of(set(Kind.HTML, Kind.CLASS), "/foo/bar/baz.java", false),

        Arguments.of(set(Kind.OTHER), "/foo/bar/baz", true),
        Arguments.of(set(Kind.SOURCE, Kind.OTHER), "/foo/bar/baz", true),
        Arguments.of(set(Kind.HTML, Kind.CLASS, Kind.OTHER), "/foo/bar/baz.blah", true)
    );
  }

  @SafeVarargs
  static <T> Set<T> set(T... args) {
    return Set.of(args);
  }

  static void makeParentDirs(Path path) throws IOException {
    var parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
  }
}
