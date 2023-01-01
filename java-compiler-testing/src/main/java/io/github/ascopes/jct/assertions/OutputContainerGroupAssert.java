/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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

import io.github.ascopes.jct.containers.OutputContainerGroup;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Assertions for output container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class OutputContainerGroupAssert
    extends AbstractContainerGroupAssert<OutputContainerGroupAssert, OutputContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   */
  public OutputContainerGroupAssert(OutputContainerGroup containerGroup) {
    super(containerGroup, OutputContainerGroupAssert.class);
  }

  /**
   * Get assertions to perform on package-oriented paths within this location.
   *
   * @return the package-oriented assertions.
   */
  public PackageContainerGroupAssert packages() {
    return new PackageContainerGroupAssert(actual);
  }

  /**
   * Get assertions to perform on module-oriented paths within this location.
   *
   * @return the module-oriented assertions.
   */
  public ModuleContainerGroupAssert modules() {
    return new ModuleContainerGroupAssert(actual);
  }
}
