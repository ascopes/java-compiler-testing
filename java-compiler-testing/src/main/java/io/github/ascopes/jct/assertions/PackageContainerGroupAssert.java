/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.repr.LocationRepresentation;
import io.github.ascopes.jct.utils.IterableUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractPathAssert;

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
   * @param fragment  the first part of the path.
   * @param fragments additional parts of the path.
   * @return this assertion object for further assertions.
   * @throws AssertionError if the file exists.
   */
  public PackageContainerGroupAssert fileDoesNotExist(String fragment, String... fragments) {
    var expectedFile = fragmentPathToString(fragment, fragments);
    var actualFile = actual.getFile(fragment, fragments);

    if (actualFile == null) {
      return this;
    }

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    throw failure(
        "Expected path \"%s\" to not exist in \"%s\" but it was found at \"%s\"",
        expectedFile,
        locationName,
        actualFile
    );
  }

  /**
   * Assert that all given files exist.
   *
   * @param path  the first path to check for
   * @param paths additional paths to check for.
   * @throws AssertionError if any of the files do not exist.
   */
  public void allFilesExist(String path, String... paths) {
    allFilesExist(IterableUtils.combineOneOrMore(path, paths));
  }

  /**
   * Assert that all given files exist.
   *
   * @param paths paths to check for.
   * @throws AssertionError if any of the files do not exist.
   */
  public void allFilesExist(Iterable<String> paths) {
    assertThat(paths).allSatisfy(this::fileExists);
  }

  /**
   * Get assertions to perform on the class loader associated with this container group.
   *
   * @return the assertions to perform.
   */
  public ClassLoaderAssert classLoader() {
    return new ClassLoaderAssert(actual.getClassLoader());
  }

  /**
   * Assert that the given file exists.
   *
   * @param fragment  the path fragment.
   * @param fragments any additional path fragments.
   * @return assertions to perform on the path of the file that exists.
   * @throws AssertionError if the file does not exist.
   */
  public AbstractPathAssert<?> fileExists(String fragment, String... fragments) {
    var expectedFile = fragmentPathToString(fragment, fragments);
    var actualFile = actual.getFile(fragment, fragments);

    if (actualFile != null) {
      return assertThat(actualFile);
    }

    var closestMatches = FuzzySearch
        .extractSorted(
            expectedFile,
            actual
                .getPackages()
                .stream()
                .flatMap(container -> uncheckedIo(() -> container
                    .listAllFiles()
                    .stream()
                    .filter(not(Files::isDirectory))
                    .map(container.getPathRoot().getPath()::relativize)
                ))
                .collect(Collectors.toList()),
            Path::toString
        )
        .stream()
        .limit(FUZZY_CUTOFF)
        .map(BoundExtractedResult::getReferent)
        .sorted()
        .collect(Collectors.toList());

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    if (closestMatches.isEmpty()) {
      throw failure("No file in %s files found named \"%s\"", locationName, expectedFile);
    } else {
      throw failure(
          "No file named \"%s\" found in %s. Did you mean...%s",
          expectedFile,
          locationName,
          closestMatches
              .stream()
              .map(Path::toString)
              .map("\n\t - "::concat)
              .collect(Collectors.joining())
      );
    }
  }

  private static String fragmentPathToString(String fragment, String... fragments) {
    // Path#toString uses the default separator if we use Path#of
    return String.join(File.separator, IterableUtils.combineOneOrMore(fragment, fragments));
  }
}
