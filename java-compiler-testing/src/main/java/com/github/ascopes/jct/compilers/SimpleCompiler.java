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
import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.io.Writer;
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
  private final PathLocationRepository fileRepository;
  private final List<Processor> annotationProcessors;
  private final List<String> annotationProcessorOptions;
  private final List<String> compilerOptions;
  private final List<String> runtimeOptions;
  private boolean warnings;
  private boolean deprecationWarnings;
  private boolean warningsAsErrors;
  private Locale locale;
  private boolean verbose;
  private boolean previewFeatures;
  private String releaseVersion;
  private String sourceVersion;
  private String targetVersion;
  private boolean includeCurrentClassPath;
  private boolean includeCurrentModulePath;
  private boolean includeCurrentPlatformClassPath;
  private LoggingMode fileManagerLogging;
  private LoggingMode diagnosticLogging;

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
  public final <T extends Exception> A configure(Configurer<A, T> configurer) throws T {
    LOGGER.debug("configure({})", configurer);
    var me = myself();
    configurer.configure(me);
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

        var writer = new TeeWriter(System.out);
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
            .warningsAsErrors(warningsAsErrors)
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
        .deprecationWarnings(deprecationWarnings)
        .warningsAsErrors(warningsAsErrors)
        .options(compilerOptions)
        .previewFeatures(previewFeatures)
        .releaseVersion(releaseVersion)
        .runtimeOptions(runtimeOptions)
        .sourceVersion(sourceVersion)
        .targetVersion(targetVersion)
        .verbose(verbose)
        .warnings(warnings)
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
   * <strong>not</strong> set to {@link LoggingMode#DISABLED}. In the latter scenario, the input
   * will be returned to the caller with no other modifications.
   *
   * @param fileManager the file manager to apply to.
   * @return the file manager to use for future operations.
   */
  protected JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLogging == LoggingMode.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLogging == LoggingMode.ENABLED) {
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
        fileManagerLogging != LoggingMode.DISABLED,
        fileManagerLogging == LoggingMode.STACKTRACES
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

    if (!annotationProcessors.isEmpty()) {
      // TODO(ascopes): would we ever want to set an empty list for this, to bypass discovery?
      task.setProcessors(annotationProcessors);
    } else {
      // TODO(ascopes): would we ever want to explicitly disable this behaviour?
      fileRepository
          .getOrCreate(StandardLocation.ANNOTATION_PROCESSOR_PATH)
          .addPaths(fileRepository.getExpected(StandardLocation.CLASS_PATH).getRoots());
    }

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

    if (includeCurrentClassPath) {
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
    if (includeCurrentPlatformClassPath) {
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
    var jrtLocations = SpecialLocations.javaRuntimeLocations();
    LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

    fileRepository
        .getOrCreate(StandardLocation.SYSTEM_MODULES)
        .addPaths(jrtLocations);
  }
}