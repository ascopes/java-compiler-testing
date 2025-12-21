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

import static io.github.ascopes.jct.utils.IterableUtils.requireNonNullValues;
import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.compilers.impl.JctCompilationFactoryImpl;
import io.github.ascopes.jct.compilers.impl.JctCompilationImpl;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManagerFactory;
import io.github.ascopes.jct.filemanagers.JctFileManagers;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Processor;
import org.jspecify.annotations.Nullable;

/**
 * Common functionality for a compiler that can be overridden and that produces a
 * {@link JctCompilationImpl} as the compilation result.
 *
 * <p>Implementations should extend this class and override anything they require.
 * In most cases, you should not need to override anything other than the constructor.
 *
 * <p>This class is <strong>not thread-safe</strong>.
 *
 * <p>If you wish to create a common set of configuration settings for instances of
 * this class, you should consider writing a custom {@link JctCompilerConfigurer} object to apply
 * the desired operations, and then apply it to instances of this class using
 * {@link #configure(JctCompilerConfigurer)}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AbstractJctCompiler implements JctCompiler {

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
  private LoggingMode diagnosticLoggingMode;
  private boolean fixJvmModulePathMismatch;
  private boolean inheritClassPath;
  private boolean inheritModulePath;
  private boolean inheritSystemModulePath;
  private LoggingMode fileManagerLoggingMode;
  private AnnotationProcessorDiscovery annotationProcessorDiscovery;
  private Set<DebuggingInfo> debuggingInfo;
  private boolean parameterInfoEnabled;

  private @Nullable String release;
  private @Nullable String source;
  private @Nullable String target;

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
    showWarnings = DEFAULT_SHOW_WARNINGS;
    showDeprecationWarnings = DEFAULT_SHOW_DEPRECATION_WARNINGS;
    failOnWarnings = DEFAULT_FAIL_ON_WARNINGS;
    compilationMode = DEFAULT_COMPILATION_MODE;
    locale = DEFAULT_LOCALE;
    logCharset = DEFAULT_LOG_CHARSET;
    previewFeatures = DEFAULT_PREVIEW_FEATURES;
    verbose = DEFAULT_VERBOSE;
    diagnosticLoggingMode = DEFAULT_DIAGNOSTIC_LOGGING_MODE;
    fixJvmModulePathMismatch = DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH;
    inheritClassPath = DEFAULT_INHERIT_CLASS_PATH;
    inheritModulePath = DEFAULT_INHERIT_MODULE_PATH;
    inheritSystemModulePath = DEFAULT_INHERIT_SYSTEM_MODULE_PATH;
    fileManagerLoggingMode = DEFAULT_FILE_MANAGER_LOGGING_MODE;
    annotationProcessorDiscovery = DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
    debuggingInfo = DEFAULT_DEBUGGING_INFO;
    parameterInfoEnabled = DEFAULT_PARAMETER_INFO_ENABLED;

    // If none of these are overridden then we assume the defaults instead.
    release = null;
    source = null;
    target = null;
  }

  @Override
  public JctCompilation compile(Workspace workspace) {
    return performCompilation(workspace, null);
  }

  @Override
  public JctCompilation compile(Workspace workspace, Collection<String> classNames) {
    // There is no reason to invoke this overload with null values, so
    // prevent this.
    requireNonNullValues(classNames, "classNames");
    return performCompilation(workspace, classNames);
  }

  @Override
  public final <E extends Exception> AbstractJctCompiler configure(
      JctCompilerConfigurer<E> configurer
  ) throws E {
    requireNonNull(configurer, "configurer");
    configurer.configure(this);
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public AbstractJctCompiler name(String name) {
    requireNonNull(name, "name");
    this.name = name;
    return this;
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public AbstractJctCompiler verbose(boolean enabled) {
    verbose = enabled;
    return this;
  }

  @Override
  public boolean isPreviewFeatures() {
    return previewFeatures;
  }

  @Override
  public AbstractJctCompiler previewFeatures(boolean enabled) {
    previewFeatures = enabled;
    return this;
  }

  @Override
  public boolean isShowWarnings() {
    return showWarnings;
  }

  @Override
  public AbstractJctCompiler showWarnings(boolean enabled) {
    showWarnings = enabled;
    return this;
  }

  @Override
  public boolean isShowDeprecationWarnings() {
    return showDeprecationWarnings;
  }

  @Override
  public AbstractJctCompiler showDeprecationWarnings(boolean enabled) {
    showDeprecationWarnings = enabled;
    return this;
  }

  @Override
  public boolean isFailOnWarnings() {
    return failOnWarnings;
  }

  @Override
  public AbstractJctCompiler failOnWarnings(boolean enabled) {
    failOnWarnings = enabled;
    return this;
  }

  @Override
  public CompilationMode getCompilationMode() {
    return compilationMode;
  }

  @Override
  public AbstractJctCompiler compilationMode(CompilationMode compilationMode) {
    this.compilationMode = compilationMode;
    return this;
  }

  @Override
  public List<String> getAnnotationProcessorOptions() {
    return List.copyOf(annotationProcessorOptions);
  }

  @Override
  public AbstractJctCompiler addAnnotationProcessorOptions(
      Iterable<String> annotationProcessorOptions
  ) {
    requireNonNullValues(annotationProcessorOptions, "annotationProcessorOptions");
    annotationProcessorOptions.forEach(this.annotationProcessorOptions::add);
    return this;
  }

  @Override
  public List<Processor> getAnnotationProcessors() {
    return List.copyOf(annotationProcessors);
  }

  @Override
  public AbstractJctCompiler addAnnotationProcessors(
      Iterable<? extends Processor> annotationProcessors
  ) {
    requireNonNullValues(annotationProcessors, "annotationProcessors");
    annotationProcessors.forEach(this.annotationProcessors::add);

    return this;
  }

  @Override
  public List<String> getCompilerOptions() {
    return List.copyOf(compilerOptions);
  }

  @Override
  public AbstractJctCompiler addCompilerOptions(Iterable<String> compilerOptions) {
    requireNonNullValues(compilerOptions, "compilerOptions");
    compilerOptions.forEach(this.compilerOptions::add);
    return this;
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

  @Nullable
  @Override
  public String getRelease() {
    return release;
  }

  @Override
  public AbstractJctCompiler release(@Nullable String release) {
    this.release = release;

    if (release != null) {
      source = null;
      target = null;
    }

    return this;
  }

  @Nullable
  @Override
  public String getSource() {
    return source;
  }

  @Override
  public AbstractJctCompiler source(@Nullable String source) {
    this.source = source;
    if (source != null) {
      release = null;
    }
    return this;
  }

  @Nullable
  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public AbstractJctCompiler target(@Nullable String target) {
    this.target = target;
    if (target != null) {
      release = null;
    }
    return this;
  }

  @Override
  public boolean isFixJvmModulePathMismatch() {
    return fixJvmModulePathMismatch;
  }

  @Override
  public AbstractJctCompiler fixJvmModulePathMismatch(boolean fixJvmModulePathMismatch) {
    this.fixJvmModulePathMismatch = fixJvmModulePathMismatch;
    return this;
  }

  @Override
  public boolean isInheritClassPath() {
    return inheritClassPath;
  }

  @Override
  public AbstractJctCompiler inheritClassPath(boolean inheritClassPath) {
    this.inheritClassPath = inheritClassPath;
    return this;
  }

  @Override
  public boolean isInheritModulePath() {
    return inheritModulePath;
  }

  @Override
  public AbstractJctCompiler inheritModulePath(boolean inheritModulePath) {
    this.inheritModulePath = inheritModulePath;
    return this;
  }

  @Override
  public boolean isInheritSystemModulePath() {
    return inheritSystemModulePath;
  }

  @Override
  public AbstractJctCompiler inheritSystemModulePath(boolean inheritSystemModulePath) {
    this.inheritSystemModulePath = inheritSystemModulePath;
    return this;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public AbstractJctCompiler locale(Locale locale) {
    requireNonNull(locale, "locale");
    this.locale = locale;
    return this;
  }

  @Override
  public Charset getLogCharset() {
    return logCharset;
  }

  @Override
  public AbstractJctCompiler logCharset(Charset logCharset) {
    requireNonNull(logCharset, "logCharset");
    this.logCharset = logCharset;
    return this;
  }

  @Override
  public LoggingMode getFileManagerLoggingMode() {
    return fileManagerLoggingMode;
  }

  @Override
  public AbstractJctCompiler fileManagerLoggingMode(LoggingMode fileManagerLoggingMode) {
    requireNonNull(fileManagerLoggingMode, "fileManagerLoggingMode");
    this.fileManagerLoggingMode = fileManagerLoggingMode;
    return this;
  }

  @Override
  public LoggingMode getDiagnosticLoggingMode() {
    return diagnosticLoggingMode;
  }

  @Override
  public AbstractJctCompiler diagnosticLoggingMode(LoggingMode diagnosticLoggingMode) {
    requireNonNull(diagnosticLoggingMode, "diagnosticLoggingMode");
    this.diagnosticLoggingMode = diagnosticLoggingMode;
    return this;
  }

  @Override
  public AnnotationProcessorDiscovery getAnnotationProcessorDiscovery() {
    return annotationProcessorDiscovery;
  }

  @Override
  public AbstractJctCompiler annotationProcessorDiscovery(
      AnnotationProcessorDiscovery annotationProcessorDiscovery
  ) {
    requireNonNull(annotationProcessorDiscovery, 
        "annotationProcessorDiscovery");
    this.annotationProcessorDiscovery = annotationProcessorDiscovery;
    return this;
  }

  @Override
  public Set<DebuggingInfo> getDebuggingInfo() {
    return debuggingInfo;
  }

  @Override
  public JctCompiler debuggingInfo(Set<DebuggingInfo> debuggingInfo) {
    requireNonNullValues(debuggingInfo, "debuggingInfo");
    this.debuggingInfo = Set.copyOf(debuggingInfo);
    return this;
  }

  @Override
  public boolean isParameterInfoEnabled() {
    return parameterInfoEnabled;
  }

  @Override
  public JctCompiler parameterInfoEnabled(boolean parameterInfoEnabled) {
    this.parameterInfoEnabled = parameterInfoEnabled;
    return this;
  }

  /**
   * Get the string representation of the compiler.
   *
   * @return the string representation of the compiler.
   */
  @Override
  public final String toString() {
    // This returns the compiler name to simplify parameterization naming in @JavacCompilerTest
    // parameterized tests.
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
   * Get the file manager factory to use for building AbstractJctCompiler file manager during
   * compilation.
   *
   * <p>Since v1.1.0, this method has provided a default implementation. Before this, it was
   * abstract. The default implementation calls
   * {@link JctFileManagers#newJctFileManagerFactory(JctCompiler)}.
   *
   * @return the factory.
   */
  public JctFileManagerFactory getFileManagerFactory() {
    return JctFileManagers.newJctFileManagerFactory(this);
  }

  /**
   * Get the compilation factory to use for building a compilation.
   *
   * <p>By default, this uses a common internal implementation that is designed to work with
   * compilers that have interfaces the same as, and behave the same as Javac.
   *
   * <p>Some obscure compiler implementations with potentially satanic rituals for initialising
   * and configuring components correctly may need to provide a custom implementation here instead.
   * In this case, this method should be overridden. Base classes are not provided for you to extend
   * in this case as this is usually not something you want to be doing. Instead, you should
   * implement {@link JctCompilationFactory} directly.
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
        .debuggingInfo(debuggingInfo)
        .parameterInfoEnabled(parameterInfoEnabled)
        .build();
  }

  @SuppressWarnings("ThrowFromFinallyBlock")
  private JctCompilation performCompilation(
        Workspace workspace, 
        @Nullable Collection<String> classNames
  ) {
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
    // bugs.
    //
    // The try-finally-try-catch-rethrow ensures we only catch IOExceptions during
    // the file manager closure, where it is a bug.
    
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
