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

import com.github.ascopes.jct.compilations.StandardCompilation;
import com.github.ascopes.jct.diagnostics.TeeWriter;
import com.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import com.github.ascopes.jct.intern.SpecialLocations;
import com.github.ascopes.jct.intern.StringUtils;
import com.github.ascopes.jct.paths.InMemoryPath;
import com.github.ascopes.jct.paths.LoggingJavaFileManagerProxy;
import com.github.ascopes.jct.paths.PathJavaFileManager;
import com.github.ascopes.jct.paths.PathLocationRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
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
 * An implementation of a wrapper around a {@code javac}-like compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class StandardCompiler implements Compiler<StandardCompiler, StandardCompilation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StandardCompiler.class);

  private final String name;
  private final JavaCompiler compiler;

  // File workspace.
  private final PathLocationRepository fileRepository;

  // Annotation processors that were explicitly added.
  private final List<Processor> annotationProcessors;

  // User-defined option collections.
  private final List<String> annotationProcessorOptions;
  private final List<String> compilerOptions;
  private final List<String> runtimeOptions;

  // Common flags.
  private boolean deprecationWarnings;
  private boolean failOnWarnings;
  private Locale locale;
  private boolean verbose;
  private boolean previewFeatures;
  private String releaseVersion;
  private String sourceVersion;
  private String targetVersion;
  private boolean warnings;
  private boolean includeCurrentClassPath;
  private boolean includeCurrentPlatformClassPath;

  // Framework-specific functionality.
  private LoggingMode fileManagerLoggingMode;
  private LoggingMode diagnosticLoggingMode;

  /**
   * Initialize this compiler handler.
   *
   * @param name     the name of the compiler.
   * @param compiler the compiler implementation to use.
   */
  public StandardCompiler(String name, JavaCompiler compiler) {
    this.name = Objects.requireNonNull(name);
    this.compiler = Objects.requireNonNull(compiler);

    fileRepository = new PathLocationRepository();

    annotationProcessors = new ArrayList<>();

    annotationProcessorOptions = new ArrayList<>();
    compilerOptions = new ArrayList<>();
    runtimeOptions = new ArrayList<>();

    deprecationWarnings = true;
    failOnWarnings = false;
    locale = Locale.ROOT;
    previewFeatures = false;
    releaseVersion = null;
    sourceVersion = null;
    targetVersion = null;
    verbose = false;
    warnings = true;
    includeCurrentClassPath = true;
    includeCurrentPlatformClassPath = true;

    fileManagerLoggingMode = LoggingMode.DISABLED;
    diagnosticLoggingMode = LoggingMode.ENABLED;
  }

  @Override
  public StandardCompilation compile() throws IOException {

    var flags = buildFlags();
    var diagnosticListener = buildDiagnosticListener();

    try (var fileManager = applyLoggingToFileManager(buildJavaFileManager())) {
      var compilationUnits = discoverCompilationUnits(fileManager);
      if (compilationUnits.isEmpty()) {
        throw new IllegalStateException("No compilation units found");
      }
      LOGGER.debug("Discovered {} compilation units {}", compilationUnits.size(), compilationUnits);

      var writer = new TeeWriter(System.err);

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

      Boolean result;

      try {
        result = task.call();

        if (result == null) {
          throw new CompilerException("The compiler failed to produce a valid result");
        }

        LOGGER.info("Compilation with compiler {} {}", name, result ? "succeeded" : "failed");
      } catch (Exception ex) {
        LOGGER.warn(
            "Compiler {} threw an exception: {}: {}",
            name,
            ex.getClass().getName(),
            ex.getMessage()
        );
        throw new CompilerException("The compiler threw an exception", ex);

      }

      var outputLines = writer.toString().lines().collect(Collectors.toList());

      return new StandardCompilation(
          failOnWarnings || flags.contains("-Werror"),
          result,
          outputLines,
          Set.copyOf(compilationUnits),
          diagnosticListener.getDiagnostics(),
          fileRepository
      );
    }
  }

  @Override
  public StandardCompiler configure(CompilerConfigurer<StandardCompiler> configurer) {
    LOGGER.debug("configure({})", configurer);
    configurer.configure(this);
    return this;
  }

  @Override
  public StandardCompiler verbose(boolean enabled) {
    LOGGER.trace("verbose {} -> {}", verbose, enabled);
    verbose = enabled;
    return this;
  }

  @Override
  public StandardCompiler previewFeatures(boolean enabled) {
    LOGGER.trace("previewFeatures {} -> {}", previewFeatures, enabled);
    previewFeatures = enabled;
    return this;
  }

  @Override
  public StandardCompiler warnings(boolean enabled) {
    LOGGER.trace("warnings {} -> {}", warnings, enabled);
    warnings = enabled;
    return this;
  }

  @Override
  public StandardCompiler deprecationWarnings(boolean enabled) {
    LOGGER.trace("deprecationWarnings {} -> {}", deprecationWarnings, enabled);
    deprecationWarnings = enabled;
    return this;
  }

  @Override
  public StandardCompiler failOnWarnings(boolean enabled) {
    LOGGER.trace("failOnWarnings {} -> {}", failOnWarnings, enabled);
    failOnWarnings = enabled;
    return this;
  }

  @Override
  public StandardCompiler addAnnotationProcessorOptions(Iterable<String> options) {
    LOGGER.trace("annotationProcessorOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      annotationProcessorOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  @Override
  public StandardCompiler addAnnotationProcessors(Iterable<? extends Processor> processors) {
    LOGGER.trace("annotationProcessors += {}", processors);
    for (var processor : Objects.requireNonNull(processors)) {
      annotationProcessors.add(Objects.requireNonNull(processor));
    }
    return this;
  }

  @Override
  public StandardCompiler addCompilerOptions(Iterable<String> options) {
    LOGGER.trace("compilerOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      compilerOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  @Override
  public StandardCompiler releaseVersion(String version) {
    LOGGER.trace("releaseVersion {} -> {}", releaseVersion, version);
    LOGGER.trace("sourceVersion {} -> null", sourceVersion);
    LOGGER.trace("targetVersion {} -> null", targetVersion);
    releaseVersion = version;
    sourceVersion = null;
    targetVersion = null;
    return this;
  }

  @Override
  public StandardCompiler sourceVersion(String version) {
    LOGGER.trace("sourceVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    sourceVersion = version;
    return this;
  }

  @Override
  public StandardCompiler targetVersion(String version) {
    LOGGER.trace("targetVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    targetVersion = version;
    return this;
  }

  @Override
  public StandardCompiler includeCurrentClassPath(boolean enabled) {
    LOGGER.trace("includeCurrentClassPath {} -> {}", includeCurrentClassPath, enabled);
    includeCurrentClassPath = enabled;
    return this;
  }

  @Override
  public StandardCompiler includeCurrentPlatformClassPath(boolean enabled) {
    LOGGER.trace(
        "includeCurrentPlatformClassPath {} -> {}",
        includeCurrentPlatformClassPath,
        enabled
    );
    includeCurrentPlatformClassPath = enabled;
    return this;
  }

  @Override
  public StandardCompiler addPath(Location location, Path path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addPath(path);
    return this;
  }

  @Override
  public StandardCompiler addPath(Location location, InMemoryPath path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addPath(path);
    return this;
  }

  @Override
  public StandardCompiler addPaths(Location location, Collection<? extends Path> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addPaths(paths);
    return this;
  }

  /**
   * Add options to pass to the Java runtime.
   *
   * @param options the options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  public StandardCompiler addRuntimeOptions(Iterable<String> options) {
    LOGGER.trace("runtimeOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      runtimeOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  /**
   * Set the output locale.
   *
   * @param locale the locale to use.
   * @return this compiler for further call chaining.
   */
  public StandardCompiler locale(Locale locale) {
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = Objects.requireNonNull(locale);
    return this;
  }

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * @param fileManagerLoggingMode the mode to use for file manager logging.
   * @return this compiler for further call chaining.
   */
  public StandardCompiler withFileManagerLogging(LoggingMode fileManagerLoggingMode) {
    LOGGER.trace(
        "fileManagerLoggingMode {} -> {}",
        this.fileManagerLoggingMode,
        fileManagerLoggingMode
    );
    this.fileManagerLoggingMode = Objects.requireNonNull(fileManagerLoggingMode);
    return this;
  }

  /**
   * Set how to handle diagnostic capture.
   *
   * @param diagnosticLoggingMode the mode to use for diagnostic capture.
   * @return this compiler for further call chaining.
   */
  public StandardCompiler withDiagnosticLogging(LoggingMode diagnosticLoggingMode) {
    LOGGER.trace(
        "diagnosticLoggingMode {} -> {}",
        this.diagnosticLoggingMode,
        diagnosticLoggingMode
    );
    this.diagnosticLoggingMode = Objects.requireNonNull(diagnosticLoggingMode);
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Format all command line flags to pass to javac.
   *
   * @return the command line flags.
   */
  private List<String> buildFlags() {
    return new JavacFlagBuilder()
        .annotationProcessorOptions(annotationProcessorOptions)
        .deprecationWarnings(deprecationWarnings)
        .failOnWarnings(failOnWarnings)
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
   * Build the file manager to use for compilation.
   *
   * @return the file manager.
   */
  private JavaFileManager buildJavaFileManager() throws IOException {
    var classOutputManager = fileRepository
        .getOrCreate(StandardLocation.CLASS_OUTPUT);

    // Ensure we have somewhere to dump our output.
    if (classOutputManager.isEmpty()) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = InMemoryPath.createPath("classes");
      classOutputManager.addPath(classOutput);
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

  /**
   * Discover the compilation units available to compile.
   *
   * @param fileManager the file manager to use.
   * @return the compilation units to use.
   * @throws IOException if an IO error occurs.
   */
  private List<? extends JavaFileObject> discoverCompilationUnits(
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

  /**
   * Apply logging interception to a given file manager if the feature has been enabled.
   *
   * @param fileManager the file manager to apply to.
   * @return the decorated file manager.
   */
  private JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLoggingMode == LoggingMode.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLoggingMode == LoggingMode.ENABLED) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  /**
   * Build a diagnostic listener.
   *
   * @return the diagnostic listener to use.
   */
  private TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener() {
    return new TracingDiagnosticListener<>(
        fileManagerLoggingMode != LoggingMode.DISABLED,
        fileManagerLoggingMode == LoggingMode.STACKTRACES
    );
  }

  /**
   * Options for how to handle logging on special internal components.
   */
  public enum LoggingMode {
    /**
     * Enable logging.
     */
    ENABLED,

    /**
     * Enable logging and include stacktraces in the logs for each entry.
     */
    STACKTRACES,

    /**
     * Do not log anything.
     */
    DISABLED,
  }

}
