/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.compilers;

import static com.github.ascopes.jct.intern.CollectionUtils.combineOneOrMore;
import static com.github.ascopes.jct.intern.CollectionUtils.combineTwoOrMore;

import com.github.ascopes.jct.paths.RamPath;
import java.nio.file.Path;
import java.util.Locale;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base definition of a compiler that can be configured to perform a compilation run against
 * sources. This is designed to provide functionality that {@code javac} does by default, for JDK
 * 17.
 *
 * @param <C> the implementation type. This is provided to allow call-chaining the implementation.
 * @param <R> the compilation type that gets returned once compilation completes.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface Compiler<C extends Compiler<C, R>, R extends Compilation> {

  /**
   * Apply a given configurer to this compiler.
   *
   * @param <T>        any exception that may be thrown.
   * @param configurer the configurer to invoke.
   * @return this compiler object for further call chaining.
   * @throws T any exception that may be thrown by the configurer.
   */
  <T extends Exception> C configure(
      CompilerConfigurer<C, T> configurer
  ) throws T;

  /**
   * Add a path to the paths for a given location.
   *
   * @param location the location to add.
   * @param path     the path to add.
   * @return this compiler object for further call chaining.
   */
  C addPath(Location location, Path path);

  /**
   * Add paths to the given location.
   *
   * @param location the location to add paths to.
   * @param paths    the paths to add.
   * @return this compiler object for further call chaining.
   */
  C addPaths(Location location, Iterable<? extends Path> paths);

  /**
   * Add paths to the given location.
   *
   * @param location the location to add paths to.
   * @param path1    the first path to add.
   * @param path2    the second path to add.
   * @param paths    additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPaths(Location location, Path path1, Path path2, Path... paths) {
    return addPaths(location, combineTwoOrMore(path1, path2, paths));
  }

  /**
   * Add a path to the class output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputPath(Path path) {
    return addPath(StandardLocation.CLASS_OUTPUT, path);
  }

  /**
   * Add paths to the class output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.CLASS_OUTPUT, paths);
  }

  /**
   * Add paths to the class output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.CLASS_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the source output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputPath(Path path) {
    return addPath(StandardLocation.SOURCE_OUTPUT, path);
  }

  /**
   * Add paths to the source output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.SOURCE_OUTPUT, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.SOURCE_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the class path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPath(Path path) {
    return addPath(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add paths to the class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.CLASS_PATH, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.CLASS_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the source path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePath(Path path) {
    return addPath(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add paths to the source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.SOURCE_PATH, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.SOURCE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the annotation processor path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPath(Path path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the annotation processor module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePath(Path path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the platform class path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPath(Path path) {
    return addPath(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.PLATFORM_CLASS_PATH, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.PLATFORM_CLASS_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the module source path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePath(Path path) {
    return addPath(StandardLocation.MODULE_SOURCE_PATH, path);
  }

  /**
   * Add paths to the module source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.MODULE_SOURCE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the upgrade module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePath(Path path) {
    return addPath(StandardLocation.UPGRADE_MODULE_PATH, path);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.UPGRADE_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the system module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModulePath(Path path) {
    return addPath(StandardLocation.SYSTEM_MODULES, path);
  }

  /**
   * Add paths to the system module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModulePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.SYSTEM_MODULES, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModulePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.SYSTEM_MODULES, path1, path2, paths);
  }

  /**
   * Add a path to the module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePath(Path path) {
    return addPath(StandardLocation.MODULE_PATH, path);
  }

  /**
   * Add paths to the module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_PATH, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the patch module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePath(Path path) {
    return addPath(StandardLocation.PATCH_MODULE_PATH, path);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.PATCH_MODULE_PATH, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.PATCH_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add an in-memory directory to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param path     the in-memory directory to add.
   * @return this compiler object for further call chaining.
   */
  C addRamPath(Location location, RamPath path);

  /**
   * Add multiple in-memory directories to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param paths    the in-memory directories to add.
   * @return this compiler object for further call chaining.
   */
  C addRamPaths(Location location, Iterable<? extends RamPath> paths);

  /**
   * Add multiple in-memory directories to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param path1    the first in-memory directory to add.
   * @param path2    the second in-memory directory to add.
   * @param paths    additional in-memory directories to add.
   * @return this compiler object for further call chaining.
   */
  default C addRamPaths(
      Location location,
      RamPath path1,
      RamPath path2,
      RamPath... paths
  ) {
    return addRamPaths(location, combineTwoOrMore(path1, path2, paths));
  }

  /**
   * Add a path to the class output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputRamPath(RamPath path) {
    return addRamPath(StandardLocation.CLASS_OUTPUT, path);
  }

  /**
   * Add paths to the class output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.CLASS_OUTPUT, paths);
  }

  /**
   * Add paths to the class output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.CLASS_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the source output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputRamPath(RamPath path) {
    return addRamPath(StandardLocation.SOURCE_OUTPUT, path);
  }

  /**
   * Add paths to the source output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SOURCE_OUTPUT, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.SOURCE_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the class path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassRamPath(RamPath path) {
    return addRamPath(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add paths to the class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.CLASS_PATH, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.CLASS_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the source path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceRamPath(RamPath path) {
    return addRamPath(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add paths to the source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SOURCE_PATH, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.SOURCE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the annotation processor path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorRamPath(RamPath path) {
    return addRamPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the annotation processor module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModuleRamPath(RamPath path) {
    return addRamPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModuleRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModuleRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the platform class path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassRamPath(RamPath path) {
    return addRamPath(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the native header output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputPath(Path path) {
    return addPath(StandardLocation.NATIVE_HEADER_OUTPUT, path);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputPaths(Iterable<? extends Path> paths) {
    return addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputPaths(Path path1, Path path2, Path... paths) {
    return addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the native header output path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputRamPath(RamPath path) {
    return addRamPath(StandardLocation.NATIVE_HEADER_OUTPUT, path);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.NATIVE_HEADER_OUTPUT, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.NATIVE_HEADER_OUTPUT, path1, path2, paths);
  }

  /**
   * Add a path to the module source path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourceRamPath(RamPath path) {
    return addRamPath(StandardLocation.MODULE_SOURCE_PATH, path);
  }

  /**
   * Add paths to the module source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourceRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourceRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.MODULE_SOURCE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the upgrade module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModuleRamPath(RamPath path) {
    return addRamPath(StandardLocation.UPGRADE_MODULE_PATH, path);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModuleRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModuleRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the system module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModuleRamPath(RamPath path) {
    return addRamPath(StandardLocation.SYSTEM_MODULES, path);
  }

  /**
   * Add paths to the system module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModuleRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SYSTEM_MODULES, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModuleRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.SYSTEM_MODULES, path1, path2, paths);
  }

  /**
   * Add a path to the module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleRamPath(RamPath path) {
    return addRamPath(StandardLocation.MODULE_PATH, path);
  }

  /**
   * Add paths to the module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.MODULE_PATH, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.MODULE_PATH, path1, path2, paths);
  }

  /**
   * Add a path to the patch module path.
   *
   * @param path the path to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModuleRamPath(RamPath path) {
    return addRamPath(StandardLocation.PATCH_MODULE_PATH, path);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModuleRamPaths(Iterable<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.PATCH_MODULE_PATH, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param path1 the first path to add.
   * @param path2 the second path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModuleRamPaths(RamPath path1, RamPath path2, RamPath... paths) {
    return addRamPaths(StandardLocation.PATCH_MODULE_PATH, path1, path2, paths);
  }

  /**
   * Set whether to use verbose output or not.
   *
   * @param enabled {@code true} for verbose output, {@code false} for normal output.
   * @return this compiler for further call chaining.
   */
  C verbose(boolean enabled);

  /**
   * Set whether to enable preview features or not.
   *
   * @param enabled {@code true} to enable preview features, or {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C previewFeatures(boolean enabled);

  /**
   * Set whether to enable displaying warnings or not.
   *
   * @param enabled {@code true} to enable warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C warnings(boolean enabled);

  /**
   * Set whether to enable deprecation warnings or not.
   *
   * <p>This is ignored if {@link #warnings(boolean)} is disabled.
   *
   * @param enabled {@code true} to enable deprecation warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C deprecationWarnings(boolean enabled);

  /**
   * Set whether to enable treating warnings as errors or not.
   *
   * <p>This is ignored if {@link #warnings(boolean)} is disabled.
   *
   * @param enabled {@code true} to enable treating warnings as errors. {@code false} to disable
   *                them.
   * @return this compiler object for further call chaining.
   */
  C warningsAsErrors(boolean enabled);

  /**
   * Add options to pass to any annotation processors.
   *
   * @param options the options to pass.
   * @return this compiler object for further call chaining.
   */
  C addAnnotationProcessorOptions(Iterable<String> options);

  /**
   * Add options to pass to any annotation processors.
   *
   * @param option  the first option to pass.
   * @param options additional options to pass.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorOptions(String option, String... options) {
    return addAnnotationProcessorOptions(combineOneOrMore(option, options));
  }

  /**
   * Add annotation processors to invoke.
   *
   * <p>This bypasses the discovery process of annotation processors provided in
   * {@link #addAnnotationProcessorPaths}.
   *
   * @param processors the processors to invoke.
   * @return this compiler object for further call chaining.
   */
  C addAnnotationProcessors(Iterable<? extends Processor> processors);

  /**
   * Add annotation processors to invoke.
   *
   * <p>This bypasses the discovery process of annotation processors provided in
   * {@link #addAnnotationProcessorPaths}.
   *
   * @param processor  the first processor to invoke.
   * @param processors additional processors to invoke.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessors(Processor processor, Processor... processors) {
    return addAnnotationProcessors(combineOneOrMore(processor, processors));
  }

  /**
   * Add command line options to pass to {@code javac}.
   *
   * @param options the options to add.
   * @return this compiler object for further call chaining.
   */
  C addCompilerOptions(Iterable<String> options);

  /**
   * Add command line options to pass to {@code javac}.
   *
   * @param option  the first option to add.
   * @param options additional options to add.
   * @return this compiler object for further call chaining.
   */
  default C addCompilerOptions(String option, String... options) {
    return addCompilerOptions(combineOneOrMore(option, options));
  }

  /**
   * Add options to pass to the Java runtime.
   *
   * @param options the options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  C addRuntimeOptions(Iterable<String> options);

  /**
   * Add options to pass to the Java runtime.
   *
   * @param option  the first option to pass to the runtime.
   * @param options additional options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  default C addRuntimeOptions(String option, String... options) {
    return addRuntimeOptions(combineOneOrMore(option, options));
  }

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C releaseVersion(String version);

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C releaseVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a release version less than 0");
    }

    return releaseVersion(Integer.toString(version));
  }

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C releaseVersion(SourceVersion version) {
    return releaseVersion(Integer.toString(version.ordinal()));
  }

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C sourceVersion(String version);

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C sourceVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a source version less than 0");
    }

    return sourceVersion(Integer.toString(version));
  }

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C sourceVersion(SourceVersion version) {
    return sourceVersion(Integer.toString(version.ordinal()));
  }

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C targetVersion(String version);

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C targetVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a target version less than 0");
    }

    return targetVersion(Integer.toString(version));
  }

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C targetVersion(SourceVersion version) {
    return targetVersion(Integer.toString(version.ordinal()));
  }

  /**
   * Set whether to include the classpath of the current JVM in the compilation or not.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentClassPath(boolean enabled);

  /**
   * Set whether to include the module path of the current JVM in the compilation or not.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentModulePath(boolean enabled);

  /**
   * Set whether to include the platform classpath of the current JVM in the compilation or not.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentPlatformClassPath(boolean enabled);

  /**
   * Set the output locale.
   *
   * @param locale the locale to use.
   * @return this compiler for further call chaining.
   */
  C locale(Locale locale);

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * @param fileManagerLoggingMode the mode to use for file manager logging.
   * @return this compiler for further call chaining.
   */
  C withFileManagerLogging(LoggingMode fileManagerLoggingMode);

  /**
   * Set how to handle diagnostic capture.
   *
   * @param diagnosticLoggingMode the mode to use for diagnostic capture.
   * @return this compiler for further call chaining.
   */
  C withDiagnosticLogging(LoggingMode diagnosticLoggingMode);

  /**
   * Invoke the compilation and return the compilation result.
   *
   * @return the compilation result.
   * @throws CompilerException            if the compiler threw an unhandled exception. This should
   *                                      not occur for compilation failures generally.
   * @throws IllegalStateException        if no compilation units were found.
   * @throws java.io.UncheckedIOException if an IO error occurs.
   */
  R compile();

  /**
   * Options for how to handle logging on special internal components.
   */
  enum LoggingMode {
    /**
     * Enable basic logging.
     */
    ENABLED,

    /**
     * Enable logging and include stacktraces in the logs for each entry.
     */
    STACKTRACES,

    /**
     * Do not log anything.
     */
    DISABLED,
  }
}
