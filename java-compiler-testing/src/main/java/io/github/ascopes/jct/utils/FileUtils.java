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

package io.github.ascopes.jct.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Utilities for handling files in the file system.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class FileUtils {

  private static final StringSlicer PACKAGE_SLICER = new StringSlicer(".");
  private static final StringSlicer RESOURCE_SPLITTER = new StringSlicer("/");

  private FileUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Convert a path to a binary name of a class.
   *
   * @param path the relative path to convert.
   * @return the expected binary name.
   * @throws IllegalArgumentException if the path is absolute.
   */
  public static String pathToBinaryName(Path path) {
    if (path.isAbsolute()) {
      throw new IllegalArgumentException("Path cannot be absolute (got " + path + ")");
    }

    return IntStream
        .range(0, path.getNameCount())
        .mapToObj(path::getName)
        .map(Path::toString)
        .map(FileUtils::stripFileExtension)
        .collect(Collectors.joining("."));
  }

  /**
   * Convert a binary class name to a package name.
   *
   * @param binaryName the binary name to convert.
   * @return the expected package name.
   */
  public static String binaryNameToPackageName(String binaryName) {
    return stripClassName(binaryName);
  }

  /**
   * Convert a binary class name to a simple class name.
   *
   * @param binaryName the binary name to convert.
   * @return the expected simple class name.
   */
  public static String binaryNameToSimpleClassName(String binaryName) {
    var lastDot = binaryName.lastIndexOf('.');

    if (lastDot == -1) {
      // The class has no package
      return binaryName;
    }

    return binaryName.substring(lastDot + 1);
  }

  /**
   * Convert a binary class name to a path.
   *
   * @param directory  the base directory the package resides within. This is used to ensure the
   *                   correct path root and provider is picked.
   * @param binaryName the binary name to convert.
   * @param kind       the kind of the file.
   * @return the expected path.
   */
  public static Path binaryNameToPath(Path directory, String binaryName, Kind kind) {
    var packageName = binaryNameToPackageName(binaryName);
    var classFileName = binaryNameToSimpleClassName(binaryName) + kind.extension;
    return resolve(directory, PACKAGE_SLICER.splitToArray(packageName)).resolve(classFileName);
  }

  /**
   * Convert a given package name to a path.
   *
   * @param directory   the base directory the package resides within. This is used to ensure the
   *                    correct path root and provider is picked.
   * @param packageName the name of the package.
   * @return the expected path.
   */
  public static Path packageNameToPath(Path directory, String packageName) {
    for (var part : PACKAGE_SLICER.splitToArray(packageName)) {
      directory = directory.resolve(part);
    }
    return directory;
  }


  /**
   * Convert a simple class name to a path.
   *
   * @param packageDirectory the directory the class resides within.
   * @param className        the simple class name.
   * @param kind             the kind of the file.
   * @return the expected path.
   */
  public static Path simpleClassNameToPath(Path packageDirectory, String className, Kind kind) {
    var classFileName = className + kind.extension;
    return resolve(packageDirectory, classFileName);
  }

  /**
   * Convert a resource name that is found in a given package to a NIO path.
   *
   * @param directory    the base directory the package resides within. This is used to ensure the
   *                     correct path root and provider is picked.
   * @param packageName  the package name that the resource resides within.
   * @param relativeName the relative name of the resource.
   * @return the expected path.
   */
  public static Path resourceNameToPath(Path directory, String packageName, String relativeName) {
    // If we have a relative name that starts with a `/`, then we assume that it is relative
    // to the root package, so we ignore the given package name.
    if (relativeName.startsWith("/")) {
      var parts = RESOURCE_SPLITTER
          .splitToStream(relativeName)
          .dropWhile(String::isEmpty)
          .toArray(String[]::new);

      return resolve(directory, parts);
    } else {
      var baseDir = resolve(directory, PACKAGE_SLICER.splitToArray(packageName));
      return relativeResourceNameToPath(baseDir, relativeName);
    }
  }

  /**
   * Convert a relative classpath resource path to a NIO path.
   *
   * @param directory    the directory the resource sits within.
   * @param relativeName the relative path of the resource within the directory.
   * @return the path to the resource on the file system.
   */
  public static Path relativeResourceNameToPath(Path directory, String relativeName) {
    var parts = RESOURCE_SPLITTER.splitToArray(relativeName);
    return resolve(directory, parts);
  }

  /**
   * Determine the kind of file in the given path.
   *
   * @param path the path to inspect.
   * @return the kind of file. If not known, this will return {@link Kind#OTHER}.
   */
  public static Kind pathToKind(Path path) {
    var fileName = path.getFileName().toString();

    for (var kind : Kind.values()) {
      if (Kind.OTHER.equals(kind)) {
        continue;
      }

      if (fileName.endsWith(kind.extension)) {
        return kind;
      }
    }

    return Kind.OTHER;
  }

  /**
   * Return a predicate for NIO paths that filters out any files that do not match one of the given
   * file kinds.
   *
   * <p><strong>Note:</strong> this expects the file to exist for the predicate to return
   * {@code true}. Any non-existant files will always return {@code false}, even if their name
   * matches one of the provided kinds.
   *
   * @param kinds the set of kinds of file to allow.
   * @return the predicate.
   */
  public static Predicate<? super Path> fileWithAnyKind(Set<? extends Kind> kinds) {
    return path -> Files.isRegularFile(path) && kinds.stream()
        .map(kind -> kind.extension)
        .anyMatch(path.toString()::endsWith);
  }

  private static Path resolve(Path root, String... parts) {
    for (var part : parts) {
      root = root.resolve(part);
    }
    return root.normalize();
  }

  private static String stripClassName(String binaryName) {
    var classIndex = binaryName.lastIndexOf('.');
    return classIndex == -1
        ? ""
        : binaryName.substring(0, classIndex);
  }

  private static String stripFileExtension(String name) {
    var extIndex = name.lastIndexOf('.');
    return extIndex == -1
        ? name
        : name.substring(0, extIndex);
  }
}
