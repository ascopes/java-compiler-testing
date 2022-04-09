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

package com.github.ascopes.jct.compilers.impl;

import static com.github.ascopes.jct.intern.IoExceptionUtils.uncheckedIo;

import com.github.ascopes.jct.compilers.Compiler;
import com.github.ascopes.jct.compilers.CompilerConfigurer;
import com.github.ascopes.jct.compilers.CompilerException;
import com.github.ascopes.jct.diagnostics.impl.TeeWriter;
import com.github.ascopes.jct.diagnostics.impl.TracingDiagnosticListener;
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
import java.util.function.Supplier;
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
@API(since = "0.0.1", status = Status.INTERNAL)
public final class CompilerImpl implements Compiler<CompilerImpl, CompilationImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompilerImpl.class);

  private final String name;
  private final JavaCompiler compiler;
  private final Supplier<FlagBuilder> flagBuilderSupplier;

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
  private boolean warningsAsErrors;
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
   * @param name                the name of the compiler.
   * @param compiler            the compiler implementation to use.
   * @param flagBuilderSupplier supplier that creates a flag builder to use to build flags.
   */
  public CompilerImpl(
      String name,
      JavaCompiler compiler,
      Supplier<FlagBuilder> flagBuilderSupplier
  ) {
    this.name = Objects.requireNonNull(name);
    this.compiler = Objects.requireNonNull(compiler);
    this.flagBuilderSupplier = Objects.requireNonNull(flagBuilderSupplier);

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
    includeCurrentPlatformClassPath = true;

    fileManagerLoggingMode = LoggingMode.DISABLED;
    diagnosticLoggingMode = LoggingMode.ENABLED;
  }

  @Override
  public CompilationImpl compile() {
    return uncheckedIo(() -> {
      var flags = buildFlags();
      var diagnosticListener = buildDiagnosticListener();

      try (var fileManager = applyLoggingToFileManager(buildJavaFileManager())) {
        var compilationUnits = discoverCompilationUnits(fileManager);
        LOGGER.debug("Discovered {} compilation units {}", compilationUnits.size(),
            compilationUnits);

        var writer = new TeeWriter(System.out);

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

        return CompilationImpl.builder()
            .warningsAsErrors(warningsAsErrors)
            .success(result)
            .outputLines(outputLines)
            .compilationUnits(Set.copyOf(compilationUnits))
            .diagnostics(diagnosticListener.getDiagnostics())
            .fileRepository(fileRepository)
            .build();
      }
    });
  }

  @Override
  public <T extends Exception> CompilerImpl configure(
      CompilerConfigurer<CompilerImpl, T> configurer
  ) throws T {
    LOGGER.debug("configure({})", configurer);
    configurer.configure(this);
    return this;
  }

  @Override
  public CompilerImpl verbose(boolean enabled) {
    LOGGER.trace("verbose {} -> {}", verbose, enabled);
    verbose = enabled;
    return this;
  }

  @Override
  public CompilerImpl previewFeatures(boolean enabled) {
    LOGGER.trace("previewFeatures {} -> {}", previewFeatures, enabled);
    previewFeatures = enabled;
    return this;
  }

  @Override
  public CompilerImpl warnings(boolean enabled) {
    LOGGER.trace("warnings {} -> {}", warnings, enabled);
    warnings = enabled;
    return this;
  }

  @Override
  public CompilerImpl deprecationWarnings(boolean enabled) {
    LOGGER.trace("deprecationWarnings {} -> {}", deprecationWarnings, enabled);
    deprecationWarnings = enabled;
    return this;
  }

  @Override
  public CompilerImpl warningsAsErrors(boolean enabled) {
    LOGGER.trace("warningsAsErrors {} -> {}", warningsAsErrors, enabled);
    warningsAsErrors = enabled;
    return this;
  }

  @Override
  public CompilerImpl addAnnotationProcessorOptions(Iterable<String> options) {
    LOGGER.trace("annotationProcessorOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      annotationProcessorOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  @Override
  public CompilerImpl addAnnotationProcessors(Iterable<? extends Processor> processors) {
    LOGGER.trace("annotationProcessors += {}", processors);
    for (var processor : Objects.requireNonNull(processors)) {
      annotationProcessors.add(Objects.requireNonNull(processor));
    }
    return this;
  }

  @Override
  public CompilerImpl addCompilerOptions(Iterable<String> options) {
    LOGGER.trace("compilerOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      compilerOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  @Override
  public CompilerImpl releaseVersion(String version) {
    LOGGER.trace("releaseVersion {} -> {}", releaseVersion, version);
    LOGGER.trace("sourceVersion {} -> null", sourceVersion);
    LOGGER.trace("targetVersion {} -> null", targetVersion);
    releaseVersion = version;
    sourceVersion = null;
    targetVersion = null;
    return this;
  }

  @Override
  public CompilerImpl sourceVersion(String version) {
    LOGGER.trace("sourceVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    sourceVersion = version;
    return this;
  }

  @Override
  public CompilerImpl targetVersion(String version) {
    LOGGER.trace("targetVersion {} -> {}", targetVersion, version);
    LOGGER.trace("releaseVersion {} -> null", releaseVersion);
    releaseVersion = null;
    targetVersion = version;
    return this;
  }

  @Override
  public CompilerImpl includeCurrentClassPath(boolean enabled) {
    LOGGER.trace("includeCurrentClassPath {} -> {}", includeCurrentClassPath, enabled);
    includeCurrentClassPath = enabled;
    return this;
  }

  @Override
  public CompilerImpl includeCurrentPlatformClassPath(boolean enabled) {
    LOGGER.trace(
        "includeCurrentPlatformClassPath {} -> {}",
        includeCurrentPlatformClassPath,
        enabled
    );
    includeCurrentPlatformClassPath = enabled;
    return this;
  }

  @Override
  public CompilerImpl addPath(Location location, Path path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addPath(path);
    return this;
  }

  @Override
  public CompilerImpl addPath(Location location, InMemoryPath path) {
    LOGGER.trace("{}.paths += {}", location.getName(), path);
    fileRepository.getOrCreate(location).addPath(path);
    return this;
  }

  @Override
  public CompilerImpl addPaths(Location location, Collection<? extends Path> paths) {
    LOGGER.trace("{}.paths += {}", location.getName(), paths);
    fileRepository.getOrCreate(location).addPaths(paths);
    return this;
  }

  @Override
  public CompilerImpl addRuntimeOptions(Iterable<String> options) {
    LOGGER.trace("runtimeOptions += {}", options);
    for (var option : Objects.requireNonNull(options)) {
      runtimeOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  @Override
  public CompilerImpl locale(Locale locale) {
    LOGGER.trace("locale {} -> {}", this.locale, locale);
    this.locale = Objects.requireNonNull(locale);
    return this;
  }

  @Override
  public CompilerImpl withFileManagerLogging(LoggingMode fileManagerLoggingMode) {
    LOGGER.trace(
        "fileManagerLoggingMode {} -> {}",
        this.fileManagerLoggingMode,
        fileManagerLoggingMode
    );
    this.fileManagerLoggingMode = Objects.requireNonNull(fileManagerLoggingMode);
    return this;
  }

  @Override
  public CompilerImpl withDiagnosticLogging(LoggingMode diagnosticLoggingMode) {
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
    return flagBuilderSupplier
        .get()
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
   * Build the file manager to use for compilation.
   *
   * @return the file manager.
   */
  private JavaFileManager buildJavaFileManager() {
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
}
