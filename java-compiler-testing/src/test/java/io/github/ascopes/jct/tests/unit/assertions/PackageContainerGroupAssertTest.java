/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.tests.unit.assertions;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someLocation;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePathRoot;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static io.github.ascopes.jct.utils.StringUtils.quoted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.assertions.PackageContainerGroupAssert;
import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.repr.LocationRepresentation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;

/**
 * {@link PackageContainerGroupAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PackageContainerGroupAssert tests")
class PackageContainerGroupAssertTest {

  @DisplayName("PackageContainerGroupAssert#allFilesExist(String, String...) tests")
  @Nested
  class AllFilesExistStringArrayTest {

    @DisplayName(".allFilesExist(String, String...) throws an exception if the string is null")
    @Test
    void allFilesExistThrowsExceptionIfFirstStringIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.allFilesExist(null, new String[0]))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("path");
    }

    @DisplayName(".allFilesExist(String, String...) throws an exception if the array is null")
    @Test
    void allFilesExistThrowsExceptionIfArrayIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.allFilesExist("foo", (String[]) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("paths");
    }

    @DisplayName(
        ".allFilesExist(String, String...) throws an exception if the array has null values"
    )
    @Test
    void allFilesExistThrowsExceptionIfArrayHasNullValues() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.allFilesExist("foo", "bar", "baz", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("paths[2]");
    }

    @DisplayName(".allFilesExist(String, String...) calls .allFilesExist(Iterable<String>)")
    @Test
    void allFilesExistCallsIterableStringOverload() {
      // Given
      var assertions = mock(PackageContainerGroupAssert.class);
      when(assertions.allFilesExist(any(), any(), any())).thenCallRealMethod();

      // When
      var result = assertions.allFilesExist("foo", "bar", "baz");

      // Then
      verify(assertions).allFilesExist("foo", "bar", "baz");
      verify(assertions).allFilesExist(List.of("foo", "bar", "baz"));
      verifyNoMoreInteractions(assertions);
      assertThat(result).isSameAs(assertions);
    }
  }

  // Not entirely sure what is defining the precedence between .allFilesExist(null) calling
  // .allFilesExist(String, String...) or .allFilesExist(Iterable<String>) here, so I am keeping
  // this explicit to prevent the test behaviour changing in the future.
  @SuppressWarnings("RedundantCast")
  @DisplayName("PackageContainerGroupAssert#allFilesExist(Iterable<String>) tests")
  @Nested
  class AllFilesExistStringIterableTest {

    @DisplayName(".allFilesExist(Iterable<String>) throws an exception if the iterable is null")
    @Test
    void allFilesExistThrowsExceptionIfIterableIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.allFilesExist((Iterable<String>) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("paths");
    }

    @DisplayName(
        ".allFilesExist(Iterable<String>) throws an exception if the iterable has null members"
    )
    @Test
    void allFilesExistThrowsExceptionIfIterableHasNullMembers() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      // Arrays#asList does not NPE if any members are null. List#of does throw NPE.
      assertThatThrownBy(() -> assertions.allFilesExist(Arrays.asList("foo", "bar", null)))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("paths[2]");
    }

    @DisplayName(
        ".allFilesExist(Iterable<String>) throws an exception if the container group is null"
    )
    @Test
    void allFilesExistThrowsExceptionIfContainerGroupIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(null);

      // Then
      // Arrays#asList does not NPE if any members are null. List#of does throw NPE.
      assertThatThrownBy(() -> assertions.allFilesExist(List.of()))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".allFilesExist(Iterable<String>) evaluates all failures")
    @Test
    void allFilesExistEvaluatesAllFailures() {
      // Given
      var assertions = mock(PackageContainerGroupAssert.class);
      when(assertions.allFilesExist(anyIterable())).thenCallRealMethod();
      when(assertions.fileExists(any()))
          .then(ctx -> fail("%s", ctx.<Object>getArgument(0)));

      // Then
      assertThatThrownBy(() -> assertions.allFilesExist(List.of("foo", "bar", "baz", "bork")))
          .isInstanceOf(AssertionError.class)
          .message()
          .satisfies(
              message -> assertThat(message).contains("foo", "bar", "baz", "bork"),
              message -> assertThat(message)
                  .containsPattern("Expected all paths in .*? to exist but one or more did not")
          );

      verify(assertions).allFilesExist(List.of("foo", "bar", "baz", "bork"));
      verify(assertions).isNotNull();
      verify(assertions).fileExists("foo");
      verify(assertions).fileExists("bar");
      verify(assertions).fileExists("baz");
      verify(assertions).fileExists("bork");
      verifyNoMoreInteractions(assertions);
    }

    @DisplayName(".allFilesExist(Iterable<String>) returns the assertions on success")
    @Test
    void allFilesExistReturnsTheAssertionsOnSuccess() {
      // Given
      var assertions = mock(PackageContainerGroupAssert.class);
      when(assertions.allFilesExist(anyIterable())).thenCallRealMethod();

      // When
      var result = assertions.allFilesExist(List.of("foo", "bar", "baz", "bork"));

      // Then
      assertThat(result).isSameAs(assertions);
    }
  }

  @DisplayName("PackageContainerGroupAssert.classLoader(...) tests")
  @Nested
  class ClassLoaderTest {

    @DisplayName(".classLoader() fails if the container group is null")
    @Test
    void classLoaderFailsIfContainerGroupIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(null);

      // Then
      assertThatThrownBy(assertions::classLoader)
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".classLoader() returns assertions for the class loader")
    @Test
    void classLoaderReturnsAssertionsForTheClassLoader() {
      // Given
      var containerGroup = mock(PackageContainerGroup.class);
      var classLoader = mock(ClassLoader.class);
      when(containerGroup.getClassLoader()).thenReturn(classLoader);
      var assertions = new PackageContainerGroupAssert(containerGroup);

      // When
      var result = assertions.classLoader();

      // Then
      assertThatCode(() -> result.isSameAs(classLoader))
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("PackageContainerGroupAssert.fileDoesNotExist(...) tests")
  @Nested
  class FileDoesNotExistTest {

    @DisplayName(".fileDoesNotExist(String, String...) throws an exception if the string is null")
    @Test
    void fileDoesNotExistThrowsExceptionIfFirstStringIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileDoesNotExist(null, new String[0]))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragment");
    }

    @DisplayName(".fileDoesNotExist(String, String...) throws an exception if the array is null")
    @Test
    void fileDoesNotExistThrowsExceptionIfArrayIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileDoesNotExist("foo", (String[]) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragments");
    }

    @DisplayName(
        ".fileDoesNotExist(String, String...) throws an exception if the array has null values"
    )
    @Test
    void fileDoesNotExistThrowsExceptionIfArrayHasNullValues() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileDoesNotExist("foo", "bar", "baz", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragments[2]");
    }

    @DisplayName(
        ".fileDoesNotExist(String, String...) throws an exception if the container group is null"
    )
    @Test
    void fileDoesNotExistThrowsExceptionIfContainerGroupIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(null);

      // Then
      // Arrays#asList does not NPE if any members are null. List#of does throw NPE.
      assertThatThrownBy(() -> assertions.fileDoesNotExist("foo", "bar", "baz"))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(".fileDoesNotExist(String, String...) fails if the file exists")
    @Test
    void fileDoesNotExistFailsIfFileExists() {
      // Given
      var actualFile = Path.of("foo", "bar", "baz");
      var location = someLocation();

      var containerGroup = mock(PackageContainerGroup.class);
      when(containerGroup.getFile(any(), any(), any())).thenReturn(actualFile);
      when(containerGroup.getLocation()).thenReturn(location);

      var assertions = new PackageContainerGroupAssert(containerGroup);

      // Then
      assertThatThrownBy(() -> assertions.fileDoesNotExist("foo", "bar", "baz"))
          .isInstanceOf(AssertionError.class)
          .hasMessageContaining(
              "Expected path %s to not exist in %s but it was found at %s",
              quoted("foo/bar/baz"),
              LocationRepresentation.getInstance().toStringOf(location),
              quoted(actualFile)
          );
    }

    @DisplayName(".fileDoesNotExist(String, String...) succeeds if the file does not exist")
    @Test
    void fileDoesNotExistSucceedsIfFileDoesNotExist() {
      // Given
      var containerGroup = mock(PackageContainerGroup.class);
      when(containerGroup.getFile(any(), any(), any())).thenReturn(null);

      var assertions = new PackageContainerGroupAssert(containerGroup);

      // When
      var result = assertions.fileDoesNotExist("foo", "bar", "baz");

      // Then
      verify(containerGroup).getFile("foo", "bar", "baz");
      verifyNoMoreInteractions(containerGroup);

      assertThatCode(() -> result.isSameAs(containerGroup))
          .withFailMessage("Expected returned assertions to be for the same container group")
          .doesNotThrowAnyException();
    }
  }

  @DisplayName("PackageContainerGroupAssert.fileExists(...) tests")
  @Nested
  class FileExistsTest {

    @DisplayName(".fileExists(String, String...) throws an exception if the string is null")
    @Test
    void fileExistsThrowsExceptionIfFirstStringIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileExists(null, new String[0]))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragment");
    }

    @DisplayName(".fileExists(String, String...) throws an exception if the array is null")
    @Test
    void fileExistsThrowsExceptionIfArrayIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileExists("foo", (String[]) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragments");
    }

    @DisplayName(
        ".fileExists(String, String...) throws an exception if the array has null values"
    )
    @Test
    void fileExistsThrowsExceptionIfArrayHasNullValues() {
      // Given
      var assertions = new PackageContainerGroupAssert(mock());

      // Then
      assertThatThrownBy(() -> assertions.fileExists("foo", "bar", "baz", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("fragments[2]");
    }

    @DisplayName(
        ".fileExists(String, String...) throws an exception if the container group is null"
    )
    @Test
    void fileExistsThrowsExceptionIfContainerGroupIsNull() {
      // Given
      var assertions = new PackageContainerGroupAssert(null);

      // Then
      // Arrays#asList does not NPE if any members are null. List#of does throw NPE.
      assertThatThrownBy(() -> assertions.fileExists("foo", "bar", "baz"))
          .isInstanceOf(AssertionError.class);
    }

    @DisplayName(
        ".fileExists(String, String...) fails with fuzzy suggestions if the file does not exist"
    )
    @Test
    void fileExistsFailsWithFuzzySuggestionsIfTheFileDoesNotExist() throws IOException {
      // Given
      var container1 = ContainerBuilder
          .withRootPath("", "home", "ashley")
          .withRelativeFile("foo", "bar", "qux")
          .withRelativeFile("baz", "bork")
          .withRelativeFile("qux", "quxx")
          .withRelativeFile("bork")
          .withRelativeFile("elephant")
          .withRelativeFile("bazz")
          .buildMock();

      var container2 = ContainerBuilder
          .withRootPath("lorem", "ipsum")
          .withRelativeFile("dolor", "sit")
          .withRelativeFile("amet")
          .withRelativeFile("foo", "bar", "bazz")
          .buildMock();

      var container3 = ContainerBuilder
          .withRootPath("doh")
          .withRelativeFile("ray", "me", "fah")
          .withRelativeFile("so", "la")
          .withRelativeFile("foo", "baz")
          .withRelativeFile("foo", "bar")
          .buildMock();

      var container4 = ContainerBuilder
          .withZipContainerPath()
          .withRelativeFile("zipa", "zipb")
          .withRelativeFile("zipc")
          .buildMock();

      var container5 = ContainerBuilder
          .withZipContainerPath("bing", "bong")
          .withRelativeFile("zipa", "zipb")
          .withRelativeFile("zipc")
          .buildMock();

      var containerGroup = mock(PackageContainerGroup.class);
      when(containerGroup.getFile(any(), any(), any())).thenReturn(null);

      // Have to declare separately outside the stubbing or Mockito gets confused.
      var listAllFilesResult = Map.of(
          container1, container1.listAllFiles(),
          container2, container2.listAllFiles(),
          container3, container3.listAllFiles(),
          container4, container4.listAllFiles(),
          container5, container5.listAllFiles()
      );

      when(containerGroup.listAllFiles())
          .thenReturn(listAllFilesResult);

      var assertions = new PackageContainerGroupAssert(containerGroup);

      // Then
      // Expect the fuzzy error message to contain certain phrases to indicate it is working as
      // we expect and giving good estimates of close names.
      assertThatThrownBy(() -> assertions.fileExists("foo", "bar", "baz"))
          .isInstanceOf(AssertionError.class)
          .message()
          .satisfies(
              message -> assertThat(message).contains("Maybe you meant:"),
              message -> assertThat(message).contains(quoted("foo/bar")),
              message -> assertThat(message).contains(quoted("foo/bar/bazz")),
              message -> assertThat(message).contains(quoted("foo/baz")),
              message -> assertThat(message)
                  .contains("No file with relative path matching \"foo/bar/baz\" was found.")
          );

    }

    @DisplayName(".fileExists(String, String...) succeeds if the file exists")
    @Test
    void fileExistsSucceedsIfFileExists() {
      // Given
      var actualFile = Path.of("foo", "bar", "baz");

      var containerGroup = mock(PackageContainerGroup.class);
      when(containerGroup.getFile(any(), any(), any())).thenReturn(actualFile);

      var assertions = new PackageContainerGroupAssert(containerGroup);

      // When
      var result = assertions.fileExists("foo", "bar", "baz");

      // Then
      verify(containerGroup).getFile("foo", "bar", "baz");
      verifyNoMoreInteractions(containerGroup);

      assertThatCode(() -> result.isSameAs(actualFile))
          .withFailMessage("Expected returned assertions to be for the discovered path")
          .doesNotThrowAnyException();
    }

  }

  private static class ContainerBuilder {

    private final Path innerRootPath;
    private final Set<Path> paths;

    private ContainerBuilder(Path path) {
      innerRootPath = path;
      paths = new LinkedHashSet<>();
      paths.add(innerRootPath);
    }

    ContainerBuilder withRelativeFile(String fragment, String... fragments) {
      var path = innerRootPath.resolve(fragment);
      for (var nextFragment : fragments) {
        path = path.resolve(nextFragment);
      }
      paths.add(path);
      return this;
    }

    Container buildMock() {
      var innerPathRoot = somePathRoot();
      when(innerPathRoot.getPath()).thenReturn(innerRootPath);
      var container = mock(Container.class, withSettings().strictness(Strictness.LENIENT));
      when(container.getInnerPathRoot()).thenReturn(innerPathRoot);
      uncheckedIo(() -> when(container.listAllFiles()).thenReturn(paths));
      return container;
    }

    static ContainerBuilder withRootPath(String fragment, String... fragments) {
      return new ContainerBuilder(Path.of(fragment, fragments));
    }

    static ContainerBuilder withZipContainerPath(String... fragments) {
      // Hack to get a valid zip container path. This is important to test as ZipPath objects
      // can have null file names, which we need to check for. UnixPath does not allow this, so
      // we can't just use the existing logic to test for this.
      var rootPath = uncheckedIo(() -> {
        var tempFile = Files.createTempFile("some-file", ".zip");
        try {
          try (var zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempFile))) {
            zipOutputStream.setComment("Empty");
            zipOutputStream.finish();
          }

          var fs = FileSystemProvider
              .installedProviders()
              .stream()
              .filter(provider -> provider.getScheme().equals("jar"))
              .findFirst()
              .orElseThrow()
              .newFileSystem(tempFile, Map.of());

          try (fs) {
            return fs.getRootDirectories().iterator().next();
          }
        } finally {
          Files.delete(tempFile);
        }
      });

      for (var fragment : fragments) {
        rootPath = rootPath.resolve(fragment);
      }

      return new ContainerBuilder(rootPath);
    }
  }
}
