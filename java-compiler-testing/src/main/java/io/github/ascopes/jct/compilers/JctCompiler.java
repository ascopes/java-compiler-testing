/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import static io.github.ascopes.jct.utils.IterableUtils.requireAtLeastOne;

import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Base definition of a compiler that can be configured to perform a compilation run against
 * sources.
 *
 * <p>JctCompiler objects are often a nexus that will manage configuring an underlying JSR-199
 * compiler internally in a platform-agnostic way.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface JctCompiler {

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
   * Default setting for fixing modules being placed on the classpath by mistake ({@code true}).
   */
  boolean DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH = true;

  /**
   * Default setting for inclusion of the current class path ({@code true}).
   */
  boolean DEFAULT_INHERIT_CLASS_PATH = true;

  /**
   * Default setting for inclusion of the current module path ({@code true}).
   */
  boolean DEFAULT_INHERIT_MODULE_PATH = true;

  /**
   * Default setting for inclusion of the system module path ({@code true}).
   */
  boolean DEFAULT_INHERIT_SYSTEM_MODULE_PATH = true;

  /**
   * Default setting for logging file manager operations ({@link LoggingMode#DISABLED}).
   */
  LoggingMode DEFAULT_FILE_MANAGER_LOGGING_MODE = LoggingMode.DISABLED;

  /**
   * Default setting for logging diagnostics ({@link LoggingMode#ENABLED}).
   */
  LoggingMode DEFAULT_DIAGNOSTIC_LOGGING_MODE = LoggingMode.ENABLED;

  /**
   * Default setting for the compilation mode to use
   * ({@link CompilationMode#COMPILATION_AND_ANNOTATION_PROCESSING}).
   */
  CompilationMode DEFAULT_COMPILATION_MODE = CompilationMode.COMPILATION_AND_ANNOTATION_PROCESSING;

  /**
   * Default setting for how to apply annotation processor discovery when no processors are
   * explicitly defined ({@link AnnotationProcessorDiscovery#INCLUDE_DEPENDENCIES}).
   */
  AnnotationProcessorDiscovery DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY =
      AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES;

  /**
   * Default charset to use for compiler logs ({@link StandardCharsets#UTF_8}).
   */
  Charset DEFAULT_LOG_CHARSET = StandardCharsets.UTF_8;

  /**
   * Default debugging info to include in the compilation (all possible info).
   */
  Set<DebuggingInfo> DEFAULT_DEBUGGING_INFO = DebuggingInfo.all();

  /**
   * Default preference for including reflective parameter information in compiled classes
   * ({@code true}).
   */
  boolean DEFAULT_PARAMETER_INFO_ENABLED = true;

  /**
   * Invoke the compilation and return the compilation result.
   *
   * <p>The actual classes to compile will be dynamically discovered. If you wish to
   * specify the specific classes to compile, see {@link #compile(Workspace, String...)} or
   * {@link #compile(Workspace, Collection)}.
   *
   * @param workspace the workspace to compile.
   * @return the compilation result.
   * @throws JctCompilerException  if the compiler threw an unhandled exception. This should not
   *                               occur for compilation failures generally.
   * @throws IllegalStateException if no compilation units were found.
   * @throws UncheckedIOException  if an IO error occurs.
   * @see #compile(Workspace, String...)
   * @see #compile(Workspace, Collection)
   */
  JctCompilation compile(Workspace workspace);

  /**
   * Invoke the compilation and return the compilation result.
   *
   * <p>Only classes matching the given class names will be compiled.
   *
   * <p>If you wish to let JCT determine which classes to compile dynamically, see
   * {@link #compile(Workspace)} instead.
   *
   * <p>Note that nested instance/static nested classes cannot be specified individually
   * here. To compile them, you must also compile their outer class that they are defined within.
   *
   * @param workspace            the workspace to compile.
   * @param classNames           the class names to compile.
   * @return the compilation result.
   * @throws JctCompilerException  if the compiler threw an unhandled exception. This should not
   *                               occur for compilation failures generally.
   * @throws NullPointerException  if any class names are null, or if the array is null.
   * @throws IllegalStateException if no compilation units were found.
   * @throws UncheckedIOException  if an IO error occurs.
   * @see #compile(Workspace)
   * @see #compile(Workspace, Collection)
   */
  default JctCompilation compile(Workspace workspace, String... classNames) {
    requireAtLeastOne(classNames, "classNames");
    return compile(workspace, List.of(classNames));
  }

  /**
   * Invoke the compilation and return the compilation result.
   *
   * <p>Only classes matching the given class names will be compiled.
   *
   * <p>If you wish to let JCT determine which classes to compile dynamically, see
   * {@link #compile(Workspace)} instead.
   *
   * <p>Note that nested instance/static nested classes cannot be specified individually
   * here. To compile them, you must also compile their outer class that they are defined within.
   *
   * @param workspace  the workspace to compile.
   * @param classNames the class names to compile.
   * @return the compilation result.
   * @throws JctCompilerException     if the compiler threw an unhandled exception. This should not
   *                                  occur for compilation failures usually.
   * @throws NullPointerException     if the {@code classNames} collection contains any null values,
   *                                  or if the collection itself is null.
   * @throws IllegalArgumentException if the collection is empty.
   * @throws IllegalStateException    if no compilation units were found.
   * @throws UncheckedIOException     if an IO error occurs.
   * @see #compile(Workspace)
   * @see #compile(Workspace, String...)
   */
  JctCompilation compile(Workspace workspace, Collection<String> classNames);

  /**
   * Apply a given configurer to this compiler.
   *
   * <p>Configurers can be lambdas, method references, or objects.
   *
   * <pre><code>
   *   // Using an object configurer
   *   var werrorConfigurer = new JctCompilerConfigurer&lt;RuntimeException&gt;() {
   *     {@literal @Override}
   *     public void configure(JctCompiler compiler) {
   *       compiler.failOnWarnings(true);
   *     }
   *   };
   *   compiler.configure(werrorConfigurer);
   *
   *   // Using a lambda configurer
   *   compiler.configure(c -&gt; c.verbose(true));
   * </code></pre>
   *
   * <p>Configurers take a type parameter that corresponds to an exception type. This
   * is the exception type that can be thrown by the configurer, or {@link RuntimeException} if no
   * checked exception is thrown. This mechanism allows configurers to propagate checked exceptions
   * to their caller where needed.
   *
   * <pre><code>
   *   class FileFlagConfigurer implements JctCompilerConfigurer&lt;IOException&gt; {
   *     private final Path path;
   *
   *     public FileFlagConfigurer(String... path) {
   *       this(Path.of(path));
   *     }
   *
   *     public FileFlagConfigurer(Path path) {
   *       this.path = path;
   *     }
   *
   *     {@literal @Override}
   *     public void configure(JctCompiler compiler) throws IOException {
   *       var flags = Files.lines(path)
   *           .map(String::trim)
   *           .filter(not(String::isBlank))
   *           .toList();
   *       compiler.addCompilerOptions(flags);
   *     }
   *   }
   *
   *   {@literal @Test}
   *   void testSomething() throws IOException {
   *     ...
   *     compiler.configure(new FileFlagConfigurer("src", "test", "resources", "flags.txt"));
   *     ...
   *   }
   * </code></pre>
   *
   * @param <E>        any exception that may be thrown.
   * @param configurer the configurer to invoke.
   * @return this compiler object for further call chaining.
   * @throws E any exception that may be thrown by the configurer. If no checked exception is
   *           thrown, then this should be treated as {@link RuntimeException}.
   */
  <E extends Exception> JctCompiler configure(JctCompilerConfigurer<E> configurer) throws E;

  /**
   * Get the friendly printable name of this compiler object.
   *
   * @return the name of the compiler.
   */
  String getName();

  /**
   * Set the friendly name of this compiler.
   *
   * <p>This will be used by the
   * {@link io.github.ascopes.jct.junit.JavacCompilerTest JUnit5 support} to name unit test cases.
   *
   * @param name the name to set.
   * @return this compiler object for further call chaining.
   */
  JctCompiler name(String name);

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
  JctCompiler addAnnotationProcessorOptions(Iterable<String> annotationProcessorOptions);

  /**
   * Add options to pass to any annotation processors.
   *
   * @param annotationProcessorOptions options to pass.
   * @return this compiler object for further call chaining.
   */
  default JctCompiler addAnnotationProcessorOptions(String... annotationProcessorOptions) {
    return addAnnotationProcessorOptions(List.of(annotationProcessorOptions));
  }

  /**
   * Get an <strong>immutable snapshot view</strong> of the current annotation processors that are
   * explicitly set to be run, in the order that they were provided to the compiler.
   *
   * @return the current annotation processors that are set.
   */
  List<Processor> getAnnotationProcessors();

  /**
   * Add annotation processors to invoke.
   *
   * <p><strong>Warning:</strong> This bypasses the discovery process of annotation processors
   * provided in the annotation processor path and annotation processor module paths, as well as any
   * other locations such as class paths and module paths.
   *
   * @param annotationProcessors the processors to invoke.
   * @return this compiler object for further call chaining.
   */
  JctCompiler addAnnotationProcessors(Iterable<? extends Processor> annotationProcessors);

  /**
   * Add annotation processors to invoke.
   *
   * <p><strong>Warning:</strong> This bypasses the discovery process of annotation processors
   * provided in the annotation processor path and annotation processor module paths, as well as any
   * other locations such as class paths and module paths.
   *
   * @param annotationProcessors processors to invoke.
   * @return this compiler object for further call chaining.
   */
  default JctCompiler addAnnotationProcessors(Processor... annotationProcessors) {
    return addAnnotationProcessors(List.of(annotationProcessors));
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
  JctCompiler addCompilerOptions(Iterable<String> compilerOptions);

  /**
   * Add command line options to pass to {@code javac}.
   *
   * @param compilerOptions options to add.
   * @return this compiler object for further call chaining.
   */
  default JctCompiler addCompilerOptions(String... compilerOptions) {
    return addCompilerOptions(List.of(compilerOptions));
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
  JctCompiler verbose(boolean enabled);

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
   * Set whether to enable compiler preview features or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PREVIEW_FEATURES}.
   *
   * <p>Generally, this feature should be avoided if testing across multiple versions
   * of Java, as preview features are often not finalised and may change without warning.
   *
   * @param enabled {@code true} to enable preview features, or {@code false} to disable them.
   * @return this compiler object for further call chaining.
   */
  JctCompiler previewFeatures(boolean enabled);

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
  JctCompiler showWarnings(boolean enabled);

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
  JctCompiler showDeprecationWarnings(boolean enabled);

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
   * <p>Some compilers may call this flag something different, such as "{@code -Werror}".
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
  JctCompiler failOnWarnings(boolean enabled);

  /**
   * Get the compilation mode that is in use.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_COMPILATION_MODE}.
   *
   * @return the compilation mode.
   */
  CompilationMode getCompilationMode();

  /**
   * Set the compilation mode to use for this compiler.
   *
   * <p>This allows you to override whether sources are compiled or annotation-processed
   * without running the full compilation process. Tuning this may provide faster test cases in some
   * situations.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_COMPILATION_MODE}.
   *
   * @param compilationMode the compilation mode to use.
   * @return this compiler object for further call chaining.
   */
  JctCompiler compilationMode(CompilationMode compilationMode);

  /**
   * Get the default release to use if no release or target version is specified.
   *
   * <p>This can <strong>not</strong> be configured generally, as it is defined by
   * the internal compiler implementation.
   *
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * @return the default release version to use.
   */
  String getDefaultRelease();

  /**
   * Get the effective release to use for the actual compilation.
   *
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * <p>This may be determined from the {@link #getSource() source},
   * {@link #getTarget() target}, {@link #getRelease() release}, and
   * {@link #getDefaultRelease() default release.}
   *
   * @return the effective release.
   */
  String getEffectiveRelease();

  /**
   * Get the current release version that is set, or {@code null} if left to the compiler to decide.
   * default.
   *
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the release version string, if set.
   */
  @Nullable
  String getRelease();

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   */
  JctCompiler release(@Nullable String release);

  /**
   * Set the release version.
   *
   * <p>This will clear any source and target version that is set.
   *
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   * @throws IllegalArgumentException      if the version is less than 0.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   */
  default JctCompiler release(int release) {
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
   * <p>Generally, this value will be an integer within a string. The value is
   * represented as a string to allow supporting compilers which may use non-integer version
   * numbers.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @param release the version to set.
   * @return this compiler object for further call chaining.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   * @throws NullPointerException if the release is null.
   */
  default JctCompiler release(SourceVersion release) {
    return release(Integer.toString(release.ordinal()));
  }

  /**
   * Request that the compiler uses a language version that corresponds to the runtime language
   * version in use on the current JVM.
   *
   * <p>For example, running this on JRE 19 would set the release to "19".
   *
   * <p>This calls {@link #release(int)} internally.
   *
   * @return this compiler object for further call chaining.
   * @throws UnsupportedOperationException if the current JVM version does not correspond to a
   *                                       supported Java release version in the compiler, or if the
   *                                       compiler does not support integral version numbers.
   * @since 1.1.0
   */
  @API(since = "1.1.0", status = Status.STABLE)
  default JctCompiler useRuntimeRelease() {
    return release(Runtime.version().feature());
  }

  /**
   * Get the current source version that is set, or {@code null} if left to the compiler to decide.
   * default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the source version string, if set.
   */
  @Nullable
  String getSource();

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   */
  JctCompiler source(@Nullable String source);

  /**
   * Set the source version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   * @throws IllegalArgumentException      if the version is less than 0.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   */
  default JctCompiler source(int source) {
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
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param source the version to set.
   * @return this compiler object for further call chaining.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   * @throws NullPointerException          if the source is null.
   */
  default JctCompiler source(SourceVersion source) {
    return source(Integer.toString(source.ordinal()));
  }

  /**
   * Get the current target version that is set, or {@code null} if left to the compiler default.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * @return the target version string, if set.
   */
  @Nullable
  String getTarget();

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   */
  JctCompiler target(@Nullable String target);

  /**
   * Set the target version.
   *
   * <p>This will clear any release version that is set.
   *
   * <p>Unless explicitly defined, the default setting is expected to be a sane compiler-specific
   * default.
   *
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   * @throws IllegalArgumentException      if the version is less than 0.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   */
  default JctCompiler target(int target) {
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
   * <p>Source and target versions have mostly been replaced with the release version
   * mechanism which controls both flags and can ensure other behaviours are consistent. This
   * feature is still provided in case you have a specific use case that is not covered by this
   * functionality.
   *
   * @param target the version to set.
   * @return this compiler object for further call chaining.
   * @throws UnsupportedOperationException if the compiler does not support integer versions.
   * @throws NullPointerException          if the target is null.
   */
  default JctCompiler target(SourceVersion target) {
    return target(Integer.toString(target.ordinal()));
  }

  /**
   * Get whether we will attempt to fix modules appearing on the classpath, or non-modules appearing
   * on the module path.
   *
   * <p>This enables correct classpath and module path detection when the test pack is a module but
   * the code being compiled in the test is not, and vice versa. We need this because many build
   * systems decide whether to populate the {@code --module-path} or the {@code --classpath}
   * with JPMS-enabled dependencies based on whether the project under compilation is a JPMS module
   * itself.
   *
   * <p>This only applies if {@link #isInheritModulePath()} or {@link #isInheritClassPath()} is
   * enabled, and only applies to the current JVM classpath and module path.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH}.
   *
   * @return {@code true} if enabled, or {@code false} if disabled.
   */
  boolean isFixJvmModulePathMismatch();

  /**
   * Get whether we will attempt to fix modules appearing on the classpath, or non-modules appearing
   * on the module path.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FIX_JVM_MODULE_PATH_MISMATCH}.
   *
   * @param fixJvmModulePathMismatch whether to enable the mismatch fixing or not.
   * @return this compiler object for further call chaining.
   */
  JctCompiler fixJvmModulePathMismatch(boolean fixJvmModulePathMismatch);

  /**
   * Get whether the class path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @return whether the current class path is being inherited or not.
   */
  boolean isInheritClassPath();

  /**
   * Set whether the class path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @param inheritClassPath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  JctCompiler inheritClassPath(boolean inheritClassPath);

  /**
   * Get whether the module path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @return whether the module path is being inherited or not.
   */
  boolean isInheritModulePath();

  /**
   * Set whether the module path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @param inheritModulePath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  JctCompiler inheritModulePath(boolean inheritModulePath);

  /**
   * Get whether the system module path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @return whether the system module path is being inherited or not.
   */
  boolean isInheritSystemModulePath();

  /**
   * Set whether the system module path is inherited from the active JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @param inheritSystemModulePath {@code true} to include it, or {@code false} to exclude it.
   * @return this compiler object for further call chaining.
   */
  JctCompiler inheritSystemModulePath(boolean inheritSystemModulePath);

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
  JctCompiler locale(Locale locale);

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
  JctCompiler logCharset(Charset logCharset);

  /**
   * Get the current file manager logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @return the current file manager logging mode.
   */
  LoggingMode getFileManagerLoggingMode();

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @param fileManagerLoggingMode the mode to use for file manager logging.
   * @return this compiler for further call chaining.
   */
  JctCompiler fileManagerLoggingMode(LoggingMode fileManagerLoggingMode);

  /**
   * Get the current diagnostic logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING_MODE}.
   *
   * @return the current diagnostic logging mode.
   */
  LoggingMode getDiagnosticLoggingMode();

  /**
   * Set how to handle diagnostic capture.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DIAGNOSTIC_LOGGING_MODE}.
   *
   * @param diagnosticLoggingMode the mode to use for diagnostic capture.
   * @return this compiler for further call chaining.
   */
  JctCompiler diagnosticLoggingMode(LoggingMode diagnosticLoggingMode);

  /**
   * Get how to perform annotation processor discovery.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY}.
   *
   * <p>Specifying any annotation processors explicitly with
   * {@link #addAnnotationProcessors(Iterable)} or
   * {@link #addAnnotationProcessors(Processor...)} will bypass this setting, treating it
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
   * {@link #addAnnotationProcessors(Processor...)} will bypass this setting, treating it
   * as being disabled.
   *
   * @param annotationProcessorDiscovery the processor discovery mode to use.
   * @return this compiler for further call chaining.
   */
  JctCompiler annotationProcessorDiscovery(
      AnnotationProcessorDiscovery annotationProcessorDiscovery);

  /**
   * Get the debugging info that is enabled.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_DEBUGGING_INFO}.
   *
   * @return the set of debugging info flags that are enabled.
   * @since 3.0.0
   */
  Set<DebuggingInfo> getDebuggingInfo();

  /**
   * Set the debugging info level to use.
   *
   * @param debuggingInfoFlags the set of debugging info flags to enable.
   * @return this compiler for further call chaining.
   * @since 3.0.0
   */
  JctCompiler debuggingInfo(Set<DebuggingInfo> debuggingInfoFlags);

  /**
   * Determine if including reflective parameter info is enabled or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link #DEFAULT_PARAMETER_INFO_ENABLED}.
   *
   * @return the parameter info inclusion preference.
   * @since 3.0.0
   */
  boolean isParameterInfoEnabled();

  /**
   * Set whether to include parameter reflective info by default in compiled classes or not.
   *
   * @param parameterInfoEnabled whether to include the parameter reflective info or not.
   * @return this compiler for further call chaining.
   * @since 3.0.0
   */
  JctCompiler parameterInfoEnabled(boolean parameterInfoEnabled);
}
