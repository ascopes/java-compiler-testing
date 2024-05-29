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
package io.github.ascopes.jct.filemanagers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
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

/**
 * Interface for a path-based {@link JavaFileObject} that points to a path on a file system
 * somewhere.
 *
 * <p>This object will always use UTF-8 encoding when obtaining readers or writers.
 *
 * <p>No access level or nesting kind information is provided by this implementation.
 *
 * @author Ashley Scopes
 * @since 1.0.0
 */
@API(since = "1.0.0", status = Status.STABLE)
public interface PathFileObject extends JavaFileObject {

  /**
   * Timestamp that is returned if the file object has never been modified.
   */
  long NOT_MODIFIED = 0L;

  /**
   * Attempt to delete the file.
   *
   * @return {@code true} if the file was deleted, or {@code false} if it was not deleted.
   */
  @Override
  boolean delete();

  /**
   * Determine if this object equals another.
   *
   * @param other the other object to compare with.
   * @return {@code true} if the other object is also a {@link FileObject} and has the same
   *     {@link #toUri() URI} as this object, or {@code false} otherwise.
   */
  @Override
  boolean equals(@Nullable Object other);

  /**
   * Get the absolute path of this file object.
   *
   * @return the full path.
   */
  Path getAbsolutePath();

  /**
   * Get the class access level, where appropriate.
   *
   * <p>In most implementations, this class will always return {@code null}, since this
   * information is not readily available without preloading the file in question and parsing it
   * first.
   *
   * <p>At the time of writing, the OpenJDK implementations of the JavaFileObject class
   * do not provide an implementation for this method either.
   *
   * @return the modifier for the access level, or {@code null} if the information is not available.
   */
  @Nullable
  @Override
  default Modifier getAccessLevel() {
    return null;
  }

  /**
   * Get the inferred binary name of the file object.
   *
   * @return the inferred binary name.
   */
  String getBinaryName();

  /**
   * Read the character content of the file into memory and decode it using the default character
   * set.
   *
   * @param ignoreEncodingErrors ignore encoding errors if {@code true}, or throw them otherwise.
   * @return the character content, encoded as UTF-8.
   * @throws IOException if an IO error occurs.
   */
  @Override
  String getCharContent(boolean ignoreEncodingErrors) throws IOException;

  /**
   * Get the kind of the file.
   *
   * @return the inferred file kind.
   */
  @Override
  Kind getKind();

  /**
   * Determine when the file was last modified.
   *
   * @return the timestamp in milliseconds since UNIX epoch that the file was last modified at, or
   *     {@link #NOT_MODIFIED} if unmodified, if the information is not available, or if an error
   *     occurred obtaining the information.
   */
  @Override
  long getLastModified();

  /**
   * Get the location of this path file object.
   *
   * @return the location.
   */
  Location getLocation();

  /**
   * Get the file name as a string.
   *
   * @return the name of the file.
   */
  @Override
  String getName();

  /**
   * Determine the class nesting kind, where appropriate.
   *
   * <p>In most implementations, this method will always return {@code null}, since this
   * information is not readily available without preloading the file in question and parsing it
   * first.
   *
   * <p>At the time of writing, the OpenJDK implementations of the JavaFileObject class
   * do not provide an implementation for this method either.
   *
   * @return the nesting kind, or {@code null} if the information is not available.
   */
  @Nullable
  @Override
  default NestingKind getNestingKind() {
    return null;
  }

  /**
   * Get the relative path of this file object.
   *
   * @return the path of this file object.
   */
  Path getRelativePath();

  /**
   * Get the root path that the package containing this file is nested within.
   *
   * @return the root path.
   */
  Path getRootPath();

  /**
   * Determine the hash code for this object.
   *
   * @return the hash code for the object.
   */
  @Override
  int hashCode();

  /**
   * Determine if a given simple name and file kind are compatible with this file object.
   *
   * <p>This will perform a case-sensitive check, regardless of the platform that it runs on.
   *
   * @param simpleName the simple name of the class to compare to this file.
   * @param kind       the kind of the class to compare to this file.
   * @return {@code true} if the simple name and kind are compatible with the current file object
   *     name, or {@code false} if not.
   */
  @Override
  boolean isNameCompatible(String simpleName, Kind kind);

  /**
   * Open an input stream into this file.
   *
   * <p>This input stream must be closed once finished with, otherwise resources will be leaked.
   *
   * <p>The returned implementation will always be buffered when appropriate.
   *
   * @return an input stream.
   * @throws NoSuchFileException if the file does not exist.
   * @throws IOException         if an IO error occurs.
   */
  @Override
  InputStream openInputStream() throws IOException;

  /**
   * Open an output stream to this file.
   *
   * <p>This will create the file first if it does not already exist. If it does exist, then this
   * will overwrite the file and truncate it. The parent directories will also be created if they do
   * not exist.
   *
   * <p>This output stream must be closed once finished with, otherwise resources will be leaked.
   *
   * <p>The returned implementation will always be buffered when appropriate.
   *
   * @return an output stream.
   * @throws IOException if an IO error occurs.
   */
  @Override
  OutputStream openOutputStream() throws IOException;

  /**
   * Open a reader to this file using the default charset (UTF-8).
   *
   * <p>This reader must be closed once finished with, otherwise resources will be leaked.
   *
   * <p>The returned implementation will always be buffered when appropriate.
   *
   * @param ignoreEncodingErrors {@code true} to suppress encoding errors, or {@code false} to throw
   *                             them to the caller.
   * @return a reader.
   * @throws NoSuchFileException if the file does not exist.
   * @throws IOException         if an IO error occurs.
   */
  @Override
  Reader openReader(boolean ignoreEncodingErrors) throws IOException;

  /**
   * Open a writer to this file using the default charset (UTF-8).
   *
   * <p>This will create the file first if it does not already exist. If it does exist, this will
   * first overwrite the file and truncate it. The parent directories will also be created if they
   * do not exist.
   *
   * <p>This input stream must be closed once finished with, otherwise resources will be leaked.
   *
   * <p>The returned implementation will always be buffered when appropriate.
   *
   * @return a writer.
   * @throws IOException if an IO error occurs.
   */
  @Override
  Writer openWriter() throws IOException;

  /**
   * Get a string representation of this object.
   *
   * @return a string representation of this object.
   */
  @Override
  String toString();

  /**
   * Determine the URI for this file.
   *
   * @return the URI for this file object.
   */
  @Override
  URI toUri();
}
