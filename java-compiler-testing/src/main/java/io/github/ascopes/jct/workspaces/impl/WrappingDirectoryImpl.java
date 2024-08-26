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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.utils.FileUtils.resolvePathRecursively;
import static io.github.ascopes.jct.utils.FileUtils.retrieveRequiredUrl;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * A wrapper around an existing {@link Path Java NIO Path} that makes it compatible with the
 * {@link PathRoot} API.
 *
 * <p>This is not designed to be used by users. The API will handle wrapping paths internally for
 * you. You may be interested in using {@link RamDirectoryImpl}, however.
 *
 * @author Ashley Scopes
 * @see RamDirectoryImpl
 * @see TempDirectoryImpl
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class WrappingDirectoryImpl implements PathRoot {

  private final @Nullable PathRoot parent;
  private final Path path;
  private final URI uri;
  private final URL url;

  /**
   * Initialize this path root from a given path.
   *
   * @param path the NIO path to wrap.
   * @throws IllegalArgumentException if the path does not support de-referencing URLs (i.e. no URL
   *                                  protocol handler is registered for the associated
   *                                  {@link java.nio.file.FileSystem} providing the path).
   */
  public WrappingDirectoryImpl(Path path) {
    this(null, path);
  }

  /**
   * Initialize this path root from another path root and a relative path to represent.
   *
   * @param parent the outer path-wrapper to use.
   * @param parts  the relative parts to resolve.
   * @throws IllegalArgumentException if the path does not support de-referencing URLs (i.e. no URL
   *                                  protocol handler is registered for the associated
   *                                  {@link java.nio.file.FileSystem} providing the path).
   */
  public WrappingDirectoryImpl(PathRoot parent, List<String> parts) {
    this(
        requireNonNull(parent, "parent"),
        resolvePathRecursively(parent.getPath(), requireNonNullValues(parts, "parts"))
    );
  }

  private WrappingDirectoryImpl(@Nullable PathRoot parent, Path path) {
    this.parent = parent;
    this.path = requireNonNull(path, "path");
    uri = path.toUri();
    url = retrieveRequiredUrl(this.path);
  }

  /**
   * {@inheritDoc}
   *
   * @return the bytes that make up the JAR that was created from this wrapped directory.
   */
  @Override
  public byte[] asJar() {
    return uncheckedIo(() -> {
      try (var outputStream = new ByteArrayOutputStream()) {
        JarFactoryImpl.getInstance().createJarFrom(outputStream, getPath());
        return outputStream.toByteArray();
      }
    });
  }

  @Nullable
  @Override
  public PathRoot getParent() {
    return parent;
  }

  @Override
  public Path getPath() {
    return path;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public URL getUrl() {
    return url;
  }

  @Override
  public boolean equals(@Nullable Object that) {
    return that instanceof WrappingDirectoryImpl
        && ((WrappingDirectoryImpl) that).getUri().equals(uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("parent", parent)
        .attribute("uri", uri)
        .toString();
  }
}
