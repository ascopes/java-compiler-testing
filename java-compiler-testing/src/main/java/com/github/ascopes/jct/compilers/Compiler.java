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

import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.paths.RamPath;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
   * Default setting for deprecation warnings ({@code true}).
   */
  boolean DEFAULT_DEPRECATION_WARNINGS = true;

  /**
   * Default setting for locale ({@link Locale#ROOT}).
   */
  Locale DEFAULT_LOCALE = Locale.ROOT;

  /**
   * Default setting for preview features ({@code false}).
   */
  boolean DEFAULT_PREVIEW_FEATURES = false;

  /**
   * Default setting for verbose logging ({@code false}).
   */
  boolean DEFAULT_VERBOSE = false;

  /**
   * Default setting for displaying warnings ({@code true}).
   */
  boolean DEFAULT_WARNINGS = true;

  /**
   * Default setting for displaying warnings as errors ({@code false}).
   */
  boolean DEFAULT_WARNINGS_AS_ERRORS = false;

  /**
   * Default setting for inclusion of the current class path ({@code true}).
   */
  boolean DEFAULT_INCLUDE_CURRENT_CLASS_PATH = true;

  /**
   * Default setting for inclusion of the current module path ({@code true}).
   */
  boolean DEFAULT_INCLUDE_CURRENT_MODULE_PATH = true;

  /**
   * Default setting for inclusion of the current platform class path ({@code true}).
   */
  boolean DEFAULT_INCLUDE_CURRENT_PLATFORM_CLASS_PATH = true;

  /**
   * Default setting for logging file manager operations ({@link LoggingMode#DISABLED}).
   */
  LoggingMode DEFAULT_FILE_MANAGER_LOGGING_MODE = LoggingMode.DISABLED;

  /**
   * Default setting for logging diagnostics ({@link LoggingMode#ENABLED}).
   */
  LoggingMode DEFAULT_DIAGNOSTIC_LOGGING_MODE = LoggingMode.ENABLED;

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
   * Get the path location repository holding any paths that have been added.
   *
   * <p>This is mutable, and care should be taken if using this interface directly.
   *
   * @return the path location repository.
   */
  PathLocationRepository getPathLocationRepository();

  // !!! BUG REGRESSION WARNING FOR THIS API !!!:
  // DO NOT REPLACE COLLECTION<PATH>  WITH ITERABLE<PATH>! THIS WOULD MAKE DIFFERENCES BETWEEN
  // PATH AND COLLECTIONS OF PATHS DIFFICULT TO DISTINGUISH, SINCE PATHS ARE THEMSELVES
  // ITERABLES OF PATHS!

  /**
   * Add paths to the given location.
   *
   * @param location the location to add paths to.
   * @param paths    the paths to add.
   * @return this compiler object for further call chaining.
   */
  C addPaths(Location location, Collection<? extends Path> paths);

  /**
   * Add paths to the given location.
   *
   * @param location the location to add paths to.
   * @param path1    the first path to add.
   * @param paths    additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPaths(Location location, Path path1, Path... paths) {
    return addPaths(location, combineOneOrMore(path1, paths));
  }

  /**
   * Add paths to the class output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.CLASS_OUTPUT, paths);
  }

  /**
   * Add paths to the class output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.CLASS_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.SOURCE_OUTPUT, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.SOURCE_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.CLASS_PATH, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.CLASS_PATH, path1, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.SOURCE_PATH, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.SOURCE_PATH, path1, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, path1, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.PLATFORM_CLASS_PATH, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.PLATFORM_CLASS_PATH, path1, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.MODULE_SOURCE_PATH, path1, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.UPGRADE_MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.SYSTEM_MODULES, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModulePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.SYSTEM_MODULES, path1, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_PATH, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.PATCH_MODULE_PATH, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.PATCH_MODULE_PATH, path1, paths);
  }

  /**
   * Add multiple in-memory directories to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param paths    the in-memory directories to add.
   * @return this compiler object for further call chaining.
   */
  C addRamPaths(Location location, Collection<? extends RamPath> paths);

  /**
   * Add multiple in-memory directories to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param path1    the first in-memory directory to add.
   * @param paths    additional in-memory directories to add.
   * @return this compiler object for further call chaining.
   */
  default C addRamPaths(Location location, RamPath path1, RamPath... paths) {
    return addRamPaths(location, combineOneOrMore(path1, paths));
  }

  /**
   * Add paths to the class output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.CLASS_OUTPUT, paths);
  }

  /**
   * Add paths to the class output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassOutputRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.CLASS_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SOURCE_OUTPUT, paths);
  }

  /**
   * Add paths to the source output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceOutputRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.SOURCE_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.CLASS_PATH, paths);
  }

  /**
   * Add paths to the class path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.CLASS_PATH, path1, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SOURCE_PATH, paths);
  }

  /**
   * Add paths to the source path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourceRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.SOURCE_PATH, path1, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
  }

  /**
   * Add paths to the annotation processor path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, path1, paths);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModuleRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
  }

  /**
   * Add paths to the annotation processor module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModuleRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, paths);
  }

  /**
   * Add paths to the platform class path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.PLATFORM_CLASS_PATH, path1, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputPaths(Path path1, Path... paths) {
    return addPaths(StandardLocation.NATIVE_HEADER_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.NATIVE_HEADER_OUTPUT, paths);
  }

  /**
   * Add paths to the native header output path.
   *
   * @param path1 the first path to add.
   * @param paths additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addNativeHeaderOutputRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.NATIVE_HEADER_OUTPUT, path1, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourceRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
  }

  /**
   * Add paths to the module source path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourceRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.MODULE_SOURCE_PATH, path1, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModuleRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
  }

  /**
   * Add paths to the upgrade module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModuleRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.UPGRADE_MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModuleRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.SYSTEM_MODULES, paths);
  }

  /**
   * Add paths to the system module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSystemModuleRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.SYSTEM_MODULES, path1, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.MODULE_PATH, paths);
  }

  /**
   * Add paths to the module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.MODULE_PATH, path1, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModuleRamPaths(Collection<? extends RamPath> paths) {
    return addRamPaths(StandardLocation.PATCH_MODULE_PATH, paths);
  }

  /**
   * Add paths to the patch module path.
   *
   * @param path1 the first path to add.
   * @param paths any additional paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModuleRamPaths(RamPath path1, RamPath... paths) {
    return addRamPaths(StandardLocation.PATCH_MODULE_PATH, path1, paths);
  }

  /**
   * Determine whether verbose logging is enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_VERBOSE}.
   *
   * <p>Note that enabling this is compiler-specific behaviour. There is no guarantee that the
   * output target or the format or verbosity of output will be consistent between different
   * compiler implementations.
   *
   * @return whether verbose logging is enabled or not.
   */
  boolean isVerboseLoggingEnabled();

  /**
   * Set whether to use verbose output or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_VERBOSE}.
   *
   * <p>Note that enabling this is compiler-specific behaviour. There is no guarantee that the
   * output target or the format or verbosity of output will be consistent between different
   * compiler implementations.
   *
   * @param enabled {@code true} for verbose output, {@code false} for normal output.
   * @return this compiler for further call chaining.
   */
  C verboseLoggingEnabled(boolean enabled);

  /**
   * Determine whether preview features are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PREVIEW_FEATURES}.
   *
   * @return whether preview features are enabled or not.
   */
  boolean isPreviewFeaturesEnabled();

  /**
   * Set whether to enable preview features or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PREVIEW_FEATURES}.
   *
   * @param enabled {@code true} to enable preview features, or {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C previewFeaturesEnabled(boolean enabled);

  /**
   * Determine whether warnings are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_WARNINGS}.
   *
   * @return whether warnings are enabled or not.
   */
  boolean isWarningsEnabled();

  /**
   * Set whether to enable displaying warnings or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_WARNINGS}.
   *
   * @param enabled {@code true} to enable warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C warningsEnabled(boolean enabled);

  /**
   * Determine whether deprecation warnings are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DEPRECATION_WARNINGS}.
   *
   * @return whether deprecation warnings are enabled or not.
   */
  boolean isDeprecationWarningsEnabled();

  /**
   * Set whether to enable deprecation warnings or not.
   *
   * <p>This is ignored if {@link #warningsEnabled(boolean)} is disabled.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DEPRECATION_WARNINGS}.
   *
   * @param enabled {@code true} to enable deprecation warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C deprecationWarningsEnabled(boolean enabled);

  /**
   * Determine whether warnings are being treated as errors or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_WARNINGS_AS_ERRORS}.
   *
   * @return whether warnings are being treated as errors or not.
   */
  boolean isTreatingWarningsAsErrors();

  /**
   * Set whether to enable treating warnings as errors or not.
   *
   * <p>This is ignored if {@link #warningsEnabled(boolean)} is disabled.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_WARNINGS_AS_ERRORS}.
   *
   * @param enabled {@code true} to enable treating warnings as errors. {@code false} to disable
   *                them.
   * @return this compiler object for further call chaining.
   */
  C treatWarningsAsErrors(boolean enabled);

  /**
   * Get an <strong>immutable snapshot view</strong> of the current annotation processor options
   * that are set.
   *
   * @return the current annotation processor options that are set.
   */
  List<String> getAnnotationProcessorOptions();

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
   * Get an <strong>immutable snapshot view</strong> of the current annotation processors that are
   * explicitly set to be run.
   *
   * @return the current annotation processors that are set.
   */
  Set<? extends Processor> getAnnotationProcessors();


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
   * Get an <strong>immutable snapshot view</strong> of the current compiler options that are set.
   *
   * @return the current compiler  options that are set.
   */
  List<String> getCompilerOptions();


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
   * Get an <strong>immutable snapshot view</strong> of the current runtime options that are set.
   *
   * @return the current runtime options that are set.
   */
  List<String> getRuntimeOptions();

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
   * Get the current release version that is set, or an empty optional if left to the compiler
   * default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the release version string.
   */
  Optional<String> getReleaseVersion();

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C withReleaseVersion(String version);

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withReleaseVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a release version less than 0");
    }

    return withReleaseVersion(Integer.toString(version));
  }

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withReleaseVersion(SourceVersion version) {
    return withReleaseVersion(Integer.toString(version.ordinal()));
  }

  /**
   * Get the current source version that is set, or an empty optional if left to the compiler
   * default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the source version string.
   */
  Optional<String> getSourceVersion();

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C withSourceVersion(String version);

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withSourceVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a source version less than 0");
    }

    return withSourceVersion(Integer.toString(version));
  }

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withSourceVersion(SourceVersion version) {
    return withSourceVersion(Integer.toString(version.ordinal()));
  }

  /**
   * Get the current target version that is set, or an empty optional if left to the compiler
   * default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the target version string.
   */
  Optional<String> getTargetVersion();

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  C withTargetVersion(String version);

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withTargetVersion(int version) {
    if (version < 0) {
      throw new IllegalArgumentException("Cannot provide a target version less than 0");
    }

    return withTargetVersion(Integer.toString(version));
  }

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param version the version to set.
   * @return this compiler object for further call chaining.
   */
  default C withTargetVersion(SourceVersion version) {
    return withTargetVersion(Integer.toString(version.ordinal()));
  }


  /**
   * Get whether the current classpath is being included or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_CLASS_PATH}.
   *
   * @return whether the current classpath is being included or not.
   */
  boolean isIncludingCurrentClassPath();

  /**
   * Set whether to include the classpath of the current JVM in the compilation or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_CLASS_PATH}.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentClassPath(boolean enabled);

  /**
   * Get whether the current module path is being included or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_MODULE_PATH}.
   *
   * @return whether the current module path is being included or not.
   */
  boolean isIncludingCurrentModulePath();

  /**
   * Set whether to include the module path of the current JVM in the compilation or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_MODULE_PATH}.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentModulePath(boolean enabled);

  /**
   * Get whether the current platform classpath is being included or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_PLATFORM_CLASS_PATH}.
   *
   * @return whether the current platform classpath is being included or not.
   */
  boolean isIncludingCurrentPlatformClassPath();

  /**
   * Set whether to include the platform classpath of the current JVM in the compilation or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INCLUDE_CURRENT_PLATFORM_CLASS_PATH}.
   *
   * @param enabled {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object.
   */
  C includeCurrentPlatformClassPath(boolean enabled);

  /**
   * Get the output locale.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_LOCALE}.
   *
   * @return the output locale to use.
   */
  Locale getLocale();

  /**
   * Set the output locale.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_LOCALE}.
   *
   * @param locale the locale to use.
   * @return this compiler for further call chaining.
   */
  C withLocale(Locale locale);

  /**
   * Get the current file manager logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @return the current file manager logging mode.
   */
  LoggingMode getFileManagerLogging();

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @param fileManagerLoggingMode the mode to use for file manager logging.
   * @return this compiler for further call chaining.
   */
  C withFileManagerLogging(LoggingMode fileManagerLoggingMode);

  /**
   * Get the current diagnostic logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING_MODE}.
   *
   * @return the current diagnostic logging mode.
   */
  LoggingMode getDiagnosticLogging();

  /**
   * Set how to handle diagnostic capture.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING_MODE}.
   *
   * @param diagnosticLoggingMode the mode to use for diagnostic capture.
   * @return this compiler for further call chaining.
   */
  C withDiagnosticLogging(LoggingMode diagnosticLoggingMode);

  /**
   * Invoke the compilation and return the compilation result.
   *
   * @return the compilation result.
   * @throws CompilerException     if the compiler threw an unhandled exception. This should not
   *                               occur for compilation failures generally.
   * @throws IllegalStateException if no compilation units were found.
   * @throws UncheckedIOException  if an IO error occurs.
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
