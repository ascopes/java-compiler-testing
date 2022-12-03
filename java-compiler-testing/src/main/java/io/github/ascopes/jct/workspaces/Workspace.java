package io.github.ascopes.jct.workspaces;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.workspaces.impl.BasicPathWrapperImpl;
import io.github.ascopes.jct.workspaces.impl.RamDirectory;
import io.github.ascopes.jct.workspaces.impl.TempDirectory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

// TODO(ascopes): interface time.
public class Workspace implements AutoCloseable {

  private final PathStrategy pathStrategy;
  private final Map<Location, List<PathWrapper>> paths;

  public Workspace() {
    this(PathStrategy.RAM_DIRECTORIES);
  }

  public Workspace(PathStrategy pathStrategy) {
    this.pathStrategy = requireNonNull(pathStrategy, "pathStrategy");
    paths = new HashMap<>();
  }

  @Override
  public void close() {
    // Close everything in a best-effort fashion.
    var exceptions = new ArrayList<Throwable>();

    for (var list : paths.values()) {
      for (var path : list) {
        if (path instanceof TestDirectory) {
          try {
            ((TestDirectory) path).close();

          } catch (Exception ex) {
            exceptions.add(ex);
          }
        }
      }
    }

    if (exceptions.size() > 0) {
      // TODO(ascopes): custom exception type.
      var newEx = new IllegalStateException("One or more components failed to close");
      exceptions.forEach(newEx::addSuppressed);
      throw newEx;
    }
  }

  public TestDirectory createPath(Location location) {
    var dir = pathStrategy.newInstance(location.getName());
    paths.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
    return dir;
  }

  public void addPath(Location location, Path path) {
    var dir = new BasicPathWrapperImpl(path);
    paths.computeIfAbsent(location, unused -> new ArrayList<>()).add(dir);
  }

  public void addPath(Location location, String moduleName, Path path) {
    addPath(new ModuleLocation(location, moduleName), path);
  }

  public TestDirectory createPath(Location location, String moduleName) {
    return createPath(new ModuleLocation(location, moduleName));
  }

  public Map<Location, List<PathWrapper>> getPaths() {
    // Create an immutable copy.
    var pathsCopy = new HashMap<Location, List<PathWrapper>>();
    paths.forEach((location, list) -> pathsCopy.put(location, List.copyOf(list)));
    return Collections.unmodifiableMap(pathsCopy);
  }

  /**
   * Strategy to use for configuring new test directories.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  public enum PathStrategy {
    RAM_DIRECTORIES(RamDirectory::newRamDirectory),
    TEMP_DIRECTORIES(TempDirectory::newTempDirectory);

    private final Function<String, TestDirectory> constructor;

    PathStrategy(Function<String, TestDirectory> constructor) {
      this.constructor = constructor;
    }

    private TestDirectory newInstance(String name) {
      return constructor.apply(name);
    }
  }
}
