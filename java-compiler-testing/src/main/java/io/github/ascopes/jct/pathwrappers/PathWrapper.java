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

import io.github.ascopes.jct.annotations.Nullable;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A path-like object that can provide a {@link Path Java NIO Path}.
 *
 * <p>Not only does this enable us to have different types of path with varying behaviour, but
 * we can also use this to enforce that other references related to the internal path are kept alive
 * for as long as the path-like object itself is kept alive.
 *
 * <p>This becomes very useful for {@link TemporaryFileSystem}, which keeps a RAM-based
 * {@link FileSystem} alive until it is garbage collected, or the
 * {@link TemporaryFileSystem#close()} operation is called. The mechanism enables cleaning up of
 * resources implicitly without resource-tidying logic polluting the user's test cases.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface PathWrapper {

  @Override
  boolean equals(@Nullable Object other);

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
   * Get the parent path wrapper, if there is one.
   *
   * @return the parent path wrapper, or {@code null} if no parent wrapper exists.
   */
  @Nullable
  PathWrapper getParent();
}
