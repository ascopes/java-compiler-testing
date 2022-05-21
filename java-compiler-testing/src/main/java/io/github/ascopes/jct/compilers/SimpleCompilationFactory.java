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

import io.github.ascopes.jct.compilers.Compiler.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.compilers.Compiler.Logging;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.jsr199.diagnostics.TeeWriter;
import io.github.ascopes.jct.jsr199.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.utils.SpecialLocations;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.compilers.managers.LoggingFileManagerProxy;
import io.github.ascopes.jct.paths.PathJavaFileManager;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
 * Helper for performing a compilation using a given compiler.
 *
 * @param <A> the compiler implementation to use.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimpleCompilationFactory<A extends java.lang.Compiler<A, SimpleCompilation>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCompilationFactory.class);

  /**
   * Run the compilation for the given compiler and return the compilation result.
   *
   * @param compiler       the compiler to run.
   * @param jsr199Compiler the underlying JSR-199 compiler to run via.
   * @param flagBuilder    the flag builder to use.
   * @return the compilation result.
   */
  public SimpleCompilation compile(
      A compiler,
      JavaCompiler jsr199Compiler,
      FlagBuilder flagBuilder
  ) {
    try {
      var flags = buildFlags(compiler, flagBuilder);
      var diagnosticListener = buildDiagnosticListener(compiler);
      var writer = buildWriter(compiler);

      try (var fileManager = buildJavaFileManager(compiler)) {
        var compilationUnits = findCompilationUnits(fileManager);
        LOGGER.debug("Found {} compilation units {}", compilationUnits.size(), compilationUnits);

        var task = buildCompilationTask(
            compiler,
            jsr199Compiler,
            writer,
            applyLoggingToFileManager(compiler, fileManager),
            diagnosticListener,
            flags,
            compilationUnits
        );

        var result = runCompilationTask(compiler, task);

        var outputLines = writer.toString().lines().collect(Collectors.toList());

        return SimpleCompilation.builder()
            .failOnWarnings(compiler.isFailOnWarnings())
            .success(result)
            .outputLines(outputLines)
            .compilationUnits(Set.copyOf(compilationUnits))
            .diagnostics(diagnosticListener.getDiagnostics())
            .pathLocationRepository(compiler.getPathLocationRepository())
            .build();
      }
    } catch (IOException ex) {
      throw new CompilerException("Failed to compile due to an IOException: " + ex, ex);
    }
  }

  /**
   * Build the {@link TeeWriter} to dump compiler logs to.
   *
   * @param compiler the compiler to use.
   * @return the tee writer.
   */
  protected TeeWriter buildWriter(A compiler) {
    return new TeeWriter(compiler.getLogCharset(), System.out);
  }

  /**
   * Build the flags to pass to the JSR-199 compiler.
   *
   * @param compiler    the compiler to use.
   * @param flagBuilder the flag builder to use.
   * @return the flags to use.
   */
  protected List<String> buildFlags(A compiler, FlagBuilder flagBuilder) {
    return flagBuilder
        .annotationProcessorOptions(compiler.getAnnotationProcessorOptions())
        .showDeprecationWarnings(compiler.isShowDeprecationWarnings())
        .failOnWarnings(compiler.isFailOnWarnings())
        .compilerOptions(compiler.getCompilerOptions())
        .runtimeOptions(compiler.getRuntimeOptions())
        .previewFeatures(compiler.isPreviewFeatures())
        .release(compiler.getRelease().orElse(null))
        .source(compiler.getSource().orElse(null))
        .target(compiler.getTarget().orElse(null))
        .verbose(compiler.isVerbose())
        .showWarnings(compiler.isShowWarnings())
        .build();
  }

  /**
   * Build the {@link JavaFileManager} to use.
   *
   * <p>Logging will be applied to this via
   * {@link #applyLoggingToFileManager(java.lang.Compiler, JavaFileManager)}, which will be handled by
   * {@link #compile(java.lang.Compiler, JavaCompiler, FlagBuilder)}.
   *
   * @param compiler the compiler to use.
   * @return the file manager to use.
   */
  protected JavaFileManager buildJavaFileManager(A compiler) {
    ensureClassOutputPathExists(compiler);
    registerClassPath(compiler);
    registerPlatformClassPath(compiler);
    registerSystemModulePath(compiler);
    return new PathJavaFileManager(compiler.getPathLocationRepository());
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
  protected List<? extends JavaFileObject> findCompilationUnits(
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
   * Apply the logging level to the file manager provided by
   * {@link #buildJavaFileManager(java.lang.Compiler)}.
   *
   * <p>The default implementation will wrap the given {@link JavaFileManager} in a
   * {@link LoggingFileManagerProxy} if the {@link java.lang.Compiler#getFileManagerLogging()} field is
   * <strong>not</strong> set to {@link Logging#DISABLED}. In the latter scenario, the input
   * will be returned to the caller with no other modifications.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to apply to.
   * @return the file manager to use for future operations.
   */
  protected JavaFileManager applyLoggingToFileManager(
      A compiler,
      JavaFileManager fileManager
  ) {
    switch (compiler.getFileManagerLogging()) {
      case Logging.STACKTRACES:
        return LoggingFileManagerProxy.wrap(fileManager, true);
      case Logging.ENABLED:
        return LoggingFileManagerProxy.wrap(fileManager, false);
      default:
        return fileManager;
    }
  }

  /**
   * Build a diagnostics listener.
   *
   * <p>This will also apply the desired logging configuration to the listener before returning it.
   *
   * @param compiler the compiler to use.
   * @return the diagnostics listener.
   */
  protected TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener(A compiler) {
    var logging = compiler.getDiagnosticLogging();

    return new TracingDiagnosticListener<>(
        logging != Logging.DISABLED,
        logging == Logging.STACKTRACES
    );
  }

  /**
   * Build the compilation task.
   *
   * @param compiler           the compiler to use.
   * @param jsr199Compiler     the JSR-199 compiler to use internally.
   * @param writer             the writer to write diagnostics to.
   * @param fileManager        the file manager to use.
   * @param diagnosticListener the diagnostic listener to use.
   * @param flags              the flags to pass to the JSR-199 compiler.
   * @param compilationUnits   the compilation units to compile with.
   * @return the compilation task, ready to be run.
   */
  protected CompilationTask buildCompilationTask(
      A compiler,
      JavaCompiler jsr199Compiler,
      Writer writer,
      JavaFileManager fileManager,
      DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> flags,
      List<? extends JavaFileObject> compilationUnits
  ) {
    var name = compiler.toString();

    var task = jsr199Compiler.getTask(
        writer,
        fileManager,
        diagnosticListener,
        flags,
        null,
        compilationUnits
    );

    configureAnnotationProcessors(compiler, task);

    task.setLocale(compiler.getLocale());

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
   * @param compiler the compiler to use.
   * @param task     the task to run.
   * @return {@code true} if the compilation succeeded, or {@code false} if compilation failed.
   * @throws CompilerException if compilation throws an unhandled exception.
   */
  protected boolean runCompilationTask(A compiler, CompilationTask task) {
    var name = compiler.toString();

    try {
      var start = System.nanoTime();
      var result = task.call();
      var duration = System.nanoTime() - start;

      if (result == null) {
        throw new CompilerException("The compiler failed to produce a valid result");
      }

      LOGGER.info("Compilation with compiler {} {} after ~{}",
          name,
          result ? "succeeded" : "failed",
          StringUtils.formatNanos(duration)
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

  private void ensureClassOutputPathExists(A compiler) {
    // We have to manually create this one as javac will not attempt to access it lazily. Instead,
    // it will just abort if it is not present. This means we cannot take advantage of the
    // PathLocationRepository creating the roots as we try to access them for this specific case.
    var classOutputManager = compiler
        .getPathLocationRepository()
        .getOrCreateManager(StandardLocation.CLASS_OUTPUT);

    // Ensure we have somewhere to dump our output.
    if (classOutputManager.isEmpty()) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = RamPath.createPath("classes-" + UUID.randomUUID(), true);
      classOutputManager.addRamPath(classOutput);
    } else {
      LOGGER.trace("At least one output path is present, so no in-memory path will be created");
    }
  }

  private void registerClassPath(A compiler) {
    // ECJ requires that we always create this, otherwise it refuses to run.
    var classPath = compiler
        .getPathLocationRepository()
        .getOrCreateManager(StandardLocation.CLASS_PATH);

    if (!compiler.isInheritClassPath()) {
      return;
    }

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

    compiler
        .getPathLocationRepository()
        .getOrCreateManager(StandardLocation.MODULE_PATH)
        .addPaths(currentModulePath);
  }

  private void registerPlatformClassPath(A compiler) {
    if (!compiler.isInheritPlatformClassPath()) {
      return;
    }

    var currentPlatformClassPath = SpecialLocations.currentPlatformClassPathLocations();

    if (!currentPlatformClassPath.isEmpty()) {
      LOGGER.debug("Adding current platform classpath to compiler: {}", currentPlatformClassPath);

      compiler.getPathLocationRepository()
          .getOrCreateManager(StandardLocation.PLATFORM_CLASS_PATH)
          .addPaths(currentPlatformClassPath);
    }
  }

  private void registerSystemModulePath(A compiler) {
    if (!compiler.isInheritSystemModulePath()) {
      return;
    }

    var jrtLocations = SpecialLocations.javaRuntimeLocations();
    LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

    compiler
        .getPathLocationRepository()
        .getOrCreateManager(StandardLocation.SYSTEM_MODULES)
        .addPaths(jrtLocations);
  }

  private void configureAnnotationProcessors(A compiler, CompilationTask task) {
    if (compiler.getAnnotationProcessors().size() > 0) {
      LOGGER.debug("Annotation processor discovery is disabled (processors explicitly provided)");
      task.setProcessors(compiler.getAnnotationProcessors());
      return;
    }

    switch (compiler.getAnnotationProcessorDiscovery()) {
      case AnnotationProcessorDiscovery.ENABLED:
        // Ensure the paths exist.
        compiler
            .getPathLocationRepository()
            .getManager(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH)
            .orElseGet(() -> compiler
                .getPathLocationRepository()
                .getOrCreateManager(StandardLocation.ANNOTATION_PROCESSOR_PATH));
        break;

      case AnnotationProcessorDiscovery.INCLUDE_DEPENDENCIES:
        compiler
            .getPathLocationRepository()
            .getManager(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH)
            .ifPresentOrElse(
                procModules -> procModules.addPaths(
                    compiler
                        .getPathLocationRepository()
                        .getExpectedManager(StandardLocation.MODULE_PATH)
                        .getRoots()),
                () -> compiler
                    .getPathLocationRepository()
                    .getOrCreateManager(StandardLocation.ANNOTATION_PROCESSOR_PATH)
                    .addPaths(compiler
                        .getPathLocationRepository()
                        .getExpectedManager(StandardLocation.CLASS_PATH)
                        .getRoots())
            );
        break;

      default:
        // Set the processor list explicitly to instruct the compiler to not perform discovery.
        task.setProcessors(List.of());
        break;
    }
  }
}
