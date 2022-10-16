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
package io.github.ascopes.jct.filemanagers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
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
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
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
  private final Path root;
  private final Path relativePath;
  private final Path fullPath;
  private final String name;
  private final URI uri;
  private final Kind kind;

  /**
   * Initialize this file object.
   *
   * @param root         the root directory that the path is a package within.
   * @param relativePath the path to point to.
   */
  public PathFileObject(Location location, Path root, Path relativePath) {
    this.location = requireNonNull(location, "location");
    this.root = requireNonNull(root, "root");
    this.relativePath = root.relativize(requireNonNull(relativePath, "relativePath"));
    fullPath = root.resolve(relativePath);
    name = relativePath.toString();
    uri = fullPath.toUri();
    kind = FileUtils.pathToKind(relativePath);
  }

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(relativePath);
    } catch (IOException ex) {
      LOGGER.warn("Ignoring error deleting {}", uri, ex);
      return false;
    }
  }

  @Override
  public boolean equals(Object other) {
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
    return decoder(ignoreEncodingErrors)
        .decode(ByteBuffer.wrap(Files.readAllBytes(fullPath)))
        .toString();
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(relativePath).toMillis();
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
    return root;
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
    return uri.hashCode();
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    return relativePath.getFileName().toString().equals(simpleName + kind.extension);
  }

  @Override
  public BufferedInputStream openInputStream() throws IOException {
    return new BufferedInputStream(openUnbufferedInputStream());
  }

  @Override
  public BufferedOutputStream openOutputStream() throws IOException {
    return new BufferedOutputStream(openUnbufferedOutputStream());
  }

  @Override
  public BufferedReader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new BufferedReader(openUnbufferedReader(ignoreEncodingErrors));
  }

  @Override
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

  private InputStream openUnbufferedInputStream() throws IOException {
    return Files.newInputStream(fullPath);
  }

  private OutputStream openUnbufferedOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(fullPath.getParent());
    return Files.newOutputStream(fullPath);
  }

  private Reader openUnbufferedReader(boolean ignoreEncodingErrors) throws IOException {
    return new InputStreamReader(openUnbufferedInputStream(), decoder(ignoreEncodingErrors));
  }

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
