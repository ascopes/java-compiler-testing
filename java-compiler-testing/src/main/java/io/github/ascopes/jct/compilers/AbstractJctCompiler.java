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
package io.github.ascopes.jct.compilers;

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.compilers.impl.JctCompilationFactoryImpl;
import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManagerFactory;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.annotation.processing.Processor;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

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
@API(since = "0.0.1", status = Status.STABLE)
public abstract class AbstractJctCompiler<A extends AbstractJctCompiler<A>>
    implements JctCompiler<A, JctCompilation> {

  private final List<Processor> annotationProcessors;
  private final List<String> annotationProcessorOptions;
  private final List<String> compilerOptions;
  private String name;
  private boolean showWarnings;
  private boolean showDeprecationWarnings;
  private boolean failOnWarnings;
  private CompilationMode compilationMode;
  private Locale locale;
  private Charset logCharset;
  private boolean verbose;
  private boolean previewFeatures;
  private @Nullable String release;
  private @Nullable String source;
  private @Nullable String target;
  private LoggingMode diagnosticLoggingMode;
  private boolean fixJvmModulePathMismatch;
  private boolean inheritClassPath;
  private boolean inheritModulePath;
  private boolean inheritPlatformClassPath;
  private boolean inheritSystemModulePath;
  private LoggingMode fileManagerLoggingMode;
  private AnnotationProcessorDiscovery annotationProcessorDiscovery;

  /**
   * Initialize this compiler.
   *
   * @param defaultName the printable default name to use for the compiler.
   */
  protected AbstractJctCompiler(String defaultName) {
    name = requireNonNull(defaultName, "name");
    annotationProcessors = new ArrayList<>();
    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    showWarnings = JctCompiler.DEFAULT_SHOW_WARNINGS;
    showDeprecationWarnings = JctCompiler.DEFAULT_SHOW_DEPRECATION_WARNINGS;
    failOnWarnings = JctCompiler.DEFAULT_FAIL_ON_WARNINGS;
    compilationMode = JctCompiler.DEFAULT_COMPILATION_MODE;
    locale = JctCompiler.DEFAULT_LOCALE;
    logCharset = JctCompiler.DEFAULT_LOG_CHARSET;
    previewFeatures = JctCompiler.DEFAULT_PREVIEW_FEATURES;
    release = null;
    source = null;
    target = null;
    verbose = JctCompiler.DEFAULT_VERBOSE;
    diagnosticLoggingMode = JctCompiler.DEFAULT_DIAGNOSTIC_LOGGING_MODE;
    fixJvmModulePathMismatch = JctCompiler.DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH;
    inheritClassPath = JctCompiler.DEFAULT_INHERIT_CLASS_PATH;
    inheritModulePath = JctCompiler.DEFAULT_INHERIT_MODULE_PATH;
    inheritPlatformClassPath = JctCompiler.DEFAULT_INHERIT_PLATFORM_CLASS_PATH;
    inheritSystemModulePath = JctCompiler.DEFAULT_INHERIT_SYSTEM_MODULE_PATH;
    fileManagerLoggingMode = JctCompiler.DEFAULT_FILE_MANAGER_LOGGING_MODE;
    annotationProcessorDiscovery = JctCompiler.DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
  }

  @Override
  public JctCompilation compile(Workspace workspace) {
    return compileInternal(workspace, null);
  }

  @Override
  public JctCompilation compile(Workspace workspace, Collection<String> classNames) {
    return compileInternal(workspace, classNames);
  }

  @Override
  public final <E extends Exception> A configure(JctCompilerConfigurer<E> configurer) throws E {
    requireNonNull(configurer, "configurer");
    configurer.configure(this);
    return myself();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public A name(String name) {
    this.name = requireNonNull(name, "name");
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
  public CompilationMode getCompilationMode() {
    return compilationMode;
  }

  @Override
  public A compilationMode(CompilationMode compilationMode) {
    this.compilationMode = compilationMode;
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
  public List<Processor> getAnnotationProcessors() {
    return List.copyOf(annotationProcessors);
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
  public String getEffectiveRelease() {
    if (release != null) {
      return release;
    }

    if (target != null) {
      return target;
    }

    return getDefaultRelease();
  }

  @Override
  public String getRelease() {
    return release;
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
  public String getSource() {
    return source;
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
  public String getTarget() {
    return target;
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
    return fixJvmModulePathMismatch;
  }

  @Override
  public A fixJvmModulePathMismatch(boolean fixJvmModulePathMismatch) {
    this.fixJvmModulePathMismatch = fixJvmModulePathMismatch;
    return myself();
  }

  @Override
  public boolean isInheritClassPath() {
    return inheritClassPath;
  }

  @Override
  public A inheritClassPath(boolean inheritClassPath) {
    this.inheritClassPath = inheritClassPath;
    return myself();
  }

  @Override
  public boolean isInheritModulePath() {
    return inheritModulePath;
  }

  @Override
  public A inheritModulePath(boolean inheritModulePath) {
    this.inheritModulePath = inheritModulePath;
    return myself();
  }

  @Override
  public boolean isInheritPlatformClassPath() {
    return inheritPlatformClassPath;
  }

  @Override
  public A inheritPlatformClassPath(boolean inheritPlatformClassPath) {
    this.inheritPlatformClassPath = inheritPlatformClassPath;
    return myself();
  }

  @Override
  public boolean isInheritSystemModulePath() {
    return inheritSystemModulePath;
  }

  @Override
  public A inheritSystemModulePath(boolean inheritSystemModulePath) {
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
    return fileManagerLoggingMode;
  }

  @Override
  public A fileManagerLoggingMode(LoggingMode fileManagerLoggingMode) {
    this.fileManagerLoggingMode = requireNonNull(fileManagerLoggingMode, "fileManagerLoggingMode");
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
    return annotationProcessorDiscovery;
  }

  @Override
  public A annotationProcessorDiscovery(AnnotationProcessorDiscovery annotationProcessorDiscovery) {
    this.annotationProcessorDiscovery = requireNonNull(
        annotationProcessorDiscovery,
        "annotationProcessorDiscovery"
    );
    return myself();
  }

  /**
   * Get the compiler name.
   *
   * @return the compiler name.
   * @see #getName()
   * @see #name(String)
   * @deprecated Use {@link #getName()} instead.
   */
  @Deprecated
  @Override
  public final String toString() {
    return name;
  }

  /**
   * Get the flag builder factory to use for building flags.
   *
   * @return the factory.
   */
  public abstract JctFlagBuilderFactory getFlagBuilderFactory();

  /**
   * Get the JSR-199 compiler factory to use for initialising an internal compiler.
   *
   * @return the factory.
   */
  public abstract Jsr199CompilerFactory getCompilerFactory();

  /**
   * Get the file manager factory to use for building a file manager during compilation.
   *
   * @return the factory.
   */
  public abstract JctFileManagerFactory getFileManagerFactory();

  /**
   * Get the compilation factory to use for building a compilation.
   *
   * <p>By default, this uses a common internal implementation that is designed to work with
   * compilers that have interfaces the same as, and behave the same as Javac.
   *
   * <p>Some obscure compiler implementations with potentially satanic rituals for initialising
   * and configuring components correctly may need to provide a custom implementation here instead.
   * In this case, this method should be overridden.
   *
   * @return the compilation factory.
   */
  public JctCompilationFactory getCompilationFactory() {
    return new JctCompilationFactoryImpl(this);
  }

  /**
   * {@inheritDoc}
   *
   * @return the default release version to use when no version is specified by the user.
   */
  @Override
  public abstract String getDefaultRelease();

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

  /**
   * Build the list of flags from this compiler object using the flag builder.
   *
   * <p>Implementations should not need to override this unless there is a special edge case
   * that needs configuring differently. This is exposed to assist in these kinds of cases.
   *
   * @param flagBuilder the flag builder to apply the flag configuration to.
   * @return the string flags to use.
   */
  protected List<String> buildFlags(JctFlagBuilder flagBuilder) {
    return flagBuilder
        .annotationProcessorOptions(annotationProcessorOptions)
        .showDeprecationWarnings(showDeprecationWarnings)
        .failOnWarnings(failOnWarnings)
        .compilerOptions(compilerOptions)
        .previewFeatures(previewFeatures)
        .release(release)
        .source(source)
        .target(target)
        .verbose(verbose)
        .showWarnings(showWarnings)
        .build();
  }

  @SuppressWarnings("ThrowFromFinallyBlock")
  private JctCompilation compileInternal(Workspace workspace, Collection<String> classNames) {
    var fileManagerFactory = getFileManagerFactory();
    var flagBuilderFactory = getFlagBuilderFactory();
    var compilerFactory = getCompilerFactory();
    var compilationFactory = getCompilationFactory();
    var flags = buildFlags(flagBuilderFactory.createFlagBuilder());
    var compiler = compilerFactory.createCompiler();
    var fileManager = fileManagerFactory.createFileManager(workspace);

    // Any internal exceptions should be rethrown as a JctCompilerException by the
    // compilation factory, so there is nothing else to worry about here.
    // Likewise, do not catch IOException on the compilation process, as it may hide
    // bugs. Hence, we have to use a try-finally-try-catch-rethrow manually rather than a nice
    // try-with-resources. This is kinda crap code, but it prevents reporting errors incorrectly.

    try {
      return compilationFactory.createCompilation(flags, fileManager, compiler, classNames);
    } finally {
      try {
        fileManager.close();
      } catch (IOException ex) {
        throw new JctCompilerException(
            "Failed to close file manager. This is probably a bug, so please report it.",
            ex
        );
      }
    }
  }
}
