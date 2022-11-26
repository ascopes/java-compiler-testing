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
package io.github.ascopes.jct.containers;

import static java.util.stream.Collectors.toUnmodifiableList;

import io.github.ascopes.jct.utils.StringSlicer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
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

  // Exclude any "empty" extensions. At the time of writing, this will just exclude Kind.EMPTY,
  // but doing this will prevent future API changes from breaking any assumptions we make. In
  // addition to this, sort by the longest extension names first. This will prevent future
  // changes that may add subsets of existing kinds from creating unexpected results.
  private static final List<Kind> KINDS = Stream
      .of(Kind.values())
      .filter(kind -> !kind.extension.isEmpty())
      .collect(toUnmodifiableList());

  private static final StringSlicer PACKAGE_SLICER = new StringSlicer(".");
  private static final StringSlicer RESOURCE_SLICER = new StringSlicer("/");

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

    var count = path.getNameCount();
    var names = new String[count];

    for (var i = 0; i < count; ++i) {
      names[i] = FileUtils.stripFileExtension(path.getName(i).toString());
    }

    return String.join(".", names);
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
    if (!relativeName.startsWith("/")) {
      var baseDir = resolve(directory, PACKAGE_SLICER.splitToArray(packageName));
      return relativeResourceNameToPath(baseDir, relativeName);
    }

    // If we have a relative name that starts with a `/`, then we assume that it is relative
    // to the root package, so we ignore the given package name. We only then use the
    // directory to determine the file system to work off of.

    // Prepend the root part, as this gets dropped otherwise.
    var parts = new ArrayList<String>();
    parts.add("/");

    for (var part : RESOURCE_SLICER.splitToArray(relativeName)) {
      if (!part.isEmpty()) {
        parts.add(part);
      }
    }

    return resolve(directory, parts);
  }

  /**
   * Convert a relative class path resource path to a NIO path.
   *
   * @param directory    the directory the resource sits within.
   * @param relativeName the relative path of the resource within the directory.
   * @return the path to the resource on the file system.
   */
  public static Path relativeResourceNameToPath(Path directory, String relativeName) {
    var parts = RESOURCE_SLICER.splitToArray(relativeName);
    return resolve(directory, parts);
  }

  /**
   * Determine the kind of file in the given path.
   *
   * @param path the path to inspect.
   * @return the kind of file. If not known, this will return {@link Kind#OTHER}.
   */
  public static Kind pathToKind(Path path) {
    // path.getFileName() will be null if the path is the root path. Shouldn't ever
    // result in this being called ideally, but this prevents unexpected NullPointerExceptions
    // elsewhere.
    var fileName = Objects.toString(path.getFileName(), "");

    for (var kind : KINDS) {
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
   * {@code true}. Any non-existent files will always return {@code false}, even if their name
   * matches one of the provided kinds.
   *
   * @param kinds the set of kinds of file to allow.
   * @return the predicate.
   */
  public static Predicate<? super Path> fileWithAnyKind(Set<? extends Kind> kinds) {
    return path -> Files.isRegularFile(path) && kinds.contains(pathToKind(path));
  }

  private static Path resolve(Path root, String... parts) {
    for (var part : parts) {
      root = root.resolve(part);
    }
    return root.normalize();
  }

  private static Path resolve(Path root, Iterable<String> parts) {
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
