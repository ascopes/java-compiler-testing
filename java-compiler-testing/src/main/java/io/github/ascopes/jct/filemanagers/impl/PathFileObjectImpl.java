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
package io.github.ascopes.jct.filemanagers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.filemanagers.PathFileObject;
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
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * @since 1.0.0 (renamed from PathFileObject introduced in 0.0.1)
 */
@API(since = "1.0.0", status = Status.INTERNAL)
public final class PathFileObjectImpl implements PathFileObject {

  private static final Logger log = LoggerFactory.getLogger(PathFileObjectImpl.class);
  private static final Charset CHARSET = StandardCharsets.UTF_8;

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
  public PathFileObjectImpl(Location location, Path rootPath, Path relativePath) {
    requireNonNull(location, "location");
    requireNonNull(rootPath, "rootPath");
    requireNonNull(relativePath, "relativePath");

    if (!rootPath.isAbsolute()) {
      throw new JctIllegalInputException("Expected rootPath to be absolute, but got " + rootPath);
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

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(absolutePath);
    } catch (IOException ex) {
      log.debug("Ignoring error deleting {}", uri, ex);
      return false;
    }
  }

  @Override
  public boolean equals(@Nullable Object other) {
    // Roughly the same as what Javac does.
    return other instanceof FileObject && uri.equals(((FileObject) other).toUri());
  }

  @Override
  public Path getAbsolutePath() {
    return absolutePath;
  }

  @Override
  public String getBinaryName() {
    return FileUtils.pathToBinaryName(relativePath);
  }

  @Override
  public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
    try (var input = openInputStream()) {
      return decoder(ignoreEncodingErrors)
          .decode(ByteBuffer.wrap(input.readAllBytes()))
          .toString();
    }
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(absolutePath).toMillis();
    } catch (IOException ex) {
      log.debug("Ignoring error reading last modified time for {}", uri, ex);
      return NOT_MODIFIED;
    }
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Path getRelativePath() {
    return relativePath;
  }

  @Override
  public Path getRootPath() {
    return rootPath;
  }

  @Override
  public int hashCode() {
    // Corresponds to what the .equals override checks for.
    return uri.hashCode();
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    // Note that this behaves case-sensitively, even on Windows.
    var fileName = simpleName + kind.extension;
    return relativePath.getFileName().toString().equals(fileName);
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return new BufferedInputStream(openUnbufferedInputStream());
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    return new BufferedOutputStream(openUnbufferedOutputStream());
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    var inputStream = openUnbufferedInputStream();
    var decoder = decoder(ignoreEncodingErrors);
    var reader = new InputStreamReader(inputStream, decoder);
    return new BufferedReader(reader);
  }

  @Override
  public Writer openWriter() throws IOException {
    var outputStream = openUnbufferedOutputStream();
    var encoder = CHARSET
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
    var writer = new OutputStreamWriter(outputStream, encoder);
    return new BufferedWriter(writer);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", uri)
        .toString();
  }

  @Override
  public URI toUri() {
    return uri;
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
}
