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

import io.github.ascopes.jct.utils.ToStringBuilder;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A wrapper around a {@link Path Java NIO Path} that makes it compatible with {@link PathLike}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class NioPath implements PathLike {

  private final Path path;
  private final URI uri;

  /**
   * Initialize this path.
   *
   * @param path the NIO path to wrap.
   */
  public NioPath(Path path) {
    this.path = requireNonNull(path, "path");
    uri = path.toUri();
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
  public boolean equals(Object other) {
    if (!(other instanceof NioPath)) {
      return false;
    }

    var that = (NioPath) other;

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
