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

package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.paths.PathLocationManager;
import java.nio.file.Path;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.FactoryBasedNavigableIterableAssert;
import org.assertj.core.api.PathAssert;

/**
 * Assertions to perform on files within a {@link PathLocationManager}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
//@formatter:off
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class PathLocationManagerAssert
    extends FactoryBasedNavigableIterableAssert<
        PathLocationManagerAssert,
        PathLocationManager,
        Path,
        PathAssert
    > {
  //@formatter:on

  private PathLocationManagerAssert(PathLocationManager pathLocationManager) {
    super(pathLocationManager, PathLocationManagerAssert.class, PathAssert::new);
  }

  /**
   * Perform an assertion on the first instance of the given file name that exists.
   *
   * <p>If no file is found, the assertion will apply to a null value.
   *
   * @param path the path to look for.
   * @return an assertion for the path of the matching file.
   */
  public OptionalPathAssert file(String path) {
    var actualFile = actual
        .findFile(path)
        .orElse(null);

    return OptionalPathAssert.assertThatPath(actual, path, actualFile);
  }

  /**
   * Create a new set of assertions for a path location manager.
   *
   * @param pathLocationManager the manager of paths to assert on for a specific location.
   * @return the assertions.
   */
  public static PathLocationManagerAssert assertThatLocation(
      PathLocationManager pathLocationManager
  ) {
    return new PathLocationManagerAssert(pathLocationManager);
  }
}
