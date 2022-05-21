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

package io.github.ascopes.jct.compilers;

import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.IterableUtils;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
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
  boolean DEFAULT_SHOW_DEPRECATION_WARNINGS = true;

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
  boolean DEFAULT_SHOW_WARNINGS = true;

  /**
   * Default setting for displaying warnings as errors ({@code false}).
   */
  boolean DEFAULT_FAIL_ON_WARNINGS = false;

  /**
   * Default setting for inclusion of the current class path ({@code true}).
   */
  boolean DEFAULT_INHERIT_CLASS_PATH = true;

  /**
   * Default setting for inclusion of the current module path ({@code true}).
   */
  boolean DEFAULT_INHERIT_MODULE_PATH = true;

  /**
   * Default setting for inclusion of the current platform class path ({@code true}).
   */
  boolean DEFAULT_INHERIT_PLATFORM_CLASS_PATH = true;

  /**
   * Default setting for inclusion of the system module path ({@code true}).
   */
  boolean DEFAULT_INHERIT_SYSTEM_MODULE_PATH = true;

  /**
   * Default setting for logging file manager operations ({@link Logging#DISABLED}).
   */
  Logging DEFAULT_FILE_MANAGER_LOGGING = Logging.DISABLED;

  /**
   * Default setting for logging diagnostics ({@link Logging#ENABLED}).
   */
  Logging DEFAULT_DIAGNOSTIC_LOGGING = Logging.ENABLED;

  /**
   * Default setting for how to apply annotation processor discovery when no processors are
   * explicitly defined ({@link AnnotationProcessorDiscovery#INCLUDE_DEPENDENCIES}).
   */
  AnnotationProcessorDiscovery DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY =
      AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES;

  /**
   * Default charset to use for reading and writing files ({@link StandardCharsets#UTF_8}).
   */
  Charset DEFAULT_FILE_CHARSET = StandardCharsets.UTF_8;

  /**
   * Default charset to use for compiler logs ({@link StandardCharsets#UTF_8}).
   */
  Charset DEFAULT_LOG_CHARSET = StandardCharsets.UTF_8;

  /**
   * Apply a given configurer to this compiler.
   *
   * @param <T>        any exception that may be thrown.
   * @param configurer the configurer to invoke.
   * @return this compiler object for further call chaining.
   * @throws T any exception that may be thrown by the configurer.
   */
  <T extends Exception> C configure(CompilerConfigurer<C, T> configurer) throws T;

  // !!! BUG REGRESSION WARNING FOR THIS API !!!:
  // DO NOT REPLACE COLLECTION<PATH>  WITH ITERABLE<PATH>! THIS WOULD MAKE DIFFERENCES BETWEEN
  // PATH AND COLLECTIONS OF PATHS DIFFICULT TO DISTINGUISH, SINCE PATHS ARE THEMSELVES
  // ITERABLES OF PATHS!

  /**
   * Add a path-like object to a given location.
   *
   * @param location the location to add.
   * @param pathLike the path-like object to add.
   * @return this compiler object for further call chaining.
   * @throws IllegalArgumentException if the location is
   *                                  {@link Location#isModuleOrientedLocation() module-oriented} or
   *                                  an {@link Location#isOutputLocation()} output location}.
   */
  C addPath(Location location, PathLike pathLike);


  /**
   * Add a path-like object to a given location.
   *
   * @param location the location to add.
   * @param pathLike the path-like object to add.
   * @return this compiler object for further call chaining.
   * @throws IllegalArgumentException if the location is not
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   */
  C addPath(Location location, String moduleName, PathLike pathLike);

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
   * @param annotationProcessorOptions the options to pass.
   * @return this compiler object for further call chaining.
   */
  C addAnnotationProcessorOptions(Iterable<String> annotationProcessorOptions);

  /**
   * Add options to pass to any annotation processors.
   *
   * @param annotationProcessorOption  the first option to pass.
   * @param annotationProcessorOptions additional options to pass.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessorOptions(
      String annotationProcessorOption,
      String... annotationProcessorOptions
  ) {
    return addAnnotationProcessorOptions(
        IterableUtils.combineOneOrMore(annotationProcessorOption, annotationProcessorOptions)
    );
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
   * @param annotationProcessors the processors to invoke.
   * @return this compiler object for further call chaining.
   */
  C addAnnotationProcessors(Iterable<? extends Processor> annotationProcessors);

  /**
   * Add annotation processors to invoke.
   *
   * <p>This bypasses the discovery process of annotation processors provided in
   * {@link #addAnnotationProcessorPaths}.
   *
   * @param annotationProcessor  the first processor to invoke.
   * @param annotationProcessors additional processors to invoke.
   * @return this compiler object for further call chaining.
   */
  default C addAnnotationProcessors(
      Processor annotationProcessor,
      Processor... annotationProcessors
  ) {
    return addAnnotationProcessors(
        IterableUtils.combineOneOrMore(annotationProcessor, annotationProcessors));
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
   * @param compilerOptions the options to add.
   * @return this compiler object for further call chaining.
   */
  C addCompilerOptions(Iterable<String> compilerOptions);

  /**
   * Add command line options to pass to {@code javac}.
   *
   * @param compilerOption  the first option to add.
   * @param compilerOptions additional options to add.
   * @return this compiler object for further call chaining.
   */
  default C addCompilerOptions(String compilerOption, String... compilerOptions) {
    return addCompilerOptions(IterableUtils.combineOneOrMore(compilerOption, compilerOptions));
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
   * @param runtimeOptions the options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  C addRuntimeOptions(Iterable<String> runtimeOptions);

  /**
   * Add options to pass to the Java runtime.
   *
   * @param runtimeOption  the first option to pass to the runtime.
   * @param runtimeOptions additional options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  default C addRuntimeOptions(String runtimeOption, String... runtimeOptions) {
    return addRuntimeOptions(IterableUtils.combineOneOrMore(runtimeOption, runtimeOptions));
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
  boolean isVerbose();

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
  C verbose(boolean enabled);

  /**
   * Determine whether preview features are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PREVIEW_FEATURES}.
   *
   * @return whether preview features are enabled or not.
   */
  boolean isPreviewFeatures();

  /**
   * Set whether to enable preview features or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PREVIEW_FEATURES}.
   *
   * @param enabled {@code true} to enable preview features, or {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C previewFeatures(boolean enabled);

  /**
   * Determine whether warnings are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_SHOW_WARNINGS}.
   *
   * @return whether warnings are enabled or not.
   */
  boolean isShowWarnings();

  /**
   * Set whether to enable displaying warnings or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_SHOW_WARNINGS}.
   *
   * @param enabled {@code true} to enable warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C showWarnings(boolean enabled);

  /**
   * Determine whether deprecation warnings are enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_SHOW_DEPRECATION_WARNINGS}.
   *
   * @return whether deprecation warnings are enabled or not.
   */
  boolean isShowDeprecationWarnings();

  /**
   * Set whether to enable deprecation warnings or not.
   *
   * <p>This is ignored if {@link #showWarnings(boolean)} is disabled.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_SHOW_DEPRECATION_WARNINGS}.
   *
   * @param enabled {@code true} to enable deprecation warnings. {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  C showDeprecationWarnings(boolean enabled);

  /**
   * Determine whether warnings are being treated as errors or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FAIL_ON_WARNINGS}.
   *
   * @return whether warnings are being treated as errors or not.
   */
  boolean isFailOnWarnings();

  /**
   * Set whether to enable treating warnings as errors or not.
   *
   * <p>This is ignored if {@link #showWarnings(boolean)} is disabled.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FAIL_ON_WARNINGS}.
   *
   * @param enabled {@code true} to enable treating warnings as errors. {@code false} to disable
   *                them.
   * @return this compiler object for further call chaining.
   */
  C failOnWarnings(boolean enabled);

  /**
   * Get the default release to use if no release or target version is specified.
   *
   * <p>This can <strong>not</strong> be configured.
   *
   * @return the default release version to use.
   */
  String getDefaultRelease();

  /**
   * Get the current release version that is set, or an empty optional if left to the compiler
   * default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the release version string.
   */
  Optional<String> getRelease();

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   */
  C release(String release);

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   */
  default C release(int release) {
    if (release < 0) {
      throw new IllegalArgumentException("Cannot provide a release version less than 0");
    }

    return release(Integer.toString(release));
  }

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   */
  default C release(SourceVersion release) {
    return release(Integer.toString(release.ordinal()));
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
  Optional<String> getSource();

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   */
  C source(String source);

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   */
  default C source(int source) {
    if (source < 0) {
      throw new IllegalArgumentException("Cannot provide a source version less than 0");
    }

    return source(Integer.toString(source));
  }

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   */
  default C source(SourceVersion source) {
    return source(Integer.toString(source.ordinal()));
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
  Optional<String> getTarget();

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   */
  C target(String target);

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   */
  default C target(int target) {
    if (target < 0) {
      throw new IllegalArgumentException("Cannot provide a target version less than 0");
    }

    return target(Integer.toString(target));
  }

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   */
  default C target(SourceVersion target) {
    return target(Integer.toString(target.ordinal()));
  }

  /**
   * Get whether the class path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @return whether the current class path is being inherited or not.
   */
  boolean isInheritClassPath();

  /**
   * Set whether the class path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @param inheritClassPath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  C inheritClassPath(boolean inheritClassPath);

  /**
   * Get whether the module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @return whether the module path is being inherited or not.
   */
  boolean isInheritModulePath();

  /**
   * Set whether the module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @param inheritModulePath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  C inheritModulePath(boolean inheritModulePath);

  /**
   * Get whether the current platform class path is being inherited from the caller JVM or not.
   *
   * <p>This may also be known as the "bootstrap class path".
   *
   * <p>Default environments probably will not provide this functionality, in which case it will be
   * ignored.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_PLATFORM_CLASS_PATH}.
   *
   * @return whether the platform class path is being inherited or not.
   */
  boolean isInheritPlatformClassPath();

  /**
   * Set whether the current platform class path is being inherited from the caller JVM or not.
   *
   * <p>This may also be known as the "bootstrap class path".
   *
   * <p>Default environments probably will not provide this functionality, in which case it will be
   * ignored.
   *
   * @param inheritPlatformClassPath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  C inheritPlatformClassPath(boolean inheritPlatformClassPath);

  /**
   * Get whether the system module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @return whether the system module path is being inherited or not.
   */
  boolean isInheritSystemModulePath();

  /**
   * Set whether the system module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @param inheritSystemModulePath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  C inheritSystemModulePath(boolean inheritSystemModulePath);

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
  C locale(Locale locale);

  /**
   * Get the charset being used to write compiler logs with.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_LOG_CHARSET}.
   *
   * @return the charset.
   */
  Charset getLogCharset();

  /**
   * Set the charset being used to write compiler logs with.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_LOG_CHARSET}.
   *
   * @param logCharset the charset to use.
   * @return this compiler for further call chaining.
   */
  C logCharset(Charset logCharset);

  /**
   * Get the current file manager logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING}.
   *
   * @return the current file manager logging mode.
   */
  Logging getFileManagerLogging();

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING}.
   *
   * @param fileManagerLogging the mode to use for file manager logging.
   * @return this compiler for further call chaining.
   */
  C fileManagerLogging(Logging fileManagerLogging);

  /**
   * Get the current diagnostic logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING}.
   *
   * @return the current diagnostic logging mode.
   */
  Logging getDiagnosticLogging();

  /**
   * Set how to handle diagnostic capture.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING}.
   *
   * @param diagnosticLogging the mode to use for diagnostic capture.
   * @return this compiler for further call chaining.
   */
  C diagnosticLogging(Logging diagnosticLogging);

  /**
   * Get how to perform annotation processor discovery.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY}.
   *
   * <p>Specifying any annotation processors explicitly with
   * {@link #addAnnotationProcessors(Iterable)} or
   * {@link #addAnnotationProcessors(Processor, Processor...)} will bypass this setting, treating it
   * as being disabled.
   *
   * @return the processor discovery mode to use.
   */
  AnnotationProcessorDiscovery getAnnotationProcessorDiscovery();

  /**
   * Set how to perform annotation processor discovery.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY}.
   *
   * <p>Specifying any annotation processors explicitly with
   * {@link #addAnnotationProcessors(Iterable)} or
   * {@link #addAnnotationProcessors(Processor, Processor...)} will bypass this setting, treating it
   * as being disabled.
   *
   * @param annotationProcessorDiscovery the processor discovery mode to use.
   * @return this compiler for further call chaining.
   */
  C annotationProcessorDiscovery(AnnotationProcessorDiscovery annotationProcessorDiscovery);

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
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  enum Logging {
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

  /**
   * Mode for annotation processor discovery when no explicit processors are provided.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  enum AnnotationProcessorDiscovery {
    /**
     * Discovery is enabled, and will also scan any dependencies in the classpath or module path.
     */
    INCLUDE_DEPENDENCIES,

    /**
     * Discovery is enabled using the provided processor paths.
     */
    ENABLED,

    /**
     * Discovery is disabled.
     */
    DISABLED,
  }

  /**
   * Function representing a configuration operation that can be applied to a compiler.
   *
   * <p>This can allow encapsulating common configuration logic across tests into a single place.
   *
   * @param <C> the compiler type.
   * @param <T> the exception that may be thrown by the configurer.
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  @FunctionalInterface
  interface CompilerConfigurer<C extends Compiler<C, ?>, T extends Exception> {

    /**
     * Apply configuration logic to the given compiler.
     *
     * @param compiler the compiler.
     * @throws T any exception that may be thrown by the configurer.
     */
    void configure(C compiler) throws T;
  }
}
