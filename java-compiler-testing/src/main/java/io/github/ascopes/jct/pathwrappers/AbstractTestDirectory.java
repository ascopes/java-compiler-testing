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

import io.github.ascopes.jct.annotations.CheckReturnValue;
import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.annotations.WillClose;
import io.github.ascopes.jct.pathwrappers.impl.PathWrapperUtils;
import io.github.ascopes.jct.utils.GarbageDisposalUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
 * Abstract base for implementing a reusable managed wrapper around a directory of some sort.
 *
 * <p>This is designed to simplify the creation of file and directory trees, and manage the release
 * of resources once no longer needed automatically, helping to keep test logic simple and clean.
 *
 * @param <I> the implementation type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractTestDirectory<I extends AbstractTestDirectory<I>>
    implements PathWrapper {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestDirectory.class);

  private final String name;
  private final Path rootDirectory;
  private final String separator;
  private final URI uri;
  private final URL url;
  private final Closeable closeHook;

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  protected AbstractTestDirectory(
      String name,
      Path rootDirectory,
      String separator,
      boolean closeOnGc,
      Closeable closeHook
  ) {

    // Register immediately so we still clean up if an exception is thrown from this constructor.
    if (closeOnGc) {
      LOGGER.trace("Registering {} to be destroyed on garbage collection", rootDirectory.toUri());
      GarbageDisposalUtils.onPhantom(this, name, closeHook);
    }

    this.name = requireNonNull(name, "name");
    this.closeHook = requireNonNull(closeHook, "closeHook");
    this.rootDirectory = requireNonNull(rootDirectory, "rootDirectory");
    this.separator = requireNonNull(separator, "separator");
    uri = this.rootDirectory.toUri();
    url = PathWrapperUtils.retrieveRequiredUrl(this.rootDirectory);
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
    return rootDirectory;
  }

  @CheckReturnValue
  @Override
  public URI getUri() {
    return uri;
  }

  @CheckReturnValue
  @Override
  public URL getUrl() {
    return url;
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
    uncheckedIo(closeHook::close);
  }

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #also
   * @see #then
   */
  @CheckReturnValue
  public I and() {
    return thisTestFileSystem();
  }

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #and
   * @see #then
   */
  @CheckReturnValue
  public I also() {
    return thisTestFileSystem();
  }

  /**
   * Method that returns the object it is called upon to enable creating fluent-language builders.
   *
   * @return this object.
   * @see #and
   * @see #also
   */
  @CheckReturnValue
  public I then() {
    return thisTestFileSystem();
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
    return new DirectoryBuilder(rootDirectory);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AbstractTestDirectory<?>)) {
      return false;
    }

    var that = (AbstractTestDirectory<?>) other;

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
    if (relativePath.isAbsolute() && !relativePath.startsWith(rootDirectory)) {
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
    return relativePath.getFileSystem() == rootDirectory.getFileSystem()
        ? relativePath.normalize()
        : rootDirectory.resolve(relativePath.toString()).normalize();
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

  private String collapsePath(String first, String... rest) {
    var joiner = new StringJoiner(separator);
    joiner.add(first);
    for (var part : rest) {
      joiner.add(part);
    }
    return joiner.toString();
  }

  private String collapsePath(Path path) {
    var joiner = new StringJoiner(separator);
    for (var part : path) {
      joiner.add(part.toString());
    }
    return joiner.toString();
  }

  @SuppressWarnings("unchecked")
  private I thisTestFileSystem() {
    return (I) this;
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
      this(rootDirectory.resolve(collapsePath(first, rest)));
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
    public I withContents(String... lines) {
      return withContents(DEFAULT_CHARSET, lines);
    }

    /**
     * Create the file with the given contents.
     *
     * @param charset the character encoding to use.
     * @param lines   the lines to write.
     * @return the file system for further configuration.
     */
    public I withContents(Charset charset, String... lines) {
      return withContents(String.join("\n", lines).getBytes(charset));
    }

    /**
     * Create the file with the given byte contents.
     *
     * @param contents the bytes to write.
     * @return the file system for further configuration.
     */
    public I withContents(byte[] contents) {
      return uncheckedIo(() -> createFile(new ByteArrayInputStream(contents)));
    }

    /**
     * Copy a resource from the class loader on the current thread into the file system.
     *
     * @param resource the resource to copy.
     * @return the file system for further configuration.
     */
    public I copiedFromClassPath(String resource) {
      return copiedFromClassPath(currentCallerClassLoader(), resource);
    }

    /**
     * Copy a resource from the given class loader into the file system.
     *
     * @param classLoader the class loader to use.
     * @param resource    the resource to copy.
     * @return the file system for further configuration.
     */
    public I copiedFromClassPath(ClassLoader classLoader, String resource) {
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
    public I copiedFromFile(File file) {
      return copiedFromFile(file.toPath());
    }

    /**
     * Copy the contents from the given path into the file system.
     *
     * @param file the file to read.
     * @return the file system for further configuration.
     */
    public I copiedFromFile(Path file) {
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
    public I copiedFromUrl(URL url) {
      return uncheckedIo(() -> createFile(url.openStream()));
    }

    /**
     * Create an empty file with nothing in it.
     *
     * @return the file system for further configuration.
     */
    public I thatIsEmpty() {
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
    public I fromInputStream(@WillClose InputStream inputStream) {
      return uncheckedIo(() -> createFile(inputStream));
    }

    private I createFile(InputStream input) throws IOException {
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
        return thisTestFileSystem();
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
      this(rootDirectory.resolve(collapsePath(first, rest)));
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
     * @param rest  any additional path fragments to copy from.
     * @return the file system for further configuration.
     */
    public I copyContentsFrom(String first, String... rest) {
      // Path.of is fine here as it is for the default file system.
      return copyContentsFrom(Path.of(first, rest));
    }

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param dir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    public I copyContentsFrom(File dir) {
      return copyContentsFrom(dir.toPath());
    }

    /**
     * Copy the contents of the directory at the given path recursively into this directory.
     *
     * @param rootDir the directory to copy the contents from.
     * @return the file system for further configuration.
     */
    public I copyContentsFrom(Path rootDir) {
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

      return thisTestFileSystem();
    }

    /**
     * Create an empty directory.
     *
     * @return the file system for further configuration.
     */
    public I thatIsEmpty() {
      uncheckedIo(() -> Files.createDirectories(targetPath));
      return thisTestFileSystem();
    }
  }
}
