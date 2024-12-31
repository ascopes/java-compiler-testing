/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;

/**
 * Extension around a {@link JavaFileManager} that allows adding of {@link PathRoot} objects to the
 * manager.
 *
 * <p>This component is responsible for bridging the gap between a {@link Workspace} and
 * a {@link javax.tools.JavaCompiler} when performing a compilation, and thus includes a number of
 * required operations that the compiler will query the file system with. In addition, this
 * interface also defines a number of additional functionalities that are useful for querying and
 * verifying the outcome of compilations within tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface JctFileManager extends JavaFileManager {

  /**
   * Add a package-oriented path to a given location.
   *
   * <p>To add a module, first obtain the module location using
   * {@link #getLocationForModule(Location, String)}, and pass that result to this call.
   *
   * @param location the location to use.
   * @param path     the path to add.
   * @see #getLocationForModule(Location, String)
   */
  void addPath(Location location, PathRoot path);

  /**
   * Add a collection of package-oriented paths to a given location.
   *
   * <p>To add a module, first obtain the module location using
   * {@link #getLocationForModule(Location, String)}, and pass that result to this call.
   *
   * @param location the location to use.
   * @param paths    the paths to add.
   * @see #getLocationForModule(Location, String)
   */
  void addPaths(Location location, Collection<? extends PathRoot> paths);

  /**
   * Close this file manager.
   *
   * <p>If the file manager is already closed, this will have no effect.
   *
   * <p>JCT File managers may choose to not implement this operation if not applicable.
   *
   * @throws IOException if an IO error occurs during closure.
   */
  @Override
  void close() throws IOException;

  /**
   * Determine if the given file object resides in the given location.
   *
   * <p>If package-oriented, then the file must exist in the given location directly.
   *
   * <p>If module-oriented, then the file must exist in one of the modules within the location.
   * This implies that {@link #getLocationForModule(Location, JavaFileObject)} would also return
   * a valid non-null value.
   *
   * @param location the location.
   * @param fileObject the file object.
   * @return {@code true} if the file exists in the given location, or {@code false} otherwise.
   * @throws IOException if an IO error occurs during the lookup.
   */
  @Override
  boolean contains(Location location, FileObject fileObject) throws IOException;

  /**
   * Copy all containers from the first location to the second location.
   *
   * @param from the first location.
   * @param to   the second location.
   */
  void copyContainers(Location from, Location to);

  /**
   * Register an empty container for the given location to indicate to the compiler that the feature
   * exists, but has no configured paths.
   *
   * <p>This is needed to coerce the behaviour for annotation processing in some cases.
   *
   * <p>If the location already exists, then do not do anything.
   *
   * <p>If the location is an output location, then this operation does not make any sense, since
   * an empty location cannot have files output to it. In this case, you will likely get an
   * exception.
   *
   * <p>Likewise, this operation does not make sense for module locations within a module-oriented
   * location group, so this operation will fail with an error for those inputs as well.
   *
   * @param location the location to apply an empty container for.
   * @throws IllegalArgumentException if the location is an output location or a module location.
   */
  void createEmptyLocation(Location location);

  /**
   * Flush this file manager.
   *
   * <p>If the file manager is already closed, this will have no effect.
   *
   * <p>JCT File managers may choose to not implement this operation if not applicable.
   *
   * @throws IOException if an IO error occurs during flushing.
   */
  @Override
  void flush() throws IOException;

  /**
   * Get the class loader for loading classes for the given location.
   *
   * @param location the location to fetch a class loader for.
   * @return a class loader, or {@code null} if loading classes for the given location is not
   *     supported.
   * @throws SecurityException        if a class loader can not be created in the current security
   *                                  context
   * @throws IllegalStateException    if {@link #close} has been called and this file manager cannot
   *                                  be reopened
   * @throws IllegalArgumentException if the location is a module-oriented location
   */
  @Nullable
  @Override
  ClassLoader getClassLoader(Location location);

  /**
   * Get the associated effective release.
   *
   * @return the effective release.
   * @since 0.0.1
   */
  String getEffectiveRelease();

  /**
   * Get a file for input operations.
   *
   * @param location     a package-oriented location.
   * @param packageName  a package name.
   * @param relativeName a relative name.
   * @return the file object, or {@code null} if not found or not available.
   * @throws IOException              if an IO error occurs reading the file details.
   * @throws IllegalArgumentException if the location is not a package-oriented location.
   */
  @Nullable
  @Override
  FileObject getFileForInput(
      Location location,
      String packageName,
      String relativeName
  ) throws IOException;

  /**
   * Get a file for output operations.
   *
   * @param location     an output location.
   * @param packageName  a package name.
   * @param relativeName a relative name.
   * @param sibling      a file object to be used as hint for placement, might be {@code null}.
   * @return the file object, or {@code null} if not found or not available.
   * @throws IOException              if an IO error occurs reading the file details.
   * @throws IllegalArgumentException if the location is not an output location.
   */
  @Nullable
  @Override
  FileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      @Nullable FileObject sibling
  ) throws IOException;

  /**
   * Get a Java file for input operations.
   *
   * @param location  a package-oriented location.
   * @param className the name of a class.
   * @param kind      the kind of file.
   * @return the file object, or {@code null} if not found or not available.
   * @throws IOException              if an IO error occurs reading the file details.
   * @throws IllegalArgumentException if the location is not a package-oriented location.
   */
  @Nullable
  @Override
  JavaFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) throws IOException;

  /**
   * Get a Java file for output operations.
   *
   * @param location  an output location.
   * @param className the name of a class.
   * @param kind      the kind of file.
   * @param sibling   a file object to be used as hint for placement, might be {@code null}.
   * @return the file object, or {@code null} if not found or not available.
   * @throws IOException              if an IO error occurs reading the file details.
   * @throws IllegalArgumentException if the location is not an output location.
   */
  @Nullable
  @Override
  JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      @Nullable FileObject sibling
  ) throws IOException;

  /**
   * Get the location for a named module within the given location.
   *
   * @param location   the module-oriented location.
   * @param moduleName the name of the module to be found.
   * @return the location of the named module, or {@code null} if not found.
   * @throws IOException              if an IO error occurs during resolution.
   * @throws IllegalArgumentException if the location is neither an output location nor a
   *                                  module-oriented location.
   */
  @Nullable
  @Override
  Location getLocationForModule(Location location, String moduleName) throws IOException;

  /**
   * Get the location for the module holding a given file object within the given location.
   *
   * @param location   the module-oriented location.
   * @param fileObject the file object to resolve the module for.
   * @return the location, or {@code null} if not found.
   * @throws IOException              if an IO error occurs during resolution.
   * @throws IllegalArgumentException if the location is neither an output location nor a
   *                                  module-oriented location.
   */
  @Nullable
  @Override
  Location getLocationForModule(Location location, JavaFileObject fileObject) throws IOException;

  /**
   * Get the container group for the given package-oriented location.
   *
   * @param location the package oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  PackageContainerGroup getPackageContainerGroup(Location location);

  /**
   * Get a collection of all package container groups in this file manager.
   *
   * @return the package container groups.
   */
  Collection<PackageContainerGroup> getPackageContainerGroups();

  /**
   * Get the container group for the given module-oriented location.
   *
   * @param location the module oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  ModuleContainerGroup getModuleContainerGroup(Location location);

  /**
   * Get a collection of all module container groups in this file manager.
   *
   * @return the module container groups.
   */
  Collection<ModuleContainerGroup> getModuleContainerGroups();

  /**
   * Get the container group for the given output-oriented location.
   *
   * @param location the output oriented location.
   * @return the container group, or null if one does not exist.
   */
  @Nullable
  OutputContainerGroup getOutputContainerGroup(Location location);

  /**
   * Get a collection of all output container groups in this file manager.
   *
   * @return the output container groups.
   */
  Collection<OutputContainerGroup> getOutputContainerGroups();

  /**
   * Handles one option.  If {@code current} is an option to this file manager it will consume any
   * arguments to that option from {@code remaining} and return true, otherwise return false.
   *
   * @param current   current option.
   * @param remaining remaining options.
   * @return {@code true} if this option was handled by this file manager, {@code false} otherwise.
   * @throws IllegalArgumentException if this option to this file manager is used incorrectly.
   * @throws IllegalStateException    if {@link #close} has been called and this file manager cannot
   *                                  be reopened.
   */
  boolean handleOption(String current, Iterator<String> remaining);

  /**
   * Determines if a location is known to this file manager.
   *
   * @param location a location.
   * @return true if the location is known to this file manager.
   */
  boolean hasLocation(Location location);

  /**
   * Infers a binary name of a file object based on a package-oriented location. The binary name
   * returned might not be a valid binary name according to the Java Language Specification.
   *
   * @param location a location.
   * @param file     a file object.
   * @return a binary name or {@code null} the file object is not found in the given location
   * @throws IllegalArgumentException if the location is a module-oriented location.
   * @throws IllegalStateException    if {@link #close} has been called and this file manager cannot
   *                                  be reopened.
   */
  @Nullable
  @Override
  String inferBinaryName(Location location, JavaFileObject file);

  /**
   * Infer the name of the module from its location, as returned by {@code getLocationForModule} or
   * {@code listModuleLocations}.
   *
   * @param location a package-oriented location representing a module.
   * @return the name of the module, or {@code null} if the name cannot be resolved or does not
   *     exist.
   * @throws IOException              if an I/O error occurred during resolution.
   * @throws IllegalArgumentException if the location is not one known to this file manager.
   */
  @Nullable
  @Override
  String inferModuleName(Location location) throws IOException;

  /**
   * Determine if the two file objects are the same file.
   *
   * @param a a file object, can be {@code null}.
   * @param b a file object, can be {@code null}.
   * @return {@code true} if both arguments are non-null and refer to the same logical file, or
   *     {@code false} otherwise.
   */
  @Override
  boolean isSameFile(@Nullable FileObject a, @Nullable FileObject b);

  /**
   * List all file objects in the given location matching the given criteria.
   *
   * @param location    the location to search in.
   * @param packageName the package name to search in, or {@code ""} to search in the root
   *                    location.
   * @param kinds       the kinds of file to return, or {@link Set#of} {@code (} {@link Kind#OTHER}
   *                    {@code )} to find all files.
   * @param recurse     {@code true} to recurse into subpackages, {@code false} to only check the
   *                    current package.
   * @return a collection of unique file objects that were found.
   * @throws IOException              if an IO error occurs reading the file system.
   * @throws IllegalArgumentException if the location is a module-oriented location
   * @throws IllegalStateException    if {@link #close} has been called and this file manager cannot
   *                                  be reopened
   */
  @Override
  Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException;

  /**
   * List all module-based locations for the given module-oriented or output location.
   *
   * @param location the module-oriented/output location for which to list the modules.
   * @return the iterable of sets of modules.
   * @throws IOException              if an IO error occurs during resolution.
   * @throws IllegalArgumentException if the location is not a module-oriented or output location.
   */
  @Override
  Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException;

  ///
  /// Default helper overrides
  ///

  /**
   * Get the location holding the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default OutputContainerGroup getClassOutputGroup() {
    return getOutputContainerGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get the location holding the {@link StandardLocation#SOURCE_OUTPUT source outputs}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default OutputContainerGroup getSourceOutputGroup() {
    return getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the location holding the {@link StandardLocation#CLASS_PATH class path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getClassPathGroup() {
    return getPackageContainerGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#SOURCE_PATH source path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getSourcePathGroup() {
    return getPackageContainerGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get the location holding the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_PATH annotation processor path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getAnnotationProcessorPathGroup() {
    return getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Get the location holding the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getAnnotationProcessorModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#MODULE_SOURCE_PATH module source path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getModuleSourcePathGroup() {
    return getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#MODULE_PATH module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.MODULE_PATH);
  }
}
