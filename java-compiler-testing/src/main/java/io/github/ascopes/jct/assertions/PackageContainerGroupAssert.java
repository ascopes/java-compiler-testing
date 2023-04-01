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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static io.github.ascopes.jct.utils.IterableUtils.combineOneOrMore;
import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.repr.LocationRepresentation;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractPathAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for package container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class PackageContainerGroupAssert
    extends AbstractContainerGroupAssert<PackageContainerGroupAssert, PackageContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   */
  public PackageContainerGroupAssert(@Nullable PackageContainerGroup containerGroup) {
    super(containerGroup, PackageContainerGroupAssert.class);
  }

  /**
   * Assert that all given files exist.
   *
   * @param path  the first path to check for
   * @param paths additional paths to check for.
   * @return this object for further call chaining.
   * @throws AssertionError       if the container group is null, or if any of the files do not
   *                              exist.
   * @throws NullPointerException if any of the paths are null.
   */
  public PackageContainerGroupAssert allFilesExist(String path, String... paths) {
    requireNonNull(path, "path must not be null");
    requireNonNullValues(paths, "paths");

    return allFilesExist(combineOneOrMore(path, paths));
  }

  /**
   * Assert that all given files exist.
   *
   * @param paths paths to check for.
   * @return this object for further call chaining.
   * @throws AssertionError       if the container group is null, or if any of the files do not
   *                              exist.
   * @throws NullPointerException if any of the paths are null.
   */
  public PackageContainerGroupAssert allFilesExist(Iterable<String> paths) {
    requireNonNullValues(paths, "paths");

    isNotNull();

    assertThat(paths).allSatisfy(this::fileExists);
    return this;
  }

  /**
   * Get assertions to perform on the class loader associated with this container group.
   *
   * @return the assertions to perform.
   * @throws AssertionError if the container group is null.
   */
  public ClassLoaderAssert classLoader() {
    isNotNull();

    return new ClassLoaderAssert(actual.getClassLoader());
  }

  /**
   * Assert that the given file does not exist.
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   assertions.fileDoesNotExist("foo/bar/baz.txt")...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   assertions.fileDoesNotExist("foo", "bar", "baz.txt");
   * </code></pre>
   *
   * @param fragment  the first part of the path.
   * @param fragments any additional parts of the path.
   * @return this assertion object for further assertions.
   * @throws AssertionError       if the file exists, or if the container group is null.
   * @throws NullPointerException if any of the fragments are null.
   */
  public PackageContainerGroupAssert fileDoesNotExist(String fragment, String... fragments) {
    requireNonNull(fragment, "fragment must not be null");
    requireNonNullValues(fragments, "fragments");

    isNotNull();

    var actualFile = actual.getFile(fragment, fragments);

    if (actualFile == null) {
      return this;
    }

    throw failure(
        "Expected path \"%s\" to not exist in \"%s\" but it was found at \"%s\"",
        userProvidedPath(fragment, fragments),
        LocationRepresentation.getInstance().toStringOf(actual.getLocation()),
        actualFile
    );
  }

  /**
   * Assert that the given file exists.
   *
   * <pre><code>
   *   // Using platform-specific separators.
   *   assertions.fileExists("foo/bar/baz.txt")...;
   *
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   assertions.fileExists("foo", "bar", "baz.txt");
   * </code></pre>
   *
   * <p>If the file does not exist, then this object will attempt to find the
   * closest matches and list them in an error message along with the assertion
   * error.
   *
   * @param fragment  the first part of the path.
   * @param fragments any additional parts of the path.
   * @return assertions to perform on the path of the file that exists.
   * @throws AssertionError       if the file does not exist, or if the container group is null.
   * @throws NullPointerException if any of the fragments are null.
   */

  public AbstractPathAssert<?> fileExists(String fragment, String... fragments) {
    requireNonNull(fragment, "fragment must not be null");
    requireNonNullValues(fragments, "fragments");

    isNotNull();

    var actualFile = actual.getFile(fragment, fragments);

    if (actualFile != null) {
      // File exists with this path. Hooray, lets return assertions on it.
      return assertThat(actualFile);
    }

    throw createNoFileFoundFuzzyError(fragment, fragments);
  }

  private AssertionError createNoFileFoundFuzzyError(String fragment, String... fragments) {
    var userInput = userProvidedPath(fragment, fragments);

    var matchesString = uncheckedIo(actual::listAllFiles)
        .entrySet()
        .stream()
        .flatMap(entry -> findFuzzyMatchesForContainer(
            fuzzySafePath(combineOneOrMore(fragment, fragments)),
            entry.getKey().getInnerPathRoot().getPath(),
            entry.getValue()
        ))
        .map(Path::toString)
        .map("\n  - "::concat)
        .collect(Collectors.joining());

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    if (matchesString.isBlank()) {
      return failure(
          "No file named \"%s\" found in %s. No similar results found.",
          userInput,
          locationName
      );
    }

    return failure(
        "No file named \"%s\" found in %s. Found similar results:\n%s",
        userInput,
        locationName,
        matchesString
    );
  }

  private Stream<Path> findFuzzyMatchesForContainer(
      String query,
      Path rootPath,
      Collection<Path> files
  ) {
    var relativeFiles = files.stream()
        .map(rootPath::relativize)
        // Filter out the root directory itself.
        .filter(not(path -> path.toString().isBlank()))
        .collect(Collectors.toSet());

    return FuzzySearch
        .extractSorted(query, relativeFiles, this::fuzzySafePath, FUZZY_MIN_SCORE)
        .stream()
        .limit(FUZZY_MAX_RESULTS)
        .map(BoundExtractedResult::getReferent);
  }

  private String fuzzySafePath(Iterable<?> fragments) {
    // We use the null byte to separate chunks of paths here to reduce the risk of ambiguity between
    // file systems that use different naming systems and path separators. For the actual resultant
    // representation, we just use the default path representation instead. This prevents
    // the user inputting 'foo/bar' and it being considered totally different to 'bar\\foo' on
    // the default file system on a Windows machine, for example.

    var iterator = fragments.iterator();
    if (!iterator.hasNext()) {
      return "";
    }

    var builder = new StringBuilder();
    builder.append(iterator.next());

    while (iterator.hasNext()) {
      builder.append('\0').append(iterator.next());
    }

    return builder.toString();
  }

  private String userProvidedPath(String fragment, String... fragments) {
    return String.join("/", combineOneOrMore(fragment, fragments));
  }
}
