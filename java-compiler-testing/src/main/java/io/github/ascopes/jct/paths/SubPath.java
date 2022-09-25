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
package io.github.ascopes.jct.paths;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.IterableUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around an existing {@link PathLike} which contains a path to some sub-location in the
 * original path.
 *
 * <p>This mechanism enables keeping the original path-like reference alive, which
 * enables handling {@link RamPath} garbage collection correctly.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class SubPath implements PathLike {

  private final PathLike parent;
  private final Path root;
  private final URI uri;

  /**
   * Initialize this path.
   *
   * @param path         the path-like path to wrap.
   * @param subPathParts the parts of the subpath to point to.
   */
  public SubPath(PathLike path, String... subPathParts) {
    parent = requireNonNull(path, "path");

    var root = path.getPath();
    for (var subPathPart : IterableUtils.requireNonNullValues(subPathParts, "subPathParts")) {
      root = root.resolve(subPathPart);
    }
    this.root = root;
    uri = root.toUri();
  }

  /**
   * Get the parent path that this path was derived from.
   *
   * @return the parent path.
   */
  public PathLike getParent() {
    return parent;
  }

  @Override
  public Path getPath() {
    return root;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof SubPath)) {
      return false;
    }

    var that = (SubPath) other;

    return uri.equals(that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("uri", uri)
        .toString();
  }
}
