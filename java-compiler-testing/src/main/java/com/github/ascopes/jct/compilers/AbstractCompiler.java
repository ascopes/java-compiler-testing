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

import static java.util.Objects.requireNonNull;

import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.paths.RamPath;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common functionality for a compiler that can be overridden.
 *
 * @param <A> the type of the class extending this class.
 * @param <S> the compilation type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractCompiler<A extends AbstractCompiler<A, S>, S extends Compilation>
    implements Compiler<A, S> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompiler.class);

  /**
   * The file repository in use.
   */
  protected final PathLocationRepository fileRepository;

  /**
   * The list of annotation processors in use.
   */
  protected final List<Processor> annotationProcessors;

  /**
   * Options to pass to the annotation processors.
   */
  protected final List<String> annotationProcessorOptions;

  /**
   * Options to pass to the compiler.
   */
  protected final List<String> compilerOptions;

  /**
   * Options to pass to the Java Runtime.
   */
  protected final List<String> runtimeOptions;

  /**
   * Whether warnings are enabled or not.
   */
  protected boolean warnings;

  /**
   * Whether deprecation are enabled or not.
   */
  protected boolean deprecationWarnings;

  /**
   * Whether warnings are treated as errors or not.
   */
  protected boolean warningsAsErrors;

  /**
   * The locale to use for diagnostics.
   */
  protected Locale locale;

  /**
   * Whether the compiler will run in verbose mode or not.
   */
  protected boolean verbose;

  /**
   * Whether preview features are enabled or not.
   */
  protected boolean previewFeatures;

  /**
   * The release version, or {@code null} if not explicitly defined.
   */
  protected String releaseVersion;

  /**
   * The source version, or {@code null} if not explicitly defined.
   */
  protected String sourceVersion;

  /**
   * The target version, or {@code null} if not explicitly defined.
   */
  protected String targetVersion;

  /**
   * Whether the classpath of the JVM calling this compiler will be passed to the compiler or not.
   */
  protected boolean includeCurrentClassPath;

  /**
   * Whether the module path of the JVM calling this compiler will be passed to the compiler or
   * not.
   */
  protected boolean includeCurrentModulePath;

  /**
   * Whether the platform classpath of the JVM calling this compiler will be passed to the compiler
   * or not.
   */
  protected boolean includeCurrentPlatformClassPath;

  /**
   * Logging verbosity for file manager operations.
   */
  protected LoggingMode fileManagerLogging;

  /**
   * Logging verbosity for diagnostic reporting.
   */
  protected LoggingMode diagnosticLogging;

  /**
   * Initialize this compiler handler.
   */
  protected AbstractCompiler() {
    // We may want to be able to customize creation of missing roots in the future. For now,
    // I am leaving this enabled by default.
    fileRepository = new PathLocationRepository();

    annotationProcessors = new ArrayList<>();

    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    warnings = DEFAULT_WARNINGS;
    deprecationWarnings = DEFAULT_DEPRECATION_WARNINGS;
    warningsAsErrors = DEFAULT_WARNINGS_AS_ERRORS;
    locale = DEFAULT_LOCALE;
    previewFeatures = DEFAULT_PREVIEW_FEATURES;
    releaseVersion = null;
    sourceVersion = null;
    targetVersion = null;
    verbose = DEFAULT_VERBOSE;
    includeCurrentClassPath = DEFAULT_INCLUDE_CURRENT_CLASS_PATH;
    includeCurrentModulePath = DEFAULT_INCLUDE_CURRENT_MODULE_PATH;
    includeCurrentPlatformClassPath = DEFAULT_INCLUDE_CURRENT_PLATFORM_CLASS_PATH;

    fileManagerLogging = DEFAULT_FILE_MANAGER_LOGGING_MODE;
    diagnosticLogging = DEFAULT_DIAGNOSTIC_LOGGING_MODE;
  }

  @Override
  public final <T extends Exception> A configure(CompilerConfigurer<A, T> configurer) throws T {
    LOGGER.debug("configure({})", configurer);
    var me = myself();
    configurer.configure(me);
    return me;
  }

  @Override
  public PathLocationRepository getPathLocationRepository() {
    return fileRepository;
  }

  @Override
  public boolean isVerboseLoggingEnabled() {
    return verbose;
  }

  @Override
  public A verboseLoggingEnabled(boolean enabled) {
    LOGGER.trace("verbose {} -> {}", verbose, enabled);
    verbose = enabled;
    return myself();
  }

  @Override
  public boolean isPreviewFeaturesEnabled() {
    return previewFeatures;
  }

  @Override
  public A previewFeaturesEnabled(boolean enabled) {
    LOGGER.trace("previewFeatures {} -> {}", previewFeatures, enabled);
    previewFeatures = enabled;
    return myself();
  }

  @Override
  public boolean isWarningsEnabled() {
    return warnings;
  }

  @Override
  public A warningsEnabled(boolean enabled) {
    LOGGER.trace("warnings {} -> {}", warnings, enabled);
    warnings = enabled;
    return myself();
  }

  @Override
  public boolean isDeprecationWarningsEnabled() {
    return deprecationWarnings;
  }

  @Override
  public A deprecationWarningsEnabled(boolean enabled) {
    LOGGER.trace("deprecationWarnings {} -> {}", deprecationWarnings, enabled);
    deprecationWarnings = enabled;
    return myself();
  }

  @Override
  public boolean isTreatingWarningsAsErrors() {
    return warningsAsErrors;
  }

  @Override
  public A treatWarningsAsErrors(boolean enabled) {
    LOGGER.trace("warningsAsErrors {} -> {}", warningsAsErrors, enabled);
    warningsAsErrors = enabled;
    return myself();
  }

  @Override
  public List<String> getAnnotationProcessorOptions() {
    return List.copyOf(annotationProcessorOptions);
  }

  @Override
  public A addAnnotationProcessorOptions(Iterable<String> options) {
    LOGGER.trace("annotationProcessorOptions += {}", options);
    for (var option : requireNonNull(options)) {
      annotationProcessorOptions.add(requireNonNull(option));
    }
    return myself();
  }

  @Override
  public Set<Processor> getAnnotationProcessors() {
    return Set.copyOf(annotationProcessors);
  }

  @Override
  public A addAnnotationProcessors(Iterable<? extends Processor> processors) {
    LOGGER.trace("annotationProcessors += {}", processors);
    for (var processor : requireNonNull(processors)) {
      annotationProcessors.add(requireNonNull(processor));
    }
    return myself();
  }

  @Override
  public List<String> getCompilerOptions() {
    return List.copyOf(compilerOptions);
  }

  @Override
  public A addCompilerOptions(Iterable<String> options) {
    LOGGER.trace("compilerOptions += {}", options);
    for (var option : requireNonNull(options)) {
      compilerOptions.add(requireNonNull(option));
    }
    return myself();
  }

  @Override
  public List<String> getRuntimeOptions() {
    return List.copyOf(runtimeOptions);
  }

  @Override
  public A addRuntimeOptions(Iterable<String> options) {
    LOGGER.trace("runtimeOptions += {}", options);
    for (var option : requireNonNull(options)) {
      runtimeOptions.add(requireNonNull(option));
    }
    return myself();
  }

  @Override
  public Optional<String> getReleaseVersion() {
    return Optional.ofNullable(releaseVersion);
  }

  @Override
  public A withReleaseVersion(String version) {
    LOGGER.trace("releaseVersion {} -> {}", releaseVersion, version);
    LOGGER.trace("sourceVersion {} -> null", sourceVersion);
    LOGGER.trace("targetVersion {} -> null", targetVersion);
    releaseVersion = version;
    sourceVersion = null;
    targetVersion = null;
    return myself();
  }

  @Override
  public Optional<String> getSourceVersion() {
    return Optional.ofNullable(sourceVersion);
  }

  @Override
  public A withSourceVersion(String version) {
    LOGGER.trace("sourceVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    sourceVersion = version;
    return myself();
  }

  @Override
  public Optional<String> getTargetVersion() {
    return Optional.ofNullable(targetVersion);
  }

  @Override
  public A withTargetVersion(String version) {
    LOGGER.trace("targetVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    targetVersion = version;
    return myself();
  }

  @Override
  public boolean isIncludingCurrentClassPath() {
    return includeCurrentClassPath;
  }

  @Override
  public A includeCurrentClassPath(boolean enabled) {
    LOGGER.trace("includeCurrentClassPath {} -> {}", includeCurrentClassPath, enabled);
    includeCurrentClassPath = enabled;
    return myself();
  }

  @Override
  public boolean isIncludingCurrentModulePath() {
    return includeCurrentModulePath;
  }

  @Override
  public A includeCurrentModulePath(boolean enabled) {
    LOGGER.trace("includeCurrentModulePath {} -> {}", includeCurrentModulePath, enabled);
    includeCurrentModulePath = enabled;
    return myself();
  }

  @Override
  public boolean isIncludingCurrentPlatformClassPath() {
    return includeCurrentPlatformClassPath;
  }

  @Override
  public A includeCurrentPlatformClassPath(boolean enabled) {
    LOGGER.trace(
        "includeCurrentPlatformClassPath {} -> {}",
        includeCurrentPlatformClassPath,
        enabled
    );
    includeCurrentPlatformClassPath = enabled;
    return myself();
  }

  @Override
  public A addPaths(Location location, Collection<? extends Path> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addPaths(paths);
    return myself();
  }

  @Override
  public A addRamPaths(Location location, Collection<? extends RamPath> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addRamPaths(paths);
    return myself();
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public A withLocale(Locale locale) {
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = requireNonNull(locale);
    return myself();
  }

  @Override
  public LoggingMode getFileManagerLogging() {
    return fileManagerLogging;
  }

  @Override
  public A withFileManagerLogging(LoggingMode fileManagerLogging) {
    LOGGER.trace(
        "fileManagerLoggingMode {} -> {}",
        this.fileManagerLogging,
        fileManagerLogging
    );
    this.fileManagerLogging = requireNonNull(fileManagerLogging);
    return myself();
  }

  @Override
  public LoggingMode getDiagnosticLogging() {
    return diagnosticLogging;
  }

  @Override
  public A withDiagnosticLogging(LoggingMode diagnosticLogging) {
    LOGGER.trace(
        "diagnosticLoggingMode {} -> {}",
        this.diagnosticLogging,
        diagnosticLogging
    );
    this.diagnosticLogging = requireNonNull(diagnosticLogging);
    return myself();
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Get this implementation of {@link AbstractCompiler}, cast to the type parameter {@link A}.
   *
   * @return this implementation of {@link AbstractCompiler}, cast to {@link A}.
   */
  @SuppressWarnings("unchecked")
  protected final A myself() {
    return (A) this;
  }

  /**
   * Get a human-readable name for the compiler.
   *
   * @return the human-readable name.
   */
  protected abstract String getName();
}