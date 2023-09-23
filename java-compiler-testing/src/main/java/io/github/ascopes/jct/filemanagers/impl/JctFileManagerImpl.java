/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.filemanagers.impl;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.containers.impl.ContainerGroupRepositoryImpl;
import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.ex.JctNotFoundException;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.ToStringBuilder;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Simple implementation of a {@link JctFileManager}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JctFileManagerImpl implements JctFileManager {

  private static final int UNSUPPORTED_ARGUMENT = -1;

  private final String effectiveRelease;
  private final ContainerGroupRepositoryImpl repository;

  public JctFileManagerImpl(String release) {
    effectiveRelease = requireNonNull(release, "release");
    repository = new ContainerGroupRepositoryImpl(release);
  }

  @Override
  public void addPath(Location location, PathRoot pathRoot) {
    repository.addPath(location, pathRoot);
  }

  @Override
  public void addPaths(Location location, Collection<? extends PathRoot> pathRoots) {
    pathRoots.forEach(pathRoot -> addPath(location, pathRoot));
  }

  @Override
  public void close() {
    repository.close();
  }

  @Override
  public boolean contains(Location location, FileObject fo) {
    if (fo instanceof PathFileObject) {
      var group = repository.getContainerGroup(location);
      return group != null && group.contains((PathFileObject) fo);
    }

    return false;
  }

  @Override
  public void copyContainers(Location from, Location to) {
    repository.copyContainers(from, to);
  }

  @Override
  public void createEmptyLocation(Location location) {
    repository.createEmptyLocation(location);
  }

  @Override
  public void flush() {
    repository.flush();
  }

  @Nullable
  @Override
  public ClassLoader getClassLoader(Location location) {
    // While we would normally enforce that we cannot get a classloader for a closed
    // file manager, we explicitly do not check for this as this is useful behaviour to have
    // retrospectively when performing assertions and tests on the resulting file manager state.
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? null
        : group.getClassLoader();
  }

  @Override
  public String getEffectiveRelease() {
    return effectiveRelease;
  }

  @Nullable
  @Override
  public FileObject getFileForInput(
      Location location,
      String packageName,
      String relativeName
  ) {
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? null
        : group.getFileForInput(packageName, relativeName);
  }

  @Nullable
  @Override
  public FileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      FileObject sibling
  ) {
    requireOutputLocation(location);
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? null
        : group.getFileForOutput(className, kind);
  }

  @Nullable
  @Override
  public JavaFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) {
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? null
        : group.getJavaFileForInput(className, kind);
  }

  @Nullable
  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      FileObject sibling
  ) {
    requireOutputLocation(location);
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? null
        : group.getJavaFileForOutput(className, kind);
  }

  @Override
  public ModuleLocation getLocationForModule(Location location, String moduleName) {
    // ModuleLocation will also validate this, but we do this to keep consistent
    // error messages.
    requireOutputOrModuleOrientedLocation(location);
    return new ModuleLocation(location, moduleName);
  }

  @Nullable
  @Override
  public ModuleLocation getLocationForModule(Location location, JavaFileObject fo) {
    requireOutputOrModuleOrientedLocation(location);

    if (fo instanceof PathFileObject) {
      var pathFileObject = (PathFileObject) fo;
      var moduleLocation = pathFileObject.getLocation();

      // The expectation is to return null if this is not for a module. Certain frameworks like
      // manifold expect this behaviour, despite it not being documented very clearly in the
      // Java compiler API.
      return ModuleLocation
          .upcast(pathFileObject.getLocation())
          .orElse(null);
    }

    throw new JctIllegalInputException(
        "File object " + fo + " is not compatible with this file manager"
    );
  }

  @Nullable
  @Override
  public ModuleContainerGroup getModuleContainerGroup(Location location) {
    requireModuleOrientedLocation(location);
    return repository.getModuleContainerGroup(location);
  }

  @Override
  public Collection<ModuleContainerGroup> getModuleContainerGroups() {
    return repository.getModuleContainerGroups();
  }

  @Nullable
  @Override
  public OutputContainerGroup getOutputContainerGroup(Location location) {
    requireOutputLocation(location);
    return repository.getOutputContainerGroup(location);
  }

  @Override
  public Collection<OutputContainerGroup> getOutputContainerGroups() {
    return repository.getOutputContainerGroups();
  }

  @Nullable
  @Override
  public PackageContainerGroup getPackageContainerGroup(Location location) {
    requirePackageLocation(location);
    return repository.getPackageContainerGroup(location);
  }

  @Override
  public Collection<PackageContainerGroup> getPackageContainerGroups() {
    return repository.getPackageContainerGroups();
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) {
    var group = repository.getContainerGroup(location);

    if (group == null) {
      throw new JctNotFoundException(
          "No container group for location " + location.getName() + " exists in this file manager"
      );
    }

    return group.getServiceLoader(service);
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    // We do not consume anything from the command line arguments in this implementation.
    return false;
  }

  @Override
  public boolean hasLocation(Location location) {
    return repository.hasLocation(location);
  }

  @Nullable
  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    requirePackageOrientedLocation(location);

    if (!(file instanceof PathFileObject)) {
      return null;
    }

    var group = repository.getPackageOrientedContainerGroup(location);

    return group == null
        ? null
        : group.inferBinaryName((PathFileObject) file);
  }

  @Nullable
  @Override
  public String inferModuleName(Location location) {
    requirePackageOrientedLocation(location);
    return ModuleLocation
        .upcast(location)
        .map(ModuleLocation::getModuleName)
        .orElse(null);
  }

  @Override
  public boolean isSameFile(@Nullable FileObject a, @Nullable FileObject b) {
    // Some annotation processors provide null values here for some reason.
    // The URI check is roughly equivalent to what Javac does to check for this.
    return a != null && b != null && Objects.equals(a.toUri(), b.toUri());
  }

  @Override
  public int isSupportedOption(String option) {
    return UNSUPPORTED_ARGUMENT;
  }

  @Override
  public Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException {
    requirePackageOrientedLocation(location);
    var group = repository.getPackageOrientedContainerGroup(location);
    return group == null
        ? Set.of()
        : group.listFileObjects(packageName, kinds, recurse);
  }

  @Override
  public Iterable<Set<Location>> listLocationsForModules(Location location) {
    requireOutputOrModuleOrientedLocation(location);
    return List.of(repository.listLocationsForModules(location));
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("repository", repository)
        .toString();
  }

  private void requireOutputOrModuleOrientedLocation(Location location) {
    if (!location.isOutputLocation() && !location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be output or module-oriented"
      );
    }
  }

  private void requireModuleOrientedLocation(Location location) {
    if (!location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be module-oriented"
      );
    }
  }

  private void requireOutputLocation(Location location) {
    if (!location.isOutputLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be an output location"
      );
    }
  }

  private void requirePackageLocation(Location location) {
    if (location.isModuleOrientedLocation() || location.isOutputLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be an input package location"
      );
    }
  }

  private void requirePackageOrientedLocation(Location location) {
    if (location.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "Location " + location.getName() + " must be package-oriented"
      );
    }
  }
}
