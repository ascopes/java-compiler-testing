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

import io.github.ascopes.jct.pathwrappers.BasicPathWrapperImpl;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import io.github.ascopes.jct.pathwrappers.RamFileSystem;
import io.github.ascopes.jct.utils.AsyncResourceCloser;
import io.github.ascopes.jct.utils.GarbageDisposal;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.SpecialLocations;
import io.github.ascopes.jct.utils.StringUtils;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A template for creating a file manager later.
 *
 * <p>File manager creation is deferred until as late as possible as to enable the specification of
 * the version to use when opening JARs that may be multi-release compatible. We have to do this to
 * ensure the behaviour for opening JARs matches the release version the code is compiled against.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class FileManagerBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileManagerBuilder.class);

  private final Lazy<List<Path>> jvmClassPath;
  private final Lazy<List<Path>> jvmModulePath;
  private final Lazy<List<Path>> jvmPlatformPath;
  private final Lazy<List<Path>> jvmSystemModules;

  private final Map<Location, LinkedHashSet<PathWrapper>> locations;

  private boolean fixJvmModulePathMismatch;
  private boolean inheritClassPath;
  private boolean inheritModulePath;
  private boolean inheritPlatformClassPath;
  private boolean inheritSystemModulePath;
  private LoggingMode fileManagerLoggingMode;
  private AnnotationProcessorDiscovery annotationProcessorDiscovery;

  /**
   * Initialize this workspace.
   */
  public FileManagerBuilder() {
    // Init these references here so we access these as late as possible but then cache the
    // results.
    jvmClassPath = new Lazy<>(SpecialLocations::currentClassPathLocations);
    jvmModulePath = new Lazy<>(SpecialLocations::currentModulePathLocations);
    jvmPlatformPath = new Lazy<>(SpecialLocations::currentPlatformClassPathLocations);
    jvmSystemModules = new Lazy<>(SpecialLocations::javaRuntimeLocations);

    locations = new HashMap<>();

    fixJvmModulePathMismatch = Compilable.DEFAULT_FIX_JVM_MODULEPATH_MISMATCH;
    inheritClassPath = Compilable.DEFAULT_INHERIT_CLASS_PATH;
    inheritModulePath = Compilable.DEFAULT_INHERIT_MODULE_PATH;
    inheritPlatformClassPath = Compilable.DEFAULT_INHERIT_PLATFORM_CLASS_PATH;
    inheritSystemModulePath = Compilable.DEFAULT_INHERIT_SYSTEM_MODULE_PATH;
    fileManagerLoggingMode = Compilable.DEFAULT_FILE_MANAGER_LOGGING_MODE;
    annotationProcessorDiscovery = Compilable.DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("locations", locations)
        .toString();
  }

  /**
   * Add a path to a package.
   *
   * @param location the location the package resides within.
   * @param path     the path to associate with the location.
   * @throws IllegalArgumentException if the location is module-oriented or output oriented.
   */
  public void addPath(Location location, PathWrapper path) {
    if (location.isOutputLocation()) {
      throw new IllegalArgumentException("Can not add paths to an output oriented location.");
    }

    if (location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Can not add paths directly to a module oriented location. Consider using "
              + "#addPath(Location, String, PathLike) or #addPath(Location, String, Path) instead"
      );
    }

    locations.computeIfAbsent(location, ignored -> new LinkedHashSet<>()).add(path);
  }

  /**
   * Add a path to a module.
   *
   * @param location the location the module resides within.
   * @param module   the name of the module to add.
   * @param path     the path to associate with the module.
   * @throws IllegalArgumentException if the {@code location} parameter is a
   *                                  {@link ModuleLocation}.
   * @throws IllegalArgumentException if the {@code location} parameter is not
   *                                  {@link Location#isModuleOrientedLocation() module-oriented}.
   * @throws IllegalArgumentException if the {@code module} parameter is not a valid module name, as
   *                                  defined by the Java Language Specification for the current
   *                                  JVM.
   */
  public void addPath(Location location, String module, PathWrapper path) {
    if (location instanceof ModuleLocation) {
      throw new IllegalArgumentException(
          "Cannot use a " + ModuleLocation.class.getName() + " with a custom module name. "
              + "Use FileManagerBuilder#addPath(Location, PathLike) "
              + "or FileManagerBuilder#addPath(Location, Path) instead."
      );
    }

    if (!location.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "Location " + StringUtils.quoted(location.getName()) + " must be module-oriented "
              + "or an output location to be able to associate a module with it."
      );
    }

    if (!SourceVersion.isName(module)) {
      throw new IllegalArgumentException(
          "Module " + StringUtils.quoted(module) + " is not a valid module name"
      );
    }

    addPath(new ModuleLocation(location, module), path);
  }

  /**
   * Get whether we will attempt to fix modules appearing on the classpath, or non-modules appearing
   * on the module path.
   *
   * <p>This enables correct classpath and module path detection when the test pack is a module but
   * the code being compiled in the test is not, and vice versa. We need this because many build
   * systems decide whether to populate the {@code --module-path} flat or the {@code --classpath}
   * with JPMS-enabled dependencies based on whether the project under compilation is a JPMS module
   * itself.
   *
   * <p>This only applies if {@link #isInheritModulePath()} or {@link #isInheritClassPath()} is
   * enabled, and only applies to the current JVM classpath and module path.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_FIX_JVM_MODULEPATH_MISMATCH}.
   *
   * @return {@code true} if enabled, or {@code false} if disabled.
   */
  public boolean isFixJvmModulePathMismatch() {
    return fixJvmModulePathMismatch;
  }

  /**
   * Get whether we will attempt to fix modules appearing on the classpath, or non-modules appearing
   * on the module path.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_FIX_JVM_MODULEPATH_MISMATCH}.
   *
   * @param fixJvmModulePathMismatch whether to enable the mismatch fixing or not.
   */
  public void setFixJvmModulePathMismatch(boolean fixJvmModulePathMismatch) {
    this.fixJvmModulePathMismatch = fixJvmModulePathMismatch;
  }

  /**
   * Get whether the class path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @return whether the current class path is being inherited or not.
   */
  public boolean isInheritClassPath() {
    return inheritClassPath;
  }

  /**
   * Set whether the class path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_CLASS_PATH}.
   *
   * @param inheritClassPath {@code true} to include it, or {@code false} to exclude it.
   */
  public void inheritClassPath(boolean inheritClassPath) {
    this.inheritClassPath = inheritClassPath;
  }

  /**
   * Get whether the module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @return whether the module path is being inherited or not.
   */
  public boolean isInheritModulePath() {
    return inheritModulePath;
  }

  /**
   * Set whether the module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_MODULE_PATH}.
   *
   * @param inheritModulePath {@code true} to include it, or {@code false} to exclude it.
   */
  public void inheritModulePath(boolean inheritModulePath) {
    this.inheritModulePath = inheritModulePath;
  }

  /**
   * Get whether the current platform class path is being inherited from the caller JVM or not.
   *
   * <p>This may also be known as the "bootstrap class path".
   *
   * <p>Default environments probably will not provide this functionality, in which case it will be
   * ignored.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_PLATFORM_CLASS_PATH}.
   *
   * @return whether the platform class path is being inherited or not.
   */
  public boolean isInheritPlatformClassPath() {
    return inheritPlatformClassPath;
  }

  /**
   * Set whether the current platform class path is being inherited from the caller JVM or not.
   *
   * <p>This may also be known as the "bootstrap class path".
   *
   * <p>Default environments probably will not provide this functionality, in which case it will be
   * ignored.
   *
   * @param inheritPlatformClassPath {@code true} to include it, or {@code false} to exclude it.
   */
  public void inheritPlatformClassPath(boolean inheritPlatformClassPath) {
    this.inheritPlatformClassPath = inheritPlatformClassPath;
  }

  /**
   * Get whether the system module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @return whether the system module path is being inherited or not.
   */
  public boolean isInheritSystemModulePath() {
    return inheritSystemModulePath;
  }

  /**
   * Set whether the system module path is inherited from the caller JVM or not.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_INHERIT_SYSTEM_MODULE_PATH}.
   *
   * @param inheritSystemModulePath {@code true} to include it, or {@code false} to exclude it.
   */
  public void inheritSystemModulePath(boolean inheritSystemModulePath) {
    this.inheritSystemModulePath = inheritSystemModulePath;
  }

  /**
   * Get the current file manager logging mode.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @return the current file manager logging mode.
   */
  public LoggingMode getFileManagerLoggingMode() {
    return fileManagerLoggingMode;
  }

  /**
   * Set how to handle logging calls to underlying file managers.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_FILE_MANAGER_LOGGING_MODE}.
   *
   * @param fileManagerLoggingMode the mode to use for file manager logging.
   */
  public void fileManagerLoggingMode(LoggingMode fileManagerLoggingMode) {
    this.fileManagerLoggingMode = fileManagerLoggingMode;
  }

  /**
   * Get how to perform annotation processor discovery.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY}.
   *
   * @return the annotation processor discovery mode.
   */
  public AnnotationProcessorDiscovery getAnnotationProcessorDiscovery() {
    return annotationProcessorDiscovery;
  }

  /**
   * Set how to perform annotation processor discovery.
   *
   * <p>Unless otherwise changed or specified, implementations should default to
   * {@link Compilable#DEFAULT_ANNOTATION_PROCESSOR_DISCOVERY}.
   *
   * @param annotationProcessorDiscovery the processor discovery mode to use.
   */
  public void annotationProcessorDiscovery(
      AnnotationProcessorDiscovery annotationProcessorDiscovery) {
    this.annotationProcessorDiscovery = annotationProcessorDiscovery;
  }

  /**
   * Create a file manager for this workspace.
   *
   * @return the file manager.
   */
  public FileManager createFileManager(String effectiveRelease) throws IOException {
    var fileManager = new FileManagerImpl(effectiveRelease);

    // Inherit known resources from the current JVM where appropriate.
    configureClassPath(fileManager);
    configureModulePath(fileManager);
    configurePlatformClassPath(fileManager);
    configureJvmSystemModules(fileManager);
    configureAnnotationProcessorPaths(fileManager);

    // Continue preparing the file manager with additional defaults we need.
    var fallbackFs = newFallbackFs(fileManager);
    // We have to manually create this one as javac will not attempt to access it lazily. Instead,
    // it will just abort if it is not present. This means we cannot take advantage of the
    // PathLocationRepository creating the roots as we try to access them for this specific case.
    createLocationIfNotPresent(fileManager, fallbackFs, StandardLocation.CLASS_OUTPUT);
    // Annotation processors that create files will need this directory to exist if it is to
    // work properly.
    createLocationIfNotPresent(fileManager, fallbackFs, StandardLocation.SOURCE_OUTPUT);

    // Copy all other explicit locations across.
    locations.forEach((location, paths) ->
        paths.forEach(path -> fileManager.addPath(location, path)));

    return fileManagerLoggingMode == LoggingMode.DISABLED
        ? fileManager
        : LoggingFileManagerProxy.wrap(fileManager,
            fileManagerLoggingMode == LoggingMode.STACKTRACES);
  }

  private Lazy<RamFileSystem> newFallbackFs(FileManagerImpl fileManager) {
    return new Lazy<>(() -> {
      var tempFs = RamFileSystem.newRamFileSystem("temp", false);
      var fileManagerName = fileManager.toString();
      var closer = new AsyncResourceCloser("tempfs for " + fileManagerName, tempFs::close);
      GarbageDisposal.onPhantom(fileManager, closer);
      return tempFs;
    });
  }

  private void createLocationIfNotPresent(
      FileManagerImpl fileManager,
      Lazy<RamFileSystem> fallbackFs,
      Location location
  ) throws IOException {
    if (!fileManager.hasLocation(location)) {
      var dir = fallbackFs.access().getPath().resolve(location.getName());
      fileManager.addPath(location, wrap(Files.createDirectories(dir)));
    }
  }

  private void configureClassPath(FileManagerImpl fileManager) {
    if (inheritClassPath) {
      for (var path : jvmClassPath.access()) {
        var wrapper = wrap(path);
        fileManager.addPath(StandardLocation.CLASS_PATH, wrapper);

        // IntelliJ appears to place modules on the classpath if we are not building the base
        // project with JPMS. This is a problem because it means we cannot compile a module
        // within a test pack not using JPMS, since the modules will be on the classpath rather
        // than the module path. Fix this by adding classpath components with modules inside into
        // the module path as well.
        if (fixJvmModulePathMismatch && containsModules(path)) {
          fileManager.addPath(StandardLocation.MODULE_PATH, wrapper);
        }
      }
    }
  }

  private void configureModulePath(FileManagerImpl fileManager) {
    if (inheritModulePath) {
      for (var path : jvmModulePath.access()) {
        var wrapper = wrap(path);

        // Since we do not know if the code being compiled will use modules or not just yet,
        // make sure any modules are on the class path as well so that they remain accessible
        // in unnamed modules.
        fileManager.addPath(StandardLocation.CLASS_PATH, wrapper);
        fileManager.addPath(StandardLocation.MODULE_PATH, wrapper);
      }
    }
  }

  private void configurePlatformClassPath(FileManagerImpl fileManager) {
    if (inheritPlatformClassPath) {
      for (var path : jvmPlatformPath.access()) {
        fileManager.addPath(StandardLocation.PLATFORM_CLASS_PATH, wrap(path));
      }
    }
  }

  private void configureJvmSystemModules(FileManagerImpl fileManager) {
    if (inheritSystemModulePath) {
      for (var path : jvmSystemModules.access()) {
        fileManager.addPath(StandardLocation.SYSTEM_MODULES, wrap(path));
      }
    }
  }

  private void configureAnnotationProcessorPaths(FileManager fileManager) {
    switch (annotationProcessorDiscovery) {
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

      case DISABLED:
      default:
        // There is nothing to do to the file manager to configure annotation processing at this
        // time.
        break;
    }
  }

  private PathWrapper wrap(Path path) {
    return new BasicPathWrapperImpl(path);
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
}
