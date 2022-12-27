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
package io.github.ascopes.jct.compilers.impl;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctFlagBuilder;
import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.AnnotationProcessorDiscovery;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingFileManagerProxy;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.impl.JctFileManagerImpl;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.utils.UtilityClass;
import io.github.ascopes.jct.utils.VisibleForTestingOnly;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectory;
import java.io.IOException;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Helper for performing the actual compilation logic during a compilation run.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class JctJsr199Integration extends UtilityClass {

  // Locations that we have to ensure exist before the compiler is run.
  private static final Set<StandardLocation> REQUIRED_LOCATIONS = Set.of(
      // We have to manually create this one as javac will not attempt to access it lazily. Instead,
      // it will just abort if it is not present. This means we cannot take advantage of the
      // container group creating the roots as we try to access them for this specific case.
      StandardLocation.SOURCE_OUTPUT,
      // Annotation processors that create files will need this directory to exist if it is to
      // work properly.
      StandardLocation.CLASS_OUTPUT,
      // We need to provide a header output path in case header generation is enabled at any stage.
      // I might make this disabled by default in the future if there is too much overhead from
      // doing this by default.
      StandardLocation.NATIVE_HEADER_OUTPUT
  );

  // Locations to duplicate paths for when using annotation processor path discovery with
  // inheritance enabled.
  // Mapping of source location to target location.
  private static final Map<StandardLocation, StandardLocation> INHERITED_AP_PATHS = Map.of(
      // https://stackoverflow.com/q/53084037
      // Seems that javac will always use the classpath to implement this behaviour, and never
      // the module path. Let's keep this simple and mimic this behaviour. If someone complains
      // about it being problematic in the future, then I am open to change how this works to
      // keep it sensible.
      // StandardLocation.MODULE_PATH, StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
      StandardLocation.CLASS_PATH, StandardLocation.ANNOTATION_PROCESSOR_PATH
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(JctJsr199Integration.class);

  private JctJsr199Integration() {
    // Static-only class.
  }

  /**
   * Initialise a new instance of this compilation factory internally and run the compilation.
   *
   * @param workspace      the workspace to use.
   * @param compiler       the compiler to use.
   * @param jsr199Compiler the JSR-199 compiler to use.
   * @param flagBuilder    the flag builder to use.
   * @return the compilation factory.
   */
  public static JctCompilationImpl compile(
      Workspace workspace,
      JctCompiler<?, ?> compiler,
      JavaCompiler jsr199Compiler,
      JctFlagBuilder flagBuilder
  ) {
    try {
      var flags = buildFlags(compiler, flagBuilder);
      var diagnosticListener = buildDiagnosticListener(compiler);
      var writer = buildWriter(compiler);

      try (var fileManager = buildFileManager(compiler, workspace)) {
        var compilationUnits = findCompilationUnits(fileManager);

        var result = performCompilerPass(
            compiler,
            jsr199Compiler,
            writer,
            flags,
            fileManager,
            diagnosticListener,
            compilationUnits
        );

        var outputLines = writer.toString().lines().collect(Collectors.toList());

        return JctCompilationImpl.builder()
            .failOnWarnings(compiler.isFailOnWarnings())
            .success(result)
            .outputLines(outputLines)
            .compilationUnits(Set.copyOf(compilationUnits))
            .diagnostics(diagnosticListener.getDiagnostics())
            .fileManager(fileManager)
            .build();
      }
    } catch (IOException ex) {
      throw new JctCompilerException("Failed to compile due to an IOException: " + ex, ex);
    }
  }

  /**
   * Build a TeeWriter.
   *
   * @param compiler the compiler to use.
   * @return the tee writer.
   */
  @VisibleForTestingOnly
  public static TeeWriter buildWriter(JctCompiler<?, ?> compiler) {
    return new TeeWriter(compiler.getLogCharset(), System.out);
  }

  /**
   * Build the flags for the compiler.
   *
   * @param compiler    the compiler.
   * @param flagBuilder the flag builder to use.
   * @return the flags.
   */
  @VisibleForTestingOnly
  public static List<String> buildFlags(JctCompiler<?, ?> compiler, JctFlagBuilder flagBuilder) {
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
   * Build a file manager from the compiler and workspace.
   *
   * <p>This also applies any logging proxy that is required.
   *
   * @param compiler  the compiler to use.
   * @param workspace the workspace to use.
   * @return the file manager.
   */
  @VisibleForTestingOnly
  public static JctFileManager buildFileManager(
      JctCompiler<?, ?> compiler,
      Workspace workspace
  ) {
    var release = determineRelease(compiler);
    var fileManager = new JctFileManagerImpl(release);

    // Copy all other explicit locations across first to give them priority.
    workspace.getAllPaths().forEach(fileManager::addPaths);

    // Inherit known resources from the current JVM where appropriate.
    configureClassPath(compiler, fileManager);
    configureModulePath(compiler, fileManager);
    configurePlatformClassPath(compiler, fileManager);
    configureJvmSystemModules(compiler, fileManager);
    configureAnnotationProcessorPaths(compiler, fileManager);

    for (var requiredLocation : REQUIRED_LOCATIONS) {
      createLocationIfNotPresent(workspace, fileManager, requiredLocation);
    }

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
   * Find any compilation units to use in a compilation.
   *
   * @param fileManager the file manager to search.
   * @return the compilation units.
   * @throws IOException if an IO error occurs.
   */
  @VisibleForTestingOnly
  public static List<JavaFileObject> findCompilationUnits(
      JavaFileManager fileManager
  ) throws IOException {
    var locations = new LinkedHashSet<Location>();

    locations.add(StandardLocation.SOURCE_OUTPUT);
    locations.add(StandardLocation.SOURCE_PATH);

    for (var modules : fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH)) {
      locations.addAll(modules);
    }

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
   * Build a tracing diagnostic listener for the compiler.
   *
   * @param compiler the compiler.
   * @return the tracing diagnostic listener.
   */
  @VisibleForTestingOnly
  public static TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener(
      JctCompiler<?, ?> compiler
  ) {
    var logging = compiler.getDiagnosticLoggingMode();

    return new TracingDiagnosticListener<>(
        logging != LoggingMode.DISABLED,
        logging == LoggingMode.STACKTRACES
    );
  }

  /**
   * Perform an individual compilation pass.
   *
   * @param compiler           the compiler to use.
   * @param jsr199Compiler     the JSR-199 compiler to build a compilation task from.
   * @param writer             the tee writer to use.
   * @param flags              the compiler flags to pass.
   * @param fileManager        the file manager to use.
   * @param diagnosticListener the tracing diagnostic listener to write diagnostics to.
   * @param compilationUnits   the compilation units to compile.
   * @return {@code true} if compilation succeeded, or {@code false} if it failed.
   * @throws IOException          if an IO error occurs.
   * @throws JctCompilerException if the compilation task throws an unhandled exception during the
   *                              run.
   */
  @VisibleForTestingOnly
  public static boolean performCompilerPass(
      JctCompiler<?, ?> compiler,
      JavaCompiler jsr199Compiler,
      TeeWriter writer,
      List<String> flags,
      JctFileManager fileManager,
      TracingDiagnosticListener<JavaFileObject> diagnosticListener,
      List<JavaFileObject> compilationUnits
  ) throws IOException {
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

  /**
   * Configure annotation processor discovery on the given compilation task.
   *
   * @param compiler the compiler to use.
   * @param task     the compilation task to use.
   */
  @VisibleForTestingOnly
  public static void configureAnnotationProcessorDiscovery(
      JctCompiler<?, ?> compiler,
      CompilationTask task
  ) {
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

  /**
   * Determine the effective release to run the compiler under.
   *
   * @param compiler the compiler to determine the release from.
   * @return the release.
   */
  @VisibleForTestingOnly
  public static String determineRelease(JctCompiler<?, ?> compiler) {
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
   * Create a location in the workspace if it is not present in the file manager.
   *
   * @param workspace   the workspace.
   * @param fileManager the file manager to check.
   * @param location    the location to check for.
   */
  @VisibleForTestingOnly
  public static void createLocationIfNotPresent(
      Workspace workspace,
      JctFileManagerImpl fileManager,
      Location location
  ) {
    if (!fileManager.hasLocation(location)) {
      LOGGER.trace("Creating a new package workspace for {}", location);
      var dir = workspace.createPackage(location);
      fileManager.addPath(location, dir);
    }
  }

  /**
   * Configure the classpath for the compiler in the file manager.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to use.
   */
  @VisibleForTestingOnly
  public static void configureClassPath(
      JctCompiler<?, ?> compiler,
      JctFileManagerImpl fileManager
  ) {
    if (compiler.isInheritClassPath()) {
      for (var path : SpecialLocationUtils.currentClassPathLocations()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the class path", path);
        fileManager.addPath(StandardLocation.CLASS_PATH, wrapper);

        // IntelliJ appears to place modules on the classpath if we are not building the base
        // project with JPMS. This is a problem because it means we cannot compile a module
        // within a test pack not using JPMS, since the modules will be on the classpath rather
        // than the module path. Fix this by adding classpath components with modules inside into
        // the module path as well.
        if (compiler.isFixJvmModulePathMismatch() && containsModules(path)) {
          LOGGER.trace("Adding {} to the module path as well since it contains modules", path);
          fileManager.addPath(StandardLocation.MODULE_PATH, wrapper);
        }
      }
    }
  }

  /**
   * Configure the module path for the compiler in the file manager.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to use.
   */
  @VisibleForTestingOnly
  public static void configureModulePath(
      JctCompiler<?, ?> compiler,
      JctFileManagerImpl fileManager
  ) {
    if (compiler.isInheritModulePath()) {
      for (var path : SpecialLocationUtils.currentModulePathLocations()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the module path and class path", path);

        // Since we do not know if the code being compiled will use modules or not just yet,
        // make sure any modules are on the class path as well so that they remain accessible
        // in unnamed modules.
        fileManager.addPath(StandardLocation.CLASS_PATH, wrapper);
        fileManager.addPath(StandardLocation.MODULE_PATH, wrapper);
      }
    }
  }

  /**
   * Configure the platform classpath for the compiler in the file manager.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to use.
   */
  @VisibleForTestingOnly
  public static void configurePlatformClassPath(
      JctCompiler<?, ?> compiler,
      JctFileManagerImpl fileManager
  ) {
    if (compiler.isInheritPlatformClassPath()) {
      for (var path : SpecialLocationUtils.currentPlatformClassPathLocations()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the platform class path", path);
        fileManager.addPath(StandardLocation.PLATFORM_CLASS_PATH, wrapper);
      }
    }
  }

  /**
   * Configure the JVM system modules for the compiler in the file manager.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to use.
   */
  @VisibleForTestingOnly
  public static void configureJvmSystemModules(
      JctCompiler<?, ?> compiler,
      JctFileManagerImpl fileManager
  ) {
    if (compiler.isInheritSystemModulePath()) {
      for (var path : SpecialLocationUtils.javaRuntimeLocations()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the system module path", path);
        fileManager.addPath(StandardLocation.SYSTEM_MODULES, wrapper);
      }
    }
  }

  /**
   * Configure the annotation processor paths for the compiler in the file manager.
   *
   * @param compiler    the compiler to use.
   * @param fileManager the file manager to use.
   */
  @VisibleForTestingOnly
  public static void configureAnnotationProcessorPaths(
      JctCompiler<?, ?> compiler,
      JctFileManagerImpl fileManager
  ) {
    switch (compiler.getAnnotationProcessorDiscovery()) {
      case ENABLED:
        LOGGER.trace("Annotation processor discovery is enabled, ensuring empty location exists");
        fileManager.ensureEmptyLocationExists(StandardLocation.ANNOTATION_PROCESSOR_PATH);
        break;

      case INCLUDE_DEPENDENCIES: {
        LOGGER.trace("Copying classpath dependencies into the annotation processor path");
        INHERITED_AP_PATHS.forEach(fileManager::copyContainers);
        break;
      }

      case DISABLED:
      default:
        LOGGER.trace("Not configuring annotation processor discovery");
        // There is nothing to do to the file manager to configure annotation processing at this
        // time.
        break;
    }
  }

  /**
   * Determine if the given path root contains modules.
   *
   * @param path the path to check
   * @return {@code true} if modules are found, or {@code false} otherwise
   */
  @VisibleForTestingOnly
  public static boolean containsModules(Path path) {
    try {
      return !ModuleFinder.of(path).findAll().isEmpty();
    } catch (FindException ex) {
      // Ignore, this just means that an invalid file name was found.
      LOGGER.trace("Ignoring exception finding modules in {}", path, ex);
      return false;
    }
  }
}
