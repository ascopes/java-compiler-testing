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

package com.github.ascopes.jct.assertions;

import com.github.ascopes.jct.paths.PathLocationRepository;
import org.assertj.core.api.AbstractObjectAssert;

/**
 * Assertions to perform on files within a {@link PathLocationRepository}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class PathLocationRepositoryAssertions
    extends AbstractObjectAssert<PathLocationRepositoryAssertions, PathLocationRepository> {

  /**
   * Initialize these assertions.
   *
   * @param pathLocationRepository the repository of paths to assert on.
   */
  private PathLocationRepositoryAssertions(PathLocationRepository pathLocationRepository) {
    super(pathLocationRepository, PathLocationRepositoryAssertions.class);
  }

  /**
   * Create a new set of assertions for a path repository.
   *
   * @param pathLocationRepository the repository of paths to assert on.
   * @return the assertions.
   */
  public static PathLocationRepositoryAssertions assertThat(
      PathLocationRepository pathLocationRepository
  ) {
    return new PathLocationRepositoryAssertions(pathLocationRepository);
  }
}
