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
