package com.github.ascopes.jct.compilers;

import com.github.ascopes.jct.compilations.StandardCompilation;
import com.github.ascopes.jct.diagnostics.LoggingTracingDiagnosticsListener;
import com.github.ascopes.jct.diagnostics.TeeWriter;
import com.github.ascopes.jct.diagnostics.TracingDiagnosticsListener;
import com.github.ascopes.jct.paths.InMemoryPath;
import com.github.ascopes.jct.paths.LoggingJavaFileManagerProxy;
import com.github.ascopes.jct.paths.PathJavaFileManager;
import com.github.ascopes.jct.paths.PathLocationRepository;
import com.github.ascopes.jct.paths.SpecialLocations;
import com.github.ascopes.jct.utils.StringUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a wrapper around a {@code javac}-like compiler.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
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
    diagnosticLoggingMode = LoggingMode.LOGGING;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompilation compile() throws IOException {

    var flags = buildFlags();
    var diagnosticListener = buildDiagnosticListener();

    try (var fileManager = applyLoggingToFileManager(buildJavaFileManager())) {
      var compilationUnits = discoverCompilationUnits(fileManager);
      if (compilationUnits.isEmpty()) {
        throw new IllegalStateException("No compilation units found");
      }

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
            "Starting compilation of {} file{} using flags {}",
            compilationUnits.size(),
            compilationUnits.size() == 1 ? "" : "s",
            StringUtils.quotedIterable(flags)
        );
      }

      Boolean result;

      try {
        result = task.call();
      } catch (Exception ex) {
        LOGGER.warn(
            "Compiler threw an exception: {}: {}",
            ex.getClass().getName(),
            ex.getMessage()
        );
        throw new CompilerException("The compiler threw an exception", ex);

      }

      if (result == null) {
        throw new CompilerException("The compiler failed to produce a valid result");
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

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler configure(CompilerConfigurer<StandardCompiler> configurer) {
    configurer.configure(this);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler verbose(boolean enabled) {
    verbose = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler previewFeatures(boolean enabled) {
    previewFeatures = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler warnings(boolean enabled) {
    warnings = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler deprecationWarnings(boolean enabled) {
    deprecationWarnings = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler failOnWarnings(boolean enabled) {
    failOnWarnings = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addAnnotationProcessorOptions(Iterable<String> options) {
    for (var option : Objects.requireNonNull(options)) {
      annotationProcessorOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addAnnotationProcessors(Iterable<? extends Processor> processors) {
    for (var processor : Objects.requireNonNull(processors)) {
      annotationProcessors.add(Objects.requireNonNull(processor));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addCompilerOptions(Iterable<String> options) {
    for (var option : Objects.requireNonNull(options)) {
      compilerOptions.add(Objects.requireNonNull(option));
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler releaseVersion(String version) {
    releaseVersion = version;
    sourceVersion = null;
    targetVersion = null;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler sourceVersion(String version) {
    releaseVersion = null;
    sourceVersion = version;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler targetVersion(String version) {
    releaseVersion = null;
    targetVersion = version;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler includeCurrentClassPath(boolean enabled) {
    includeCurrentClassPath = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler includeCurrentPlatformClassPath(boolean enabled) {
    includeCurrentPlatformClassPath = enabled;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addPath(Location location, Path path) {
    fileRepository.getOrCreateLocationManager(location).addPath(path);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addPath(Location location, InMemoryPath path) {
    fileRepository.getOrCreateLocationManager(location).addPath(path);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StandardCompiler addPaths(Location location, Collection<? extends Path> paths) {
    fileRepository.getOrCreateLocationManager(location).addPaths(paths);
    return this;
  }

  /**
   * Add options to pass to the Java runtime.
   *
   * @param options the options to pass to the runtime.
   * @return this compiler for further call chaining.
   */
  public StandardCompiler addRuntimeOptions(Iterable<String> options) {
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
    var fileManager = new PathJavaFileManager(fileRepository);

    // Ensure we have somewhere to dump our output.
    var classOutput = InMemoryPath.create("classes");

    fileRepository
        .getOrCreateLocationManager(StandardLocation.CLASS_OUTPUT)
        .addPath(classOutput);

    // ECJ requires that we always create this, otherwise it refuses to run.
    var classPath = fileRepository
        .getOrCreateLocationManager(StandardLocation.CLASS_PATH);

    if (includeCurrentClassPath) {
      var currentClassPath = SpecialLocations.currentClassPathLocations();
      LOGGER.info("Adding current classpath to compiler");
      LOGGER.debug("Current classpath is {}", currentClassPath);
      classPath.addPaths(currentClassPath);
    }

    if (includeCurrentPlatformClassPath) {
      var currentPlatformClassPath = SpecialLocations.currentPlatformClassPathLocations();
      LOGGER.info("Adding current platform classpath to compiler");
      LOGGER.debug("Current platform classpath is {}", currentPlatformClassPath);

      fileRepository
          .getOrCreateLocationManager(StandardLocation.PLATFORM_CLASS_PATH)
          .addPaths(currentPlatformClassPath);
    }

    fileRepository
        .getOrCreateLocationManager(StandardLocation.SYSTEM_MODULES)
        .addPaths(SpecialLocations.javaRuntimeLocations());

    return fileManager;
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
    if (fileManagerLoggingMode == LoggingMode.LOGGING_WITH_STACKTRACES) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, true);
    }

    if (fileManagerLoggingMode == LoggingMode.LOGGING) {
      return LoggingJavaFileManagerProxy.wrap(fileManager, false);
    }

    return fileManager;
  }

  /**
   * Build a diagnostic listener.
   *
   * @return the diagnostic listener to use.
   */
  private TracingDiagnosticsListener<JavaFileObject> buildDiagnosticListener() {
    var clock = Clock.systemDefaultZone();

    if (diagnosticLoggingMode == LoggingMode.LOGGING_WITH_STACKTRACES) {
      return new LoggingTracingDiagnosticsListener<>(clock, true);
    }

    if (diagnosticLoggingMode == LoggingMode.LOGGING) {
      return new LoggingTracingDiagnosticsListener<>(clock, false);
    }

    return new TracingDiagnosticsListener<>(clock);
  }

  /**
   * Options for how to handle logging on special internal components.
   */
  public enum LoggingMode {
    /**
     * Enable logging.
     */
    LOGGING,

    /**
     * Enable logging and include stacktraces in the logs for each entry.
     */
    LOGGING_WITH_STACKTRACES,

    /**
     * Do not log anything.
     */
    DISABLED,
  }

}
