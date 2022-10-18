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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.assertions.helpers.LocationRepresentation;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import java.nio.file.Path;
import java.util.stream.Collectors;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractPathAssert;
import org.assertj.core.api.PathAssert;

/**
 * Assertions for package container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class PackageContainerGroupAssert
    extends AbstractContainerGroupAssert<PackageContainerGroupAssert, PackageContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   */
  public PackageContainerGroupAssert(PackageContainerGroup containerGroup) {
    super(containerGroup, PackageContainerGroupAssert.class);
  }

  /**
   * Assert that the given file does not exist.
   *
   * @param path the relative path to look for.
   * @return this assertion object for further assertions.
   * @throws AssertionError if the file exists.
   */
  public PackageContainerGroupAssert withoutFile(String path) {
    var file = actual.findFile(path);

    if (file == null) {
      return this;
    }

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    throw failure(
        "Expected path %s to not exist in %s but it was found at %s",
        path,
        locationName,
        file
    );
  }

  /**
   * Assert that the given file exists.
   *
   * @param path the relative path to look for.
   * @return assertions to perform on the path of the file that exists.
   * @throws AssertionError if the file does not exist.
   */
  public AbstractPathAssert<?> withFile(String path) {
    var file = actual.findFile(path);

    if (file != null) {
      return new PathAssert(file);
    }

    var closestMatches = FuzzySearch
        .extractSorted(
            path,
            actual
                .getPackages()
                .stream()
                .flatMap(container -> uncheckedIo(() -> container
                    .listAllFiles()
                    .stream()
                    .map(container.getPathWrapper().getPath()::relativize)
                ))
                .collect(Collectors.toList()),
            Path::toString,
            FUZZY_CUTOFF
        )
        .stream()
        .filter(it -> it.getScore() >= FUZZY_MIN_SCORE)
        .map(BoundExtractedResult::getReferent)
        .collect(Collectors.toList());

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    if (closestMatches.isEmpty()) {
      throw failure("No file in %s files found named %s", locationName, path);
    } else {
      throw failure(
          "No file named \"%s\" found in %s. Did you mean...%s",
          path,
          locationName,
          closestMatches
              .stream()
              .map(Path::toString)
              .map("\n\t - "::concat)
              .collect(Collectors.joining())
      );
    }
  }
}
