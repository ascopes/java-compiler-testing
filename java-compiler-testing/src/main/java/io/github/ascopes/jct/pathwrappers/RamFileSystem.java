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
package io.github.ascopes.jct.pathwrappers;

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import io.github.ascopes.jct.annotations.CheckReturnValue;
import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.annotations.WillClose;
import io.github.ascopes.jct.utils.AsyncResourceCloser;
import io.github.ascopes.jct.utils.GarbageDisposal;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a temporary in-memory file system that exposes the root path of said file
 * system.
 *
 * <p>This provides a utility for constructing complex and dynamic directory tree structures
 * quickly and simply using fluent chained methods.
 *
 * <p>These file systems are integrated into the {@link FileSystem} API, and can be configured to
 * automatically destroy themselves once this RamPath handle is garbage collected.
 *
 * <p>In addition, these paths follow POSIX file system semantics, meaning that files are handled
 * with case-sensitive names, and use forward slashes to separate paths.
 *
 * <p>While this will create a global {@link FileSystem}, it is recommended that you only interact
 * with the file system via this class to prevent potentially confusing behaviour elsewhere.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class RamFileSystem implements PathWrapper {

  private static final String SEPARATOR = "/";

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final Logger LOGGER = LoggerFactory.getLogger(RamFileSystem.class);

  private final Path path;
  private final URI uri;
  private final String name;

  /**
   * Initialize this in-memory path.
   *
   * @param name                     the name of the file system to create.
   * @param closeOnGarbageCollection {@code true} to delegate
   */
  @SuppressWarnings("ThisEscapedInObjectConstruction")
  private RamFileSystem(String name, boolean closeOnGarbageCollection) {
    this.name = requireNonNull(name, "name");

    var config = Configuration
        .builder(PathType.unix())
        .setSupportedFeatures(Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL)
        .setAttributeViews("basic", "posix")
        .setRoots(SEPARATOR)
        .setWorkingDirectory(SEPARATOR)
        .setPathEqualityUsesCanonicalForm(true)
        .build();

    var fileSystem = Jimfs.newFileSystem(config);
    path = fileSystem.getRootDirectories().iterator().next().resolve(name);
    uri = path.toUri();

    // Ensure the base directory exists.
    uncheckedIo(() -> Files.createDirectories(path));

    if (closeOnGarbageCollection) {
      GarbageDisposal.onPhantom(this, new AsyncResourceCloser(name, fileSystem));
    }

    LOGGER.trace("Initialized new in-memory directory {}", path);
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code null} in all cases. This implementation cannot have a parent path.
   */
  @CheckReturnValue
  @Nullable
  @Override
  public PathWrapper getParent() {
    return null;
  }

  @CheckReturnValue
  @Override
  public Path getPath() {
    return path;
  }

  @CheckReturnValue
  @Override
  public URI getUri() {
    return uri;
  }

  /**
   * Get the identifying name of the temporary file system.
   *
   * @return the identifier string.
   */
  @CheckReturnValue
  public String getName() {
    return name;
  }

  /**
   * Close the underlying file system.
   *
   * @throws UncheckedIOException if an IO error occurs.
   */
  public void close() {
    uncheckedIo(path.getFileSystem()::close);
  }

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   */
  @CheckReturnValue
  public RamFileSystem and() {
    return this;
  }

  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * @param first the first path fragment.
   * @param rest  any additional path fragments.
   * @return the file builder.
   */
  @CheckReturnValue
  public FileBuilder createFile(String first, String... rest) {
    return new FileBuilder(first, rest);
  }

  /**
   * Create a file builder for the given path in this RAM file system.
   *
   * @param path the <strong>relative</strong> path within the RAM file system to use.
   * @return the file builder.
   */
  @CheckReturnValue
  public FileBuilder createFile(Path path) {
    return new FileBuilder(path);
  }

  /**
   * Create a directory builder for the given path in this RAM file system.
   *
   * @param first the first path fragment.
   * @param rest  any additional path fragments.
   * @return the directory builder.
   */
  @CheckReturnValue
  public DirectoryBuilder createDirectory(String first, String... rest) {
    return new DirectoryBuilder(first, rest);
  }

  /**
   * Create a directory builder for the given path in this RAM file system.
   *
   * @param path the <strong>relative</strong> path within the RAM file system to use.
   * @return the directory builder.
   */
  @CheckReturnValue
  public DirectoryBuilder createDirectory(Path path) {
    return new DirectoryBuilder(path);
  }

  /**
   * Add contents to the root directory in this RAM file system.
   *
   * @return the directory builder.
   */
  @CheckReturnValue
  public DirectoryBuilder rootDirectory() {
    return new DirectoryBuilder(path);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof RamFileSystem)) {
      return false;
    }

    var that = (RamFileSystem) other;

    return name.equals(that.name)
        && uri.equals(that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, uri);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("name", name)
        .attribute("uri", uri)
        .toString();
  }

  private Path makeRelativeToHere(Path relativePath) {
    if (relativePath.isAbsolute() && !relativePath.startsWith(path)) {
      var fixedPath = relativePath.getRoot().relativize(relativePath);

      LOGGER.warn(
          "Treating {} as relative path {} (hint: consider removing the leading forward-slash)",
          relativePath,
          fixedPath
      );

      relativePath = fixedPath;
    }

    // ToString is needed as JIMFS will fail on trying to make a relative path from a different
    // provider.
    return relativePath.getFileSystem() == path.getFileSystem()
        ? relativePath.normalize()
        : path.resolve(relativePath.toString()).normalize();
  }

  /**
   * Create a new in-memory path.
   *
   * <p>The underlying in-memory file system will be closed and destroyed when the returned
   * object is garbage collected, or when {@link #close()} is called on it manually.
   *
   * @param name a symbolic name to give the path. This must be a valid POSIX directory name.
   * @return the in-memory path.
   * @see #newRamFileSystem(String, boolean)
   */
  @CheckReturnValue
  public static RamFileSystem newRamFileSystem(String name) {
    return newRamFileSystem(name, true);
  }

  /**
   * Create a new in-memory path.
   *
   * @param name                     a symbolic name to give the path. This must be a valid POSIX
   *                                 directory name.
   * @param closeOnGarbageCollection if {@code true}, then the {@link #close()} operation will be
   *                                 called on the underlying {@link FileSystem} as soon as the
   *                                 returned object from this method is garbage collected. If
   *                                 {@code false}, then you must close the underlying file system
   *                                 manually using the {@link #close()} method on the returned
   *                                 object. Failing to do so will lead to resources being leaked.
   * @return the in-memory path.
   * @see #newRamFileSystem(String)
   */
  @CheckReturnValue
  public static RamFileSystem newRamFileSystem(String name, boolean closeOnGarbageCollection) {
    return new RamFileSystem(name, closeOnGarbageCollection);
  }

  @CheckReturnValue
  private static InputStream maybeBuffer(InputStream input, String scheme) {
    if (input instanceof BufferedInputStream || input instanceof ByteArrayInputStream) {
      return input;
    }

    scheme = scheme == null
        ? "unknown"
        : scheme.toLowerCase(Locale.ENGLISH);

    switch (scheme) {
      case "classpath":
      case "jimfs":
      case "jrt":
      case "ram":
        return input;
      default:
        LOGGER.trace("Decided to wrap input {} in a buffer", input);
        return new BufferedInputStream(input);
    }
  }

  private static ClassLoader currentCallerClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  private static String collapsePath(String first, String... rest) {
    var joiner = new StringJoiner(SEPARATOR);
    joiner.add(first);
    for (var part : rest) {
      joiner.add(part);
    }
    return joiner.toString();
  }

  private static String collapsePath(Path path) {
    var joiner = new StringJoiner(SEPARATOR);
    for (var part : path) {
      joiner.add(part.toString());
    }
    return joiner.toString();
  }

  /**
   * Chainable builder for creating individual files.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  public final class FileBuilder {

    private final Path targetPath;

    private FileBuilder(String first, String... rest) {
      this(path.resolve(collapsePath(first, rest)));
    }

    private FileBuilder(Path targetPath) {
      this.targetPath = makeRelativeToHere(targetPath);
    }

    /**
     * Create the file with the given contents.
     *
     * @param lines the lines to write using the default charset.
     * @return the file system for further configuration.
     */
    public RamFileSystem withContents(String... lines) {
      return withContents(DEFAULT_CHARSET, lines);
    }

    /**
     * Create the file with the given contents.
     *
     * @param charset the character encoding to use.
     * @param lines   the lines to write.
     * @return the file system for further configuration.
     */
    public RamFileSystem withContents(Charset charset, String... lines) {
      return withContents(String.join("\n", lines).getBytes(charset));
    }

    /**
     * Create the file with the given byte contents.
     *
     * @param contents the bytes to write.
     * @return the file system for further configuration.
     */
    public RamFileSystem withContents(byte[] contents) {
      return uncheckedIo(() -> createFile(new ByteArrayInputStream(contents)));
    }

    /**
     * Copy a resource from the class loader on the current thread into the file system.
     *
     * @param resource the resource to copy.
     * @return the file system for further configuration.
     */
    public RamFileSystem copiedFromClassPath(String resource) {
      return copiedFromClassPath(currentCallerClassLoader(), resource);
    }

    /**
     * Copy a resource from the given class loader into the file system.
     *
     * @param classLoader the class loader to use.
     * @param resource    the resource to copy.
     * @return the file system for further configuration.
     */
    public RamFileSystem copiedFromClassPath(ClassLoader classLoader, String resource) {
      return uncheckedIo(() -> {
        try (var input = classLoader.getResourceAsStream(resource)) {
          if (input == null) {
            throw new FileNotFoundException("classpath:" + resource);
          }

          return createFile(input);
        }
      });
    }

    /**
     * Copy the contents from the given file into the file system.
     *
     * @param file the file to read.
     * @return the file system for further configuration.
     */
    public RamFileSystem copiedFromFile(File file) {
      return copiedFromFile(file.toPath());
    }

    /**
     * Copy the contents from the given path into the file system.
     *
     * @param file the file to read.
     * @return the file system for further configuration.
     */
    public RamFileSystem copiedFromFile(Path file) {
      return uncheckedIo(() -> {
        try (var input = Files.newInputStream(file)) {
          return createFile(input);
        }
      });
    }

    /**
     * Copy the contents from the given URL into the file system.
     *
     * @param url the URL to read.
     * @return the file system for further configuration.
     */
    public RamFileSystem copiedFromUrl(URL url) {
      return uncheckedIo(() -> createFile(url.openStream()));
    }

    /**
     * Create an empty file with nothing in it.
     *
     * @return the file system for further configuration.
     */
    public RamFileSystem thatIsEmpty() {
      return fromInputStream(InputStream.nullInputStream());
    }

    /**
     * Copy the contents from the given input stream into the file system.
     *
     * <p>The input stream will be closed when reading completes.
     *
     * @param inputStream the input stream to read.
     * @return the file system for further configuration.
     */
    public RamFileSystem fromInputStream(@WillClose InputStream inputStream) {
      return uncheckedIo(() -> createFile(inputStream));
    }

    private RamFileSystem createFile(InputStream input) throws IOException {
      Files.createDirectories(targetPath.getParent());

      var opts = new OpenOption[]{
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
      };

      try (
          var output = Files.newOutputStream(targetPath, opts);
          var bufferedInput = maybeBuffer(input, targetPath.toUri().getScheme())
      ) {
        bufferedInput.transferTo(output);
        return RamFileSystem.this;
      }
    }
  }

  /**
   * Chainable builder for creating directories.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  public final class DirectoryBuilder {

    private final Path targetPath;

    private DirectoryBuilder(String first, String... rest) {
      this(path.resolve(collapsePath(first, rest)));
    }

    private DirectoryBuilder(Path targetPath) {
      this.targetPath = makeRelativeToHere(targetPath);
    }

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * <p>This uses the default file system.
     *
     * @param first the first path fragment of the directory to copy from.
     * @param rest  any additional path fragements to copy from.
     * @return the file system for further configuration.
     */
    public RamFileSystem copyContentsFrom(String first, String... rest) {
      // Path.of is fine here as it is for the default file system.
      return copyContentsFrom(Path.of(first, rest));
    }

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param dir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    public RamFileSystem copyContentsFrom(File dir) {
      return copyContentsFrom(dir.toPath());
    }

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param rootDir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    public RamFileSystem copyContentsFrom(Path rootDir) {
      uncheckedIo(() -> {
        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {

          @Override
          public FileVisitResult preVisitDirectory(
              Path dir,
              BasicFileAttributes attrs
          ) throws IOException {
            // Fix windows-style separators if needed.
            var targetChildDirectory = targetPath.resolve(collapsePath(rootDir.relativize(dir)));

            LOGGER.trace("making directory {} (existing {})", targetChildDirectory, dir);

            // Ignore if the directory already exists (will occur for the root).
            Files.createDirectories(targetChildDirectory);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(
              Path file,
              BasicFileAttributes attrs
          ) throws IOException {
            // Fix windows-style separators if needed.
            var targetFile = targetPath.resolve(collapsePath(rootDir.relativize(file)));

            LOGGER.trace("copying {} to {}", file, targetFile);

            Files.copy(file, targetFile);
            return FileVisitResult.CONTINUE;
          }
        });
      });

      return RamFileSystem.this;
    }

    /**
     * Create an empty directory.
     *
     * @return the file system for further configuration.
     */
    public RamFileSystem thatIsEmpty() {
      uncheckedIo(() -> Files.createDirectories(targetPath));
      return RamFileSystem.this;
    }
  }
}
