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
package io.github.ascopes.jct.workspaces;

import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.workspaces.impl.WorkspaceImpl;
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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface Workspace extends AutoCloseable {

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
   * @param location the location to associate with.
   * @param path     the path to add.
   * @throws IllegalArgumentException if the inputs are invalid.
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
   * @param location   the location to associate with.
   * @param moduleName the name of the module.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the inputs are invalid.
   */
  void addModule(Location location, String moduleName, Path path);

  /**
   * Add a package to the class outputs.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addClassOutputPackage(Path path) {
    addPackage(StandardLocation.CLASS_OUTPUT, path);
  }

  /**
   * Add a module to the class outputs.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addClassOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.CLASS_OUTPUT, moduleName, path);
  }

  /**
   * Add a package to the source outputs.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addSourceOutputPackage(Path path) {
    addPackage(StandardLocation.SOURCE_OUTPUT, path);
  }

  /**
   * Add a module to the source outputs.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addSourceOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.SOURCE_OUTPUT, moduleName, path);
  }

  /**
   * Add a package to the class path.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addClassPathPackage(Path path) {
    addPackage(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add a module to the module path.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.MODULE_PATH, moduleName, path);
  }

  /**
   * Add a package to the source path.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addSourcePathPackage(Path path) {
    addPackage(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add a module to the source path.
   *
   * <p>Note that this will signal to the compiler to run in multi-module compilation mode.
   * Any source packages that were {@link #addSourcePathPackage(Path) added} or
   * {@link #createSourcePathPackage()}  created} will be ignored.
   *
   * <p>If you are using a single-module, you can
   * {@link #addSourcePathPackage(Path) add a source package} and include a {@code module-info.java}
   * in the base directory instead.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addSourcePathModule(String moduleName, Path path) {
    addModule(StandardLocation.MODULE_SOURCE_PATH, moduleName, path);
  }

  /**
   * Add a package to the annotation processor path.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addAnnotationProcessorPathPackage(Path path) {
    addPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add a module to the annotation processor path.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addAnnotationProcessorPathModule(String moduleName, Path path) {
    addModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName, path);
  }

  /**
   * Add a package to the platform class path (also known as the boot class path).
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addPlatformClassPathPackage(Path path) {
    addPackage(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  /**
   * Add a package to the native header outputs.
   *
   * @param path the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addNativeHeaderOutputPackage(Path path) {
    addPackage(StandardLocation.NATIVE_HEADER_OUTPUT, path);
  }

  /**
   * Add a module to the native header outputs.
   *
   * @param moduleName the name of the module.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addNativeHeaderOutputModule(String moduleName, Path path) {
    addModule(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName, path);
  }

  /**
   * Add a module to the upgrade module path.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addUpgradeModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName, path);
  }

  /**
   * Add a module to the system module path.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addSystemModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.SYSTEM_MODULES, moduleName, path);
  }

  /**
   * Add a module to the patch module path.
   *
   * @param moduleName the module name.
   * @param path       the path to add.
   * @throws IllegalArgumentException if the path does not exist.
   */
  default void addPatchModulePathModule(String moduleName, Path path) {
    addModule(StandardLocation.PATCH_MODULE_PATH, moduleName, path);
  }

  /**
   * Attempt to close all resources in this workspace.
   *
   * @throws UncheckedIOException if an error occurs.
   */
  @Override
  void close();

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
   * @param location the location to associate with.
   * @return the test directory that was created.
   * @throws IllegalArgumentException if the inputs are invalid.
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
   * @param location   the location to associate with.
   * @param moduleName the module name to use.
   * @return the test directory that was created.
   * @throws IllegalArgumentException if the inputs are invalid.
   */
  ManagedDirectory createModule(Location location, String moduleName);

  /**
   * Create a package in the class outputs.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createClassOutputPackage() {
    return createPackage(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Create a module in the class outputs.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createClassOutputModule(String moduleName) {
    return createModule(StandardLocation.CLASS_OUTPUT, moduleName);
  }

  /**
   * Create a package in the source outputs.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createSourceOutputPackage() {
    return createPackage(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Create a module in the source outputs.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createSourceOutputModule(String moduleName) {
    return createModule(StandardLocation.SOURCE_OUTPUT, moduleName);
  }

  /**
   * Create a package in the class path.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createClassPathPackage() {
    return createPackage(StandardLocation.CLASS_PATH);
  }

  /**
   * Create a module in the module path.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createModulePathModule(String moduleName) {
    return createModule(StandardLocation.MODULE_PATH, moduleName);
  }

  /**
   * Create a package in the source path.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createSourcePathPackage() {
    return createPackage(StandardLocation.SOURCE_PATH);
  }

  /**
   * Create a module in the source path.
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
   */
  default ManagedDirectory createSourcePathModule(String moduleName) {
    return createModule(StandardLocation.MODULE_SOURCE_PATH, moduleName);
  }

  /**
   * Create a package in the annotation processor path.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createAnnotationProcessorPathPackage() {
    return createPackage(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Create a module in the annotation processor path.
   *
   * <p>Note that this will be ignored if the compiler is provided with explicit annotation
   * processor instances to run.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createAnnotationProcessorPathModule(String moduleName) {
    return createModule(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, moduleName);
  }

  /**
   * Create a package in the platform class path (also known as the boot class path).
   *
   * @return the created test directory.
   */
  default ManagedDirectory createPlatformClassPathPackage() {
    return createPackage(StandardLocation.PLATFORM_CLASS_PATH);
  }

  /**
   * Create a package in the native header outputs.
   *
   * @return the created test directory.
   */
  default ManagedDirectory createNativeHeaderOutputPackage() {
    return createPackage(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Create a module in the native header outputs.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createNativeHeaderOutputModule(String moduleName) {
    return createModule(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName);
  }

  /**
   * Create a module in the upgrade module path.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createUpgradeModulePathModule(String moduleName) {
    return createModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName);
  }

  /**
   * Create a module in the system module path.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createSystemModulePathModule(String moduleName) {
    return createModule(StandardLocation.SYSTEM_MODULES, moduleName);
  }

  /**
   * Create a module in the patch module path.
   *
   * @param moduleName the module name.
   * @return the created test directory.
   */
  default ManagedDirectory createPatchModulePathModule(String moduleName) {
    return createModule(StandardLocation.PATCH_MODULE_PATH, moduleName);
  }

  /**
   * Get an immutable copy of the current paths to operate on.
   *
   * @return the paths.
   */
  Map<Location, ? extends List<? extends PathRoot>> getAllPaths();

  /**
   * Get the path strategy in use.
   *
   * @return the path strategy.
   */
  PathStrategy getPathStrategy();

  /**
   * Create a new default workspace instance using the default path strategy.
   *
   * @return the workspace.
   */
  static Workspace newWorkspace() {
    return newWorkspace(PathStrategy.defaultStrategy());
  }

  /**
   * Create a new default workspace instance using the given path strategy.
   *
   * @param pathStrategy the path strategy to use.
   * @return the workspace.
   */
  static Workspace newWorkspace(PathStrategy pathStrategy) {
    return new WorkspaceImpl(pathStrategy);
  }
}
