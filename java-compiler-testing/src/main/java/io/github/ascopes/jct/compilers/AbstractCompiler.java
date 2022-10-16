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
package io.github.ascopes.jct.compilers;

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.ex.CompilerAlreadyUsedException;
import io.github.ascopes.jct.paths.PathLike;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common functionality for a compiler that can be overridden and that produces a
 * {@link CompilationImpl} as the compilation result.
 *
 * <p>Implementations should extend this class and override anything they require.
 * In most cases, you should not need to override anything other than the constructor.
 *
 * <p>Each instance of this class should be considered to be single-use. Mutation does
 * <strong>not</strong> produce a new instance, meaning that this class is not immutable by
 * design.
 *
 * <p>This class is <strong>not</strong> thread-safe.
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
public abstract class AbstractCompiler<A extends AbstractCompiler<A>>
    implements Compilable<A, CompilationImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompiler.class);

  // Use atomics for this to ensure no race conditions
  // if the user makes a mistake during parallel test runs.
  // We do not enforce thread safety but this one will prevent
  // flaky tests at zero cost, so we make an exception for this.
  private final AtomicBoolean alreadyCompiled;

  private final String name;
  private final JavaCompiler jsr199Compiler;
  private final FlagBuilder flagBuilder;
  private final FileManagerBuilder fileManagerTemplate;
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
  private @Nullable String release;
  private @Nullable String source;
  private @Nullable String target;
  private boolean inheritClassPath;
  private boolean inheritModulePath;
  private boolean inheritPlatformClassPath;
  private boolean inheritSystemModulePath;
  private LoggingMode fileManagerLoggingMode;
  private LoggingMode diagnosticLoggingMode;
  private AnnotationProcessorDiscovery annotationProcessorDiscovery;

  /**
   * Initialize this compiler.
   *
   * @param name                the friendly name of the compiler.
   * @param fileManagerTemplate the simple file manager template to use.
   * @param jsr199Compiler      the JSR-199 compiler implementation to use.
   * @param flagBuilder         the flag builder to use.
   */
  protected AbstractCompiler(
      String name,
      FileManagerBuilder fileManagerTemplate,
      JavaCompiler jsr199Compiler,
      FlagBuilder flagBuilder
  ) {
    alreadyCompiled = new AtomicBoolean(false);

    this.name = requireNonNull(name, "name");
    this.fileManagerTemplate = requireNonNull(fileManagerTemplate, "fileManagerTemplate");
    this.jsr199Compiler = requireNonNull(jsr199Compiler, "jsr199Compiler");
    this.flagBuilder = requireNonNull(flagBuilder, "flagBuilder");

    annotationProcessors = new ArrayList<>();
    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    showWarnings = Compilable.DEFAULT_SHOW_WARNINGS;
    showDeprecationWarnings = Compilable.DEFAULT_SHOW_DEPRECATION_WARNINGS;
    failOnWarnings = Compilable.DEFAULT_FAIL_ON_WARNINGS;
    locale = Compilable.DEFAULT_LOCALE;
    logCharset = Compilable.DEFAULT_LOG_CHARSET;
    previewFeatures = Compilable.DEFAULT_PREVIEW_FEATURES;

    release = null;
    source = null;
    target = null;

    verbose = Compilable.DEFAULT_VERBOSE;
    inheritClassPath = Compilable.DEFAULT_INHERIT_CLASS_PATH;
    inheritModulePath = Compilable.DEFAULT_INHERIT_MODULE_PATH;
    inheritPlatformClassPath = Compilable.DEFAULT_INHERIT_PLATFORM_CLASS_PATH;
    inheritSystemModulePath = Compilable.DEFAULT_INHERIT_SYSTEM_MODULE_PATH;
    fileManagerLoggingMode = Compilable.DEFAULT_FILE_MANAGER_LOGGING_MODE;
    diagnosticLoggingMode = Compilable.DEFAULT_DIAGNOSTIC_LOGGING_MODE;
    annotationProcessorDiscovery = Compilable.DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
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
  public CompilationImpl compile() {
    if (alreadyCompiled.getAndSet(true)) {
      throw new CompilerAlreadyUsedException();
    }

    var factory = new CompilationFactory<A>();

    return factory.compile(
        myself(),
        fileManagerTemplate,
        jsr199Compiler,
        flagBuilder
    );
  }

  @Override
  public final <T extends Exception> A configure(
      CompilerConfigurer<? super A, T> configurer
  ) throws T {
    requireNonNull(configurer, "configurer");

    LOGGER.debug("configure({})", configurer);
    var me = myself();
    configurer.configure(me);

    return me;
  }

  @Override
  public A addPath(Location location, PathLike pathLike) {
    requireNonNull(location, "location");
    requireNonNull(pathLike, "pathLike");

    LOGGER.trace("{}.paths += {}", location.getName(), pathLike.getPath());
    fileManagerTemplate.addPath(location, pathLike);

    return myself();
  }

  @Override
  public A addPath(Location location, String moduleName, PathLike pathLike) {
    requireNonNull(location, "location");
    requireNonNull(moduleName, "moduleName");
    requireNonNull(pathLike, "pathLike");

    LOGGER.trace("{}[{}].paths += {}", location.getName(), moduleName, pathLike.getPath());
    fileManagerTemplate.addPath(location, moduleName, pathLike);

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
  public A release(@Nullable String release) {
    LOGGER.trace("release {} -> {}", this.release, release);
    this.release = release;

    if (release != null) {
      LOGGER.trace("source {} -> null", source);
      source = null;

      LOGGER.trace("target {} -> null", target);
      target = null;
    }

    return myself();
  }

  @Override
  public Optional<String> getSource() {
    return Optional.ofNullable(source);
  }

  @Override
  public A source(@Nullable String source) {
    LOGGER.trace("source {} -> {}", target, source);
    this.source = source;

    if (source != null) {
      LOGGER.trace("release {} -> null", release);
      release = null;
    }

    return myself();
  }

  @Override
  public Optional<String> getTarget() {
    return Optional.ofNullable(target);
  }

  @Override
  public A target(@Nullable String target) {
    LOGGER.trace("target {} -> {}", this.target, target);
    this.target = target;

    if (target != null) {
      LOGGER.trace("release {} -> null", release);
      release = null;
    }

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
  public LoggingMode getFileManagerLoggingMode() {
    return fileManagerLoggingMode;
  }

  @Override
  public A fileManagerLoggingMode(LoggingMode fileManagerLoggingMode) {
    requireNonNull(fileManagerLoggingMode, "fileManagerLoggingMode");

    LOGGER.trace(
        "fileManagerLoggingMode {} -> {}",
        this.fileManagerLoggingMode,
        fileManagerLoggingMode
    );
    this.fileManagerLoggingMode = fileManagerLoggingMode;

    return myself();
  }

  @Override
  public LoggingMode getDiagnosticLoggingMode() {
    return diagnosticLoggingMode;
  }

  @Override
  public A diagnosticLoggingMode(LoggingMode diagnosticLoggingMode) {
    requireNonNull(diagnosticLoggingMode, "diagnosticLoggingMode");

    LOGGER.trace(
        "diagnosticLoggingMode {} -> {}",
        this.diagnosticLoggingMode,
        diagnosticLoggingMode
    );
    this.diagnosticLoggingMode = diagnosticLoggingMode;

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
  public final String toString() {
    return name;
  }

  /**
   * Get this implementation of {@link AbstractCompiler}, cast to the type parameter {@link A}.
   *
   * @return this implementation of {@link AbstractCompiler}, cast to {@link A}.
   */
  protected final A myself() {
    @SuppressWarnings("unchecked")
    var me = (A) this;

    return me;
  }
}
