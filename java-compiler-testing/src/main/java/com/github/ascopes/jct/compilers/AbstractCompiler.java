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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
  protected boolean deprecationWarnings;
  protected boolean warningsAsErrors;
  protected Locale locale;
  protected boolean verbose;
  protected boolean previewFeatures;
  protected String releaseVersion;
  protected String sourceVersion;
  protected String targetVersion;
  protected boolean warnings;
  protected boolean includeCurrentClassPath;
  protected boolean includeCurrentModulePath;
  protected boolean includeCurrentPlatformClassPath;

  // Framework-specific functionality.
  protected LoggingMode fileManagerLoggingMode;
  protected LoggingMode diagnosticLoggingMode;

  /**
   * Initialize this compiler handler.
   */
  protected AbstractCompiler() {
    fileRepository = new PathLocationRepository();

    annotationProcessors = new ArrayList<>();

    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    deprecationWarnings = true;
    locale = Locale.ROOT;
    previewFeatures = false;
    releaseVersion = null;
    sourceVersion = null;
    targetVersion = null;
    verbose = false;
    warnings = true;
    includeCurrentClassPath = true;
    includeCurrentModulePath = true;
    includeCurrentPlatformClassPath = true;

    fileManagerLoggingMode = LoggingMode.DISABLED;
    diagnosticLoggingMode = LoggingMode.ENABLED;
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
  public A verbose(boolean enabled) {
    LOGGER.trace("verbose {} -> {}", verbose, enabled);
    verbose = enabled;
    return myself();
  }

  @Override
  public A previewFeatures(boolean enabled) {
    LOGGER.trace("previewFeatures {} -> {}", previewFeatures, enabled);
    previewFeatures = enabled;
    return myself();
  }

  @Override
  public A warnings(boolean enabled) {
    LOGGER.trace("warnings {} -> {}", warnings, enabled);
    warnings = enabled;
    return myself();
  }

  @Override
  public A deprecationWarnings(boolean enabled) {
    LOGGER.trace("deprecationWarnings {} -> {}", deprecationWarnings, enabled);
    deprecationWarnings = enabled;
    return myself();
  }

  @Override
  public A warningsAsErrors(boolean enabled) {
    LOGGER.trace("warningsAsErrors {} -> {}", warningsAsErrors, enabled);
    warningsAsErrors = enabled;
    return myself();
  }

  @Override
  public A addAnnotationProcessorOptions(Iterable<String> options) {
    LOGGER.trace("annotationProcessorOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      annotationProcessorOptions.add(Objects.requireNonNull(option));
    }
    return myself();
  }

  @Override
  public A addAnnotationProcessors(Iterable<? extends Processor> processors) {
    LOGGER.trace("annotationProcessors += {}", processors);
    for (var processor : Objects.requireNonNull(processors)) {
      annotationProcessors.add(Objects.requireNonNull(processor));
    }
    return myself();
  }

  @Override
  public A addCompilerOptions(Iterable<String> options) {
    LOGGER.trace("compilerOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      compilerOptions.add(Objects.requireNonNull(option));
    }
    return myself();
  }

  @Override
  public A releaseVersion(String version) {
    LOGGER.trace("releaseVersion {} -> {}", releaseVersion, version);
    LOGGER.trace("sourceVersion {} -> null", sourceVersion);
    LOGGER.trace("targetVersion {} -> null", targetVersion);
    releaseVersion = version;
    sourceVersion = null;
    targetVersion = null;
    return myself();
  }

  @Override
  public A sourceVersion(String version) {
    LOGGER.trace("sourceVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    sourceVersion = version;
    return myself();
  }

  @Override
  public A targetVersion(String version) {
    LOGGER.trace("targetVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    targetVersion = version;
    return myself();
  }

  @Override
  public A includeCurrentClassPath(boolean enabled) {
    LOGGER.trace("includeCurrentClassPath {} -> {}", includeCurrentClassPath, enabled);
    includeCurrentClassPath = enabled;
    return myself();
  }

  @Override
  public A includeCurrentModulePath(boolean enabled) {
    LOGGER.trace("includeCurrentModulePath {} -> {}", includeCurrentModulePath, enabled);
    includeCurrentModulePath = enabled;
    return myself();
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
  public A addPath(Location location, Path path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addPath(path);
    return myself();
  }

  @Override
  public A addPaths(Location location, Iterable<? extends Path> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addPaths(paths);
    return myself();
  }

  @Override
  public A addRamPath(Location location, RamPath path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addRamPath(path);
    return myself();
  }

  @Override
  public A addRamPaths(Location location, Iterable<? extends RamPath> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addRamPaths(paths);
    return myself();
  }

  @Override
  public A addRuntimeOptions(Iterable<String> options) {
    LOGGER.trace("runtimeOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      runtimeOptions.add(Objects.requireNonNull(option));
    }
    return myself();
  }

  @Override
  public A locale(Locale locale) {
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = Objects.requireNonNull(locale);
    return myself();
  }

  @Override
  public A withFileManagerLogging(LoggingMode fileManagerLoggingMode) {
    LOGGER.trace(
        "fileManagerLoggingMode {} -> {}",
        this.fileManagerLoggingMode,
        fileManagerLoggingMode
    );
    this.fileManagerLoggingMode = Objects.requireNonNull(fileManagerLoggingMode);
    return myself();
  }

  @Override
  public A withDiagnosticLogging(LoggingMode diagnosticLoggingMode) {
    LOGGER.trace(
        "diagnosticLoggingMode {} -> {}",
        this.diagnosticLoggingMode,
        diagnosticLoggingMode
    );
    this.diagnosticLoggingMode = Objects.requireNonNull(diagnosticLoggingMode);
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
    var classOutputManager = fileRepository
        .getOrCreate(StandardLocation.CLASS_OUTPUT);

    // Ensure we have somewhere to dump our output.
    if (classOutputManager.isEmpty()) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = RamPath.createPath("classes");
      classOutputManager.addRamPath(classOutput);
    } else {
      LOGGER.debug("At least one output path is present, so no in-memory path will be created");
    }

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

    if (includeCurrentPlatformClassPath) {
      var currentPlatformClassPath = SpecialLocations.currentPlatformClassPathLocations();

      if (!currentPlatformClassPath.isEmpty()) {
        LOGGER.debug("Adding current platform classpath to compiler: {}", currentPlatformClassPath);

        fileRepository
            .getOrCreate(StandardLocation.PLATFORM_CLASS_PATH)
            .addPaths(currentPlatformClassPath);
      }
    }

    var jrtLocations = SpecialLocations.javaRuntimeLocations();
    LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

    fileRepository
        .getOrCreate(StandardLocation.SYSTEM_MODULES)
        .addPaths(jrtLocations);

    return new PathJavaFileManager(fileRepository);
  }

  protected List<? extends JavaFileObject> discoverCompilationUnits(
      JavaFileManager fileManager
  ) throws IOException {
    var locations = new ArrayList<Location>();
    locations.add(StandardLocation.SOURCE_PATH);

    fileManager
        .listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)
        .forEach(locations::addAll);

    var objects = new ArrayList<JavaFileObject>();

    for (var location : locations) {
      for (var fileObject : fileManager.list(location, "", Set.of(Kind.SOURCE), true)) {
        objects.add(fileObject);
      }
    }

    return objects;
  }

  protected JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLoggingMode == LoggingMode.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLoggingMode == LoggingMode.ENABLED) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  protected TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener() {
    return new TracingDiagnosticListener<>(
        fileManagerLoggingMode != LoggingMode.DISABLED,
        fileManagerLoggingMode == LoggingMode.STACKTRACES
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
      var result = task.call();

      if (result == null) {
        throw new CompilerException("The compiler failed to produce a valid result");
      }

      LOGGER.info("Compilation with compiler {} {}", name, result ? "succeeded" : "failed");

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

  @SuppressWarnings("unchecked")
  protected final A myself() {
    return (A) this;
  }

  protected abstract S doCompile() throws IOException;

  protected abstract String getName();

  protected abstract JavaCompiler createJsr199Compiler();

  protected abstract FlagBuilder createFlagBuilder();
}