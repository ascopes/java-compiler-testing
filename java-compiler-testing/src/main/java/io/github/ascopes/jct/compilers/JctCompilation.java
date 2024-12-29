/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.jspecify.annotations.Nullable;

/**
 * The result of a compilation.
 *
 * <p>This provides access to a number of useful pieces of information
 * including the file manager used for the compilation, compiler logs,
 * and diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface JctCompilation {

  /**
   * Get the command line arguments that were passed to the compiler.
   *
   * @return the command line arguments.
   * @since 0.5.0
   */
  List<String> getArguments();

  /**
   * Determine if warnings were treated as errors.
   *
   * @return {@code true} if warnings were treated as errors, or {@code false} otherwise.
   */
  boolean isFailOnWarnings();

  /**
   * Determine if the compilation was successful or not.
   *
   * @return {@code true} if successful, or {@code false} if not successful.
   */
  boolean isSuccessful();

  /**
   * Determine if the compilation was a failure or not.
   *
   * @return {@code true} if not successful, or {@code false} if successful.
   */
  default boolean isFailure() {
    return !isSuccessful();
  }

  /**
   * Get the lines of output produced by the compiler, if any were captured.
   *
   * <p>This is separate to diagnostics.
   *
   * @return the lines of output.
   */
  List<String> getOutputLines();

  /**
   * Get the compilation units used in the compilation.
   *
   * @return the compilation units.
   */
  Set<JavaFileObject> getCompilationUnits();

  /**
   * Get the diagnostics that were reported by the compilation, in the order that they
   * were reported.
   *
   * @return the diagnostics
   */
  List<TraceDiagnostic<JavaFileObject>> getDiagnostics();

  /**
   * Get the file manager that was used to store and manage files.
   *
   * <p>This can be used to obtain a classloader for any compiled sources,
   * which can then be used to reflectively test what the compiler produced.
   *
   * @return the file manager.
   */
  JctFileManager getFileManager();

  /**
   * Get the output container group for class outputs.
   *
   * <p>This usually consists of any {@code *.class} files produced by the compiler,
   * and is equivalent to {@code target/classes} in a Maven project.
   *
   * @return the output container group, or {@code null} if it does not exist.
   */
  @Nullable
  default OutputContainerGroup getClassOutputs() {
    return getFileManager().getOutputContainerGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get the output container group for source outputs.
   *
   * <p>This consists of any generated source code created by annotation processors,
   * and is equivalent to {@code target/generated-sources} in a Maven project.
   *
   * @return the output container group, or {@code null} if it does not exist.
   */
  @Nullable
  default OutputContainerGroup getSourceOutputs() {
    return getFileManager().getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the package container group for the class path.
   *
   * <p>This represents the class path used for compilation.
   *
   * @return the package container group, or {@code null} if it does not exist.
   */
  @Nullable
  default PackageContainerGroup getClassPath() {
    return getFileManager().getPackageContainerGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get the package container group for the source path.
   *
   * <p>This is equivalent to {@code src/main/java} and {@code src/main/resources}
   * in a Maven project.
   *
   * @return the package container group, or {@code null} if it does not exist.
   */
  @Nullable
  default PackageContainerGroup getSourcePath() {
    return getFileManager().getPackageContainerGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get the package container group for the annotation processor path.
   *
   * @return the package container group, or {@code null} if it does not exist.
   */
  @Nullable
  default PackageContainerGroup getAnnotationProcessorPath() {
    return getFileManager().getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Get the module container group for the annotation processor module path.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getAnnotationProcessorModulePath() {
    return getFileManager()
        .getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
  }

  /**
   * Get the package container group for the platform class path (a.k.a. the bootstrap class path).
   *
   * <p>You generally do not need to use this. The platform class path mechanism has been mostly
   * replaced by the use of the system modules path as of Java 11. It is simply provided for
   * backwards compatibility.
   *
   * @return the package container group, or {@code null} if it does not exist.
   */
  @Nullable
  default PackageContainerGroup getPlatformClassPath() {
    return getFileManager().getPackageContainerGroup(StandardLocation.PLATFORM_CLASS_PATH);
  }

  /**
   * Get the output container group for the native header file outputs.
   *
   * <p>If you invoke {@code javac} with the {@code -H} flag, then this represents the
   * directory that C/C++ header file stubs for JNI are written to.
   *
   * @return the output container group, or {@code null} if it does not exist.
   */
  @Nullable
  default OutputContainerGroup getNativeHeaderOutputs() {
    return getFileManager().getOutputContainerGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Get the module container group for the module source path.
   *
   * <p>Many build tools do not provide a direct equivalent of this mechanism as of now, but
   * this is a source path introduced in Java 9 that allows specifying multiple named JPMS
   * modules to compile under a single compilation invocation.
   *
   * <p>For example, you may use this in a project to compile an API module, a default
   * implementation module, and a module containing unit tests all together.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getModuleSourcePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get the module container group for the upgrade module path.
   *
   * <p>You generally will not need to use this, as this is a mechanism used to upgrade
   * modules in-place incrementally with fixes without redistributing the entire application.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getUpgradeModulePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.UPGRADE_MODULE_PATH);
  }

  /**
   * Get the module container group for all system modules that are part of the JDK distribution.
   *
   * <p>This will usually just point to the Java standard library.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getSystemModules() {
    return getFileManager().getModuleContainerGroup(StandardLocation.SYSTEM_MODULES);
  }

  /**
   * Get the module container group for the module path.
   *
   * <p>This is equivalent to the class path, but holds any JPMS modules.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getModulePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.MODULE_PATH);
  }

  /**
   * Get the module container group for the patch module path.
   *
   * <p>You generally will not need to use this. It consists of patchable module sources that
   * can be used to inject additional classes into a module. This can be used for cases like
   * unit tests where you wish to embed the unit test classes into the existing application
   * module to exploit features such as package private access.
   *
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getPatchModulePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH);
  }
}
