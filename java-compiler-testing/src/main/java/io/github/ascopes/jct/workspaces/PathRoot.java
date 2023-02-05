/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.workspaces;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * A path-like object that can provide a {@link Path Java NIO Path}.
 *
 * <p>This enables wrapping various implementations and providers of {@link Path} objects
 * in a translucent façade that enables representing paths in a hierarchical format.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface PathRoot {

  /**
   * Determine if two path roots are equivalent. If the provided object is {@code null} or not an
   * instance of a {@link PathRoot}, then this will return {@code false} unless otherwise
   * specified.
   *
   * @param other the object to compare with.
   * @return {@code true} if semantically equal, or {@code false} otherwise.
   */
  @Override
  boolean equals(@Nullable Object other);

  /**
   * Determine the hash-code for the object.
   *
   * @return the hash code.
   */
  @Override
  int hashCode();

  /**
   * Get the {@link Path Java NIO Path} for this path-like object.
   *
   * @return the path.
   * @see #getUri()
   * @see #getUrl()
   */
  Path getPath();

  /**
   * Get a URI representation of this path-like object.
   *
   * @return the URI.
   * @see #getUrl()
   * @see #getPath()
   */
  URI getUri();

  /**
   * Get a URL representation of this path-like object.
   *
   * @return the URL.
   * @see #getUri()
   * @see #getPath()
   */
  URL getUrl();

  /**
   * Get the parent path root, if there is one.
   *
   * @return the parent path root, or {@code null} if no parent root exists.
   */
  PathRoot getParent();
}
