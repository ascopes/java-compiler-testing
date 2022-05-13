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

package io.github.ascopes.jct.paths;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.StringUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File object that can be used with paths, and that is bound to a specific charset.
 *
 * <p>All inputs and outputs are buffered automatically.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PathJavaFileObject implements JavaFileObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathJavaFileObject.class);
  private static final long NOT_MODIFIED = 0L;

  private final Location location;
  private final Path path;
  private final String name;
  private final URI uri;
  private final Kind kind;
  private final Charset charset;

  /**
   * Initialize the file object.
   *
   * @param location the location of the object.
   * @param path     the path to the object.
   * @param name     the given name to use to identify the file.
   * @param kind     the kind of the file.
   * @param charset  the charset to use to read the file textually. If the file is binary then this
   *                 can be any value.
   */
  public PathJavaFileObject(Location location, Path path, String name, Kind kind, Charset charset) {
    this.location = requireNonNull(location);
    this.path = requireNonNull(path);
    this.name = requireNonNull(name);
    uri = path.toUri();
    this.kind = requireNonNull(kind);
    this.charset = requireNonNull(charset);
  }

  /**
   * Get the location of the file.
   *
   * @return the location.
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Get the path to the file.
   *
   * @return the path.
   */
  public Path getPath() {
    return path;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    return path.getFileName().toString().endsWith(simpleName + kind.extension);
  }

  @Override
  public NestingKind getNestingKind() {
    // We do not have access to this info, so return null to
    // indicate this to JSR-199.
    return null;
  }

  @Override
  public Modifier getAccessLevel() {
    // We do not have access to this info, so return null to
    // indicate this to JSR-199.
    return null;
  }

  @Override
  public URI toUri() {
    // TODO(ascopes): mitigate bug where URI from path in JAR starts with
    // jar://file:/// rather than jar:///, causing filesystem resolution
    // to fail. This might be an issue if JARs are added from non-root file
    // systems though, so I don't know the best way of working around this.
    return uri;
  }

  @Override
  public String getName() {
    return name;
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
  public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return decoder(ignoreEncodingErrors)
        .decode(ByteBuffer.wrap(Files.readAllBytes(path)))
        .toString();
  }

  @Override
  public BufferedWriter openWriter() throws IOException {
    return new BufferedWriter(openUnbufferedWriter());
  }

  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ex) {
      LOGGER.warn("Ignoring error reading last modified time for {}", uri, ex);
      return NOT_MODIFIED;
    }
  }

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(path);
    } catch (IOException ex) {
      LOGGER.warn("Ignoring error deleting {}", uri, ex);
      return false;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "{location=" + StringUtils.quoted(location.getName()) + ", "
        + "uri=" + StringUtils.quoted(uri) + ", "
        + "kind=" + kind
        + "}";
  }

  private InputStream openUnbufferedInputStream() throws IOException {
    return Files.newInputStream(path);
  }

  private OutputStream openUnbufferedOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(path.getParent());
    return Files.newOutputStream(path);
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

    return charset
        .newDecoder()
        .onUnmappableCharacter(action)
        .onMalformedInput(action);
  }

  private CharsetEncoder encoder() {
    return charset
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
  }
}
