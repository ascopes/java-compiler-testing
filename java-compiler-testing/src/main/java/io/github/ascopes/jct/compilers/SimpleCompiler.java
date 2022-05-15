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

import static io.github.ascopes.jct.intern.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.paths.PathJavaFileObjectFactory;
import io.github.ascopes.jct.paths.PathLocationRepository;
import io.github.ascopes.jct.paths.RamPath;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common functionality for a compiler that can be overridden and that produces a
 * {@link SimpleCompilation} as the compilation result.
 *
 * <p>Implementations should extend this class and override anything they require.
 * In most cases, you should not need to override anything other than the constructor.
 *
 * <p>Each instance of this class should be considered to be single-use. Mutation
 * does <strong>not</strong> produce a new instance, meaning that this class is not immutable by
 * design.
 *
 * <p>If you wish to create a common set of configuration settings for instances of
 * this class, you should consider writing a custom {@link CompilerConfigurer} object to apply the
 * desired operations, and then apply it to instances of this class using
 * {@link #configure(CompilerConfigurer)}.
 *
 * @param <A> the type of the class extending this class.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class SimpleCompiler<A extends SimpleCompiler<A>>
    implements Compiler<A, SimpleCompilation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCompiler.class);
  private final String name;
  private final JavaCompiler jsr199Compiler;
  private final FlagBuilder flagBuilder;
  private final PathJavaFileObjectFactory pathJavaFileObjectFactory;
  private final PathLocationRepository fileRepository;
  private final List<Processor> annotationProcessors;
  private final List<String> annotationProcessorOptions;
  private final List<String> compilerOptions;
  private final List<String> runtimeOptions;
  private boolean showWarnings;
  private boolean showDeprecationWarnings;
  private boolean failOnWarnings;
  private Locale locale;
  private Charset logCharset;
  private boolean verbose;
  private boolean previewFeatures;
  private String release;
  private String source;
  private String target;
  private boolean inheritClassPath;
  private boolean inheritModulePath;
  private boolean inheritPlatformClassPath;
  private boolean inheritSystemModulePath;
  private Logging fileManagerLogging;
  private Logging diagnosticLogging;
  private AnnotationProcessorDiscovery annotationProcessorDiscovery;
  private boolean compiled;

  /**
   * Initialize this compiler.
   *
   * @param name           the friendly name of the compiler.
   * @param jsr199Compiler the JSR-199 compiler implementation to use.
   * @param flagBuilder    the flag builder to use.
   */
  protected SimpleCompiler(
      String name,
      JavaCompiler jsr199Compiler,
      FlagBuilder flagBuilder
  ) {
    this.name = requireNonNull(name, "name");
    this.jsr199Compiler = requireNonNull(jsr199Compiler, "jsr199Compiler");
    this.flagBuilder = requireNonNull(flagBuilder, "flagBuilder");

    // We may want to be able to customize creation of missing roots in the future. For now,
    // I am leaving this enabled by default.
    pathJavaFileObjectFactory = new PathJavaFileObjectFactory(DEFAULT_FILE_CHARSET);
    fileRepository = new PathLocationRepository(pathJavaFileObjectFactory);

    annotationProcessors = new ArrayList<>();

    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    showWarnings = DEFAULT_SHOW_WARNINGS;
    showDeprecationWarnings = DEFAULT_SHOW_DEPRECATION_WARNINGS;
    failOnWarnings = DEFAULT_FAIL_ON_WARNINGS;
    locale = DEFAULT_LOCALE;
    logCharset = DEFAULT_LOG_CHARSET;
    previewFeatures = DEFAULT_PREVIEW_FEATURES;
    release = null;
    source = null;
    target = null;
    verbose = DEFAULT_VERBOSE;
    inheritClassPath = DEFAULT_INHERIT_CLASS_PATH;
    inheritModulePath = DEFAULT_INHERIT_MODULE_PATH;
    inheritPlatformClassPath = DEFAULT_INHERIT_PLATFORM_CLASS_PATH;
    inheritSystemModulePath = DEFAULT_INHERIT_SYSTEM_MODULE_PATH;
    fileManagerLogging = DEFAULT_FILE_MANAGER_LOGGING;
    diagnosticLogging = DEFAULT_DIAGNOSTIC_LOGGING;
    annotationProcessorDiscovery = DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
  }

  /**
   * Get the flag builder to use.
   *
   * @return the flag builder.
   */
  public FlagBuilder getFlagBuilder() {
    return flagBuilder;
  }

  /**
   * Get the JSR-199 compiler to use.
   *
   * @return the JSR-199 compiler.
   */
  public JavaCompiler getJsr199Compiler() {
    return jsr199Compiler;
  }

  /**
   * Get the friendly name of the compiler implementation.
   *
   * @return the friendly name.
   */
  public String getName() {
    return name;
  }

  @Override
  public SimpleCompilation compile() {
    // We delegate to a different method here to allow easier testing of additional behaviour
    // internally, such as the ECJ global lock that we apply to prevent bugs. Without this, we'd
    // have to mock dozens of additional moving parts. It is difficult to stub super methods
    // if we go down that route.
    if (compiled) {
      throw new AlreadyUsedCompilerException("There has been a second call to compile() in this Compiler");
    }
	compiled = true;
    return doCompile();
  }

  @Override
  public final <T extends Exception> A configure(CompilerConfigurer<A, T> configurer) throws T {
    LOGGER.debug("configure({})", configurer);
    var me = myself();
    configurer.configure(me);
    return me;
  }

  @Override
  public A addPaths(Location location, Collection<? extends Path> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreateManager(location).addPaths(paths);
    return myself();
  }

  @Override
  public A addRamPaths(Location location, Collection<? extends RamPath> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreateManager(location).addRamPaths(paths);
    return myself();
  }

  @Override
  public PathLocationRepository getPathLocationRepository() {
    return fileRepository;
  }

  @Override
  public Charset getFileCharset() {
    return pathJavaFileObjectFactory.getCharset();
  }

  @Override
  public A fileCharset(Charset fileCharset) {
    requireNonNull(fileCharset, "fileCharset");
    LOGGER.trace("fileCharset {} -> {}", pathJavaFileObjectFactory.getCharset(), fileCharset);
    pathJavaFileObjectFactory.setCharset(fileCharset);
    return myself();
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public A verbose(boolean enabled) {
    LOGGER.trace("verbose {} -> {}", verbose, enabled);
    verbose = enabled;
    return myself();
  }

  @Override
  public boolean isPreviewFeatures() {
    return previewFeatures;
  }

  @Override
  public A previewFeatures(boolean enabled) {
    LOGGER.trace("previewFeatures {} -> {}", previewFeatures, enabled);
    previewFeatures = enabled;
    return myself();
  }

  @Override
  public boolean isShowWarnings() {
    return showWarnings;
  }

  @Override
  public A showWarnings(boolean enabled) {
    LOGGER.trace("showWarnings {} -> {}", showWarnings, enabled);
    showWarnings = enabled;
    return myself();
  }

  @Override
  public boolean isShowDeprecationWarnings() {
    return showDeprecationWarnings;
  }

  @Override
  public A showDeprecationWarnings(boolean enabled) {
    LOGGER.trace("showDeprecationWarnings {} -> {}", showDeprecationWarnings, enabled);
    showDeprecationWarnings = enabled;
    return myself();
  }

  @Override
  public boolean isFailOnWarnings() {
    return failOnWarnings;
  }

  @Override
  public A failOnWarnings(boolean enabled) {
    LOGGER.trace("failOnWarnings {} -> {}", failOnWarnings, enabled);
    failOnWarnings = enabled;
    return myself();
  }

  @Override
  public List<String> getAnnotationProcessorOptions() {
    return List.copyOf(annotationProcessorOptions);
  }

  @Override
  public A addAnnotationProcessorOptions(Iterable<String> annotationProcessorOptions) {
    requireNonNullValues(annotationProcessorOptions, "annotationProcessorOptions");
    LOGGER.trace("annotationProcessorOptions += {}", annotationProcessorOptions);
    annotationProcessorOptions.forEach(this.annotationProcessorOptions::add);
    return myself();
  }

  @Override
  public Set<Processor> getAnnotationProcessors() {
    return Set.copyOf(annotationProcessors);
  }

  @Override
  public A addAnnotationProcessors(Iterable<? extends Processor> annotationProcessors) {
    requireNonNullValues(annotationProcessors, "annotationProcessors");
    LOGGER.trace("annotationProcessors += {}", annotationProcessors);
    annotationProcessors.forEach(this.annotationProcessors::add);
    return myself();
  }

  @Override
  public List<String> getCompilerOptions() {
    return List.copyOf(compilerOptions);
  }

  @Override
  public A addCompilerOptions(Iterable<String> compilerOptions) {
    requireNonNullValues(compilerOptions, "compilerOptions");
    LOGGER.trace("compilerOptions += {}", compilerOptions);
    compilerOptions.forEach(this.compilerOptions::add);
    return myself();
  }

  @Override
  public List<String> getRuntimeOptions() {
    return List.copyOf(runtimeOptions);
  }

  @Override
  public A addRuntimeOptions(Iterable<String> runtimeOptions) {
    requireNonNullValues(runtimeOptions, "runtimeOptions");
    LOGGER.trace("runtimeOptions += {}", runtimeOptions);
    runtimeOptions.forEach(this.runtimeOptions::add);
    return myself();
  }

  @Override
  public Optional<String> getRelease() {
    return Optional.ofNullable(release);
  }

  @Override
  public A release(String release) {
    LOGGER.trace("release {} -> {}", this.release, release);
    this.release = release;
    LOGGER.trace("source {} -> null", source);
    source = null;
    LOGGER.trace("target {} -> null", target);
    target = null;
    return myself();
  }

  @Override
  public Optional<String> getSource() {
    return Optional.ofNullable(source);
  }

  @Override
  public A source(String source) {
    LOGGER.trace("source {} -> {}", target, source);
    this.source = source;
    LOGGER.trace("release {} -> null", release);
    release = null;
    return myself();
  }

  @Override
  public Optional<String> getTarget() {
    return Optional.ofNullable(target);
  }

  @Override
  public A target(String target) {
    LOGGER.trace("target {} -> {}", this.target, target);
    this.target = target;
    LOGGER.trace("release {} -> null", release);
    release = null;
    return myself();
  }

  @Override
  public boolean isInheritClassPath() {
    return inheritClassPath;
  }

  @Override
  public A inheritClassPath(boolean inheritClassPath) {
    LOGGER.trace("inheritClassPath {} -> {}", this.inheritClassPath, inheritClassPath);
    this.inheritClassPath = inheritClassPath;
    return myself();
  }

  @Override
  public boolean isInheritModulePath() {
    return inheritModulePath;
  }

  @Override
  public A inheritModulePath(boolean inheritModulePath) {
    LOGGER.trace("inheritModulePath {} -> {}", this.inheritModulePath, inheritModulePath);
    this.inheritModulePath = inheritModulePath;
    return myself();
  }

  @Override
  public boolean isInheritPlatformClassPath() {
    return inheritPlatformClassPath;
  }

  @Override
  public A inheritPlatformClassPath(boolean inheritPlatformClassPath) {
    LOGGER.trace(
        "inheritPlatformClassPath {} -> {}",
        this.inheritPlatformClassPath,
        inheritPlatformClassPath
    );
    this.inheritPlatformClassPath = inheritPlatformClassPath;
    return myself();
  }

  @Override
  public boolean isInheritSystemModulePath() {
    return inheritSystemModulePath;
  }

  @Override
  public A inheritSystemModulePath(boolean inheritSystemModulePath) {
    LOGGER.trace(
        "inheritSystemModulePath {} -> {}",
        this.inheritSystemModulePath,
        inheritSystemModulePath
    );
    this.inheritSystemModulePath = inheritSystemModulePath;
    return myself();
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public A locale(Locale locale) {
    requireNonNull(locale, "locale");
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = locale;
    return myself();
  }

  @Override
  public Charset getLogCharset() {
    return logCharset;
  }

  @Override
  public A logCharset(Charset logCharset) {
    requireNonNull(logCharset, "logCharset");
    LOGGER.trace("logCharset {} -> {}", this.logCharset, logCharset);
    this.logCharset = logCharset;
    return myself();
  }

  @Override
  public Logging getFileManagerLogging() {
    return fileManagerLogging;
  }

  @Override
  public A fileManagerLogging(Logging fileManagerLogging) {
    requireNonNull(fileManagerLogging, "fileManagerLogging");
    LOGGER.trace(
        "fileManagerLogging {} -> {}",
        this.fileManagerLogging,
        fileManagerLogging
    );
    this.fileManagerLogging = fileManagerLogging;
    return myself();
  }

  @Override
  public Logging getDiagnosticLogging() {
    return diagnosticLogging;
  }

  @Override
  public A diagnosticLogging(Logging diagnosticLogging) {
    requireNonNull(diagnosticLogging, "diagnosticLogging");
    LOGGER.trace(
        "diagnosticLogging {} -> {}",
        this.diagnosticLogging,
        diagnosticLogging
    );
    this.diagnosticLogging = diagnosticLogging;
    return myself();
  }

  @Override
  public AnnotationProcessorDiscovery getAnnotationProcessorDiscovery() {
    return annotationProcessorDiscovery;
  }

  @Override
  public A annotationProcessorDiscovery(AnnotationProcessorDiscovery annotationProcessorDiscovery) {
    requireNonNull(annotationProcessorDiscovery, "annotationProcessorDiscovery");
    LOGGER.trace(
        "annotationProcessorDiscovery {} -> {}",
        this.annotationProcessorDiscovery,
        annotationProcessorDiscovery
    );
    this.annotationProcessorDiscovery = annotationProcessorDiscovery;
    return myself();
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Get this implementation of {@link SimpleCompiler}, cast to the type parameter {@link A}.
   *
   * @return this implementation of {@link SimpleCompiler}, cast to {@link A}.
   */
  @SuppressWarnings("unchecked")
  protected final A myself() {
    return (A) this;
  }

  /**
   * Perform the compilation.
   *
   * @return the compilation result.
   */
  protected SimpleCompilation doCompile() {
    return new SimpleCompilationFactory<A>().compile(
        myself(),
        jsr199Compiler,
        flagBuilder
    );
  }
}
