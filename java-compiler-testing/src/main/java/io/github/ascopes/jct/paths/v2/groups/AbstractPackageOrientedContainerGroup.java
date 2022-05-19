package io.github.ascopes.jct.paths.v2.groups;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.Lazy;
import io.github.ascopes.jct.paths.ModuleLocation;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.paths.v2.PathFileObject;
import io.github.ascopes.jct.paths.v2.classloaders.ContainerClassLoader;
import io.github.ascopes.jct.paths.v2.containers.Container;
import io.github.ascopes.jct.paths.v2.containers.DirectoryContainer;
import io.github.ascopes.jct.paths.v2.containers.JarContainer;
import io.github.ascopes.jct.paths.v2.containers.RamPathContainer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An abstract base implementation for a group of containers that relate to a specific location.
 *
 * <p>This mechanism enables the ability to have locations with more than one path in them,
 * which is needed to facilitate the Java compiler's distributed class path, module handling, and
 * other important features.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractPackageOrientedContainerGroup
    implements PackageOrientedContainerGroup {

  private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
      ".zip",
      ".jar",
      ".war"
  );

  protected final String release;
  private final List<Container> containers;
  private final Lazy<ClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param release the release to use for multi-release JARs.
   */
  protected AbstractPackageOrientedContainerGroup(String release) {
    this.release = requireNonNull(release, "release");

    containers = new ArrayList<>();
    classLoaderLazy = new Lazy<>(this::createClassLoader);
  }

  @Override
  public void addPath(Path path) throws IOException {
    var archive = ARCHIVE_EXTENSIONS
        .stream()
        .anyMatch(path.getFileName().toString().toLowerCase(Locale.ROOT)::endsWith);

    var container = archive
        ? new JarContainer(getLocation(), path, release)
        : new DirectoryContainer(getLocation(), path);

    containers.add(container);
  }

  @Override
  public void addPath(RamPath ramPath) {
    containers.add(new RamPathContainer(getLocation(), ramPath));
  }

  @Override
  public boolean contains(PathFileObject fileObject) {
    return containers
        .stream()
        .anyMatch(container -> container.contains(fileObject));
  }

  @Override
  public void close() throws IOException {
    // Close everything in a best-effort fashion.
    var exceptions = new ArrayList<Throwable>();

    for (var container : containers) {
      try {
        container.close();
      } catch (IOException ex) {
        exceptions.add(ex);
      }
    }

    try {
      classLoaderLazy.destroy();
    } catch (Exception ex) {
      exceptions.add(ex);
    }

    if (exceptions.size() > 0) {
      var ioEx = new IOException("One or more components failed to close");
      exceptions.forEach(ioEx::addSuppressed);
      throw ioEx;
    }
  }

  @Override
  public Optional<Path> findFile(String path) {
    return containers
        .stream()
        .flatMap(container -> container.findFile(path).stream())
        .findFirst();
  }

  @Override
  public Optional<ClassLoader> getClassLoader() {
    return Optional.of(classLoaderLazy.access());
  }

  @Override
  public Optional<PathFileObject> getFileForInput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForInput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getFileForOutput(
      String packageName,
      String relativeName
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getFileForOutput(packageName, relativeName).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getJavaFileForInput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForInput(className, kind).stream())
        .findFirst();
  }

  @Override
  public Optional<PathFileObject> getJavaFileForOutput(
      String className,
      Kind kind
  ) {
    return containers
        .stream()
        .flatMap(container -> container.getJavaFileForOutput(className, kind).stream())
        .findFirst();
  }

  @Override
  public <S> Optional<ServiceLoader<S>> getServiceLoader(Class<S> service) {
    var location = getLocation();

    if (location instanceof ModuleLocation) {
      throw new UnsupportedOperationException("Cannot load services from specific modules");
    }

    return Optional.of(ServiceLoader.load(service, classLoaderLazy.access()));
  }

  @Override
  public Optional<String> inferBinaryName(PathFileObject fileObject) {
    return containers
        .stream()
        .flatMap(container -> container.inferBinaryName(fileObject).stream())
        .findFirst();
  }

  @Override
  public boolean isEmpty() {
    return containers.isEmpty();
  }

  @Override
  public Collection<? extends PathFileObject> list(
      String packageName,
      Set<? extends Kind> kinds,
      boolean recurse
  ) throws IOException {
    var items = new ArrayList<PathFileObject>();

    for (var container : containers) {
      items.addAll(container.list(packageName, kinds, recurse));
    }

    return items;
  }

  protected ContainerClassLoader createClassLoader() {
    return new ContainerClassLoader(getLocation(), getContainers());
  }

  protected final List<? extends Container> getContainers() {
    return Collections.unmodifiableList(containers);
  }
}
