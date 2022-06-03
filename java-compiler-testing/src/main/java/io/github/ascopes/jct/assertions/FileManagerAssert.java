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

package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.jsr199.FileManager;
import io.github.ascopes.jct.jsr199.containers.ModuleContainerGroup;
import io.github.ascopes.jct.jsr199.containers.OutputContainerGroup;
import io.github.ascopes.jct.jsr199.containers.PackageContainerGroup;
import javax.tools.JavaFileManager.Location;
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
  public OptionalAssert<PackageContainerGroupAssert, PackageContainerGroup> packageGroup(
      Location location
  ) {
    return new OptionalAssert<>(
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
  public OptionalAssert<ModuleContainerGroupAssert, ModuleContainerGroup> moduleGroup(
      Location location
  ) {
    return new OptionalAssert<>(
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
  public OptionalAssert<OutputContainerGroupAssert, OutputContainerGroup> outputGroup(
      Location location
  ) {
    return new OptionalAssert<>(
        actual.getOutputContainerGroup(location).orElse(null),
        OutputContainerGroupAssert::new
    );
  }
}
