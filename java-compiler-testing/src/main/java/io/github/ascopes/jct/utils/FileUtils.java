package io.github.ascopes.jct.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.tools.JavaFileObject.Kind;

public final class FileUtils {

  private static final StringSlicer PACKAGE_SLICER = new StringSlicer(".");
  private static final StringSlicer RESOURCE_SPLITTER = new StringSlicer("/");

  private FileUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

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

  public static String binaryNameToPackageName(String binaryName) {
    return stripClassName(binaryName);
  }

  public static String binaryNameToClassName(String binaryName) {
    var lastDot = binaryName.lastIndexOf('.');

    if (lastDot == -1) {
      // The class has no package
      return binaryName;
    }

    return binaryName.substring(lastDot + 1);
  }

  public static Path binaryNameToPath(Path directory, String binaryName, Kind kind) {
    var packageName = binaryNameToPackageName(binaryName);
    var classFileName = binaryNameToClassName(binaryName) + kind.extension;
    return resolve(directory, PACKAGE_SLICER.splitToArray(packageName)).resolve(classFileName);
  }

  public static Path classNameToPath(Path packageDirectory, String className, Kind kind) {
    var classFileName = className + kind.extension;
    return resolve(packageDirectory, classFileName);
  }

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


  public static Path relativeResourceNameToPath(Path directory, String relativeName) {
    var parts = RESOURCE_SPLITTER.splitToArray(relativeName);
    return resolve(directory, parts);
  }

  public static Path packageNameToPath(Path root, String packageName) {
    for (var part : PACKAGE_SLICER.splitToArray(packageName)) {
      root = root.resolve(part);
    }
    return root;
  }

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

  public static Predicate<? super Path> fileWithAnyKind(Set<? extends Kind> kinds) {
    return path -> Files.isRegularFile(path) && kinds
        .stream()
        .map(kind -> kind.extension)
        .anyMatch(path.toString()::endsWith);
  }

  private static Path resolve(Path root, String... parts) {
    for (var part : parts) {
      root = root.resolve(part);
    }
    //return root.normalize().relativize(root);
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
