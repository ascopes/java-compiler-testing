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

package io.github.ascopes.jct.paths.v2;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.FileUtils;
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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
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
  private static final long NOT_MODIFIED = 0L;

  private final Location location;
  private final Path path;
  private final String name;
  private final URI uri;
  private final Kind kind;

  /**
   * Initialize this file object.
   *
   * @param path the path to point to.
   */
  public PathFileObject(Location location, Path path) {
    this.location = requireNonNull(location, "location");
    this.path = requireNonNull(path, "path");
    name = path.toString();
    uri = path.toUri();
    kind = FileUtils.pathToKind(path);
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
  public Modifier getAccessLevel() {
    // Null implies that the access level is unknown.
    return null;
  }

  @Override
  public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return decoder(ignoreEncodingErrors)
        .decode(ByteBuffer.wrap(Files.readAllBytes(path)))
        .toString();
  }

  @Override
  public Kind getKind() {
    return kind;
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

  /**
   * Get the location of this path file object.
   *
   * @return the location.
   */
  public Location getLocation() {
    return location;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public NestingKind getNestingKind() {
    // Null implies that the nesting kind is unknown.
    return null;
  }

  /**
   * Get the path of this file object.
   *
   * @return the path of this file object.
   */
  public Path getPath() {
    return path;
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    return simpleName.endsWith(kind.extension);
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

    return StandardCharsets.UTF_8
        .newDecoder()
        .onUnmappableCharacter(action)
        .onMalformedInput(action);
  }

  private CharsetEncoder encoder() {
    return StandardCharsets.UTF_8
        .newEncoder()
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .onMalformedInput(CodingErrorAction.REPORT);
  }

  public static Function<? super Path, ? extends PathFileObject> forLocation(Location location) {
    return path -> new PathFileObject(location, path);
  }
}
