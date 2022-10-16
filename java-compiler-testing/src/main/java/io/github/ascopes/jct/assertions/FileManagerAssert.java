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
package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.jsr199.FileManager;
import io.github.ascopes.jct.jsr199.groups.ModuleContainerGroup;
import io.github.ascopes.jct.jsr199.groups.OutputContainerGroup;
import io.github.ascopes.jct.jsr199.groups.PackageContainerGroup;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for a file manager.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class FileManagerAssert extends AbstractAssert<FileManagerAssert, FileManager> {

  /**
   * Initialize this file manager assertion object.
   *
   * @param fileManager the file manager to perform assertions upon.
   */
  public FileManagerAssert(FileManager fileManager) {
    super(fileManager, FileManagerAssert.class);
  }

  /**
   * Perform assertions on the given package group, if it has been configured.
   *
   * <p>If not configured, this will return an empty optional.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public MaybeAssert<PackageContainerGroupAssert, PackageContainerGroup> packageGroup(
      Location location
  ) {
    return new MaybeAssert<>(
        actual.getPackageContainerGroup(location).orElse(null),
        PackageContainerGroupAssert::new
    );
  }

  /**
   * Perform assertions on the given module group, if it has been configured.
   *
   * <p>If not configured, this will return an empty optional.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public MaybeAssert<ModuleContainerGroupAssert, ModuleContainerGroup> moduleGroup(
      Location location
  ) {
    return new MaybeAssert<>(
        actual.getModuleContainerGroup(location).orElse(null),
        ModuleContainerGroupAssert::new
    );
  }

  /**
   * Perform assertions on the given output group, if it has been configured.
   *
   * <p>If not configured, this will return an empty optional.
   *
   * @param location the location to configure.
   * @return the assertions to perform.
   */
  public MaybeAssert<OutputContainerGroupAssert, OutputContainerGroup> outputGroup(
      Location location
  ) {
    return new MaybeAssert<>(
        actual.getOutputContainerGroup(location).orElse(null),
        OutputContainerGroupAssert::new
    );
  }

  /**
   * Get assertions on the path containing class outputs, if it exists.
   *
   * @return the assertions to perform on the class outputs, or an empty optional if no group exists
   *     for that location.
   */
  public MaybeAssert<OutputContainerGroupAssert, OutputContainerGroup> classOutput() {
    return outputGroup(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Get assertions on the path containing generated source outputs, if it exists.
   *
   * @return the assertions to perform on the source outputs, or an empty optional if no group
   *     exists for that location.
   */
  public MaybeAssert<OutputContainerGroupAssert, OutputContainerGroup> sourceOutput() {
    return outputGroup(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Get assertions on the path containing header outputs, if it exists.
   *
   * @return the assertions to perform on the header outputs, or an empty optional if no group
   *     exists for that location.
   */
  public MaybeAssert<OutputContainerGroupAssert, OutputContainerGroup> generatedHeaders() {
    return outputGroup(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Get assertions on the path containing the class path, if it exists.
   *
   * @return the assertions to perform on the class path, or an empty optional if no group exists
   *     for that location.
   */
  public MaybeAssert<PackageContainerGroupAssert, PackageContainerGroup> classPath() {
    return packageGroup(StandardLocation.CLASS_PATH);
  }

  /**
   * Get assertions on the path containing the source path, if it exists.
   *
   * @return the assertions to perform on the source path, or an empty optional if no group exists
   *     for that location.
   */
  public MaybeAssert<PackageContainerGroupAssert, PackageContainerGroup> sourcePath() {
    return packageGroup(StandardLocation.SOURCE_PATH);
  }

  /**
   * Get assertions on the path containing the module path, if it exists.
   *
   * @return the assertions to perform on the module path, or an empty optional if no group exists
   *     for that location.
   */
  public MaybeAssert<ModuleContainerGroupAssert, ModuleContainerGroup> modulePath() {
    return moduleGroup(StandardLocation.MODULE_PATH);
  }
}
