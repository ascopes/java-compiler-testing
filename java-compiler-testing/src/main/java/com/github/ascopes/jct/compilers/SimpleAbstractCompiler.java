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
import com.github.ascopes.jct.paths.RamPath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
 * Abstract compiler that compiles to a {@link SimpleCompilation} object.
 *
 * @param <A> the type of the class extending this class.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class SimpleAbstractCompiler<A extends SimpleAbstractCompiler<A>>
    extends AbstractCompiler<A, SimpleCompilation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAbstractCompiler.class);

  private final Supplier<? extends JavaCompiler> compilerSupplier;
  private final Supplier<? extends FlagBuilder> flagBuilderSupplier;

  /**
   * Initialize this compiler.
   *
   * @param compilerSupplier the supplier of the compiler implementation to use internally.
   * @param flagBuilderSupplier supplier of a flag builder to build flags with.
   */
  protected SimpleAbstractCompiler(
      Supplier<? extends JavaCompiler> compilerSupplier,
      Supplier<? extends FlagBuilder> flagBuilderSupplier
  ) {
    this.compilerSupplier = requireNonNull(compilerSupplier);
    this.flagBuilderSupplier = requireNonNull(flagBuilderSupplier);
  }

  @Override
  public SimpleCompilation compile() {
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

  private List<String> buildFlags() {
    return flagBuilderSupplier.get()
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
  private List<? extends JavaFileObject> discoverCompilationUnits(
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
  private JavaFileManager applyLoggingToFileManager(JavaFileManager fileManager) {
    if (fileManagerLogging == LoggingMode.STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLogging == LoggingMode.ENABLED) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  private TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener() {
    return new TracingDiagnosticListener<>(
        fileManagerLogging != LoggingMode.DISABLED,
        fileManagerLogging == LoggingMode.STACKTRACES
    );
  }

  private CompilationTask buildCompilationTask(
      Writer writer,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> flags,
      List<? extends JavaFileObject> compilationUnits
  ) {
    var name = getName();
    var compiler = compilerSupplier.get();

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

  private boolean runCompilationTask(CompilationTask task) {
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
