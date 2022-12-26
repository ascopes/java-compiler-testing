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
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.SpecialLocationUtils;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.utils.VisibleForTestingOnly;
import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectory;
import java.io.IOException;
import java.io.Writer;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(JctCompilationFactory.class);

  private final Workspace workspace;
  private final A compiler;
  private final JavaCompiler jsr199Compiler;
  private final JctFlagBuilder flagBuilder;

  private final Lazy<List<Path>> jvmClassPath;
  private final Lazy<List<Path>> jvmModulePath;
  private final Lazy<List<Path>> jvmPlatformPath;
  private final Lazy<List<Path>> jvmSystemModules;

  /**
   * Initialise this compilation factory.
   *
   * <p>Consider using {@link #compile(Workspace, JctCompiler, JavaCompiler, JctFlagBuilder)}
   * instead of initialising this class directly, as this constructor is only visible for testing
   * purposes.
   *
   * @param workspace      the workspace.
   * @param compiler       the compiler.
   * @param jsr199Compiler the JSR-199 compiler.
   * @param flagBuilder    the flag builder.
   */
  @VisibleForTestingOnly
  public JctCompilationFactory(
      Workspace workspace,
      A compiler,
      JavaCompiler jsr199Compiler,
      JctFlagBuilder flagBuilder
  ) {
    this.workspace = workspace;
    this.compiler = compiler;
    this.jsr199Compiler = jsr199Compiler;
    this.flagBuilder = flagBuilder;
    jvmClassPath = new Lazy<>(SpecialLocationUtils::currentClassPathLocations);
    jvmModulePath = new Lazy<>(SpecialLocationUtils::currentModulePathLocations);
    jvmPlatformPath = new Lazy<>(SpecialLocationUtils::currentPlatformClassPathLocations);
    jvmSystemModules = new Lazy<>(SpecialLocationUtils::javaRuntimeLocations);
  }

  /**
   * Run the compilation for the given compiler and return the compilation result.
   *
   * @return the compilation result.
   */
  public JctCompilationImpl build() {
    try {
      var flags = buildFlags(compiler, flagBuilder);
      var diagnosticListener = buildDiagnosticListener(compiler);
      var writer = buildWriter(compiler);

      try (var fileManager = buildFileManager()) {
        var previousCompilationUnits = new LinkedHashSet<JavaFileObject>();

        var result = performCompilerPass(
            compiler,
            jsr199Compiler,
            writer,
            flags,
            fileManager,
            diagnosticListener,
            previousCompilationUnits
        );

        var outputLines = writer.toString().lines().collect(Collectors.toList());

        if (result == CompilationResult.SKIPPED) {
          LOGGER.warn("There was nothing to compile...");
        }

        return JctCompilationImpl.builder()
            .failOnWarnings(compiler.isFailOnWarnings())
            .success(result == CompilationResult.SUCCESS)
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

  private TeeWriter buildWriter(A compiler) {
    return new TeeWriter(compiler.getLogCharset(), System.out);
  }

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

  private JctFileManager buildFileManager() {
    var fileManager = new JctFileManagerImpl(determineRelease());

    // Copy all other explicit locations across first to give them priority.
    workspace.getAllPaths().forEach(fileManager::addPaths);

    // Inherit known resources from the current JVM where appropriate.
    configureClassPath(fileManager);
    configureModulePath(fileManager);
    configurePlatformClassPath(fileManager);
    configureJvmSystemModules(fileManager);
    configureAnnotationProcessorPaths(fileManager);

    for (var requiredLocation : REQUIRED_LOCATIONS) {
      createLocationIfNotPresent(fileManager, requiredLocation);
    }

    return fileManager;
  }

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

    //previousCompilationUnits.addAll(objects);
    return objects;
  }

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

  private TracingDiagnosticListener<JavaFileObject> buildDiagnosticListener(A compiler) {
    var logging = compiler.getDiagnosticLoggingMode();

    return new TracingDiagnosticListener<>(
        logging != LoggingMode.DISABLED,
        logging == LoggingMode.STACKTRACES
    );
  }

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

  private String determineRelease() {
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

  private void createLocationIfNotPresent(JctFileManagerImpl fileManager, Location location) {
    if (!fileManager.hasLocation(location)) {
      LOGGER.trace("Creating a new package workspace for {}", location);
      var dir = workspace.createPackage(location);
      fileManager.addPath(location, dir);
    }
  }

  private void configureClassPath(JctFileManagerImpl fileManager) {
    if (compiler.isInheritClassPath()) {
      for (var path : jvmClassPath.access()) {
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

  private void configureModulePath(JctFileManagerImpl fileManager) {
    if (compiler.isInheritModulePath()) {
      for (var path : jvmModulePath.access()) {
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

  private void configurePlatformClassPath(JctFileManagerImpl fileManager) {
    if (compiler.isInheritPlatformClassPath()) {
      for (var path : jvmPlatformPath.access()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the platform class path", path);
        fileManager.addPath(StandardLocation.PLATFORM_CLASS_PATH, wrapper);
      }
    }
  }

  private void configureJvmSystemModules(JctFileManagerImpl fileManager) {
    if (compiler.isInheritSystemModulePath()) {
      for (var path : jvmSystemModules.access()) {
        var wrapper = new WrappingDirectory(path);

        LOGGER.trace("Adding {} to the system module path", path);
        fileManager.addPath(StandardLocation.SYSTEM_MODULES, wrapper);
      }
    }
  }

  private void configureAnnotationProcessorPaths(JctFileManager fileManager) {
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

  private boolean containsModules(Path path) {
    try {
      return !ModuleFinder.of(path).findAll().isEmpty();
    } catch (FindException ex) {
      // Ignore, this just means that an invalid file name was found.
      LOGGER.trace("Ignoring exception finding modules in {}", path, ex);
      return false;
    }
  }

  /**
   * Initialise a new instance of this compilation factory internally and run the compilation.
   *
   * @param workspace      the workspace to use.
   * @param compiler       the compiler to use.
   * @param jsr199Compiler the JSR-199 compiler to use.
   * @param flagBuilder    the flag builder to use.
   * @param <A>            the compiler type.
   * @return the compilation factory.
   */
  public static <A extends JctCompiler<A, JctCompilationImpl>> JctCompilationImpl compile(
      Workspace workspace,
      A compiler,
      JavaCompiler jsr199Compiler,
      JctFlagBuilder flagBuilder
  ) {
    // This method is mostly pointless as we could call the constructor instead. However, Mockito
    // makes verifying the arguments passed to a constructor much more difficult than arguments
    // passed to a static method, so this acts as a nexus to make testing a bit easier for me.
    return new JctCompilationFactory<>(workspace, compiler, jsr199Compiler, flagBuilder).build();
  }


  /**
   * Outcome of a compilation pass.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  private enum CompilationResult {
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
