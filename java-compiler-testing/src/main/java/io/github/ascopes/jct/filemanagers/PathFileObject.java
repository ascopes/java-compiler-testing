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
package io.github.ascopes.jct.filemanagers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.FileUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple {@link JavaFileObject} that points to a path on some {@link FileSystem} somewhere.
 *
 * <p>This object will always use UTF-8 encoding when obtaining readers or writers.
 *
 * <p>No access level or nesting kind information is provided by this implementation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class PathFileObject implements JavaFileObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathFileObject.class);
  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private static final long NOT_MODIFIED = 0L;

  private final Location location;
  private final Path rootPath;
  private final Path relativePath;
  private final Path absolutePath;
  private final String name;
  private final URI uri;
  private final Kind kind;

  /**
   * Initialize this file object.
   *
   * @param location     the location that the file object is located within.
   * @param rootPath     the root directory that the path is a package within.
   * @param relativePath the path to point to, relative to the root.
   */
  public PathFileObject(Location location, Path rootPath, Path relativePath) {
    requireNonNull(location, "location");
    requireNonNull(rootPath, "rootPath");
    requireNonNull(relativePath, "relativePath");

    if (!rootPath.isAbsolute()) {
      throw new IllegalArgumentException("Expected rootPath to be absolute, but got " + rootPath);
    }

    this.location = location;
    this.rootPath = rootPath;

    // TODO(ascopes): should we allow absolute paths in the input here? Not sure that it makes a
    //  lot of sense.
    this.relativePath = relativePath.isAbsolute()
        ? rootPath.relativize(relativePath)
        : relativePath;

    absolutePath = rootPath.resolve(relativePath);
    name = this.relativePath.toString();
    uri = absolutePath.toUri();
    kind = FileUtils.pathToKind(relativePath);
  }

  /**
   * Attempt to delete the file.
   *
   * @return {@code true} if the file was deleted, or {@code false} if it was not deleted.
   */
  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(absolutePath);
    } catch (IOException ex) {
      LOGGER.debug("Ignoring error deleting {}", uri, ex);
      return false;
    }
  }

  /**
   * Determine if this object equals another.
   *
   * @param other the other object to compare with.
   * @return {@code true} if the other object is also a {@link FileObject} and has the same
   *     {@link #toUri() URI} as this object, or {@code false} otherwise.
   */
  @Override
  public boolean equals(@Nullable Object other) {
    // Roughly the same as what Javac does.
    return other instanceof FileObject && uri.equals(((FileObject) other).toUri());
  }

  /**
   * Get the absolute path of this file object.
   *
   * @return the full path.
   */
  public Path getAbsolutePath() {
    return absolutePath;
  }

  /**
   * Get the class access level, where appropriate.
   *
   * <p>In this implementation, this class will always return {@code null}, since this
   * information is not readily available without preloading the file in question and parsing it
   * first.
   *
   * <p>At the time of writing, the OpenJDK implementations of the JavaFileObject class
   * do not provide an implementation for this method either.
   *
   * @return {@code null}, always. This implementation does not provide this functionality.
   */
  @Override
  public Modifier getAccessLevel() {
    return unknown();
  }

  /**
   * Get the inferred binary name of the file object.
   *
   * @return the inferred binary name.
   * @since 0.2.1
   */
  @API(since = "0.2.1", status = Status.STABLE)
  public String getBinaryName() {
    return FileUtils.pathToBinaryName(relativePath);
  }

  /**
   * Read the character content of the file into memory and decode it using the default character
   * set.
   *
   * @param ignoreEncodingErrors ignore encoding errors if {@code true}, or throw them otherwise.
   * @return the character content, encoded as UTF-8.
   * @throws IOException if an IO error occurs.
   */
  @Override
  public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
    try (var input = openInputStream()) {
      return decoder(ignoreEncodingErrors)
          .decode(ByteBuffer.wrap(input.readAllBytes()))
          .toString();
    }
  }

  /**
   * Get the full path of this file object.
   *
   * @return the full path.
   * @deprecated use {@link #getAbsolutePath()} instead.
   */
  @Deprecated(since = "0.7.3", forRemoval = true)
  @SuppressWarnings("DeprecatedIsStillUsed")
  public Path getFullPath() {
    return absolutePath;
  }

  /**
   * Get the kind of the file.
   *
   * @return the inferred file kind.
   */
  @Override
  public Kind getKind() {
    return kind;
  }

  /**
   * Determine when the file was last modified.
   *
   * @return the timestamp in milliseconds since UNIX epoch that the file was last modified at, or
   *     {@code 0L} if unmodified.
   */
  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(absolutePath).toMillis();
    } catch (IOException ex) {
      LOGGER.debug("Ignoring error reading last modified time for {}", uri, ex);
      return NOT_MODIFIED;
    }
  }

  /**
   * Get the location of this path file object.
   *
   * @return the location.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Get the file name as a string.
   *
   * @return the name of the file.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Determine the class nesting kind, where appropriate.
   *
   * <p>In this implementation, this class will always return {@code null}, since this
   * information is not readily available without preloading the file in question and parsing it
   * first.
   *
   * <p>At the time of writing, the OpenJDK implementations of the JavaFileObject class
   * do not provide an implementation for this method either.
   *
   * @return {@code null} in all cases, this operation is not implemented.
   */
  @Override
  public NestingKind getNestingKind() {
    return unknown();
  }

  /**
   * Get the relative path of this file object.
   *
   * @return the path of this file object.
   */
  public Path getRelativePath() {
    return relativePath;
  }

  /**
   * Get the root path that the package containing this file is nested within.
   *
   * @return the root path.
   */
  public Path getRootPath() {
    return rootPath;
  }

  /**
   * Determine the hash code for this object.
   *
   * @return the hash code for the object.
   */
  @Override
  public int hashCode() {
    // Corresponds to what the .equals override checks for.
    return uri.hashCode();
  }

  /**
   * Determine if a given simple name and file kind are compatible with this file object.
   *
   * <p>This will perform a case-sensitive check, regardless of the platform that it runs on.
   *
   * @return {@code true} if the simple name and kind are compatible with the current file object
   *     name, or {@code false} if not.
   */
  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    // Note that this behaves case-sensitively, even on Windows.
    var fileName = simpleName + kind.extension;

    return relativePath.getFileName().toString().equals(fileName);
  }

  /**
   * Open an input stream into this file.
   *
   * <p>This input stream must be closed once finished with, otherwise
   * resources will be leaked.
   *
   * <p>The returned implementation will always be buffered.
   *
   * @return a buffered input stream.
   * @throws NoSuchFileException if the file does not exist.
   * @throws IOException         if an IO error occurs.
   */
  @Override
  public BufferedInputStream openInputStream() throws IOException {
    return new BufferedInputStream(openUnbufferedInputStream());
  }

  /**
   * Open an output stream to this file.
   *
   * <p>This will create the file first if it does not already exist. If it does
   * exist, then this will overwrite the file and truncate it. The parent directories
   * will also be created if they do not exist.
   *
   * <p>This output stream must be closed once finished with, otherwise
   * resources will be leaked.
   *
   * <p>The returned implementation will always be buffered.
   *
   * @return a buffered output stream.
   * @throws IOException if an IO error occurs.
   */
  @Override
  public BufferedOutputStream openOutputStream() throws IOException {
    return new BufferedOutputStream(openUnbufferedOutputStream());
  }

  /**
   * Open a reader to this file using the default charset (UTF-8).
   *
   * <p>This reader must be closed once finished with, otherwise
   * resources will be leaked.
   *
   * <p>The returned implementation will always be buffered.
   *
   * @param ignoreEncodingErrors {@code true} to suppress encoding errors, or {@code false} to throw
   *                             them to the caller.
   * @return a buffered reader.
   * @throws NoSuchFileException if the file does not exist.
   * @throws IOException         if an IO error occurs.
   */
  @Override
  public BufferedReader openReader(boolean ignoreEncodingErrors) throws IOException {
    var inputStream = openUnbufferedInputStream();
    var decoder = decoder(ignoreEncodingErrors);
    var reader = new InputStreamReader(inputStream, decoder);
    return new BufferedReader(reader);
  }

  /**
   * Open a writer to this file using the default charset (UTF-8).
   *
   * <p>This will create the file first if it does not already exist. If it does exist,
   * this will first overwrite the file and truncate it. The parent directories
   * will also be created if they do not exist.
   *
   * <p>This input stream must be closed once finished with, otherwise
   * resources will be leaked.
   *
   * <p>The returned implementation will always be buffered.
   *
   * @return a buffered writer.
   * @throws IOException if an IO error occurs.
   */
  @Override
  public BufferedWriter openWriter() throws IOException {
    var outputStream = openUnbufferedOutputStream();
    var encoder = CHARSET
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
    var writer = new OutputStreamWriter(outputStream, encoder);
    return new BufferedWriter(writer);
  }

  /**
   * Determine the URI for this file.
   *
   * @return the URI for this file object.
   */
  @Override
  public URI toUri() {
    return uri;
  }

  /**
   * Get a string representation of this object.
   *
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", uri)
        .toString();
  }

  private InputStream openUnbufferedInputStream() throws IOException {
    return Files.newInputStream(absolutePath);
  }

  private OutputStream openUnbufferedOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(absolutePath.getParent());
    return Files.newOutputStream(absolutePath);
  }

  private CharsetDecoder decoder(boolean ignoreEncodingErrors) {
    var action = ignoreEncodingErrors
        ? CodingErrorAction.IGNORE
        : CodingErrorAction.REPORT;

    return CHARSET
        .newDecoder()
        .onUnmappableCharacter(action)
        .onMalformedInput(action);
  }

  private <T> T unknown() {
    return null;
  }
}
