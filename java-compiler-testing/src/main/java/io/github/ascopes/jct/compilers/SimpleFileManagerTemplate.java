package io.github.ascopes.jct.compilers;

import io.github.ascopes.jct.jsr199.ModuleLocation;
import io.github.ascopes.jct.jsr199.SimpleFileManager;
import io.github.ascopes.jct.paths.NioPath;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.StringUtils;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * A template for creating a file manager later.
 * <p>
 * File manager creation is deferred until as late as possible as to enable the specification of the
 * version to use when opening JARs that may be multi-release compatible. We have to do this to
 * ensure the behaviour for opening JARs matches the release version the code is compiled against.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimpleFileManagerTemplate {

  private final Map<Location, LinkedHashSet<PathLike>> locations;

  /**
   * Initialize this workspace.
   */
  public SimpleFileManagerTemplate() {
    locations = new HashMap<>();
  }

  /**
   * Add a path to a package.
   *
   * @param location the location the package resides within.
   * @param path     the path to associate with the location.
   * @throws IllegalArgumentException if the location is module-oriented or output oriented.
   */
  public void addPath(Location location, Path path) {
    addPath(location, new NioPath(path));
  }

  /**
   * Add a path to a module.
   *
   * @param location the location the module resides within.
   * @param module   the name of the module to add.
   * @param path     the path to associate with the module.
   * @throws IllegalArgumentException if the {@code location} parameter is a
   *                                  {@link ModuleLocation}.
   * @throws IllegalArgumentException if the {@code location} parameter is not
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   * @throws IllegalArgumentException if the {@code module} parameter is not a valid module name, as
   *                                  defined by the Java Language Specification for the current
   *                                  JVM.
   */
  public void addPath(Location location, String module, Path path) {
    addPath(location, module, new NioPath(path));
  }

  /**
   * Add a path to a package.
   *
   * @param location the location the package resides within.
   * @param path     the path to associate with the location.
   * @throws IllegalArgumentException if the location is module-oriented or output oriented.
   */
  public void addPath(Location location, PathLike path) {
    if (location.isOutputLocation()) {
      throw new IllegalArgumentException("Can not add paths to an output oriented location.");
    }

    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Can not add paths directly to a module oriented location. Consider using "
              + "#addPath(Location, String, PathLike) or #addPath(Location, String, Path) instead"
      );
    }

    locations.computeIfAbsent(location, ignored -> new LinkedHashSet<>()).add(path);
  }

  /**
   * Add a path to a module.
   *
   * @param location the location the module resides within.
   * @param module   the name of the module to add.
   * @param path     the path to associate with the module.
   * @throws IllegalArgumentException if the {@code location} parameter is a
   *                                  {@link ModuleLocation}.
   * @throws IllegalArgumentException if the {@code location} parameter is not
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   * @throws IllegalArgumentException if the {@code module} parameter is not a valid module name, as
   *                                  defined by the Java Language Specification for the current
   *                                  JVM.
   */
  public void addPath(Location location, String module, PathLike path) {
    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException(
          "Cannot use a " + ModuleLocation.class.getName() + " with a custom module name. "
              + "Use SimpleFileManagerTemplate#addPath(Location, PathLike) "
              + "or SimpleFileManagerTemplate#addPath(Location, Path) instead."
      );
    }

    if (!location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Location " + StringUtils.quoted(location.getName()) + " must be module-oriented "
              + "or an output location to be able to associate a module with it."
      );
    }

    if (!SourceVersion.isName(module)) {
      throw new IllegalArgumentException(
          "Module " + StringUtils.quoted(module) + " is not a valid module name"
      );
    }

    addPath(new ModuleLocation(location, module), path);
  }

  /**
   * Create a file manager for this workspace.
   *
   * @param release the release version to use.
   * @return the file manager.
   */
  public SimpleFileManager createFileManager(String release) {
    var manager = new SimpleFileManager(release);
    locations.forEach((location, paths) -> {
      for (var path : paths) {
        manager.addPath(location, path);
      }
    });

    return manager;
  }

  /**
   * Get the paths associated with the given package-oriented location.
   *
   * @param location the location to get.
   * @return the paths.
   * @throws IllegalArgumentException if the location is module-oriented.
   */
  public List<? extends PathLike> getPaths(Location location) {
    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException("Cannot get paths from a module-oriented location");
    }

    return Optional
        .ofNullable(locations.get(location))
        .map(List::copyOf)
        .orElseGet(List::of);
  }

  /**
   * Get the modules associated with the given module-oriented/output-oriented location.
   *
   * @param location the location to get.
   * @return the locations.
   * @throws IllegalArgumentException if the location is neither output nor package oriented.
   */
  public Collection<? extends ModuleLocation> getModuleLocations(Location location) {
    if (!location.isModuleOrientedLocation() && !location.isOutputLocation()) {
      throw new IllegalArgumentException(
          "Cannot get modules from a non-module-oriented/non-output location"
      );
    }

    return locations
        .keySet()
        .stream()
        .filter(ModuleLocation.class::isInstance)
        .map(ModuleLocation.class::cast)
        .filter(hasParent(location))
        .collect(Collectors.toUnmodifiableList());
  }

  private Predicate<ModuleLocation> hasParent(Location parent) {
    return moduleLocation -> moduleLocation.getParent().equals(parent);
  }
}
