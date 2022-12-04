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
package io.github.ascopes.jct.workspaces.impl;

import static io.github.ascopes.jct.utils.FileUtils.retrieveRequiredUrl;
import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.ManagedDirectory;
import io.github.ascopes.jct.workspaces.PathWrapper;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
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
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public abstract class AbstractManagedDirectory implements ManagedDirectory {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractManagedDirectory.class);

  private final String name;
  private final Path rootDirectory;
  private final String separator;
  private final URI uri;
  private final URL url;

  /**
   * Initialise this abstract test directory.
   *
   * @param name          the name of the test directory.
   * @param rootDirectory the root directory of the test directory.
   * @param separator     the path separator to use.
   */
  protected AbstractManagedDirectory(String name, Path rootDirectory, String separator) {
    this.name = requireNonNull(name, "name");
    this.rootDirectory = requireNonNull(rootDirectory, "rootDirectory");
    this.separator = requireNonNull(separator, "separator");
    uri = this.rootDirectory.toUri();
    url = retrieveRequiredUrl(this.rootDirectory);
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

  @CheckReturnValue
  @Override
  public String getName() {
    return name;
  }

  @CheckReturnValue
  @Override
  public FileBuilder createFile(String first, String... rest) {
    return new FileBuilderImpl(first, rest);
  }

  @CheckReturnValue
  @Override
  public FileBuilder createFile(Path path) {
    return new FileBuilderImpl(path);
  }

  @CheckReturnValue
  @Override
  public DirectoryBuilder createDirectory(String first, String... rest) {
    return new DirectoryBuilderImpl(first, rest);
  }

  @CheckReturnValue
  @Override
  public DirectoryBuilder createDirectory(Path path) {
    return new DirectoryBuilderImpl(path);
  }

  @CheckReturnValue
  @Override
  public DirectoryBuilder rootDirectory() {
    return new DirectoryBuilderImpl(rootDirectory);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof AbstractManagedDirectory)) {
      return false;
    }

    var that = (AbstractManagedDirectory) other;

    return name.equals(that.name) && uri.equals(that.uri);
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

  @CheckReturnValue
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
  private static InputStream maybeBuffer(InputStream input, @Nullable String scheme) {
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

  @CheckReturnValue
  private static ClassLoader currentCallerClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  @CheckReturnValue
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

  /**
   * Chainable builder for creating individual files.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  public final class FileBuilderImpl implements FileBuilder {

    private final Path targetPath;

    private FileBuilderImpl(String first, String... rest) {
      this(rootDirectory.resolve(collapsePath(first, rest)));
    }

    private FileBuilderImpl(Path targetPath) {
      this.targetPath = makeRelativeToHere(targetPath);
    }

    @Override
    public ManagedDirectory withContents(String... lines) {
      return withContents(DEFAULT_CHARSET, lines);
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory withContents(Charset charset, String... lines) {
      return withContents(String.join("\n", lines).getBytes(charset));
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory withContents(byte[] contents) {
      return uncheckedIo(() -> createFile(new ByteArrayInputStream(contents)));
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copiedFromClassPath(String resource) {
      return copiedFromClassPath(currentCallerClassLoader(), resource);
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copiedFromClassPath(ClassLoader classLoader, String resource) {
      return uncheckedIo(() -> {
        try (var input = classLoader.getResourceAsStream(resource)) {
          if (input == null) {
            throw new FileNotFoundException("classpath:" + resource);
          }

          return createFile(input);
        }
      });
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copiedFromFile(File file) {
      return copiedFromFile(file.toPath());
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copiedFromFile(Path file) {
      return uncheckedIo(() -> {
        try (var input = Files.newInputStream(file)) {
          return createFile(input);
        }
      });
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copiedFromUrl(URL url) {
      return uncheckedIo(() -> createFile(url.openStream()));
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory thatIsEmpty() {
      return fromInputStream(InputStream.nullInputStream());
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory fromInputStream(@WillClose InputStream inputStream) {
      return uncheckedIo(() -> createFile(inputStream));
    }

    @CheckReturnValue
    private ManagedDirectory createFile(InputStream input) throws IOException {
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
        return AbstractManagedDirectory.this;
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
  public final class DirectoryBuilderImpl implements DirectoryBuilder {

    private final Path targetPath;

    private DirectoryBuilderImpl(String first, String... rest) {
      this(rootDirectory.resolve(collapsePath(first, rest)));
    }

    private DirectoryBuilderImpl(Path targetPath) {
      this.targetPath = makeRelativeToHere(targetPath);
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copyContentsFrom(String first, String... rest) {
      // Path.of is fine here as it is for the default file system.
      return copyContentsFrom(Path.of(first, rest));
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copyContentsFrom(File dir) {
      return copyContentsFrom(dir.toPath());
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory copyContentsFrom(Path rootDir) {
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

      return AbstractManagedDirectory.this;
    }

    @CheckReturnValue
    @Override
    public ManagedDirectory thatIsEmpty() {
      uncheckedIo(() -> Files.createDirectories(targetPath));
      return AbstractManagedDirectory.this;
    }
  }
}
