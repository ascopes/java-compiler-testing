package io.github.ascopes.jct.jsr199;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.jsr199.containers.ContainerGroup;
import io.github.ascopes.jct.jsr199.containers.ModuleOrientedContainerGroup;
import io.github.ascopes.jct.jsr199.containers.OutputOrientedContainerGroup;
import io.github.ascopes.jct.jsr199.containers.PackageOrientedContainerGroup;
import io.github.ascopes.jct.jsr199.containers.SimpleModuleOrientedContainerGroup;
import io.github.ascopes.jct.jsr199.containers.SimpleOutputOrientedContainerGroup;
import io.github.ascopes.jct.jsr199.containers.SimplePackageOrientedContainerGroup;
import io.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class SimpleFileManager implements FileManager {

  private final String release;
  private final Map<Location, PackageOrientedContainerGroup> packages;
  private final Map<Location, ModuleOrientedContainerGroup> modules;
  private final Map<Location, OutputOrientedContainerGroup> outputs;

  public SimpleFileManager(String release) {
    this.release = requireNonNull(release, "release");
    packages = new HashMap<>();
    modules = new HashMap<>();
    outputs = new HashMap<>();
  }

  @SuppressWarnings("resource")
  public void addPath(Location location, Path path) throws IOException {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;

      if (location.isOutputLocation()) {
        outputs
            .computeIfAbsent(
                location,
                parent -> new SimpleOutputOrientedContainerGroup(parent, release)
            )
            .addPath(moduleLocation.getModuleName(), path);
      } else {
        modules
            .computeIfAbsent(
                location,
                parent -> new SimpleModuleOrientedContainerGroup(parent, release)
            )
            .addPath(moduleLocation.getModuleName(), path);
      }
    } else {
      if (location.isOutputLocation()) {
        outputs
            .computeIfAbsent(
                location,
                parent -> new SimpleOutputOrientedContainerGroup(parent, release)
            )
            .addPath(path);
      } else {
        packages
            .computeIfAbsent(
                location,
                parent -> new SimplePackageOrientedContainerGroup(parent, release)
            )
            .addPath(path);
      }
    }
  }

  @SuppressWarnings("resource")
  public void addPath(Location location, RamPath path) throws IOException {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;

      if (location.isOutputLocation()) {
        outputs
            .computeIfAbsent(
                location,
                parent -> new SimpleOutputOrientedContainerGroup(parent, release)
            )
            .addPath(moduleLocation.getModuleName(), path);
      } else {
        modules
            .computeIfAbsent(
                location,
                parent -> new SimpleModuleOrientedContainerGroup(parent, release)
            )
            .addPath(moduleLocation.getModuleName(), path);
      }
    } else {
      if (location.isOutputLocation()) {
        outputs
            .computeIfAbsent(
                location,
                parent -> new SimpleOutputOrientedContainerGroup(parent, release)
            )
            .addPath(path);
      } else {
        packages
            .computeIfAbsent(
                location,
                parent -> new SimplePackageOrientedContainerGroup(parent, release)
            )
            .addPath(path);
      }
    }
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return getPackageOrientedOrOutputGroup(location)
        .flatMap(PackageOrientedContainerGroup::getClassLoader)
        .orElseThrow(() -> new UnsupportedOperationException(
            "Location " + location + " does not support loading classes"
        ));
  }

  @Override
  public Iterable<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException {
    var maybeGroup = getPackageOrientedOrOutputGroup(location);

    if (maybeGroup.isEmpty()) {
      return List.of();
    }

    // TODO(ascopes): fix this to not be unchecked.
    @SuppressWarnings("unchecked")
    var listing = (Collection<JavaFileObject>) maybeGroup.get().list(packageName, kinds, recurse);

    return listing;
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (!(file instanceof PathFileObject)) {
      return null;
    }

    var pathFileObject = (PathFileObject) file;

    return getPackageOrientedOrOutputGroup(location)
        .flatMap(group -> group.inferBinaryName(pathFileObject))
        .orElse(null);

  }

  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    return Objects.equals(a.toUri(), b.toUri());
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    return false;
  }

  @Override
  public boolean hasLocation(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      return getModuleOrientedOrOutputGroup(moduleLocation.getParent())
          .map(group -> group.hasLocation(moduleLocation))
          .orElse(false);
    }

    return packages.containsKey(location)
        || modules.containsKey(location)
        || outputs.containsKey(location);
  }

  @Override
  public JavaFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) {
    return getPackageOrientedOrOutputGroup(location)
        .flatMap(group -> group.getJavaFileForInput(className, kind))
        .orElse(null);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      FileObject sibling
  ) {
    return getPackageOrientedOrOutputGroup(location)
        .flatMap(group -> group.getJavaFileForOutput(className, kind))
        .orElse(null);
  }

  @Override
  public FileObject getFileForInput(
      Location location,
      String packageName,
      String relativeName
  ) {
    return getPackageOrientedOrOutputGroup(location)
        .flatMap(group -> group.getFileForInput(packageName, relativeName))
        .orElse(null);
  }

  @Override
  public FileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      FileObject sibling
  ) {
    return getPackageOrientedOrOutputGroup(location)
        .flatMap(group -> group.getFileForOutput(packageName, relativeName))
        .orElse(null);
  }

  @Override
  public void flush() {
    // Do nothing.
  }

  @Override
  public void close() throws IOException {
    // TODO: close on GC rather than anywhere else.
  }

  @Override
  public Location getLocationForModule(Location location, String moduleName) {
    return new ModuleLocation(location, moduleName);
  }

  @Override
  public Location getLocationForModule(Location location, JavaFileObject fo) {
    if (fo instanceof PathFileObject) {
      var pathFileObject = (PathFileObject) fo;
      var moduleLocation = pathFileObject.getLocation();

      if (moduleLocation instanceof ModuleLocation) {
        return moduleLocation;
      }

      throw new IllegalArgumentException("File object " + fo + " is not for a module");
    }

    throw new IllegalArgumentException(
        "File object " + fo + " does not appear to be registered to a module"
    );
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(
      Location location,
      Class<S> service
  ) {
    return getGroup(location)
        .flatMap(group -> group.getServiceLoader(service))
        .orElseThrow(() -> new NoSuchElementException(
            "No service for " + service.getName() + " exists"
        ));
  }

  @Override
  public String inferModuleName(Location location) {
    requirePackageOrientedLocation(location);

    return location instanceof ModuleLocation
        ? ((ModuleLocation) location).getModuleName()
        : null;
  }

  @Override
  public Iterable<Set<Location>> listLocationsForModules(Location location) {
    requireOutputOrModuleOrientedLocation(location);

    return getModuleOrientedOrOutputGroup(location)
        .map(ModuleOrientedContainerGroup::getLocationsForModules)
        .orElseGet(List::of);
  }

  @Override
  public boolean contains(Location location, FileObject fo) throws IOException {
    if (!(fo instanceof PathFileObject)) {
      return false;
    }

    return getGroup(location)
        .map(group -> group.contains((PathFileObject) fo))
        .orElse(false);
  }

  @Override
  public int isSupportedOption(String option) {
    return 0;
  }

  private Optional<ContainerGroup> getGroup(Location location) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      return getModuleOrientedOrOutputGroup(moduleLocation.getParent())
          .map(group -> group.forModule(moduleLocation.getModuleName()));
    }

    return Optional
        .<ContainerGroup>ofNullable(packages.get(location))
        .or(() -> Optional.ofNullable(modules.get(location)))
        .or(() -> Optional.ofNullable(outputs.get(location)));
  }

  private Optional<ModuleOrientedContainerGroup> getModuleOrientedOrOutputGroup(Location location) {
    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException(
          "Cannot get a module-oriented group from a ModuleLocation"
      );
    }

    return Optional
        .ofNullable(modules.get(location))
        .or(() -> Optional.ofNullable(outputs.get(location)));
  }

  private Optional<PackageOrientedContainerGroup> getPackageOrientedOrOutputGroup(
      Location location
  ) {
    if (location instanceof ModuleLocation) {
      var moduleLocation = (ModuleLocation) location;
      return Optional
          .ofNullable(modules.get(location))
          .or(() -> Optional.ofNullable(outputs.get(location)))
          .map(group -> group.forModule(moduleLocation.getModuleName()));
    }

    return Optional
        .ofNullable(packages.get(location))
        .or(() -> Optional.ofNullable(outputs.get(location)));
  }

  private void requireOutputOrModuleOrientedLocation(Location location) {
    if (!location.isOutputLocation() && !location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Location " + location.getName() + " must be output or module-oriented"
      );
    }
  }

  private void requirePackageOrientedLocation(Location location) {
    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Location " + location.getName() + " must be package-oriented"
      );
    }
  }
}
