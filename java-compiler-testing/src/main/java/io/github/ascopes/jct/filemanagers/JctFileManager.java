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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.Workspace;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Extension around a {@link JavaFileManager} that allows adding of {@link PathRoot} objects to the
 * manager.
 *
 * <p>This component is responsible for bridging the gap between a {@link Workspace} and
 * a {@link javax.tools.JavaCompiler} when performing a compilation, and thus includes a number of
 * required operations that the compiler will query the file system with. In addition, this
 * interface also defines a number of additional functionalities that are useful for querying and
 * verifying the outcome of compilations within tests.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface JctFileManager extends JavaFileManager {

  /**
   * Add a package-oriented path to a given location.
   *
   * <p>To add a module, first obtain the module location using
   * {@link #getLocationForModule(Location, String)}, and pass that result to this call.
   *
   * @param location the location to use.
   * @param path     the path to add.
   * @see #getLocationForModule(Location, String)
   */
  void addPath(Location location, PathRoot path);

  /**
   * Add a collection of package-oriented paths to a given location.
   *
   * <p>To add a module, first obtain the module location using
   * {@link #getLocationForModule(Location, String)}, and pass that result to this call.
   *
   * @param location the location to use.
   * @param paths    the paths to add.
   * @see #getLocationForModule(Location, String)
   */
  void addPaths(Location location, Collection<? extends PathRoot> paths);

  /**
   * Copy all containers from the first location to the second location.
   *
   * @param from the first location.
   * @param to   the second location.
   */
  void copyContainers(Location from, Location to);

  /**
   * Register an empty container for the given location to indicate to the compiler that the feature
   * exists, but has no configured paths.
   *
   * <p>This is needed to coerce the behaviour for annotation processing in some cases.
   *
   * <p>If the location already exists, then do not do anything.
   *
   * <p>If the location is an output location, then this operation does not make any sense, since
   * an empty location cannot have files output to it. In this case, you will likely get an
   * exception.
   *
   * <p>Likewise, this operation does not make sense for module locations within a module-oriented
   * location group, so this operation will fail with an error for those inputs as well.
   *
   * @param location the location to apply an empty container for.
   * @throws IllegalArgumentException if the location is an output location or a module location.
   */
  void createEmptyLocation(Location location);

  /**
   * Get the associated effective release.
   *
   * @return the effective release.
   * @since 0.0.1
   */
  String getEffectiveRelease();

  /**
   * Get the container group for the given package-oriented location.
   *
   * @param location the package oriented location.
   * @return the container group, or null if one does not exist.
   */
  PackageContainerGroup getPackageContainerGroup(Location location);

  /**
   * Get a collection of all package container impl in this file manager.
   *
   * @return the package container impl.
   */
  Collection<PackageContainerGroup> getPackageContainerGroups();

  /**
   * Get the container group for the given module-oriented location.
   *
   * @param location the module oriented location.
   * @return the container group, or null if one does not exist.
   */
  ModuleContainerGroup getModuleContainerGroup(Location location);

  /**
   * Get a collection of all module container impl in this file manager.
   *
   * @return the module container impl.
   */
  Collection<ModuleContainerGroup> getModuleContainerGroups();

  /**
   * Get the container group for the given output-oriented location.
   *
   * @param location the output oriented location.
   * @return the container group, or null if one does not exist.
   */
  OutputContainerGroup getOutputContainerGroup(Location location);

  /**
   * Get a collection of all output container impl in this file manager.
   *
   * @return the output container impl.
   */
  Collection<OutputContainerGroup> getOutputContainerGroups();

  /**
   * List all file objects in the given location matching the given criteria.
   *
   * @param location    the location to search in.
   * @param packageName the package name to search in, or {@code ""} to search in the root
   *                    location.
   * @param kinds       the kinds of file to return, or {@link Set#of} {@code (} {@link Kind#OTHER}
   *                    {@code )} to find all files.
   * @param recurse     {@code true} to recurse into subpackages, {@code false} to only check the
   *                    current package.
   * @return a collection of unique file objects that were found.
   * @throws IOException if an IO error occurs reading the file system.
   */
  @Override
  Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException;

  ///
  /// Default helper overrides
  ///

  /**
   * Get the location holding the {@link StandardLocation#CLASS_OUTPUT class outputs}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default OutputContainerGroup getClassOutputGroup() {
    return getOutputContainerGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get the location holding the {@link StandardLocation#SOURCE_OUTPUT source outputs}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default OutputContainerGroup getSourceOutputGroup() {
    return getOutputContainerGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get the location holding the {@link StandardLocation#CLASS_PATH class path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getClassPathGroup() {
    return getPackageContainerGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#SOURCE_PATH source path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getSourcePathGroup() {
    return getPackageContainerGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get the location holding the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_PATH annotation processor path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default PackageContainerGroup getAnnotationProcessorPathGroup() {
    return getPackageContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_PATH);
  }

  /**
   * Get the location holding the
   * {@link StandardLocation#ANNOTATION_PROCESSOR_MODULE_PATH annotation processor module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getAnnotationProcessorModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
  }

  /**
   * Get the location holding the
   * {@link StandardLocation#NATIVE_HEADER_OUTPUT native header outputs}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   * @deprecated This is a specialist method that is usually not used. To keep the API simple,
   * this is being removed. You should instead call {@link #getOutputContainerGroup(Location)},
   * passing {@link StandardLocation#NATIVE_HEADER_OUTPUT} as the first parameter.
   */
  @Deprecated(since = "0.6.0", forRemoval = true)
  @Nullable
  @SuppressWarnings("DeprecatedIsStillUsed")
  default OutputContainerGroup getNativeHeaderOutputGroup() {
    return getOutputContainerGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Get the location holding the {@link StandardLocation#MODULE_SOURCE_PATH module source path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getModuleSourcePathGroup() {
    return getModuleContainerGroup(StandardLocation.MODULE_SOURCE_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#UPGRADE_MODULE_PATH upgrade module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   * @deprecated This is a specialist method that is usually not used. To keep the API simple,
   * this is being removed. You should instead call {@link #getModuleContainerGroup(Location)},
   * passing {@link StandardLocation#SYSTEM_MODULES} as the first parameter.
   */
  @Deprecated(since = "0.6.0", forRemoval = true)
  @Nullable
  @SuppressWarnings("DeprecatedIsStillUsed")
  default ModuleContainerGroup getUpgradeModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.UPGRADE_MODULE_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#SYSTEM_MODULES system modules}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   * @deprecated This is a specialist method that is usually not used. To keep the API simple,
   * this is being removed. You should instead call {@link #getModuleContainerGroup(Location)},
   * passing {@link StandardLocation#SYSTEM_MODULES} as the first parameter.
   */
  @Deprecated(since = "0.6.0", forRemoval = true)
  @Nullable
  @SuppressWarnings("DeprecatedIsStillUsed")
  default ModuleContainerGroup getSystemModulesGroup() {
    return getModuleContainerGroup(StandardLocation.SYSTEM_MODULES);
  }

  /**
   * Get the location holding the {@link StandardLocation#MODULE_PATH module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   */
  @Nullable
  default ModuleContainerGroup getModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.MODULE_PATH);
  }

  /**
   * Get the location holding the {@link StandardLocation#PATCH_MODULE_PATH patch module path}.
   *
   * @return the location, or {@code null} if the location is not present in the file manager.
   * @since 0.1.0
   * @deprecated This is a specialist method that is usually not used. To keep the API simple,
   * this is being removed. You should instead call {@link #getModuleContainerGroup(Location)},
   * passing {@link StandardLocation#PATCH_MODULE_PATH} as the first parameter.
   */
  @Deprecated(since = "0.6.0", forRemoval = true)
  @Nullable
  @SuppressWarnings("DeprecatedIsStillUsed")
  default ModuleContainerGroup getPatchModulePathGroup() {
    return getModuleContainerGroup(StandardLocation.PATCH_MODULE_PATH);
  }
}
