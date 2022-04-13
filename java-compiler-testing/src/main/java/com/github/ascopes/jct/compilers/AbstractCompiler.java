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

import static com.github.ascopes.jct.intern.IoExceptionUtils.uncheckedIo;
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

  // File workspace.
  protected final PathLocationRepository fileRepository;

  // Annotation processors that were explicitly added.
  protected final List<Processor> annotationProcessors;

  // User-defined option collections.
  protected final List<String> annotationProcessorOptions;
  protected final List<String> compilerOptions;
  protected final List<String> runtimeOptions;

  // Common flags.
  protected boolean warnings;
  protected boolean deprecationWarnings;
  protected boolean warningsAsErrors;
  protected Locale locale;
  protected boolean verbose;
  protected boolean previewFeatures;
  protected String releaseVersion;
  protected String sourceVersion;
  protected String targetVersion;
  protected boolean includeCurrentClassPath;
  protected boolean includeCurrentModulePath;
  protected boolean includeCurrentPlatformClassPath;

  // Framework-specific functionality.
  protected LoggingMode fileManagerLogging;
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
  public final S compile() {
    return uncheckedIo(this::doCompile);
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

  protected List<String> buildFlags() {
    return createFlagBuilder()
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

  protected JavaFileManager buildJavaFileManager() {
    registerClassOutputPath();
    registerClassPath();
    registerPlatformClassPath();
    registerJrtJimage();
    return new PathJavaFileManager(fileRepository);
  }

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

  protected JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLogging == LoggingMode.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLogging == LoggingMode.ENABLED) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  protected TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener() {
    return new TracingDiagnosticListener<>(
        fileManagerLogging != LoggingMode.DISABLED,
        fileManagerLogging == LoggingMode.STACKTRACES
    );
  }

  protected CompilationTask buildCompilationTask(
      Writer writer,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> flags,
      List<? extends JavaFileObject> compilationUnits
  ) {
    var name = getName();
    var compiler = createJsr199Compiler();

    var task = compiler.getTask(
        writer,
        fileManager,
        diagnosticListener,
        flags,
        null,
        compilationUnits
    );

    task.setProcessors(annotationProcessors);
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

  protected Boolean runCompilationTask(CompilationTask task) {
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

  protected void registerClassOutputPath() {
    // We have to manually create this one as javac will not attempt to access it lazily. Instead,
    // it will just abort if it is not present. This means we cannot take advantage of the
    // PathLocationRepository creating the roots as we try to access them for this specific case.
    var classOutputManager = fileRepository
        .getOrCreate(StandardLocation.CLASS_OUTPUT);

    // Ensure we have somewhere to dump our output.
    if (classOutputManager.isEmpty()) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = RamPath.createPath("classes-" + UUID.randomUUID());
      classOutputManager.addRamPath(classOutput);
    } else {
      LOGGER.trace("At least one output path is present, so no in-memory path will be created");
    }
  }

  protected void registerClassPath() {
    // ECJ requires that we always create this, otherwise it refuses to run.
    var classPath = fileRepository
        .getOrCreate(StandardLocation.CLASS_PATH);

    if (includeCurrentClassPath) {
      var currentClassPath = SpecialLocations.currentClassPathLocations();

      LOGGER.debug("Adding current classpath to compiler: {}", currentClassPath);
      classPath.addPaths(currentClassPath);
    }

    if (includeCurrentClassPath) {
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

  protected void registerPlatformClassPath() {
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

  protected void registerJrtJimage() {
    var jrtLocations = SpecialLocations.javaRuntimeLocations();
    LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

    fileRepository
        .getOrCreate(StandardLocation.SYSTEM_MODULES)
        .addPaths(jrtLocations);
  }

  @SuppressWarnings("unchecked")
  protected final A myself() {
    return (A) this;
  }

  protected abstract S doCompile() throws IOException;

  protected abstract String getName();

  protected abstract JavaCompiler createJsr199Compiler();

  protected abstract FlagBuilder createFlagBuilder();
}