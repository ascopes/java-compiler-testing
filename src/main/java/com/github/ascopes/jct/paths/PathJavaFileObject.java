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

/**
 * File object that can be used with paths.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class PathJavaFileObject implements JavaFileObject {

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

  /**
   * {@inheritDoc}
   *
   * @return the kind.
   */
  @Override
  public Kind getKind() {
    return kind;
  }

  /**
   * {@inheritDoc}
   *
   * @param simpleName the simple name.
   * @param kind       the kind.
   * @return {@code true} if the name is compatible, {@code false} otherwise.
   */
  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    return path.getFileName().toString().endsWith(simpleName + kind.extension);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code null}.
   */
  @Override
  public NestingKind getNestingKind() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code null}.
   */
  @Override
  public Modifier getAccessLevel() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @return the URI.
   */
  @Override
  public URI toUri() {
    return uri;
  }

  /**
   * {@inheritDoc}
   *
   * @return the name of the object.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * @return the input stream.
   * @throws IOException if an IO error occurs.
   */
  @Override
  public InputStream openInputStream() throws IOException {
    return new BufferedInputStream(Files.newInputStream(path));
  }

  /**
   * {@inheritDoc}
   *
   * @return the output stream.
   * @throws IOException if an IO error occurs.
   */
  @Override
  public OutputStream openOutputStream() throws IOException {
    // Ensure parent directories exist first.
    Files.createDirectories(path.getParent());
    return new BufferedOutputStream(Files.newOutputStream(path));
  }

  /**
   * {@inheritDoc}
   *
   * @param ignoreEncodingErrors {@code true} to ignore encoding errors, {@code false} to report
   *                             them using exceptions.
   * @return the reader.
   * @throws IOException if the reader cannot be opened.
   */
  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new InputStreamReader(openInputStream(), decoder(ignoreEncodingErrors));
  }

  /**
   * {@inheritDoc}
   *
   * @param ignoreEncodingErrors {@code true} to ignore encoding errors, {@code false} to report
   *                             them using exceptions.
   * @return the text content of the file.
   * @throws IOException if the reader cannot be opened, or if an encoding error occurs when {@code
   *                     ignoreEncodingErrors} is set to {@code false}.
   */
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return decoder(ignoreEncodingErrors)
        .decode(ByteBuffer.wrap(Files.readAllBytes(path)))
        .toString();
  }

  /**
   * {@inheritDoc}
   *
   * @return the writer.
   * @throws IOException if the writer cannot be opened.
   */
  @Override
  public Writer openWriter() throws IOException {
    return new OutputStreamWriter(openOutputStream(), encoder());
  }

  /**
   * {@inheritDoc}
   *
   * @return the UNIX timestamp of the last-modified time of the file in milliseconds, or {@code 0}
   * if unavailable or not supported.
   */
  @Override
  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException ex) {
      return 0L;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code true} if the file was successfully deleted, or {@code false} if the file failed
   * to be deleted or was not found on the file system.
   */
  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(path);
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return the string representation of this object.
   */
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
