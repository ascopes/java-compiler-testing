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
package io.github.ascopes.jct.tests.unit.utils;

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static io.github.ascopes.jct.tests.helpers.Fixtures.someText;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.ascopes.jct.tests.helpers.UtilityClassTestTemplate;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SpecialLocationUtils} tests.
 *
 * <p>This is a little tricky to test, as we have to mock system properties and management beans
 * for this to be testable, and both of these are effectively global variables, so we have to
 * isolate this test from everything else to prevent interfering with other tests that may be
 * running in parallel.
 *
 * <p>This is complicated further by the fact the underlying APIs we use provide these paths in
 * a colon-separated-string format, which means we have to assume the default file system is being
 * used. This means we cannot test with a temporary file system, and have to rely on
 * temp-directories provided by the host OS we are running on.
 *
 * @author Ashley Scopes
 */
@DisplayName("SpecialLocationsUtils tests")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("modifies static state temporarily")
class SpecialLocationsUtilsTest implements UtilityClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return SpecialLocationUtils.class;
  }

  @DisplayName("javaRuntimeLocations() returns the JRT location")
  @EnabledForJreRange(min = JRE.JAVA_9, disabledReason = "unavailable without JPMS")
  @Test
  void javaRuntimeLocationsReturnsTheJrtLocation() {
    // When
    var paths = SpecialLocationUtils.javaRuntimeLocations();

    // Then
    assertThat(paths)
        .singleElement()
        .extracting(Path::toUri)
        .hasToString("jrt:/");
  }

  @DisplayName("currentClassPathLocations() returns the class path locations that exist")
  @Test
  void currentClassPathLocationsReturnsTheClassPathLocationsThatExist() throws IOException {
    // Given
    try (
        var tempPaths = new TempPaths();
        var mx = new MockedMxBean<>(ManagementFactory::getRuntimeMXBean, RuntimeMXBean.class)
    ) {
      // We always exclude this path.
      var ideaRt = tempPaths.addPath("idea_rt.jar"); 
      
      given(mx.mock.getClassPath()).willReturn(tempPaths.toPathString());
      // We don't want to include non-existent paths in this, so test it by deleting one of them.
      var deletedPath = tempPaths.deleteRandomPath();

      // When
      var actual = SpecialLocationUtils.currentClassPathLocations();

      // Then
      var expected = tempPaths.allExcept(deletedPath, ideaRt);
      assertThat(actual).containsExactlyElementsOf(expected);
    }
  }

  @DisplayName("currentModulePathLocations() returns the class path locations that exist")
  @Test
  void currentModulePathLocationsReturnsTheClassPathLocationsThatExist() throws IOException {
    // Given
    try (
        var tempPaths = new TempPaths();
        var ignored = new MockedSystemProperty("jdk.module.path", tempPaths.toPathString())
    ) {
      // We always exclude this path.
      var ideaRt = tempPaths.addPath("idea_rt.jar");
      
      // We don't want to include non-existent paths in this, so test it by deleting one of them.
      var deletedPath = tempPaths.deleteRandomPath();

      // When
      var actual = SpecialLocationUtils.currentModulePathLocations();

      // Then
      var expected = tempPaths.allExcept(deletedPath, ideaRt);
      assertThat(actual).containsExactlyElementsOf(expected);
    }
  }

  @DisplayName("currentPlatformClassPathLocations() returns the class path locations that exist")
  @Test
  void currentPlatformClassPathLocationsReturnsTheClassPathLocationsThatExist() throws IOException {
    // Given
    try (
        var tempPaths = new TempPaths();
        var mx = new MockedMxBean<>(ManagementFactory::getRuntimeMXBean, RuntimeMXBean.class)
    ) {
      given(mx.mock.getBootClassPath()).willReturn(tempPaths.toPathString());
      given(mx.mock.isBootClassPathSupported()).willReturn(true);

      // We don't want to include non-existent paths in this, so test it by deleting one of them.
      var deletedPath = tempPaths.deleteRandomPath();

      // When
      var actual = SpecialLocationUtils.currentPlatformClassPathLocations();

      // Then
      var expected = tempPaths.allExcept(deletedPath);
      assertThat(actual).containsExactlyElementsOf(expected);
    }
  }

  @DisplayName("currentPlatformClassPathLocations() returns empty when boot path not supported")
  @Test
  void currentPlatformClassPathLocationsReturnsEmptyWhenBootPathNotSupported() {
    // Given
    try (var mx = new MockedMxBean<>(ManagementFactory::getRuntimeMXBean, RuntimeMXBean.class)) {
      given(mx.mock.isBootClassPathSupported()).willReturn(false);

      // When
      var actual = SpecialLocationUtils.currentPlatformClassPathLocations();

      // Then
      assertThat(actual).isEmpty();
      then(mx.mock).should().isBootClassPathSupported();
      then(mx.mock).shouldHaveNoMoreInteractions();
    }
  }

  private static class TempPaths implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempPaths.class);
    private final Path root;
    private final List<Path> paths;

    private TempPaths() throws IOException {
      // Has to be on the default file system.
      root = Files.createTempDirectory("SpecialLocationsTest_" + UUID.randomUUID());
      Files.createDirectories(root);
      LOGGER.debug("Created tempdir {}", root);

      paths = new ArrayList<>();
      for (var i = 0; i < 10; ++i) {
        var nextPath = Files
            .createDirectory(root.resolve(someText()))
            .toAbsolutePath();
        LOGGER.trace("Created dir within temp location {}", nextPath);
        paths.add(nextPath);
      }
    }

    @SuppressWarnings("SameParameterValue")
    private Path addPath(String path) {
      var actual = root.resolve(path);
      paths.add(actual);
      return actual;
    }

    private Path deleteRandomPath() throws IOException {
      // Do NOT remove it from the list. We are testing that entries provided that do not
      // actually exist get discarded by our logic.
      var path = oneOf(paths);
      Files.delete(path);
      return path;
    }

    private List<Path> allExcept(Path... excludedPaths) {
      return paths
          .stream()
          .filter(not(List.of(excludedPaths)::contains))
          .collect(Collectors.toList());
    }

    private String toPathString() {
      return paths
          .stream()
          .map(Path::toAbsolutePath)
          .map(Path::toString)
          .collect(Collectors.joining(File.pathSeparator));
    }

    @Override
    public void close() throws IOException {
      Files.walkFileTree(root, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          LOGGER.trace("Deleted file for temp location {}", file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          LOGGER.trace("Deleted dir for temp location {}", dir);
          return FileVisitResult.CONTINUE;
        }
      });
      LOGGER.debug("Deleted temp dir {}", root);
    }
  }

  private static class MockedMxBean<T> implements AutoCloseable {

    private final MockedStatic<ManagementFactory> control;
    private final T mock;

    private MockedMxBean(Verification staticAccessor, Class<T> typeToMock) {
      control = Mockito.mockStatic(ManagementFactory.class);
      mock = Mockito.mock(typeToMock);
      control.when(staticAccessor).thenReturn(mock);
    }

    @Override
    public void close() {
      control.close();
    }
  }

  private static class MockedSystemProperty implements AutoCloseable {

    private final String name;
    private final String oldValue;

    private MockedSystemProperty(String name, String newValue) {
      this.name = name;
      oldValue = System.getProperty(name);
      System.setProperty(name, newValue);
    }

    @Override
    public void close() {
      if (oldValue == null) {
        System.clearProperty(name);
      } else {
        System.setProperty(name, oldValue);
      }
    }
  }
}
