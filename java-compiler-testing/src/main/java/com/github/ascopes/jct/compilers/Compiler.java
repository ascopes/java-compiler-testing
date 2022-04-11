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

import com.github.ascopes.jct.paths.InMemoryPath;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
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
   * @param options the options to pass.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorOptions(String... options) {
    return addAnnotationProcessorOptions(List.of(options));
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
   * @param processors the processors to invoke.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessors(Processor... processors) {
    return addAnnotationProcessors(List.of(processors));
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
   * @param options the options to add.
   * @return this compiler object for further call chaining.
   */
  default C addCompilerOptions(String... options) {
    return addCompilerOptions(List.of(options));
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
    return releaseVersion(version.ordinal());
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
    return sourceVersion(version.ordinal());
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
    return targetVersion(version.ordinal());
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
   * Add options to pass to the Java runtime.
   *
   * @param options the options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  C addRuntimeOptions(Iterable<String> options);

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
   * Add a path to the paths for a given location.
   *
   * @param location the location to add.
   * @param path     the path to add.
   * @return this compiler object for further call chaining.
   */
  C addPath(Location location, Path path);

  /**
   * Add an in-memory directory to the paths for a given location.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param location the location to add.
   * @param path     the in-memory directory to add.
   * @return this compiler object for further call chaining.
   */
  C addPath(Location location, InMemoryPath path);

  /**
   * Add paths to the given location.
   *
   * @param location the location to add paths to.
   * @param paths    the paths to add.
   * @return this compiler object for further call chaining.
   */
  C addPaths(Location location, Collection<? extends Path> paths);

  /**
   * Add a path to look for annotation processors in.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPath(Path path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add a RAM path to look for annotation processors in.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPath(InMemoryPath path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_PATH, path);
  }

  /**
   * Add paths to look for annotation processors in.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_PATH, paths);
  }

  /**
   * Add a path that contains modules to look for annotation processors in.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePath(Path path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path);
  }

  /**
   * Add a RAM path that contains modules to look for annotation processors in.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePath(InMemoryPath path) {
    return addPath(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, path);
  }

  /**
   * Add paths that contain modules to look for annotation processors in.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, paths);
  }

  /**
   * Add a path to the classpath.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addClassPath(Path path) {
    return addPath(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add a RAM path to the classpath.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addClassPath(InMemoryPath path) {
    return addPath(StandardLocation.CLASS_PATH, path);
  }

  /**
   * Add paths to the classpath.
   *
   * <p>You can add directory trees, JAR files, and WAR files.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addClassPaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.CLASS_PATH, paths);
  }

  /**
   * Add a path to the module path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addModulePath(Path path) {
    return addPath(StandardLocation.MODULE_PATH, path);
  }

  /**
   * Add a RAM path to the module path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addModulePath(InMemoryPath path) {
    return addPath(StandardLocation.MODULE_PATH, path);
  }

  /**
   * Add paths containing modules to the module path.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_PATH, paths);
  }

  /**
   * Add a path to the module source path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePath(Path path) {
    return addPath(StandardLocation.MODULE_SOURCE_PATH, path);
  }

  /**
   * Add a RAM path to the module source path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePath(InMemoryPath path) {
    return addPath(StandardLocation.MODULE_SOURCE_PATH, path);
  }

  /**
   * Add paths containing modules of sources to compile.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addModuleSourcePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.MODULE_SOURCE_PATH, paths);
  }

  /**
   * Add a path to the patch module path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addPathModulePath(Path path) {
    return addPath(StandardLocation.PATCH_MODULE_PATH, path);
  }

  /**
   * Add a RAM path to the patch module path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addPathModulePath(InMemoryPath path) {
    return addPath(StandardLocation.PATCH_MODULE_PATH, path);
  }

  /**
   * Add paths containing patch modules to use during compilation.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addPatchModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.PATCH_MODULE_PATH, paths);
  }

  /**
   * Add a RAM path to the class path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPath(Path path) {
    return addPath(StandardLocation.PLATFORM_CLASS_PATH, path);
  }

  /**
   * Add a RAM path to the platform class path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addPlatformClassPath(InMemoryPath path) {
    return addPath(StandardLocation.PLATFORM_CLASS_PATH, path);
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
   * Add a path to the source path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePath(Path path) {
    return addPath(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add a RAM path to the source path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePath(InMemoryPath path) {
    return addPath(StandardLocation.SOURCE_PATH, path);
  }

  /**
   * Add paths containing sources to compile.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addSourcePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.SOURCE_PATH, paths);
  }

  /**
   * Add a path to the upgrade module path.
   *
   * @param path the path.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePath(Path path) {
    return addPath(StandardLocation.UPGRADE_MODULE_PATH, path);
  }

  /**
   * Add a RAM path to the upgrade module path.
   *
   * <p>Note that this will take ownership of the path in the underlying file repository.
   *
   * @param path the in-memory path.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePath(InMemoryPath path) {
    return addPath(StandardLocation.UPGRADE_MODULE_PATH, path);
  }

  /**
   * Add paths containing upgraded modules to use during compilation.
   *
   * @param paths the paths to add.
   * @return this compiler object for further call chaining.
   */
  default C addUpgradeModulePaths(Collection<? extends Path> paths) {
    return addPaths(StandardLocation.UPGRADE_MODULE_PATH, paths);
  }

  /**
   * Options for how to handle logging on special internal components.
   */
  enum LoggingMode {
    /**
     * Enable logging.
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
