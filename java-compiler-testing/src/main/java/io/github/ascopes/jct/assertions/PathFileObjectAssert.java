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
package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.compilers.PathFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.PathAssert;

/**
 * Assertions for {@link PathFileObject Path file objects}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class PathFileObjectAssert
    extends AbstractJavaFileObjectAssert<PathFileObjectAssert, PathFileObject> {

  /**
   * Create a new instance of this assertion object.
   *
   * @param actual the path file object to assert upon.
   */
  public PathFileObjectAssert(PathFileObject actual) {
    super(actual, PathFileObjectAssert.class);
  }

  /**
   * Perform an assertion on the file object's full path.
   *
   * @return the assertions for the path.
   */
  public PathAssert relativePath() {
    return new PathAssert(actual.getRelativePath());
  }

  /**
   * Perform an assertion on the file object's full path.
   *
   * @return the assertions for the path.
   */
  public PathAssert fullPath() {
    return new PathAssert(actual.getFullPath());
  }
}
