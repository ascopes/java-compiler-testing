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
import io.github.ascopes.jct.utils.StringUtils;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
        userProvidedPath(combineOneOrMore(fragment, fragments)),
        LocationRepresentation.getInstance().toStringOf(actual.getLocation()),
        actualFile
    );
  }

  /**
   * Assert that the given file exists.
   *
   * <pre><code>
   *   // Letting JCT infer the correct path separators to use (recommended).
   *   assertions.fileExists("foo", "bar", "baz.txt");
   *
   *   // Using platform-specific separators (more likely to produce unexpected results).
   *   assertions.fileExists("foo/bar/baz.txt")...;
   * </code></pre>
   *
   * <p>If the file does not exist, then this object will attempt to find the
   * closest matches and list them in an error message along with the assertion error.
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

    var expected = combineOneOrMore(fragment, fragments);
    throw failure(StringUtils.resultNotFoundWithFuzzySuggestions(
        fuzzySafePath(expected),
        userProvidedPath(expected),
        listAllUniqueFilesForAllContainers(),
        this::fuzzySafePath,
        this::userProvidedPath,
        "path"
    ));
  }

  private Set<Path> listAllUniqueFilesForAllContainers() {
    return uncheckedIo(actual::listAllFiles)
        .keySet()
        .stream()
        // Make all the files relative to their roots.
        .flatMap(container -> uncheckedIo(container::listAllFiles)
            .stream()
            .map(container.getInnerPathRoot().getPath()::relativize))
        // Filter out the inner path root itself, preventing few confusing issues with zero-length
        // file names.
        .filter(not(path -> path.getFileName().toString().isBlank()))
        // Remove duplicates (don't think this can ever happen but this is just to be safe).
        .collect(Collectors.toSet());
  }

  private <T> String userProvidedPath(Iterable<T> parts) {
    return StreamSupport
        .stream(parts.spliterator(), false)
        .map(Objects::toString)
        .collect(Collectors.joining("/"));
  }

  private <T> String fuzzySafePath(Iterable<T> parts) {
    // Join on null bytes as we don't ever use those in normal path names. This way, we ignore
    // file-system and OS-specific path separators creating ambiguities (like how Windows uses
    // backslashes rather than forward slashes to delimit paths).
    return StreamSupport
        .stream(parts.spliterator(), false)
        .map(Objects::toString)
        .collect(Collectors.joining("\0"));
  }
}
