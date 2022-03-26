package com.github.ascopes.jct.paths;

import com.github.ascopes.jct.utils.AsyncResourceCloser;
import com.github.ascopes.jct.utils.DirectoryTreePrettyPrinter;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Feature;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.PathType;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a temporary in-memory file system that exposes the root path of said file
 * system.
 *
 * <p>This provides a utility for constructing complex and dynamic directory tree structures
 * quickly and simply using fluent chained methods.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class InMemoryPath implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryPath.class);
  private static final Cleaner CLEANER = Cleaner.create();

  private final Path path;
  private final String identifier;

  /**
   * Initialize this forwarding path.
   *
   * @param path the path to delegate to.
   */
  private InMemoryPath(String identifier, Path path) {
    this.path = path;
    this.identifier = Objects.requireNonNull(identifier);
  }

  /**
   * Get the identifying name of the temporary file system.
   *
   * @return the identifier string.
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Get the root path of the in-memory directory.
   *
   * @return the root path.
   */
  public Path getPath() {
    return path;
  }

  /**
   * Close the underlying file system.
   *
   * @throws IOException if an IO error occurs.
   */
  @Override
  public void close() throws IOException {
    path.getFileSystem().close();
  }

  public InMemoryPath createFile(Path filePath, byte[] content) throws IOException {
    try (var inputStream = new ByteArrayInputStream(content)) {
      return copyFrom(inputStream, filePath);
    }
  }

  public InMemoryPath createFile(Path filePath, String... lines) throws IOException {
    return createFile(filePath, String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
  }

  public InMemoryPath createFile(String fileName, byte[] content) throws IOException {
    return createFile(Path.of(fileName), content);
  }

  public InMemoryPath createFile(String fileName, String... lines) throws IOException {
    return createFile(Path.of(fileName), lines);
  }

  public InMemoryPath copyFromClassPath(String resource, Path targetPath) throws IOException {
    return copyFromClassPath(getClass().getClassLoader(), resource, targetPath);
  }

  public InMemoryPath copyFromClassPath(String resource, String targetPath) throws IOException {
    return copyFromClassPath(getClass().getClassLoader(), resource, targetPath);
  }

  public InMemoryPath copyFromClassPath(
      ClassLoader loader,
      String resource,
      Path targetPath
  ) throws IOException {
    var input = loader.getResourceAsStream(resource);
    if (input == null) {
      throw new FileNotFoundException("classpath:" + resource);
    }
    try (input) {
      return copyFrom(input, targetPath);
    }
  }

  public InMemoryPath copyFromClassPath(
      ClassLoader loader,
      String resource,
      String targetPath
  ) throws IOException {
    return copyFromClassPath(loader, resource, Path.of(targetPath));
  }

  public InMemoryPath copyTreeFromClassPath(
      String packageName,
      Path targetPath
  ) throws IOException {
    return copyTreeFromClassPath(getClass().getClassLoader(), packageName, targetPath);
  }

  public InMemoryPath copyTreeFromClassPath(
      String packageName,
      String targetPath
  ) throws IOException {
    return copyTreeFromClassPath(getClass().getClassLoader(), packageName, targetPath);
  }

  public InMemoryPath copyTreeFromClassPath(
      ClassLoader loader,
      String packageName,
      Path targetPath
  ) throws IOException {
    targetPath = makeRelativeToHere(targetPath);
    var directory = Files.createDirectories(targetPath);

    var config = new ConfigurationBuilder()
        .addClassLoaders(loader)
        .forPackages(packageName);

    var resources = new Reflections(config).getAll(Scanners.Resources);

    for (var resource : resources) {
      try (var inputStream = loader.getResourceAsStream(resource)) {
        if (inputStream == null) {
          // This shouldn't ever happen I don't think, but better safe than sorry with providing
          // a semi-meaningful error message if it can happen.
          throw new IOException("Failed to find resource " + resource + " somehow!");
        }

        // +1 to discard the period.
        var resourcePath = directory.resolve(resource.substring(packageName.length() + 1));
        Files.createDirectories(resourcePath.getParent());
        copyFrom(inputStream, resourcePath);
      }
    }

    return this;
  }

  public InMemoryPath copyTreeFromClassPath(
      ClassLoader loader,
      String packageName,
      String targetPath
  ) throws IOException {
    return copyTreeFromClassPath(loader, packageName, Path.of(targetPath));
  }

  public InMemoryPath copyFrom(Path existingFile, Path targetPath) throws IOException {
    try (var input = Files.newInputStream(existingFile)) {
      return copyFrom(input, targetPath);
    }
  }

  public InMemoryPath copyFrom(Path existingFile, String targetPath) throws IOException {
    return copyFrom(existingFile, Path.of(targetPath));
  }


  public InMemoryPath copyFrom(URL url, Path targetPath) throws IOException {
    try (var input = url.openStream()) {
      return copyFrom(input, targetPath);
    }
  }

  public InMemoryPath copyFrom(URL url, String targetPath) throws IOException {
    try (var input = url.openStream()) {
      return copyFrom(input, targetPath);
    }
  }

  public InMemoryPath copyFrom(InputStream input, String targetPath) throws IOException {
    return copyFrom(input, Path.of(targetPath));
  }

  public InMemoryPath copyFrom(InputStream input, Path targetPath) throws IOException {
    input = maybeBuffer(input, targetPath.toUri().getScheme());
    var path = makeRelativeToHere(targetPath);
    var options = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
    Files.createDirectories(path.getParent());

    try (var output = Files.newOutputStream(path, options)) {
      input.transferTo(output);
    }
    return this;
  }

  /**
   * Copy the entire tree to a temporary directory on the default file system so that it can be
   * inspected manually in a file explorer.
   *
   * <p>You must then delete the created directory manually.
   *
   * @return the path to the temporary directory that was created.
   * @throws IOException if something goes wrong copying the tree out of memory.
   */
  public Path copyToTempDir() throws IOException {
    return Files.copy(
        path,
        Files.createTempDirectory(identifier),
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.COPY_ATTRIBUTES
    );
  }

  /**
   * Get a pretty-printed string showing the structure of this in-memory path.
   *
   * @return the string representation.
   * @throws IOException if an IO error occurs.
   */
  public String pretty() throws IOException {
    return DirectoryTreePrettyPrinter.prettyPrint(path);
  }

  private Path makeRelativeToHere(Path path) {
    // ToString is needed as JIMFS will fail on trying to make a relative path from a different
    // provider.
    if (path.isAbsolute()) {
      throw new IllegalArgumentException(
          "Cannot use absolute paths for the target path (" + path + ")");
    }

    return path.getFileSystem() == this.path.getFileSystem()
        ? path.normalize()
        : this.path.resolve(path.toString()).normalize();
  }

  /**
   * Create a new in-memory path.
   *
   * <p>The underlying in-memory file system will be closed and destroyed when the returned
   * object is garbage collected, or when {@link #close()} is called on it manually.
   *
   * @param name a symbolic name to give the path. This must be a valid POSIX directory name.
   * @return the in-memory path.
   * @throws IOException if an IO error occured.
   * @see #create(String)
   * @see #create(String, boolean)
   */
  public static InMemoryPath create(String name) throws IOException {
    return create(name, true);
  }

  /**
   * Create a new in-memory path.
   *
   * @param name                     a symbolic name to give the path. This must be a valid POSIX
   *                                 directory name.
   * @param closeOnGarbageCollection if {@code true}, then the {@link #close()} operation will be
   *                                 called on the underlying {@link java.nio.file.FileSystem} as
   *                                 soon as the returned object from this method is garbage
   *                                 collected. If {@code false}, then you must close the underlying
   *                                 file system manually using the {@link #close()} method on the
   *                                 returned object. Failing to do so will lead to resources being
   *                                 leaked.
   * @return the in-memory path.
   * @see #create(String)
   */
  public static InMemoryPath create(String name, boolean closeOnGarbageCollection) {

    var config = Configuration
        .builder(PathType.unix())
        .setSupportedFeatures(Feature.LINKS, Feature.SYMBOLIC_LINKS, Feature.FILE_CHANNEL)
        .setAttributeViews("basic", "posix")
        .setRoots("/")
        .setWorkingDirectory("/")
        .setPathEqualityUsesCanonicalForm(true)
        .build();

    var fileSystem = Jimfs.newFileSystem(config);
    var rootPath = fileSystem.getRootDirectories().iterator().next().resolve(name);

    var inMemoryPath = new InMemoryPath(name, rootPath);
    var uri = inMemoryPath.path.toUri().toString();

    if (closeOnGarbageCollection) {
      CLEANER.register(inMemoryPath, new AsyncResourceCloser(uri, fileSystem));
    }

    LOGGER.info("Created new in-memory directory {}", uri);

    return inMemoryPath;
  }

  private static InputStream maybeBuffer(InputStream input, String scheme) {
    if (input instanceof BufferedInputStream) {
      return input;
    }

    scheme = scheme == null
        ? "unknown"
        : scheme.toLowerCase(Locale.ROOT);

    switch (scheme) {
      case "classpath":
      case "jimfs":
      case "jrt":
      case "ram":
        return input;
      default:
        return new BufferedInputStream(input);
    }
  }
}
