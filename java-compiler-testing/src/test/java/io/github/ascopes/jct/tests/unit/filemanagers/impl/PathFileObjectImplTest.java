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
package io.github.ascopes.jct.tests.unit.filemanagers.impl;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someAbsolutePath;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someBinaryData;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLinesOfText;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someRelativePath;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someTemporaryFileSystem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.impl.PathFileObjectImpl;
import io.github.ascopes.jct.utils.FileUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.FileTime;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link PathFileObjectImpl} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PathFileObjectImpl tests")
class PathFileObjectImplTest {

  @DisplayName("Passing a null location to the constructor raises an exception")
  @Test
  void passingNullLocationToConstructorRaisesException() {
    // Then
    assertThatThrownBy(() -> new PathFileObjectImpl(null, someAbsolutePath(), someRelativePath()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("location");
  }

  @DisplayName("Passing a null root path to the constructor raises an exception")
  @Test
  void passingNullRootPathToConstructorRaisesException() {
    // Then
    assertThatThrownBy(() -> new PathFileObjectImpl(someLocation(), null, someRelativePath()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("rootPath");
  }

  @DisplayName("Passing a null relative path to the constructor raises an exception")
  @Test
  void passingNullRelativePathToConstructorRaisesException() {
    // Then
    assertThatThrownBy(() -> new PathFileObjectImpl(someLocation(), someAbsolutePath(), null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("relativePath");
  }

  @DisplayName("Passing a relative path to the constructor for the root path raises an exception")
  @Test
  void passingRelativePathToConstructorForRootPathRaisesException() {
    // Given
    var rootPath = someRelativePath();

    // Then
    assertThatThrownBy(() -> new PathFileObjectImpl(someLocation(), rootPath, someRelativePath()))
        .isInstanceOf(JctIllegalInputException.class)
        .hasMessage("Expected rootPath to be absolute, but got " + rootPath);
  }

  @DisplayName(".delete() will delete an existing file")
  @Test
  void deleteWillDeleteAnExistingFile() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      Files.createDirectories(dir);
      Files.createFile(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // When
      var result = fileObject.delete();

      // Then
      assertThat(file).doesNotExist();
      assertThat(result).isTrue();
    }
  }

  @DisplayName(".delete() will not delete a missing file")
  @Test
  void deleteWillNotDeleteMissingFile() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      Files.createDirectories(dir);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // When
      var result = fileObject.delete();

      // Then
      assertThat(file).doesNotExist();
      assertThat(result).isFalse();
    }
  }

  @DisplayName(".delete() will ignore internal errors")
  @Test
  void deleteWillIgnoreInternalErrors() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeDir = rootDir.relativize(dir);

      Files.createDirectories(dir);
      Files.createFile(file);

      // Purposely using a directory rather than a file here to trigger a deletion error.
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeDir);

      // When
      var result = fileObject.delete();

      // Then
      assertThat(dir).exists().isDirectory();
      assertThat(result).isFalse();
    }
  }

  @DisplayName(".equals(null) returns false")
  @Test
  void equalsNullReturnsFalse() {
    // Given
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), someRelativePath());

    // Then
    assertThat(fileObject)
        .isNotEqualTo(null);
  }

  @DisplayName(".equals(Object) returns false if the object is not a path file object")
  @Test
  void equalsReturnsFalseIfNotPathFileObjectImpl() {
    // Given
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), someRelativePath());

    // Then
    assertThat(fileObject)
        .isNotEqualTo(mock(FileObject.class))
        .isNotEqualTo(mock(JavaFileObject.class))
        .isNotEqualTo("foobar")
        .isNotEqualTo(1234);
  }

  @DisplayName(".equals(PathFileObjectImpl) returns true if the file object has the same URI")
  @Test
  void equalsReturnsTrueIfTheFileObjectHasTheSameUri() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();

    var fileObject1 = new PathFileObjectImpl(someLocation(), rootPath, relativePath);
    var fileObject2 = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject1).isEqualTo(fileObject2);
  }

  @DisplayName(".equals(PathFileObjectImpl) returns true if the file object is the same instance")
  @SuppressWarnings("EqualsWithItself")
  @Test
  void equalsReturnsTrueIfTheFileObjectIsTheSameInstance() {
    // Given
    var location = someLocation();
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();

    var fileObject = new PathFileObjectImpl(location, rootPath, relativePath);

    // Then
    assertThat(fileObject.equals(fileObject)).isTrue();
  }

  @DisplayName(".getAbsolutePath() returns the absolute path")
  @Test
  void getAbsolutePathReturnsTheAbsolutePath() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject.getAbsolutePath())
        .isEqualTo(rootPath.resolve(relativePath));
  }

  @DisplayName(".getAccessLevel() returns null")
  @Test
  void getAccessLevelReturnsNull() {
    // Given
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), someRelativePath());

    // Then
    assertThat(fileObject.getAccessLevel()).isNull();
  }

  @DisplayName(".getBinaryName() gets the binary name of the relative path")
  @Test
  void getBinaryNameGetsTheBinaryNameOfTheRelativePath() {
    // Given
    var relativePath = someRelativePath().resolve("Cat.java");
    var expectedBinaryName = FileUtils.pathToBinaryName(relativePath);
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), relativePath);

    // Then
    assertThat(fileObject.getBinaryName())
        .isEqualTo(expectedBinaryName);
  }

  @DisplayName(".getCharContent(...) returns the character content")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for ignoreEncodingErrors={0}")
  void getCharContentReturnsCharacterContent(boolean ignoreEncodingErrors) throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var text = someLinesOfText();

      Files.createDirectories(dir);
      Files.writeString(file, text, StandardCharsets.UTF_8);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      assertThat(fileObject.getCharContent(ignoreEncodingErrors))
          .isEqualTo(text);
    }
  }

  @DisplayName(".getCharContent(...) ignores encoding errors when instructed")
  @Test
  void getCharContentIgnoresEncodingErrors() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var badlyEncodedBytes = new byte[]{(byte) 0xC0, (byte) 0xC1, (byte) 0xF5, (byte) 0xFF};

      Files.createDirectories(dir);
      Files.write(file, badlyEncodedBytes);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      assertThatCode(() -> fileObject.getCharContent(true))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName(".getCharContent(...) propagates encoding errors when instructed")
  @Test
  void getCharContentPropagatesEncodingErrors() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var badlyEncodedBytes = new byte[]{(byte) 0xC0, (byte) 0xC1, (byte) 0xF5, (byte) 0xFF};

      Files.createDirectories(dir);
      Files.write(file, badlyEncodedBytes);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      assertThatThrownBy(() -> fileObject.getCharContent(false))
          .isInstanceOf(MalformedInputException.class);
    }
  }

  @DisplayName(".getLastModified() returns the last modified timestamp")
  @Test
  void getLastModifiedReturnsTheLastModifiedTimestamp() throws Exception {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      // Create the file first to separate the creation and modification timestamps.
      Files.createDirectories(dir);
      Files.createFile(file);
      // Write something to it first.
      Files.writeString(file, "foobar");
      // Wait a moment or two
      Thread.sleep(500);
      // Write again to change the timestamp for modification
      Files.writeString(file, "bazbork");

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      var creationTime = ((FileTime) Files.getAttribute(file, "creationTime")).toMillis();
      var lastModified = fileObject.getLastModified();

      assertThat(lastModified)
          .isNotEqualTo(creationTime)
          .isEqualTo(Files.getLastModifiedTime(file).toMillis());
    }
  }

  @DisplayName(".getLastModified() returns 0 if the file does not exist")
  @Test
  void getLastModifiedReturnsZeroIfTheFileDoesNotExist() throws Exception {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      Files.createDirectories(dir);
      // Purposely do not create the file.

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      var lastModified = fileObject.getLastModified();

      assertThat(lastModified).isZero();
    }
  }

  @DisplayName(".getLocation() returns the location")
  @Test
  void getLocationReturnsTheLocation() {
    // Given
    var location = someLocation();
    var fileObject = new PathFileObjectImpl(location, someAbsolutePath(), someRelativePath());

    // Then
    assertThat(fileObject.getLocation())
        .isSameAs(location);
  }

  @DisplayName(
      ".getName() returns the file name when the relative path is initialised from a relative path"
  )
  @Test
  void getNameReturnsTheFileNameWhenRelativePathIsRelative() {
    // Given
    var relativePath = someRelativePath();
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), relativePath);

    // Then
    assertThat(fileObject.getName())
        .isEqualTo(relativePath.toString());
  }

  @DisplayName(
      ".getName() returns the file name when the relative path is initialised from an absolute path"
  )
  @Test
  void getNameReturnsTheFileNameWhenRelativePathIsInitialisedFromAnAbsolutePath() {
    // Given
    var absolutePath = someAbsolutePath();
    var relativePath = someRelativePath();
    var fileObject = new PathFileObjectImpl(
        someLocation(),
        absolutePath,
        absolutePath.resolve(relativePath)
    );

    // Then
    assertThat(fileObject.getName())
        .isEqualTo(relativePath.toString());
  }

  @DisplayName(".getNestingKind() returns null")
  @Test
  void getNestingKindReturnsNull() {
    // Given
    var fileObject = new PathFileObjectImpl(someLocation(), someAbsolutePath(), someRelativePath());

    // Then
    assertThat(fileObject.getNestingKind()).isNull();
  }

  @DisplayName(".getRelativePath() returns the relative path if initialised from a relative path")
  @Test
  void getRelativePathReturnsRelativePathIfInitialisedFromRelativePath() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath,
        rootPath.resolve(relativePath));

    // Then
    assertThat(fileObject.getRelativePath()).isEqualTo(relativePath);
  }

  @DisplayName(".getRelativePath() returns the relative path if initialised from an absolute path")
  @Test
  void getRelativePathReturnsRelativePathIfInitialisedFromAbsolutePath() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject.getRelativePath()).isEqualTo(relativePath);
  }

  @DisplayName(".getRootPath() returns the root path")
  @Test
  void getRootPathReturnsTheRootPath() {
    // Given
    var rootPath = someAbsolutePath();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, someRelativePath());

    // Then
    assertThat(fileObject.getRootPath()).isEqualTo(rootPath);
  }

  @DisplayName(".hashCode() returns the URI hash code")
  @Test
  void hashCodeUsesUriHashCode() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var uri = rootPath.resolve(relativePath).toUri();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject).hasSameHashCodeAs(uri);
  }

  @DisplayName(".isNameCompatible(...) returns the expected value")
  @CsvSource({
      // Valid cases
      "   Foo.java,   Foo,   SOURCE,   true",
      "  Bar.class,   Bar,    CLASS,   true",
      "   Baz.html,   Baz,     HTML,   true",
      "       Bork,  Bork,    OTHER,   true",
      // Simple name case-insensitive matches that should fail.
      "   Foo.java,   foo,   SOURCE,  false",
      "  Bar.class,   bar,    CLASS,  false",
      "   Baz.html,   baz,     HTML,  false",
      "       Bork,  bork,    OTHER,  false",
      // Different simple names
      "   Foo.java,   Bar,   SOURCE,  false",
      "  Bar.class,   Baz,    CLASS,  false",
      "   Baz.html,  Bork,     HTML,  false",
      "       Bork,   Foo,    OTHER,  false",
      // Different kinds
      "   Foo.java,   Foo,    CLASS,  false",
      "  Bar.class,   Bar,   SOURCE,  false",
      "   Baz.html,   Baz,    OTHER,  false",
      "       Bork,  Bork,     HTML,  false",
  })
  @ParameterizedTest(name = "expect {3} when fileName={0}, simpleName={1}, kind={2}")
  void isNameCompatibleReturnsTheExpectedValue(
      String fileName,
      String simpleName,
      Kind kind,
      boolean expectCompatible
  ) {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath().resolve(fileName);
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    assertThat(fileObject.isNameCompatible(simpleName, kind))
        .isEqualTo(expectCompatible);
  }

  @DisplayName(".openInputStream() reads the correct file contents")
  @Test
  void openInputStreamReadsTheCorrectFileContents() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      var data = someBinaryData();

      Files.createDirectories(dir);
      Files.write(file, data);

      // Then
      assertThat(fileObject.openInputStream())
          .hasBinaryContent(data);
    }
  }

  @DisplayName(".openInputStream() throws NoSuchFileException if the file does not exist")
  @Test
  void openInputStreamThrowsNoSuchFileExceptionIfFileDoesNotExist() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      Files.createDirectories(dir);

      // Then
      assertThatThrownBy(fileObject::openInputStream)
          .isInstanceOf(NoSuchFileException.class);
    }
  }

  @DisplayName(
      ".openOutputStream() enables writing to the expected file when it does yet not exist"
  )
  @Test
  void openOutputStreamEnablesWritingToTheExpectedFileWhenItDoesNotYetExist() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      var data = someBinaryData();

      Files.createDirectories(dir);

      // When
      try (var os = fileObject.openOutputStream()) {
        os.write(data);
      }

      // Then
      assertThat(file).hasBinaryContent(data);
    }
  }

  @DisplayName(".openOutputStream() overwrites any existing file")
  @Test
  void openOutputStreamOverwritesAnyExistingFile() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      var data = someBinaryData();

      Files.createDirectories(dir);
      Files.write(file, new byte[]{1, 2, 3, 4, 5, 6});

      // When
      try (var os = fileObject.openOutputStream()) {
        os.write(data);
      }

      // Then
      assertThat(file).hasBinaryContent(data);
    }
  }

  @DisplayName(".openReader(...) returns the character content")
  @ValueSource(booleans = {true, false})
  @ParameterizedTest(name = "for ignoreEncodingErrors={0}")
  void openReaderReturnsCharacterContent(boolean ignoreEncodingErrors) throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var text = someLinesOfText();

      Files.createDirectories(dir);
      Files.writeString(file, text, StandardCharsets.UTF_8);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      try (var reader = fileObject.openReader(ignoreEncodingErrors)) {
        var stringWriter = new StringWriter();
        reader.transferTo(stringWriter);
        assertThat(stringWriter.toString()).isEqualTo(text);
      }
    }
  }

  @DisplayName(".openReader(...) ignores encoding errors when instructed")
  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  void openReaderIgnoresEncodingErrors() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var badlyEncodedBytes = new byte[]{(byte) 0xC0, (byte) 0xC1, (byte) 0xF5, (byte) 0xFF};

      Files.createDirectories(dir);
      Files.write(file, badlyEncodedBytes);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      assertThatCode(() -> {
        // Then
        try (var reader = (BufferedReader) fileObject.openReader(true)) {
          while (reader.readLine() != null) {
            // discard the line.
          }
        }
      }).doesNotThrowAnyException();
    }
  }

  @DisplayName(".openReader(...) propagates encoding errors when instructed")
  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  void openReaderPropagatesEncodingErrors() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);

      var badlyEncodedBytes = new byte[]{(byte) 0xC0, (byte) 0xC1, (byte) 0xF5, (byte) 0xFF};

      Files.createDirectories(dir);
      Files.write(file, badlyEncodedBytes);

      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);

      // Then
      assertThatThrownBy(() -> {
        try (var reader = (BufferedReader) fileObject.openReader(false)) {
          while (reader.readLine() != null) {
            // discard the line.
          }
        }
      }).isInstanceOf(MalformedInputException.class);
    }
  }

  @DisplayName(
      ".openWriter() enables writing to the expected file when it does yet not exist"
  )
  @Test
  void openWriterEnablesWritingToTheExpectedFileWhenItDoesNotYetExist() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      var text = someLinesOfText();

      Files.createDirectories(dir);

      // When
      try (var writer = fileObject.openWriter()) {
        writer.write(text);
      }

      // Then
      assertThat(file).hasContent(text);
    }
  }

  @DisplayName(".openWriter() overwrites any existing file")
  @Test
  void openWriterOverwritesAnyExistingFile() throws IOException {
    // Given
    try (var fs = someTemporaryFileSystem()) {
      var rootDir = fs.getRootPath().resolve("root-a").resolve("root-sub-a");
      var dir = rootDir.resolve("foo").resolve("bar");
      var file = dir.resolve("Baz.txt");
      var relativeFile = rootDir.relativize(file);
      var fileObject = new PathFileObjectImpl(someLocation(), rootDir, relativeFile);
      var text = someLinesOfText();

      Files.createDirectories(dir);
      Files.write(file, new byte[]{1, 2, 3, 4, 5, 6});

      // When
      try (var writer = fileObject.openWriter()) {
        writer.write(text);
      }

      // Then
      assertThat(file).hasContent(text);
    }
  }

  @DisplayName(".toUri() returns the URI")
  @Test
  void toUriReturnsTheUri() {
    // Given
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var uri = rootPath.resolve(relativePath).toUri();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject.toUri())
        .isEqualTo(uri);
  }

  @DisplayName(".toString() returns the expected value")
  @Test
  void toStringReturnsTheExpectedValue() {
    var rootPath = someAbsolutePath();
    var relativePath = someRelativePath();
    var uri = rootPath.resolve(relativePath).toUri();
    var fileObject = new PathFileObjectImpl(someLocation(), rootPath, relativePath);

    // Then
    assertThat(fileObject.toString())
        .isEqualTo("PathFileObjectImpl{uri=\"%s\"}", uri);
  }
}
