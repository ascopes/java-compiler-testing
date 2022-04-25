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

import com.github.ascopes.jct.intern.SpecialLocations;
import com.github.ascopes.jct.intern.StringUtils;
import com.github.ascopes.jct.paths.LoggingJavaFileManagerProxy;
import com.github.ascopes.jct.paths.PathJavaFileManager;
import com.github.ascopes.jct.paths.PathJavaFileObjectFactory;
import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common functionality for a compiler that can be overridden and that produces a
 * {@link SimpleCompilation} as the compilation result.
 *
 * @param <A> the type of the class extending this class.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimpleCompiler<A extends SimpleCompiler<A>>
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
  private Logging diagnostics;
  private ProcessorDiscovery annotationProcessorDiscovery;

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
    this.name = requireNonNull(name);
    this.jsr199Compiler = requireNonNull(jsr199Compiler);
    this.flagBuilder = requireNonNull(flagBuilder);

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
    diagnostics = DEFAULT_DIAGNOSTICS;
    annotationProcessorDiscovery = DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
  }

  @Override
  public final <T extends Exception> A configure(
      CompilerConfigurer<A, T> compilerConfigurer
  ) throws T {
    LOGGER.debug("configure({})", compilerConfigurer);
    var me = myself();
    compilerConfigurer.configure(me);
    return me;
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
   * Get the flag builder to use.
   *
   * @return the flag builder.
   */
  public FlagBuilder getFlagBuilder() {
    return flagBuilder;
  }

  @Override
  public SimpleCompilation compile() {
    // We delegate to a different method here to allow easier testing of additional behaviour
    // internally, such as the ECJ global lock that we apply to prevent bugs. Without this, we'd
    // have to mock dozens of additional moving parts.
    return performEntireCompilation();
  }

  @Override
  public PathLocationRepository getPathLocationRepository() {
    return fileRepository;
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
    requireNonNull(options);
    LOGGER.trace("runtimeOptions += {}", options);
    for (var option : options) {
      runtimeOptions.add(requireNonNull(option));
    }
    return myself();
  }

  @Override
  public Charset getFileCharset() {
    return pathJavaFileObjectFactory.getCharset();
  }

  @Override
  public A fileCharset(Charset charset) {
    requireNonNull(charset);
    LOGGER.trace("fileCharset {} -> {}", pathJavaFileObjectFactory.getCharset(), charset);
    pathJavaFileObjectFactory.setCharset(charset);
    return myself();
  }

  @Override
  public Optional<String> getRelease() {
    return Optional.ofNullable(release);
  }

  @Override
  public A release(String version) {
    LOGGER.trace("release {} -> {}", release, version);
    LOGGER.trace("source {} -> null", source);
    LOGGER.trace("target {} -> null", target);
    release = version;
    source = null;
    target = null;
    return myself();
  }

  @Override
  public Optional<String> getSource() {
    return Optional.ofNullable(source);
  }

  @Override
  public A source(String version) {
    LOGGER.trace("source {} -> {}", target, version);
    LOGGER.trace("release {} -> null", release);
    release = null;
    source = version;
    return myself();
  }

  @Override
  public Optional<String> getTarget() {
    return Optional.ofNullable(target);
  }

  @Override
  public A target(String version) {
    LOGGER.trace("target {} -> {}", target, version);
    LOGGER.trace("release {} -> null", release);
    release = null;
    target = version;
    return myself();
  }

  @Override
  public boolean isInheritClassPath() {
    return inheritClassPath;
  }

  @Override
  public A inheritClassPath(boolean enabled) {
    LOGGER.trace("inheritClassPath {} -> {}", inheritClassPath, enabled);
    inheritClassPath = enabled;
    return myself();
  }

  @Override
  public boolean isInheritModulePath() {
    return inheritModulePath;
  }

  @Override
  public A inheritModulePath(boolean enabled) {
    LOGGER.trace("inheritModulePath {} -> {}", inheritModulePath, enabled);
    inheritModulePath = enabled;
    return myself();
  }

  @Override
  public boolean isInheritPlatformClassPath() {
    return inheritPlatformClassPath;
  }

  @Override
  public A inheritPlatformClassPath(boolean enabled) {
    LOGGER.trace(
        "inheritPlatformClassPath {} -> {}",
        inheritPlatformClassPath,
        enabled
    );
    inheritPlatformClassPath = enabled;
    return myself();
  }

  @Override
  public boolean isInheritSystemModulePath() {
    return inheritSystemModulePath;
  }

  @Override
  public A inheritSystemModulePath(boolean enabled) {
    LOGGER.trace(
        "inheritSystemModulePath {} -> {}",
        inheritSystemModulePath,
        enabled
    );
    inheritSystemModulePath = enabled;
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
  public A locale(Locale locale) {
    requireNonNull(locale);
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = locale;
    return myself();
  }

  @Override
  public Charset getLogCharset() {
    return logCharset;
  }

  @Override
  public A logCharset(Charset charset) {
    requireNonNull(charset);
    LOGGER.trace("logCharset {} -> {}", logCharset, charset);
    logCharset = charset;
    return myself();
  }

  @Override
  public Logging getFileManagerLogging() {
    return fileManagerLogging;
  }

  @Override
  public A fileManagerLogging(Logging fileManagerLogging) {
    requireNonNull(fileManagerLogging);
    LOGGER.trace(
        "fileManagerLogging {} -> {}",
        this.fileManagerLogging,
        fileManagerLogging
    );
    this.fileManagerLogging = fileManagerLogging;
    return myself();
  }

  @Override
  public Logging getDiagnostics() {
    return diagnostics;
  }

  @Override
  public A diagnostics(Logging diagnostics) {
    requireNonNull(diagnostics);
    LOGGER.trace(
        "diagnostics {} -> {}",
        this.diagnostics,
        diagnostics
    );
    this.diagnostics = diagnostics;
    return myself();
  }

  @Override
  public ProcessorDiscovery getAnnotationProcessorDiscovery() {
    return annotationProcessorDiscovery;
  }

  @Override
  public A annotationProcessorDiscovery(ProcessorDiscovery annotationProcessorDiscovery) {
    requireNonNull(annotationProcessorDiscovery);
    LOGGER.trace(
        "annotationProcessorDiscovery {} -> {}",
        this.annotationProcessorDiscovery,
        annotationProcessorDiscovery
    );
    this.annotationProcessorDiscovery = annotationProcessorDiscovery;
    return myself();
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
   * Run the entire compilation.
   *
   * <p>This will perform several steps. Each step can be overridden if needed.
   *
   * <ol>
   *   <li>Build flags using {@link #buildFlags()};</li>
   *   <li>Build a diagnostics listener using {@link #buildDiagnosticListener()};</li>
   *   <li>Build a file manager using {@link #buildJavaFileManager()} ()};</li>
   *   <li>Wrap said file manager in a logging interceptor using
   *        {@link #applyLoggingToFileManager(JavaFileManager)};</li>
   *   <li>Determine compilation units to compile using
   *        {@link #discoverCompilationUnits(JavaFileManager)}</li>
   *   <li>Build a compilation task using
   *        {@link #buildCompilationTask(Writer, JavaFileManager, DiagnosticListener, List, List)}
   *        </li>
   *   <li>Run the compilation task using {@link #runCompilationTask(CompilationTask)}</li>
   *   <li>Wrap the JSR-199 compilation result and state in a {@link SimpleCompilation} and
   *        return it.</li>
   * </ol>
   *
   * <p>Any {@link IOException}s that get thrown will be converted into
   * {@link CompilerException} instances and rethrown.
   *
   * @return the compilation result.
   */
  protected SimpleCompilation performEntireCompilation() {
    try {
      var flags = buildFlags();
      var diagnosticListener = buildDiagnosticListener();

      try (var fileManager = applyLoggingToFileManager(buildJavaFileManager())) {
        var compilationUnits = discoverCompilationUnits(fileManager);
        LOGGER.debug("Discovered {} compilation units {}", compilationUnits.size(),
            compilationUnits);

        var writer = new TeeWriter(logCharset, System.out);
        var task = buildCompilationTask(
            writer,
            fileManager,
            diagnosticListener,
            flags,
            compilationUnits
        );

        var result = runCompilationTask(task);

        var outputLines = writer.toString().lines().collect(Collectors.toList());

        return SimpleCompilation.builder()
            .warningsAsErrors(failOnWarnings)
            .success(result)
            .outputLines(outputLines)
            .compilationUnits(Set.copyOf(compilationUnits))
            .diagnostics(diagnosticListener.getDiagnostics())
            .fileRepository(fileRepository)
            .build();
      }
    } catch (IOException ex) {
      throw new CompilerException("Failed to compile due to an IOException: " + ex, ex);
    }
  }

  /**
   * Build the flags to pass to the JSR-199 compiler.
   *
   * @return the flags to use.
   */
  protected List<String> buildFlags() {
    return flagBuilder
        .annotationProcessorOptions(annotationProcessorOptions)
        .deprecationWarnings(showDeprecationWarnings)
        .warningsAsErrors(failOnWarnings)
        .options(compilerOptions)
        .previewFeatures(previewFeatures)
        .releaseVersion(release)
        .runtimeOptions(runtimeOptions)
        .sourceVersion(source)
        .targetVersion(target)
        .verbose(verbose)
        .warnings(showWarnings)
        .build();
  }

  /**
   * Build the {@link JavaFileManager} to use.
   *
   * <p>Logging will be applied to this via {@link #applyLoggingToFileManager(JavaFileManager)},
   * which will be handled by {@link #performEntireCompilation()}.
   *
   * @return the file manager to use.
   */
  private JavaFileManager buildJavaFileManager() {
    ensureClassOutputPathExists();
    registerClassPath();
    registerPlatformClassPath();
    registerJrtJimage();
    return new PathJavaFileManager(fileRepository);
  }

  /**
   * Discover all relevant compilation units for the file manager.
   *
   * <p>The default implementation will consider both {@link StandardLocation#SOURCE_PATH}
   * <em>and</em> {@link StandardLocation#MODULE_SOURCE_PATH} locations.
   *
   * @param fileManager the file manager to get the compilation units for.
   * @return the list of compilation units.
   * @throws IOException if an IO error occurs discovering any compilation units.
   */
  protected List<? extends JavaFileObject> discoverCompilationUnits(
      JavaFileManager fileManager
  ) throws IOException {
    var locations = new LinkedHashSet<Location>();
    locations.add(StandardLocation.SOURCE_PATH);

    fileManager
        .listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)
        .forEach(locations::addAll);

    var objects = new ArrayList<JavaFileObject>();

    for (var location : locations) {
      var items = fileManager.list(location, "", Set.of(Kind.SOURCE), true);
      for (var fileObject : items) {
        objects.add(fileObject);
      }
    }

    return objects;
  }

  /**
   * Apply the logging level to the file manager provided by {@link #buildJavaFileManager()}.
   *
   * <p>The default implementation will wrap the given {@link JavaFileManager} in a
   * {@link LoggingJavaFileManagerProxy} if the {@link #fileManagerLogging} field is
   * <strong>not</strong> set to {@link Logging#DISABLED}. In the latter scenario, the input
   * will be returned to the caller with no other modifications.
   *
   * @param fileManager the file manager to apply to.
   * @return the file manager to use for future operations.
   */
  protected JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLogging == Logging.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLogging == Logging.ENABLED) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  /**
   * Build a diagnostics listener.
   *
   * <p>This will also apply the desired logging configuration to the listener before returning it.
   *
   * @return the diagnostics listener.
   */
  protected TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener() {
    return new TracingDiagnosticListener<>(
        fileManagerLogging != Logging.DISABLED,
        fileManagerLogging == Logging.STACKTRACES
    );
  }

  /**
   * Build a new compilation task.
   *
   * @param writer             the writer to dump standard output to.
   * @param fileManager        the file manager to use.
   * @param diagnosticListener the diagnostics listener to use.
   * @param flags              the flags to use.
   * @param compilationUnits   the compilation units to use.
   * @return the compilation task.
   */
  private CompilationTask buildCompilationTask(
      Writer writer,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> flags,
      List<? extends JavaFileObject> compilationUnits
  ) {
    var name = getName();

    var task = jsr199Compiler.getTask(
        writer,
        fileManager,
        diagnosticListener,
        flags,
        null,
        compilationUnits
    );

    configureAnnotationProcessors(task);

    task.setLocale(locale);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Starting compilation of {} file{} with compiler {} using flags {}",
          compilationUnits.size(),
          compilationUnits.size() == 1 ? "" : "s",
          name,
          StringUtils.quotedIterable(flags)
      );
    }

    return task;
  }

  /**
   * Run the compilation task.
   *
   * <p>Any exceptions that get thrown will be wrapped in {@link CompilerException} instances
   * before being rethrown.
   *
   * @param task the task to run.
   * @return {@code true} if the compilation succeeded, or {@code false} if compilation failed.
   * @throws CompilerException if compilation throws an unhandled exception.
   */
  protected boolean runCompilationTask(CompilationTask task) {
    var name = getName();

    try {
      var start = System.nanoTime();
      var result = task.call();

      if (result == null) {
        throw new CompilerException("The compiler failed to produce a valid result");
      }

      LOGGER.info("Compilation with compiler {} {} after ~{}ms",
          name,
          result ? "succeeded" : "failed",
          Math.round((System.nanoTime() - start) / 1_000_000.0)
      );

      return result;
    } catch (Exception ex) {
      LOGGER.warn(
          "Compiler {} threw an exception: {}: {}",
          name,
          ex.getClass().getName(),
          ex.getMessage()
      );
      throw new CompilerException("The compiler threw an exception", ex);
    }
  }

  private void ensureClassOutputPathExists() {
    // We have to manually create this one as javac will not attempt to access it lazily. Instead,
    // it will just abort if it is not present. This means we cannot take advantage of the
    // PathLocationRepository creating the roots as we try to access them for this specific case.
    var classOutputManager = fileRepository
        .getOrCreate(StandardLocation.CLASS_OUTPUT);

    // Ensure we have somewhere to dump our output.
    if (classOutputManager.isEmpty()) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = RamPath.createPath("classes-" + UUID.randomUUID(), true);
      classOutputManager.addRamPath(classOutput);
    } else {
      LOGGER.trace("At least one output path is present, so no in-memory path will be created");
    }
  }

  private void registerClassPath() {
    // ECJ requires that we always create this, otherwise it refuses to run.
    var classPath = fileRepository
        .getOrCreate(StandardLocation.CLASS_PATH);

    if (inheritClassPath) {
      var currentClassPath = SpecialLocations.currentClassPathLocations();

      LOGGER.debug("Adding current classpath to compiler: {}", currentClassPath);
      classPath.addPaths(currentClassPath);

      var currentModulePath = SpecialLocations.currentModulePathLocations();

      LOGGER.debug(
          "Adding current module path to compiler class path and module path: {}",
          currentModulePath
      );

      // For some reason, the JDK module path has to also be added to the classpath for it
      // to be recognised. Failing to do this prevents the classes and test-classes directories
      // being added to the classpath with the other dependencies. This would otherwise result in
      // all dependencies being loaded, but not the code the user is actually trying to test.
      //
      // Weird, but it is what it is, I guess.
      classPath.addPaths(currentModulePath);

      fileRepository
          .getOrCreate(StandardLocation.MODULE_PATH)
          .addPaths(currentModulePath);
    }
  }

  private void registerPlatformClassPath() {
    if (inheritPlatformClassPath) {
      var currentPlatformClassPath = SpecialLocations.currentPlatformClassPathLocations();

      if (!currentPlatformClassPath.isEmpty()) {
        LOGGER.debug("Adding current platform classpath to compiler: {}", currentPlatformClassPath);

        fileRepository
            .getOrCreate(StandardLocation.PLATFORM_CLASS_PATH)
            .addPaths(currentPlatformClassPath);
      }
    }
  }

  private void registerJrtJimage() {
    if (inheritSystemModulePath) {
      var jrtLocations = SpecialLocations.javaRuntimeLocations();
      LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

      fileRepository
          .getOrCreate(StandardLocation.SYSTEM_MODULES)
          .addPaths(jrtLocations);
    }
  }

  private void configureAnnotationProcessors(CompilationTask task) {
    if (annotationProcessors.size() > 0) {
      LOGGER.debug("Annotation processor discovery is disabled (processors explicitly provided)");
      task.setProcessors(annotationProcessors);
      return;
    }

    switch (annotationProcessorDiscovery) {
      case ENABLED: {
        // Ensure the paths exist.
        fileRepository
            .get(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH)
            .orElseGet(() -> fileRepository
                .getOrCreate(StandardLocation.ANNOTATION_PROCESSOR_PATH));
        break;
      }

      case INCLUDE_DEPENDENCIES:
        fileRepository
            .get(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH)
            .ifPresentOrElse(
                procModules -> procModules.addPaths(
                    fileRepository
                        .getExpected(StandardLocation.MODULE_PATH)
                        .getRoots()),
                () -> fileRepository
                    .getOrCreate(StandardLocation.ANNOTATION_PROCESSOR_PATH)
                    .addPaths(fileRepository
                        .getExpected(StandardLocation.CLASS_PATH)
                        .getRoots())
            );
        break;

      case DISABLED:
      default:
        // Set the processor list explicitly to instruct the compiler to not perform discovery.
        task.setProcessors(List.of());
        break;
    }
  }
}
