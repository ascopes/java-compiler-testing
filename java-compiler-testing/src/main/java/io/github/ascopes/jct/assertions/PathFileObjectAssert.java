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
package io.github.ascopes.jct.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.filemanagers.PathFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractPathAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for {@link PathFileObject Path file objects}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class PathFileObjectAssert
    extends AbstractJavaFileObjectAssert<PathFileObjectAssert, PathFileObject> {

  /**
   * Create a new instance of this assertion object.
   *
   * @param actual the path file object to assert upon.
   */
  public PathFileObjectAssert(@Nullable PathFileObject actual) {
    super(actual, PathFileObjectAssert.class);
  }

  /**
   * Perform an assertion on the file object's full path.
   *
   * @return the assertions for the path.
   * @throws AssertionError if the file object is null.
   */
  public AbstractPathAssert<?> relativePath() {
    isNotNull();

    return assertThat(actual.getRelativePath());
  }

  /**
   * Perform an assertion on the file object's absolute path.
   *
   * @return the assertions for the path.
   * @throws AssertionError if the file object is null.
   * @deprecated use {@link #absolutePath()} instead.
   */
  @Deprecated(forRemoval = true, since = "0.7.3")
  @SuppressWarnings("removal")
  public AbstractPathAssert<?> fullPath() {
    isNotNull();

    return assertThat(actual.getFullPath());
  }

  /**
   * Perform an assertion on the file object's absolute path.
   *
   * @return the assertions for the path.
   * @throws AssertionError if the file object is null.
   */
  public AbstractPathAssert<?> absolutePath() {
    isNotNull();

    return assertThat(actual.getAbsolutePath());
  }
}
