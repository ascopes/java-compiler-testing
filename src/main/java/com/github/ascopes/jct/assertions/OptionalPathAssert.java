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

import com.github.ascopes.jct.paths.PathLocationManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.PathAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Assertions on a path that we have requested for a given location that may not actually exist.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class OptionalPathAssert extends AbstractAssert<OptionalPathAssert, Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OptionalPathAssert.class);
  private final PathLocationManager manager;
  private final String providedPath;

  private OptionalPathAssert(
      PathLocationManager manager,
      String providedPath,
      Path resolvedPath
  ) {
    super(resolvedPath, OptionalPathAssert.class);
    this.manager = Objects.requireNonNull(manager);
    this.providedPath = Objects.requireNonNull(providedPath);
  }

  /**
   * Assert that the file exists.
   *
   * @return assertions to perform on the path itself.
   */
  public PathAssert exists() {
    if (actual == null) {
      var similarMatches = findSimilarlyNamedPaths();

      if (similarMatches.isEmpty()) {
        throw failure(
            "Path %s was not found in any of the roots for location %s",
            providedPath,
            manager.getLocation().getName()
        );
      } else {
        var names = similarMatches
            .stream()
            .map(Path::toString)
            .map("        - "::concat)
            .collect(Collectors.joining("\n"));

        throw failure(
            "Path %s was not found in any of the roots for location %s.\n"
            + "    Maybe you meant:\n%s",
            providedPath,
            manager.getLocation().getName(),
            names
        );
      }
    }

    return new PathAssert(actual);
  }

  /**
   * Assert that the file does not exist.
   *
   * @return this object for further call chaining.
   */
  public OptionalPathAssert doesNotExist() {
    if (actual != null) {
      throw failure(
          "Expected path %s to not exist but it was found in location %s as %s",
          providedPath,
          manager.getLocation().getName(),
          actual
      );
    }

    return this;
  }

  private Collection<Path> findSimilarlyNamedPaths() {
    var files = new LinkedHashSet<Path>();
    for (var root : manager.getPaths()) {
      try {
        Files
            .walk(root)
            .filter(Files::isRegularFile)
            .map(root::relativize)
            .forEach(files::add);
      } catch (IOException ex) {
        LOGGER.error(
            "Failed to walk file root {} in location {}",
            root,
            manager.getLocation(),
            ex
        );
      }
    }

    return FuzzySearch
        .extractAll(providedPath, files, Path::toString, 5)
        .stream()
        .filter(result -> result.getScore() > 80)
        .map(BoundExtractedResult::getReferent)
        .collect(Collectors.toList());
  }

  /**
   * Create a new set of assertions for a potential path.
   *
   * @param manager      the location manager to find the path in.
   * @param providedPath the provided path we were requested to resolve.
   * @param resolvedPath the resolved path, or {@code null} if not found.
   * @return the assertions.
   */
  public static OptionalPathAssert assertThat(
      PathLocationManager manager,
      String providedPath,
      Path resolvedPath
  ) {
    return new OptionalPathAssert(manager, providedPath, resolvedPath);
  }
}
