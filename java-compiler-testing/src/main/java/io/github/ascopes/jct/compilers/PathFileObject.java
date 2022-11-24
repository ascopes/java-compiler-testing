/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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
package io.github.ascopes.jct.compilers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.impl.FileUtils;
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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple {@link JavaFileObject} that points to a path on some {@link FileSystem} somewhere.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PathFileObject implements JavaFileObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathFileObject.class);
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final long NOT_MODIFIED = 0L;

  private final Location location;
  private final Path rootPath;
  private final Path relativePath;
  private final Path fullPath;
  private final String name;
  private final URI uri;
  private final Kind kind;

  /**
   * Initialize this file object.
   *
   * @param rootPath     the root directory that the path is a package within.
   * @param relativePath the path to point to, relative to the root.
   */
  public PathFileObject(Location location, Path rootPath, Path relativePath) {
    requireNonNull(location, "location");
    requireNonNull(rootPath, "rootPath");
    requireNonNull(relativePath, "relativePath");

    if (!rootPath.isAbsolute()) {
      throw new IllegalArgumentException("rootPath must be absolute");
    }

    this.location = location;
    this.rootPath = rootPath;

    if (relativePath.isAbsolute()) {
      this.relativePath = rootPath.relativize(relativePath);
    } else {
      this.relativePath = relativePath;
    }

    fullPath = rootPath.resolve(relativePath);
    name = relativePath.toString();
    uri = fullPath.toUri();
    kind = FileUtils.pathToKind(relativePath);
  }

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(fullPath);
    } catch (IOException ex) {
      LOGGER.warn("Ignoring error deleting {}", uri, ex);
      return false;
    }
  }

  @Override
  public boolean equals(@Nullable Object other) {
    // Roughly the same as what Javac does.
    return other instanceof FileObject && uri.equals(((FileObject) other).toUri());
  }

  @Nullable
  @Override
  public Modifier getAccessLevel() {
    // Null implies that the access level is unknown.
    return null;
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
      return Files.getLastModifiedTime(fullPath).toMillis();
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
   * Get the root path that the package containing this file is nested within.
   *
   * @return the root path.
   */
  public Path getRoot() {
    return rootPath;
  }

  @Override
  public String getName() {
    return name;
  }

  @Nullable
  @Override
  public NestingKind getNestingKind() {
    // Null implies that the nesting kind is unknown.
    return null;
  }

  /**
   * Get the full path of this file object.
   *
   * @return the full path.
   */
  public Path getFullPath() {
    return fullPath;
  }

  /**
   * Get the relative path of this file object.
   *
   * @return the path of this file object.
   */
  public Path getRelativePath() {
    return relativePath;
  }

  @Override
  public int hashCode() {
    // Corresponds to what the .equals override checks for.
    return uri.hashCode();
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    // TODO(ascopes): does this need to be case insensitive on Windows?
    var fileName = simpleName + kind.extension;

    return relativePath.getFileName().toString().equals(fileName);
  }

  @Override
  @WillNotClose
  public BufferedInputStream openInputStream() throws IOException {
    return new BufferedInputStream(openUnbufferedInputStream());
  }

  @Override
  @WillNotClose
  public BufferedOutputStream openOutputStream() throws IOException {
    return new BufferedOutputStream(openUnbufferedOutputStream());
  }

  @Override
  @WillNotClose
  public BufferedReader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new BufferedReader(openUnbufferedReader(ignoreEncodingErrors));
  }

  @Override
  @WillNotClose
  public BufferedWriter openWriter() throws IOException {
    return new BufferedWriter(openUnbufferedWriter());
  }

  @Override
  public URI toUri() {
    return uri;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", uri)
        .toString();
  }

  @WillNotClose
  private InputStream openUnbufferedInputStream() throws IOException {
    return Files.newInputStream(fullPath);
  }

  @WillNotClose
  private OutputStream openUnbufferedOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(fullPath.getParent());
    return Files.newOutputStream(fullPath);
  }

  @WillNotClose
  private Reader openUnbufferedReader(boolean ignoreEncodingErrors) throws IOException {
    return new InputStreamReader(openUnbufferedInputStream(), decoder(ignoreEncodingErrors));
  }

  @WillNotClose
  private Writer openUnbufferedWriter() throws IOException {
    return new OutputStreamWriter(openUnbufferedOutputStream(), encoder());
  }

  private CharsetDecoder decoder(boolean ignoreEncodingErrors) {
    var action = ignoreEncodingErrors
        ? CodingErrorAction.IGNORE
        : CodingErrorAction.REPORT;

    return DEFAULT_CHARSET
        .newDecoder()
        .onUnmappableCharacter(action)
        .onMalformedInput(action);
  }

  private CharsetEncoder encoder() {
    return DEFAULT_CHARSET
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
  }
}
