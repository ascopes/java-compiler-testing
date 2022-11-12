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
import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.pathwrappers.TestDirectoryFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Common functionality for a compiler that can be overridden and that produces a
 * {@link JctCompilationImpl} as the compilation result.
 *
 * <p>Implementations should extend this class and override anything they require.
 * In most cases, you should not need to override anything other than the constructor.
 *
 * <p>This class is <strong>not</strong> thread-safe.
 *
 * <p>If you wish to create a common set of configuration settings for instances of
 * this class, you should consider writing a custom {@link JctCompilerConfigurer} object to apply
 * the desired operations, and then apply it to instances of this class using
 * {@link #configure(JctCompilerConfigurer)}.
 *
 * @param <A> the type of the class extending this class.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractJctCompiler<A extends AbstractJctCompiler<A>>
    implements JctCompiler<A, JctCompilationImpl> {

  private final String name;
  private final JavaCompiler jsr199Compiler;
  private final JctFlagBuilder flagBuilder;
  private final FileManagerBuilder fileManagerBuilder;
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
  private LoggingMode diagnosticLoggingMode;

  /**
   * Initialize this compiler.
   *
   * @param name               the friendly name of the compiler.
   * @param fileManagerBuilder the simple file manager template to use.
   * @param jsr199Compiler     the JSR-199 compiler implementation to use.
   * @param flagBuilder        the flag builder to use.
   */
  protected AbstractJctCompiler(
      String name,
      FileManagerBuilder fileManagerBuilder,
      JavaCompiler jsr199Compiler,
      JctFlagBuilder flagBuilder
  ) {
    this.name = requireNonNull(name, "name");
    this.fileManagerBuilder = requireNonNull(fileManagerBuilder, "fileManagerTemplate");
    this.jsr199Compiler = requireNonNull(jsr199Compiler, "jsr199Compiler");
    this.flagBuilder = requireNonNull(flagBuilder, "flagBuilder");

    annotationProcessors = new ArrayList<>();
    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    showWarnings = JctCompiler.DEFAULT_SHOW_WARNINGS;
    showDeprecationWarnings = JctCompiler.DEFAULT_SHOW_DEPRECATION_WARNINGS;
    failOnWarnings = JctCompiler.DEFAULT_FAIL_ON_WARNINGS;
    locale = JctCompiler.DEFAULT_LOCALE;
    logCharset = JctCompiler.DEFAULT_LOG_CHARSET;
    previewFeatures = JctCompiler.DEFAULT_PREVIEW_FEATURES;

    release = null;
    source = null;
    target = null;

    verbose = JctCompiler.DEFAULT_VERBOSE;
    diagnosticLoggingMode = JctCompiler.DEFAULT_DIAGNOSTIC_LOGGING_MODE;
  }

  /**
   * Get the flag builder to use.
   *
   * @return the flag builder.
   */
  public JctFlagBuilder getFlagBuilder() {
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
  public JctCompilationImpl compile() {
    var factory = new JctCompilationFactory<A>();

    return factory.compile(
        myself(),
        fileManagerBuilder,
        jsr199Compiler,
        flagBuilder
    );
  }

  @Override
  public final <T extends Exception> A configure(
      JctCompilerConfigurer<? super A, T> configurer
  ) throws T {
    requireNonNull(configurer, "configurer");
    var me = myself();
    configurer.configure(me);

    return me;
  }

  @Override
  public A addPath(Location location, PathWrapper pathLike) {
    requireNonNull(location, "location");
    requireNonNull(pathLike, "pathLike");
    fileManagerBuilder.addPath(location, pathLike);
    return myself();
  }

  @Override
  public A addPath(Location location, String moduleName, PathWrapper pathLike) {
    requireNonNull(location, "location");
    requireNonNull(moduleName, "moduleName");
    requireNonNull(pathLike, "pathLike");
    fileManagerBuilder.addPath(location, moduleName, pathLike);
    return myself();
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public A verbose(boolean enabled) {
    verbose = enabled;
    return myself();
  }

  @Override
  public boolean isPreviewFeatures() {
    return previewFeatures;
  }

  @Override
  public A previewFeatures(boolean enabled) {
    previewFeatures = enabled;
    return myself();
  }

  @Override
  public boolean isShowWarnings() {
    return showWarnings;
  }

  @Override
  public A showWarnings(boolean enabled) {
    showWarnings = enabled;
    return myself();
  }

  @Override
  public boolean isShowDeprecationWarnings() {
    return showDeprecationWarnings;
  }

  @Override
  public A showDeprecationWarnings(boolean enabled) {
    showDeprecationWarnings = enabled;
    return myself();
  }

  @Override
  public boolean isFailOnWarnings() {
    return failOnWarnings;
  }

  @Override
  public A failOnWarnings(boolean enabled) {
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
    runtimeOptions.forEach(this.runtimeOptions::add);
    return myself();
  }

  @Override
  public Optional<String> getRelease() {
    return Optional.ofNullable(release);
  }

  @Override
  public A release(@Nullable String release) {
    this.release = release;

    if (release != null) {
      source = null;
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
    this.source = source;
    if (source != null) {
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
    this.target = target;
    if (target != null) {
      release = null;
    }
    return myself();
  }

  @Override
  public boolean isFixJvmModulePathMismatch() {
    return fileManagerBuilder.isFixJvmModulePathMismatch();
  }

  @Override
  public A fixJvmModulePathMismatch(boolean fixJvmModulePathMismatch) {
    fileManagerBuilder.setFixJvmModulePathMismatch(fixJvmModulePathMismatch);
    return myself();
  }

  @Override
  public boolean isInheritClassPath() {
    return fileManagerBuilder.isInheritClassPath();
  }

  @Override
  public A inheritClassPath(boolean inheritClassPath) {
    fileManagerBuilder.inheritClassPath(inheritClassPath);
    return myself();
  }

  @Override
  public boolean isInheritModulePath() {
    return fileManagerBuilder.isInheritModulePath();
  }

  @Override
  public A inheritModulePath(boolean inheritModulePath) {
    fileManagerBuilder.inheritModulePath(inheritModulePath);
    return myself();
  }

  @Override
  public boolean isInheritPlatformClassPath() {
    return fileManagerBuilder.isInheritPlatformClassPath();
  }

  @Override
  public A inheritPlatformClassPath(boolean inheritPlatformClassPath) {
    fileManagerBuilder.inheritPlatformClassPath(inheritPlatformClassPath);
    return myself();
  }

  @Override
  public boolean isInheritSystemModulePath() {
    return fileManagerBuilder.isInheritSystemModulePath();
  }

  @Override
  public A inheritSystemModulePath(boolean inheritSystemModulePath) {
    fileManagerBuilder.inheritSystemModulePath(inheritSystemModulePath);
    return myself();
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public A locale(Locale locale) {
    requireNonNull(locale, "locale");
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
    this.logCharset = logCharset;
    return myself();
  }

  @Override
  public LoggingMode getFileManagerLoggingMode() {
    return fileManagerBuilder.getFileManagerLoggingMode();
  }

  @Override
  public A fileManagerLoggingMode(LoggingMode fileManagerLoggingMode) {
    requireNonNull(fileManagerLoggingMode, "fileManagerLoggingMode");
    fileManagerBuilder.fileManagerLoggingMode(fileManagerLoggingMode);
    return myself();
  }

  @Override
  public LoggingMode getDiagnosticLoggingMode() {
    return diagnosticLoggingMode;
  }

  @Override
  public A diagnosticLoggingMode(LoggingMode diagnosticLoggingMode) {
    requireNonNull(diagnosticLoggingMode, "diagnosticLoggingMode");
    this.diagnosticLoggingMode = diagnosticLoggingMode;
    return myself();
  }

  @Override
  public AnnotationProcessorDiscovery getAnnotationProcessorDiscovery() {
    return fileManagerBuilder.getAnnotationProcessorDiscovery();
  }

  @Override
  public A annotationProcessorDiscovery(AnnotationProcessorDiscovery annotationProcessorDiscovery) {
    requireNonNull(annotationProcessorDiscovery, "annotationProcessorDiscovery");
    fileManagerBuilder.annotationProcessorDiscovery(annotationProcessorDiscovery);
    return myself();
  }

  @Override
  public TestDirectoryFactory getTestDirectoryFactory() {
    return fileManagerBuilder.getTestDirectoryFactory();
  }

  @Override
  public A testDirectoryFactory(TestDirectoryFactory testDirectoryFactory) {
    fileManagerBuilder.setTestDirectoryFactory(testDirectoryFactory);
    return myself();
  }

  @Override
  public final String toString() {
    return name;
  }

  /**
   * Get this implementation of {@link AbstractJctCompiler}, cast to the type parameter {@link A}.
   *
   * @return this implementation of {@link AbstractJctCompiler}, cast to {@link A}.
   */
  protected final A myself() {
    @SuppressWarnings("unchecked")
    var me = (A) this;

    return me;
  }
}
