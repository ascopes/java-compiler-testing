/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.workspaces;

import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Interface for a Workspace to hold files and directories within.
 *
 * <p>This acts as a nexus for managing the lifetime of test sources and directories,
 * and should be used within a try-with-resources block to ensure temporary files get released after
 * the test completes.
 *
 * <p>While this interface may seem somewhat intimidating due to the number of methods it provides,
 * you will usually only ever need to use a small subset of them. The main ones you probably will
 * want to use are:
 *
 * <ul>
 *   <li>
 *     {@link #addSourcePathPackage(Path)} - for copying a source path tree from your file system.
 *   </li>
 *   <li>
 *     {@link #createSourcePathPackage()} - for creating a new source path tree.
 *   </li>
 *   <li>
 *    {@link #addClassPathPackage(Path)} - for adding a class path resource (usually a JAR or a
 *    directory of packages of classes).
 *   </li>
 *   <li>
 *    {@link #addModulePathModule(String, Path)} - for adding a module path resource (usually a JAR
 *    or a directory of packages of classes).
 *   </li>
 *   <li>
 *     {@link #getClassPathPackages()} - for fetching all class path resources.
 *   </li>
 *   <li>
 *     {@link #getModulePathModules()} - for fetching all module path resources.
 *   </li>
 *   <li>
 *     {@link #getSourceOutputPackages()} - for fetching all generated source packages.
 *   </li>
 *   <li>
 *     {@link #getSourceOutputModules()} - for fetching all generated source modules.
 *   </li>
 *   <li>
 *     {@link #close()} - to close any resources in the temporary file system.
 *   </li>
 * </ul>
 *
 * <p>A simple example of usage of this interface would be the following:
 *
 * <pre><code>
 * try (Workspace workspace = Workspaces.newWorkspace()) {
 *   workspace
 *      .createSourcePathPackage()
 *      .copyContentsFrom("src", "test", "resources", "test-data");
 *
 *   var compilation = someCompiler.compile(workspace);
 *
 *   assertThat(compilation).isSuccessful();
 * }
 * </code></pre>
 *
 * <p>As of 3.2.0, you can use a functional version of the above instead if this is more
 * suitable for your use-case:
 *
 * <pre><code>
 * Workspaces.newWorkspace().use(workspace -> {
 *   ...
 * });
 * </code></pre>
 *
 * <p>Remember that files that are created as the result of a compilation can be queried via
 * {@link JctFileManager}, which is accessible on the {@code compilation} result object. This may
 * more accurately represent the logical project structure that is the result of various
 * processing operations during compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Workspace extends AutoCloseable {

  /**
   * Attempt to close all resources in this workspace.
   *
   * @throws UncheckedIOException if an error occurs.
   */
  @Override
  void close();

  /**
   * Determine if the workspace is closed or not.
   *
   * @return {@code true} if closed, {@code false} if open.
   * @since 0.4.0
   */
  @API(since = "0.4.0", status = Status.STABLE)
  boolean isClosed();

  ///
  /// Accessor operations
  ///

  /**
   * Get an immutable copy of the current paths to operate on.
   *
   * @return the paths.
   */
  Map<Location, List<? extends PathRoot>> getAllPaths();

  /**
   * Get the collection of path roots associated with the given module.
   *
   * <p>Usually this should only ever contain one path root at a maximum, although
   * {@link Workspace} does not explicitly enforce this constraint.
   *
   * <p>If no results were found, then an empty collection is returned.
   *
   * @param location   the module-oriented or output location.
   * @param moduleName the module name within the location.
   * @return the collection of paths.
   * @throws IllegalArgumentException if the location is neither
   *                                  {@link Location#isModuleOrientedLocation() module-oriented} or
   *                                  an {@link Location#isOutputLocation() output location}. This
   *                                  will also be raised if this method is called with an instance
   *                                  of {@link ModuleLocation} (you should use
   *                                  {@link #getPackages(Location)} instead for this).
   * @see #getPackages(Location)
   * @see #getModules(Location)
   * @since 0.1.0
   */
  List<? extends PathRoot> getModule(Location location, String moduleName);

  /**
   * Get the collection of modules associated with the given location.
   *
   * <p>If no results were found, then an empty map is returned.
   *
   * @param location the location to get the modules for.
   * @return the map of module names to lists of associated paths.
   * @throws IllegalArgumentException if the location is neither
   *                                  {@link Location#isModuleOrientedLocation() module-oriented} or
   *                                  an {@link Location#isOutputLocation() output location}. This
   *                                  will also be raised if this method is called with an instance
   *                                  of {@link ModuleLocation}.
   * @see #getModule(Location, String)
   * @since 0.1.0
   */
  Map<String, List<? extends PathRoot>> getModules(Location location);

  /**
   * Get the path strategy in use.
   *
   * @return the path strategy.
   */
  PathStrategy getPathStrategy();

  /**
   * Get the collection of path roots associated with the given location.
   *
   * <p>If no results were found, then an empty collection is returned.
   *
   * @param location the location to get.
   * @return the collection of paths.
   * @throws IllegalArgumentException if the location is
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   * @see #getModule(Location, String)
   * @since 0.1.0
   */
  List<? extends PathRoot> getPackages(Location location);

  ///
  /// Mutative operations
  ///

  /**
   * Add an existing package root to this workspace and associate it with the given location.
   *
   * <p>The following constraints must be met, otherwise an {@link IllegalArgumentException}
   * will be thrown:
   *
   * <ul>
   *   <li>
   *     The {@code location} must not be
   *     {@link Location#isModuleOrientedLocation() module-oriented}.
   *   </li>
   *   <li>
   *     The {@code path} must exist.
   *   </li>
   * </ul>
   *
   * <p>For example, this would add the package tree in {@code src/test/resources/packages}
   * into the {@link StandardLocation#SOURCE_PATH source code path} that is used to compile files:
   *
   * <pre><code>
   *   var path = Path.of("src", "test", "resources", "packages");
   *   workspace.addPackage(StandardLocation.SOURCE_PATH, path);
   * </code></pre>
   *
   * @param location the location to associate with.
   * @param path     the path to add.
   * @throws IllegalArgumentException if the inputs are invalid.
   * @see #addModule(Location, String, Path)
   * @see #createPackage(Location)
   * @see #createModule(Location, String)
   */
  void addPackage(Location location, Path path);

  /**
   * Add an existing package root to this workspace and associate it with the given module name in
   * the given location.
   *
   * <p>The following constraints must be met, otherwise an {@link IllegalArgumentException}
   * will be thrown:
   *
   * <ul>
   *   <li>
   *     The {@code location} must be {@link Location#isModuleOrientedLocation() module-oriented}
   *     or an {@link Location#isOutputLocation() output location}.
   *   </li>
   *   <li>
   *     The {@code location} must not be a {@link ModuleLocation module-location handle} already.
   *   </li>
   *   <li>
   *     The {@code path} must exist.
   *   </li>
   * </ul>
   *
   * <p>For example, this would add the package tree in {@code src/test/resources/packages}
   * into the {@link StandardLocation#MODULE_SOURCE_PATH module source code path} under the module
   * name {@code foo.bar}:
   *
   * <pre><code>
   *   var path = Path.of("src", "test", "resources", "packages");
   *   workspace.addModule(StandardLocation.MODULE_SOURCE_PATH, "foo.bar", path);
   * </code></pre>
   *
   * @param location   the location to associate with.
   * @param moduleName the name of the module.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the inputs are invalid.
   * @see #addPackage(Location, Path)
   * @see #createPackage(Location)
   * @see #createModule(Location, String)
   */
  void addModule(Location location, String moduleName, Path path);

  /**
   * Create a new test directory for a package root and associate it with the given location.
   *
   * <p>This path will be destroyed when the workspace is {@link #close() closed}.
   *
   * <p>The following constraints must be met, otherwise an {@link IllegalArgumentException}
   * will be thrown:
   *
   * <ul>
   *   <li>
   *     The {@code location} must not be
   *     {@link Location#isModuleOrientedLocation() module-oriented}.
   *   </li>
   * </ul>
   *
   * <p>For example, this would create a new source root that you could add files to:
   *
   * <pre><code>
   *   workspace.createPackage(StandardLocation.SOURCE_PATH);
   * </code></pre>
   *
   * @param location the location to associate with.
   * @return the test directory that was created.
   * @throws IllegalArgumentException if the inputs are invalid.
   * @see #createModule(Location, String)
   * @see #addPackage(Location, Path)
   * @see #addModule(Location, String, Path)
   */
  ManagedDirectory createPackage(Location location);

  /**
   * Create a new test directory for a package root and associate it with the given module name in
   * the given location.
   *
   * <p>This path will be destroyed when the workspace is {@link #close() closed}.
   *
   * <p>The following constraints must be met, otherwise an {@link IllegalArgumentException}
   * will be thrown:
   *
   * <ul>
   *   <li>
   *     The {@code location} must be {@link Location#isModuleOrientedLocation() module-oriented}
   *     or an {@link Location#isOutputLocation() output location}.
   *   </li>
   *   <li>
   *     The {@code location} must not be a {@link ModuleLocation module-location handle} already.
   *   </li>
   * </ul>
   *
   * <p>For example, this would create a new multi-module source root that you could add files to,
   * for a module named {@code foo.bar}:
   *
   * <pre><code>
   *   workspace.createModule(StandardLocation.MODULE_SOURCE_PATH, "foo.bar");
   * </code></pre>
   *
   * @param location   the location to associate with.
   * @param moduleName the module name to use.
   * @return the test directory that was created.
   * @throws IllegalArgumentException if the inputs are invalid.
   * @see #createPackage(Location)
   * @see #addPackage(Location, Path)
   * @see #addModule(Location, String, Path)
   */
  ManagedDirectory createModule(Location location, String moduleName);

  ///
  /// Default implementation helpers.
  ///

  /**
   * Add a package to the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addClassOutputModule(String, Path)
   * @see #createClassOutputPackage()
   * @see #createClassOutputModule(String)
   */
  default void addClassOutputPackage(Path path) {
    addPackage(StandardLocation.CLASS_OUTPUT, path);
  }

  /**
   * Add a module to the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #addClassOutputPackage(Path)
   * @see #createClassOutputPackage()
   * @see #createClassOutputModule(String)
   */
  default void addClassOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.CLASS_OUTPUT, moduleName, path);
  }

  /**
   * Add a package to the {@link StandardLocation#SOURCE_OUTPUT generated source outputs}.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addSourceOutputModule(String, Path)
   * @see #createSourceOutputPackage()
   * @see #createSourceOutputModule(String)
   */
  default void addSourceOutputPackage(Path path) {
    addPackage(StandardLocation.SOURCE_OUTPUT, path);
  }

  /**
   * Add a module to the {@link StandardLocation#SOURCE_OUTPUT generated source outputs}.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #addSourceOutputPackage(Path)
   * @see #createSourceOutputPackage()
   * @see #createSourceOutputModule(String)
   */
  default void addSourceOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.SOURCE_OUTPUT, moduleName, path);
  }

  /**
   * Add a package to the {@link StandardLocation#CLASS_PATH class path}.
   *
   * <p>If you are adding JPMS modules, you may want to use
   * {@link #addModulePathModule(String, Path)} instead.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addModulePathModule(String, Path)
   * @see #createClassPathPackage()
   * @see #createModulePathModule(String)
   */
  default void addClassPathPackage(Path path) {
    addPackage(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add a module to the {@link StandardLocation#MODULE_PATH module path}.
   *
   * <p>If you are adding non-JPMS modules, you may want to use
   * {@link #addClassPathPackage(Path)} instead.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #addClassPathPackage(Path)
   * @see #createClassPathPackage()
   * @see #createModulePathModule(String)
   */
  default void addModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.MODULE_PATH, moduleName, path);
  }

  /**
   * Add a package to the {@link StandardLocation#SOURCE_PATH legacy source path}.
   *
   * <p>This is the location you will usually tend to use for your source code.
   *
   * <p>If you wish to define multiple JPMS modules in your source code tree to compile together,
   * you will want to consider using {@link #addSourcePathModule(String, Path)} instead. For most
   * purposes, however, this method is the one you will want to be using if your code is not in a
   * <strong>named</strong> module directory (so not something like
   * {@code src/my.module/org/example/...}).
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addSourcePathModule(String, Path)
   * @see #createSourcePathPackage()
   * @see #createSourcePathModule(String)
   */
  default void addSourcePathPackage(Path path) {
    addPackage(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add a module to the {@link StandardLocation#MODULE_SOURCE_PATH module source path}.
   *
   * <p>Note that this will signal to the compiler to run in multi-module compilation mode.
   * Any source packages that were {@link #addSourcePathPackage(Path) added} or
   * {@link #createSourcePathPackage() created} will be ignored.
   *
   * <p>If you are using a single-module, you can
   * {@link #addSourcePathPackage(Path) add a source package} and include a {@code module-info.java}
   * in the base directory instead.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #addSourcePathPackage(Path)
   * @see #createSourcePathPackage()
   * @see #createSourcePathModule(String)
   */
  default void addSourcePathModule(String moduleName, Path path) {
    addModule(StandardLocation.MODULE_SOURCE_PATH, moduleName, path);
  }

  /**
   * Add a package to the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_PATH annotation processor path}.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addAnnotationProcessorPathModule(String, Path)
   * @see #createAnnotationProcessorPathPackage()
   * @see #createAnnotationProcessorPathModule(String)
   */
  default void addAnnotationProcessorPathPackage(Path path) {
    addPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add a module to the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #addAnnotationProcessorPathPackage(Path)
   * @see #createAnnotationProcessorPathPackage()
   * @see #createAnnotationProcessorPathModule
   */
  default void addAnnotationProcessorPathModule(String moduleName, Path path) {
    addModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName, path);
  }

  /**
   * Create a package in the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createClassOutputModule(String)
   * @see #addClassOutputPackage(Path)
   * @see #addClassOutputModule(String, Path)
   */
  default ManagedDirectory createClassOutputPackage() {
    return createPackage(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Create a module in the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createClassOutputPackage()
   * @see #addClassOutputPackage(Path)
   * @see #addClassOutputModule(String, Path)
   */
  default ManagedDirectory createClassOutputModule(String moduleName) {
    return createModule(StandardLocation.CLASS_OUTPUT, moduleName);
  }

  /**
   * Create a package in the {@link StandardLocation#SOURCE_OUTPUT generated source outputs}.
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createSourceOutputModule(String)
   * @see #addSourceOutputPackage(Path)
   * @see #addSourceOutputModule(String, Path)
   */
  default ManagedDirectory createSourceOutputPackage() {
    return createPackage(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Create a module in the {@link StandardLocation#SOURCE_OUTPUT generated source outputs}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createSourceOutputPackage()
   * @see #addSourceOutputPackage(Path)
   * @see #addSourceOutputModule(String, Path)
   */
  default ManagedDirectory createSourceOutputModule(String moduleName) {
    return createModule(StandardLocation.SOURCE_OUTPUT, moduleName);
  }

  /**
   * Create a package in the {@link StandardLocation#CLASS_PATH class path}.
   *
   * <p>If you are adding JPMS modules, you may want to use
   * {@link #createModulePathModule(String)} instead.
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createModulePathModule(String)
   * @see #addClassPathPackage(Path)
   * @see #addModulePathModule(String, Path)
   */
  default ManagedDirectory createClassPathPackage() {
    return createPackage(StandardLocation.CLASS_PATH);
  }

  /**
   * Create a module in the {@link StandardLocation#MODULE_PATH module path}.
   *
   * <p>If you are adding non-JPMS modules, you may want to use
   * {@link #createClassPathPackage()} instead.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createClassPathPackage()
   * @see #addModulePathModule(String, Path)
   * @see #addClassPathPackage(Path)
   */
  default ManagedDirectory createModulePathModule(String moduleName) {
    return createModule(StandardLocation.MODULE_PATH, moduleName);
  }

  /**
   * Create a package in the {@link StandardLocation#SOURCE_PATH source path}.
   *
   * <p>If you wish to define multiple JPMS modules in your source code tree to compile together,
   * you will want to consider using {@link #createSourcePathModule(String)} instead. For most
   * purposes, however, this method is the one you will want to be using if your code is not in a
   * <strong>named</strong> module directory (so not something like
   * {@code src/my.module/org/example/...}).
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createSourcePathModule(String)
   * @see #addSourcePathPackage(Path)
   * @see #addSourcePathModule(String, Path)
   */
  default ManagedDirectory createSourcePathPackage() {
    return createPackage(StandardLocation.SOURCE_PATH);
  }

  /**
   * Create a module in the {@link StandardLocation#MODULE_SOURCE_PATH module source path}.
   *
   * <p>Note that this will signal to the compiler to run in multi-module compilation mode.
   * Any source packages that were {@link #createSourcePathPackage() created} or
   * {@link #addSourcePathPackage(Path) added} will be ignored.
   *
   * <p>If you are using a single-module, you can
   * {@link #createSourcePathPackage() create a source package} and include a
   * {@code module-info.java} in the base directory instead.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createSourcePathPackage()
   * @see #addSourcePathPackage(Path)
   * @see #addSourcePathModule(String, Path)
   */
  default ManagedDirectory createSourcePathModule(String moduleName) {
    return createModule(StandardLocation.MODULE_SOURCE_PATH, moduleName);
  }

  /**
   * Create a package in the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_PATH annotation processor path}.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createAnnotationProcessorPathModule(String)
   * @see #addAnnotationProcessorPathPackage(Path)
   * @see #addAnnotationProcessorPathModule(String, Path)
   */
  default ManagedDirectory createAnnotationProcessorPathPackage() {
    return createPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Create a module in the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createAnnotationProcessorPathPackage()
   * @see #addAnnotationProcessorPathPackage(Path)
   * @see #addAnnotationProcessorPathModule(String, Path)
   */
  default ManagedDirectory createAnnotationProcessorPathModule(String moduleName) {
    return createModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName);
  }

  /**
   * Get the non-module path roots for {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getClassOutputPackages() {
    return getPackages(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get the module path roots for {@link StandardLocation#CLASS_OUTPUT class outputs} for the given
   * module name.
   *
   * @param moduleName the module name.
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getClassOutputModule(String moduleName) {
    return getModule(StandardLocation.CLASS_OUTPUT, moduleName);
  }

  /**
   * Get the module path roots for {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @return the roots in a map of module names to lists of roots, or an empty map if none were
   *     found.
   * @since 0.1.0
   */
  default Map<String, List<? extends PathRoot>> getClassOutputModules() {
    return getModules(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get the non-module path roots for {@link StandardLocation#SOURCE_OUTPUT source outputs}.
   *
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getSourceOutputPackages() {
    return getPackages(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the module path roots for {@link StandardLocation#SOURCE_OUTPUT source outputs} for the
   * given module name.
   *
   * @param moduleName the module name.
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getSourceOutputModule(String moduleName) {
    return getModule(StandardLocation.SOURCE_OUTPUT, moduleName);
  }


  /**
   * Get the module path roots for {@link StandardLocation#SOURCE_OUTPUT source outputs}.
   *
   * @return the roots in a map of module names to lists of roots, or an empty map if none were
   *     found.
   * @since 0.1.0
   */
  default Map<String, List<? extends PathRoot>> getSourceOutputModules() {
    return getModules(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the path roots for the {@link StandardLocation#CLASS_PATH class path}.
   *
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getClassPathPackages() {
    return getPackages(StandardLocation.CLASS_PATH);
  }

  /**
   * Get the path roots for the {@link StandardLocation#SOURCE_PATH source path}.
   *
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getSourcePathPackages() {
    return getPackages(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get the path roots for the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_PATH annotation processor path}.
   *
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getAnnotationProcessorPathPackages() {
    return getPackages(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Get the path roots for the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * @param moduleName the module name to get.
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getAnnotationProcessorPathModule(String moduleName) {
    return getModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName);
  }

  /**
   * Get the module path roots for the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * @return the roots in a map of module names to lists of roots, or an empty map if none were
   *     found.
   * @since 0.1.0
   */
  default Map<String, List<? extends PathRoot>> getAnnotationProcessorPathModules() {
    return getModules(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
  }

  /**
   * Get the path roots for the {@link StandardLocation#MODULE_SOURCE_PATH module source path}.
   *
   * @param moduleName the module name to get.
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getSourcePathModule(String moduleName) {
    return getModule(StandardLocation.MODULE_SOURCE_PATH, moduleName);
  }

  /**
   * Get the module path roots for the
   * {@link StandardLocation#MODULE_SOURCE_PATH module source paths}.
   *
   * @return the roots in a map of module names to lists of roots, or an empty map if none were
   *     found.
   * @since 0.1.0
   */
  default Map<String, List<? extends PathRoot>> getSourcePathModules() {
    return getModules(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get the path roots for the {@link StandardLocation#MODULE_PATH module path}.
   *
   * @param moduleName the module name to get.
   * @return the roots in a collection, or an empty collection if none were found.
   * @since 0.1.0
   */
  default List<? extends PathRoot> getModulePathModule(String moduleName) {
    return getModule(StandardLocation.MODULE_PATH, moduleName);
  }

  /**
   * Get the module path roots for the {@link StandardLocation#MODULE_PATH module paths}.
   *
   * @return the roots in a map of module names to lists of roots, or an empty map if none were
   *     found.
   * @since 0.1.0
   */
  default Map<String, List<? extends PathRoot>> getModulePathModules() {
    return getModules(StandardLocation.MODULE_PATH);
  }

  ///
  /// Functional APIs.
  ///

  /**
   * Functional equivalent of consuming this object with a try-with-resources.
   *
   * <p>This workspace will be {@link #close() closed} upon completion of this method or upon
   * an exception being raised.
   *
   * @param consumer the consumer to pass this object to.
   * @param <T> the exception that the consumer can throw, or {@link RuntimeException}
   *            if no checked exception is thrown.
   * @throws UncheckedIOException if the closure fails after the consumer is called.
   * @throws T the checked exception that the consumer can throw.
   * @since 3.2.0
   */
  @API(since = "3.2.0", status = Status.STABLE)
  default <T extends Throwable> void use(ThrowingWorkspaceConsumer<T> consumer) throws T {
    try {
      consumer.accept(this);
    } finally {
      close();
    }
  }

  /**
   * A consumer functional interface that consumes a workspace and
   * can throw a checked exception.
   *
   * @param <T> the exception type that can be thrown, or {@link RuntimeException}
   *            if no checked exception is thrown.
   * @author Ashley Scopes
   * @since 3.2.0
   */
  @API(since = "3.2.0", status = Status.STABLE)
  interface ThrowingWorkspaceConsumer<T extends Throwable> {

    /**
     * Consume a workspace.
     *
     * @param workspace the workspace.
     * @throws T the checked exception that can be thrown.
     */
    void accept(Workspace workspace) throws T;
  }
}

