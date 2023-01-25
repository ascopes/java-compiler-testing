/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Interface representing the result of a compilation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@ThreadSafe
public interface JctCompilation {

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
   * Get the diagnostics that were reported by the compilation.
   *
   * @return the diagnostics
   */
  List<TraceDiagnostic<JavaFileObject>> getDiagnostics();

  /**
   * Get the file manager that was used to store and manage files.
   *
   * @return the file manager.
   */
  JctFileManager getFileManager();

  /**
   * Get the output container group for class outputs.
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
   * @return the output container group, or {@code null} if it does not exist.
   */
  @Nullable
  default OutputContainerGroup getSourceOutputs() {
    return getFileManager().getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the package container group for the class path.
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
   * @return the package container group, or {@code null} if it does not exist.
   */
  @Nullable
  default PackageContainerGroup getPlatformClassPath() {
    return getFileManager().getPackageContainerGroup(StandardLocation.PLATFORM_CLASS_PATH);
  }

  /**
   * Get the output container group for the native header file outputs.
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
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getModuleSourcePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get the module container group for the upgrade module path.
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
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getSystemModules() {
    return getFileManager().getModuleContainerGroup(StandardLocation.SYSTEM_MODULES);
  }

  /**
   * Get the module container group for the module path.
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
   * @return the module container group, or {@code null} if it does not exist.
   */
  @Nullable
  default ModuleContainerGroup getPatchModulePath() {
    return getFileManager().getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH);
  }
}
