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
package io.github.ascopes.jct.pathwrappers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.ToStringBuilder;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around an existing {@link Path Java NIO Path} that makes it compatible with the
 * {@link PathWrapper} API.
 *
 * <p>This is not designed to be used by users. The API will handle wrapping paths internally for
 * you. You may be interested in using {@link RamDirectory}, however.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class BasicPathWrapperImpl implements PathWrapper {

  private final @Nullable PathWrapper parent;
  private final Path path;
  private final URI uri;
  private final URL url;

  /**
   * Initialize this path wrapper from a given path.
   *
   * @param path the NIO path to wrap.
   * @throws IllegalArgumentException if the path does not support dereferencing URLs (i.e. no URL
   *                                  protocol handler is registered for the associated
   *                                  {@link java.nio.file.FileSystem} providing the path).
   */
  public BasicPathWrapperImpl(Path path) {
    this(null, path);
  }

  /**
   * Initialize this path wrapper from another path wrapper and a relative path to represent.
   *
   * @param parent the outer path-wrapper to use.
   * @param parts  the relative parts to resolve.
   * @throws IllegalArgumentException if the path does not support dereferencing URLs (i.e. no URL
   *                                  protocol handler is registered for the associated
   *                                  {@link java.nio.file.FileSystem} providing the path).
   */
  public BasicPathWrapperImpl(PathWrapper parent, String... parts) {
    this(parent, resolveRecursively(parent.getPath(), parts));
  }

  private BasicPathWrapperImpl(@Nullable PathWrapper parent, Path path) {
    this.parent = parent;
    this.path = requireNonNull(path, "path");
    uri = path.toUri();
    url = PathWrapperUtils.retrieveRequiredUrl(this.path);
  }

  @Nullable
  @Override
  public PathWrapper getParent() {
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
  public boolean equals(Object other) {
    if (!(other instanceof PathWrapper)) {
      return false;
    }

    var that = (PathWrapper) other;

    return uri.equals(that.getUri());
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("parent", parent)
        .attribute("uri", uri)
        .toString();
  }

  private static Path resolveRecursively(Path root, String... parts) {
    for (var part : parts) {
      root = root.resolve(part);
    }

    return root;
  }
}
