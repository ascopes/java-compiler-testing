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
package io.github.ascopes.jct.workspaces;

import io.github.ascopes.jct.workspaces.impl.RamDirectory;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A path-like object that can provide a {@link Path Java NIO Path}.
 *
 * <p>Not only does this enable us to have different types of path with varying behaviour, but
 * we can also use this to enforce that other references related to the internal path are kept alive
 * for as long as the path-like object itself is kept alive.
 *
 * <p>This becomes very useful for {@link RamDirectory}, which keeps a RAM-based
 * {@link FileSystem} alive until it is garbage collected, or the {@link RamDirectory#close()}
 * operation is called. The mechanism enables cleaning up of resources implicitly without
 * resource-tidying logic polluting the user's test cases.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
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
   */
  Path getPath();

  /**
   * Get a URI representation of this path-like object.
   *
   * @return the URI.
   */
  URI getUri();

  /**
   * Get a URL representation of this path-like object.
   *
   * @return the URL.
   */
  URL getUrl();

  /**
   * Get the parent path root, if there is one.
   *
   * @return the parent path root, or {@code null} if no parent root exists.
   */
  @Nullable
  PathRoot getParent();
}
