/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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

import static io.github.ascopes.jct.utils.IoExceptionUtils.uncheckedIo;

import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.JctFileManagerBuilder;
import io.github.ascopes.jct.filemanagers.LoggingFileManagerProxy;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
public final class JctCompilationFactory<A extends JctCompiler<A, JctCompilationImpl>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctCompilationFactory.class);

  /**
   * Initialise this factory.
   */
  public JctCompilationFactory() {
    // Nothing to do here.
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).toString();
  }

  /**
   * Run the compilation for the given compiler and return the compilation result.
   *
   * @param compiler       the compiler to run.
   * @param builder        the template to compile files within.
   * @param jsr199Compiler the underlying JSR-199 compiler to run via.
   * @param flagBuilder    the flag builder to use.
   * @return the compilation result.
   */
  public JctCompilationImpl compile(
      A compiler,
      JctFileManagerBuilder builder,
      JavaCompiler jsr199Compiler,
      JctFlagBuilder flagBuilder
  ) {
    try {
      var flags = buildFlags(compiler, flagBuilder);
      var diagnosticListener = buildDiagnosticListener(compiler);
      var writer = buildWriter(compiler);

      try (var fileManager = buildFileManager(compiler, builder)) {
        var previousCompilationUnits = new LinkedHashSet<JavaFileObject>();

        boolean result;

        loop:
        do {
          var nextResult = performCompilerPass(
              compiler,
              jsr199Compiler,
              writer,
              flags,
              fileManager,
              diagnosticListener,
              previousCompilationUnits
          );

          switch (nextResult) {
            case SUCCESS:
              result = true;
              break;

            case FAILURE:
              result = false;
              break;

            case SKIPPED:
            default:
              result = true;
              LOGGER.info("Nothing else to compile. Finishing up...");
              break loop;
          }
        } while (result);

        var outputLines = writer.toString().lines().collect(Collectors.toList());

        return JctCompilationImpl.builder()
            .failOnWarnings(compiler.isFailOnWarnings())
            .success(result)
            .outputLines(outputLines)
            .compilationUnits(Set.copyOf(previousCompilationUnits))
            .diagnostics(diagnosticListener.getDiagnostics())
            .fileManager(fileManager)
            .build();
      }
    } catch (IOException ex) {
      throw new JctCompilerException("Failed to compile due to an IOException: " + ex, ex);
    }
  }

  /**
   * Run compilation once, after discovering any new sources to compile.
   *
   * @param compiler                 the compiler object..
   * @param jsr199Compiler           the JSR-199 compiler.
   * @param writer                   the output log writer.
   * @param flags                    the flags to pass to the compiler.
   * @param fileManager              the file manager to use.
   * @param diagnosticListener       the diagnostic listener to use.
   * @param previousCompilationUnits any previous compilation units. This will be updated after each
   *                                 call.
   * @return the outcome of the compilation.
   * @throws IOException if an {@link IOException} occurs during processing.
   */
  private CompilationResult performCompilerPass(
      A compiler,
      JavaCompiler jsr199Compiler,
      TeeWriter writer,
      List<String> flags,
      JctFileManager fileManager,
      TracingDiagnosticListener<JavaFileObject> diagnosticListener,
      Set<JavaFileObject> previousCompilationUnits
  ) throws IOException {
    var compilationUnits = findCompilationUnits(fileManager, previousCompilationUnits);
    LOGGER.debug("Found {} compilation units {}", compilationUnits.size(), compilationUnits);

    if (compilationUnits.isEmpty()) {
      return CompilationResult.SKIPPED;
    }

    var task = buildCompilationTask(
        compiler,
        jsr199Compiler,
        writer,
        applyLoggingToFileManager(compiler, fileManager),
        diagnosticListener,
        flags,
        compilationUnits
    );

    return runCompilationTask(compiler, task)
        ? CompilationResult.SUCCESS
        : CompilationResult.FAILURE;
  }

  /**
   * Build the {@link TeeWriter} to dump compiler logs to.
   *
   * @param compiler the compiler to use.
   * @return the tee writer.
   */
  private TeeWriter buildWriter(A compiler) {
    return new TeeWriter(compiler.getLogCharset(), System.out);
  }

  /**
   * Build the flags to pass to the JSR-199 compiler.
   *
   * @param compiler    the compiler to use.
   * @param flagBuilder the flag builder to use.
   * @return the flags to use.
   */
  private List<String> buildFlags(A compiler, JctFlagBuilder flagBuilder) {
    return flagBuilder
        .annotationProcessorOptions(compiler.getAnnotationProcessorOptions())
        .showDeprecationWarnings(compiler.isShowDeprecationWarnings())
        .failOnWarnings(compiler.isFailOnWarnings())
        .compilerOptions(compiler.getCompilerOptions())
        .runtimeOptions(compiler.getRuntimeOptions())
        .previewFeatures(compiler.isPreviewFeatures())
        .release(compiler.getRelease())
        .source(compiler.getSource())
        .target(compiler.getTarget())
        .verbose(compiler.isVerbose())
        .showWarnings(compiler.isShowWarnings())
        .build();
  }

  /**
   * Build the {@link JavaFileManager} to use.
   *
   * <p>LoggingMode will be applied to this via
   * {@link #applyLoggingToFileManager(JctCompiler, JctFileManager)}, which will be handled by
   * {@link #compile(JctCompiler, JctFileManagerBuilder, JavaCompiler, JctFlagBuilder)}.
   *
   * @param compiler the compiler to use.
   * @param builder  the file manager builder to build from.
   * @return the file manager to use.
   */
  private JctFileManager buildFileManager(A compiler, JctFileManagerBuilder builder) {
    return uncheckedIo(() -> builder.createFileManager(determineRelease(compiler)));
  }

  /**
   * Discover all relevant compilation units for the file manager.
   *
   * <p>The default implementation will consider both {@link StandardLocation#SOURCE_PATH}
   * <em>and</em> {@link StandardLocation#MODULE_SOURCE_PATH} locations.
   *
   * @param fileManager              the file manager to get the compilation units for.
   * @param previousCompilationUnits the compilation units that were already compiled. This enables
   *                                 tracking which sources change in the file manager root during
   *                                 compilation to enable recompilation of generated sources
   *                                 automatically.
   * @return the list of compilation units.
   * @throws IOException if an IO error occurs discovering any compilation units.
   */
  private List<? extends JavaFileObject> findCompilationUnits(
      JavaFileManager fileManager,
      Set<JavaFileObject> previousCompilationUnits
  ) throws IOException {
    var locations = new LinkedHashSet<Location>();
    locations.add(StandardLocation.SOURCE_OUTPUT);
    locations.add(StandardLocation.SOURCE_PATH);

    fileManager
        .listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)
        .forEach(locations::addAll);

    var objects = new ArrayList<JavaFileObject>();

    for (var location : locations) {
      var items = fileManager.list(location, "", Set.of(Kind.SOURCE), true);
      for (var fileObject : items) {

        if (!previousCompilationUnits.contains(fileObject)) {
          objects.add(fileObject);
        }
      }
    }

    previousCompilationUnits.addAll(objects);
    return objects;
  }

  /**
   * Apply the logging level to the file manager provided by
   * {@link #buildFileManager(JctCompiler, JctFileManagerBuilder)}.
   *
   * <p>The default implementation will wrap the given {@link JavaFileManager} in a
   * {@link LoggingFileManagerProxy} if the {@link JctCompiler#getFileManagerLoggingMode()} field
   * is
   * <strong>not</strong> set to {@link LoggingMode#DISABLED}. In the latter scenario, the input
   * will be returned to the caller with no other modifications.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to apply to.
   * @return the file manager to use for future operations.
   */
  private JctFileManager applyLoggingToFileManager(
      A compiler,
      JctFileManager fileManager
  ) {
    switch (compiler.getFileManagerLoggingMode()) {
      case STACKTRACES:
        return LoggingFileManagerProxy.wrap(fileManager, true);
      case ENABLED:
        return LoggingFileManagerProxy.wrap(fileManager, false);
      case DISABLED:
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
  private TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener(A compiler) {
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
  private CompilationTask buildCompilationTask(
      A compiler,
      JavaCompiler jsr199Compiler,
      Writer writer,
      JctFileManager fileManager,
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

    LOGGER
        .atInfo()
        .addArgument(compilationUnits::size)
        .addArgument(() -> compilationUnits.size() == 1 ? "" : "s")
        .addArgument(name)
        .addArgument(() -> StringUtils.quotedIterable(flags))
        .log("Starting compilation of {} file{} with compiler {} using flags {}");

    return task;
  }

  /**
   * Run the compilation task.
   *
   * <p>Any exceptions that get thrown will be wrapped in {@link JctCompilerException} instances
   * before being rethrown.
   *
   * @param compiler the compiler to use.
   * @param task     the task to run.
   * @return {@code true} if the compilation succeeded, or {@code false} if compilation failed.
   * @throws JctCompilerException if compilation throws an unhandled exception.
   */
  private boolean runCompilationTask(A compiler, CompilationTask task) {
    var name = compiler.toString();

    try {
      var start = System.nanoTime();
      var result = task.call();
      var duration = System.nanoTime() - start;

      if (result == null) {
        throw new JctCompilerException("The compiler failed to produce a valid result");
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
      throw new JctCompilerException("The compiler threw an exception", ex);
    }
  }

  private void configureAnnotationProcessorDiscovery(A compiler, CompilationTask task) {
    if (compiler.getAnnotationProcessors().size() > 0) {
      LOGGER.debug("Annotation processor discovery is disabled (processors explicitly provided)");
      task.setProcessors(compiler.getAnnotationProcessors());

    } else if (compiler.getAnnotationProcessorDiscovery()
        == AnnotationProcessorDiscovery.DISABLED) {
      LOGGER.trace("Annotation processor discovery is disabled (explicitly disabled)");
      // Set the processor list explicitly to instruct the compiler to not perform discovery.
      task.setProcessors(List.of());

    } else {
      LOGGER.trace("Annotation processor discovery will be performed");
    }
  }

  private String determineRelease(JctCompiler<?, ?> compiler) {
    if (compiler.getRelease() != null) {
      LOGGER.trace("Using explicitly set release as the base release version internally");
      return compiler.getRelease();
    }

    if (compiler.getTarget() != null) {
      LOGGER.trace("Using explicitly set target as the base release version internally");
      return compiler.getTarget();
    }

    LOGGER.trace("Using compiler default release as the base release version internally");
    return compiler.getDefaultRelease();
  }

  /**
   * Outcome of a compilation pass.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  protected enum CompilationResult {
    /**
     * The compilation succeeded.
     */
    SUCCESS,

    /**
     * The compilation failed.
     */
    FAILURE,

    /**
     * There was nothing else to compile, so nothing was run.
     */
    SKIPPED,
  }
}
