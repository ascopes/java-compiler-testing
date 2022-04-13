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

package com.github.ascopes.jct.paths;

import com.github.ascopes.jct.intern.StringUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File object that can be used with paths.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PathJavaFileObject implements JavaFileObject {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathJavaFileObject.class);

  private final Location location;
  private final Path path;
  private final String name;
  private final URI uri;
  private final Kind kind;

  /**
   * Initialize the file object.
   *
   * @param location the location of the object.
   * @param path     the path to the object.
   * @param name     the given name to use to identify the file.
   */
  public PathJavaFileObject(Location location, Path path, String name) {
    this.location = Objects.requireNonNull(location);
    this.path = Objects.requireNonNull(path);
    this.name = Objects.requireNonNull(name);
    uri = path.toUri();

    var fileName = path.getFileName().toString();

    if (fileName.endsWith(Kind.SOURCE.extension)) {
      kind = Kind.SOURCE;
    } else if (fileName.endsWith(Kind.HTML.extension)) {
      kind = Kind.HTML;
    } else if (fileName.endsWith(Kind.CLASS.extension)) {
      kind = Kind.CLASS;
    } else {
      kind = Kind.OTHER;
    }
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
    return null;
  }

  @Override
  public Modifier getAccessLevel() {
    return null;
  }

  @Override
  public URI toUri() {
    return uri;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return new BufferedInputStream(Files.newInputStream(path));
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(path.getParent());
    return new BufferedOutputStream(Files.newOutputStream(path));
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new InputStreamReader(openInputStream(), decoder(ignoreEncodingErrors));
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return decoder(ignoreEncodingErrors)
        .decode(ByteBuffer.wrap(Files.readAllBytes(path)))
        .toString();
  }

  @Override
  public Writer openWriter() throws IOException {
    return new OutputStreamWriter(openOutputStream(), encoder());
  }

  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ex) {
      LOGGER.warn("Ignoring error reading last modified time for {}", uri, ex);
      return 0L;
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
    return "PathJavaFileObject{"
        + "location=" + StringUtils.quoted(location.getName()) + ", "
        + "uri=" + StringUtils.quoted(uri) + ", "
        + "kind=" + kind
        + "}";
  }

  private static CharsetDecoder decoder(boolean ignoreEncodingErrors) {
    var action = ignoreEncodingErrors
        ? CodingErrorAction.IGNORE
        : CodingErrorAction.REPORT;

    return charset()
        .newDecoder()
        .onUnmappableCharacter(action)
        .onMalformedInput(action);
  }

  private static CharsetEncoder encoder() {
    return charset()
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
  }

  private static Charset charset() {
    return StandardCharsets.UTF_8;
  }
}
