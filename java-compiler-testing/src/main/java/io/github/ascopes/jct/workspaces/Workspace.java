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
package io.github.ascopes.jct.workspaces;

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
   * Get the path strategy in use.
   *
   * @return the path strategy.
   */
  PathStrategy getPathStrategy();

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
   *   workspace.addLocation(StandardLocation.SOURCE_PATH, path);
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
   * purposes, however, this method is the one you will want to be using if your code is not in
   * a <strong>named</strong> module directory
   * (so not something like {@code src/my.module/org/example/...}).
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
   * Add a package to the {@link StandardLocation#PLATFORM_CLASS_PATH platform class path}
   * (also known as the boot class path).
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #createPlatformClassPathPackage()
   */
  default void addPlatformClassPathPackage(Path path) {
    addPackage(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  /**
   * Add a package to the {@link StandardLocation#NATIVE_HEADER_OUTPUT native header outputs}.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addNativeHeaderOutputModule(String, Path)
   * @see #createNativeHeaderOutputPackage()
   * @see #createNativeHeaderOutputModule(String)
   */
  default void addNativeHeaderOutputPackage(Path path) {
    addPackage(StandardLocation.NATIVE_HEADER_OUTPUT, path);
  }

  /**
   * Add a module to the {@link StandardLocation#NATIVE_HEADER_OUTPUT native header outputs}.
   *
   * @param moduleName the name of the module.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addPackage(Location, Path)
   * @see #addNativeHeaderOutputPackage(Path)
   * @see #createNativeHeaderOutputPackage()
   * @see #createNativeHeaderOutputModule(String)
   */
  default void addNativeHeaderOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName, path);
  }

  /**
   * Add a module to the {@link StandardLocation#UPGRADE_MODULE_PATH upgrade module path}.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #createUpgradeModulePathModule(String)
   */
  default void addUpgradeModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName, path);
  }

  /**
   * Add a module to the {@link StandardLocation#SYSTEM_MODULES system module path}.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #createSystemModulePathModule(String)
   */
  default void addSystemModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.SYSTEM_MODULES, moduleName, path);
  }

  /**
   * Add a module to the {@link StandardLocation#PATCH_MODULE_PATH patch module path}.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   * @see #addModule(Location, String, Path)
   * @see #createPatchModulePathModule(String)
   */
  default void addPatchModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.PATCH_MODULE_PATH, moduleName, path);
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
   * purposes, however, this method is the one you will want to be using if your code is not in
   * a <strong>named</strong> module directory
   * (so not something like {@code src/my.module/org/example/...}).
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
   * Create a package in the {@link StandardLocation#PLATFORM_CLASS_PATH platform class path}
   * (also known as the boot class path).
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #addPlatformClassPathPackage(Path)
   */
  default ManagedDirectory createPlatformClassPathPackage() {
    return createPackage(StandardLocation.PLATFORM_CLASS_PATH);
  }

  /**
   * Create a package in the {@link StandardLocation#NATIVE_HEADER_OUTPUT native header outputs}.
   *
   * @return the created test directory.
   * @see #createPackage(Location)
   * @see #createNativeHeaderOutputModule(String)
   * @see #addNativeHeaderOutputPackage(Path)
   * @see #addNativeHeaderOutputModule(String, Path)
   */
  default ManagedDirectory createNativeHeaderOutputPackage() {
    return createPackage(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Create a module in the {@link StandardLocation#NATIVE_HEADER_OUTPUT native header outputs}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createNativeHeaderOutputPackage()
   * @see #addNativeHeaderOutputPackage(Path)
   * @see #addNativeHeaderOutputModule(String, Path)
   */
  default ManagedDirectory createNativeHeaderOutputModule(String moduleName) {
    return createModule(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName);
  }

  /**
   * Create a module in the {@link StandardLocation#UPGRADE_MODULE_PATH upgrade module path}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #addUpgradeModulePathModule(String, Path)
   */
  default ManagedDirectory createUpgradeModulePathModule(String moduleName) {
    return createModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName);
  }

  /**
   * Create a module in the {@link StandardLocation#SYSTEM_MODULES system module path}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #addSystemModulePathModule(String, Path)
   */
  default ManagedDirectory createSystemModulePathModule(String moduleName) {
    return createModule(StandardLocation.SYSTEM_MODULES, moduleName);
  }

  /**
   * Create a module in the {@link StandardLocation#PATCH_MODULE_PATH patch module path}.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   * @see #createModule(Location, String)
   * @see #createPatchModulePathModule(String)
   */
  default ManagedDirectory createPatchModulePathModule(String moduleName) {
    return createModule(StandardLocation.PATCH_MODULE_PATH, moduleName);
  }
}
