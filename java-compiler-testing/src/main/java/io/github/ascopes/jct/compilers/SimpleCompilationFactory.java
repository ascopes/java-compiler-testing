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

import io.github.ascopes.jct.jsr199.FileManager;
import io.github.ascopes.jct.jsr199.LoggingFileManagerProxy;
import io.github.ascopes.jct.jsr199.diagnostics.TeeWriter;
import io.github.ascopes.jct.jsr199.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.paths.NioPath;
import io.github.ascopes.jct.paths.RamPath;
import io.github.ascopes.jct.utils.SpecialLocations;
import io.github.ascopes.jct.utils.StringUtils;
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
public class SimpleCompilationFactory<A extends Compilable<A, SimpleCompilation>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCompilationFactory.class);

  /**
   * Run the compilation for the given compiler and return the compilation result.
   *
   * @param compiler       the compiler to run.
   * @param template       the template to compile files within.
   * @param jsr199Compiler the underlying JSR-199 compiler to run via.
   * @param flagBuilder    the flag builder to use.
   * @return the compilation result.
   */
  public SimpleCompilation compile(
      A compiler,
      SimpleFileManagerTemplate template,
      JavaCompiler jsr199Compiler,
      FlagBuilder flagBuilder
  ) {
    try {
      var flags = buildFlags(compiler, flagBuilder);
      var diagnosticListener = buildDiagnosticListener(compiler);
      var writer = buildWriter(compiler);

      try (var fileManager = buildFileManager(compiler, template)) {
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
            .fileManager(fileManager)
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
   * <p>LoggingMode will be applied to this via
   * {@link #applyLoggingToFileManager(Compilable, FileManager)}, which will be handled by
   * {@link #compile(Compilable, SimpleFileManagerTemplate, JavaCompiler, FlagBuilder)}.
   *
   * @param compiler the compiler to use.
   * @return the file manager to use.
   */
  protected FileManager buildFileManager(A compiler, SimpleFileManagerTemplate template) {
    var release = compiler.getRelease()
        .or(compiler::getTarget)
        .orElseGet(compiler::getDefaultRelease);

    var fileManager = template.createFileManager(release);
    ensureClassOutputPathExists(fileManager);
    registerClassPath(compiler, fileManager);
    registerPlatformClassPath(compiler, fileManager);
    registerSystemModulePath(compiler, fileManager);
    registerAnnotationProcessorPaths(compiler, fileManager);
    return fileManager;
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
   * {@link #buildFileManager(Compilable, SimpleFileManagerTemplate)}.
   *
   * <p>The default implementation will wrap the given {@link JavaFileManager} in a
   * {@link LoggingFileManagerProxy} if the {@link Compilable#getFileManagerLoggingMode()} field is
   * <strong>not</strong> set to {@link LoggingMode#DISABLED}. In the latter scenario, the input
   * will be returned to the caller with no other modifications.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to apply to.
   * @return the file manager to use for future operations.
   */
  protected FileManager applyLoggingToFileManager(
      A compiler,
      FileManager fileManager
  ) {
    switch (compiler.getFileManagerLoggingMode()) {
      case STACKTRACES:
        return LoggingFileManagerProxy.wrap(fileManager, true);
      case ENABLED:
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
    var logging = compiler.getDiagnosticLoggingMode();

    return new TracingDiagnosticListener<>(
        logging != LoggingMode.DISABLED,
        logging == LoggingMode.STACKTRACES
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
      FileManager fileManager,
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

    configureAnnotationProcessorDiscovery(compiler, task);

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

  private void ensureClassOutputPathExists(FileManager fileManager) {
    // We have to manually create this one as javac will not attempt to access it lazily. Instead,
    // it will just abort if it is not present. This means we cannot take advantage of the
    // PathLocationRepository creating the roots as we try to access them for this specific case.
    if (!fileManager.hasLocation(StandardLocation.CLASS_OUTPUT)) {
      LOGGER.debug("No class output location was specified, so an in-memory path is being created");
      var classOutput = RamPath.createPath("classes-" + UUID.randomUUID(), true);
      fileManager.addPath(StandardLocation.CLASS_OUTPUT, classOutput);
    } else {
      LOGGER.trace("At least one output path is present, so no in-memory path will be created");
    }
  }

  private void registerClassPath(A compiler, FileManager fileManager) {
    // ECJ requires that we always create this, otherwise it refuses to run.
    if (!compiler.isInheritClassPath()) {
      return;
    }

    var currentClassPath = SpecialLocations.currentClassPathLocations();

    LOGGER.debug("Adding current classpath to compiler: {}", currentClassPath);
    for (var classPath : currentClassPath) {
      fileManager.addPath(StandardLocation.CLASS_PATH, new NioPath(classPath));
    }

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
    for (var modulePath : currentModulePath) {
      var modulePathLike = new NioPath(modulePath);
      fileManager.addPath(StandardLocation.CLASS_PATH, modulePathLike);
      fileManager.addPath(StandardLocation.MODULE_PATH, modulePathLike);
    }
  }

  private void registerPlatformClassPath(A compiler, FileManager fileManager) {
    if (!compiler.isInheritPlatformClassPath()) {
      return;
    }

    var currentPlatformClassPath = SpecialLocations.currentPlatformClassPathLocations();

    if (!currentPlatformClassPath.isEmpty()) {
      LOGGER.debug("Adding current platform classpath to compiler: {}", currentPlatformClassPath);

      for (var classPath : currentPlatformClassPath) {
        fileManager.addPath(StandardLocation.PLATFORM_CLASS_PATH, new NioPath(classPath));
      }
    }
  }

  private void registerSystemModulePath(A compiler, FileManager fileManager) {
    if (!compiler.isInheritSystemModulePath()) {
      return;
    }

    var jrtLocations = SpecialLocations.javaRuntimeLocations();
    LOGGER.trace("Adding JRT locations to compiler: {}", jrtLocations);

    for (var jrtLocation : jrtLocations) {
      fileManager.addPath(StandardLocation.SYSTEM_MODULES, new NioPath(jrtLocation));
    }
  }

  private void registerAnnotationProcessorPaths(A compiler, FileManager fileManager) {
    switch (compiler.getAnnotationProcessorDiscovery()) {
      case ENABLED:
        fileManager.ensureEmptyLocationExists(StandardLocation.ANNOTATION_PROCESSOR_PATH);
        break;

      case INCLUDE_DEPENDENCIES: {
        // https://stackoverflow.com/q/53084037
        // Seems that javac will always use the classpath to implement this behaviour, and never
        // the module path. Let's keep this simple and mimic this behaviour. If someone complains
        // about it being problematic in the future, then I am open to change how this works to
        // keep it sensible.
        fileManager.copyContainers(
            StandardLocation.CLASS_PATH,
            StandardLocation.ANNOTATION_PROCESSOR_PATH
        );

        break;
      }

      default:
        // There is nothing to do to the file manager to configure annotation processing at this
        // time.
        break;
    }
  }

  private void configureAnnotationProcessorDiscovery(A compiler, CompilationTask task) {
    if (compiler.getAnnotationProcessors().size() > 0) {
      LOGGER.debug("Annotation processor discovery is disabled (processors explicitly provided)");
      task.setProcessors(compiler.getAnnotationProcessors());
    } else if (compiler.getAnnotationProcessorDiscovery()
        == AnnotationProcessorDiscovery.DISABLED) {
      // Set the processor list explicitly to instruct the compiler to not perform discovery.
      task.setProcessors(List.of());
    }
  }
}
