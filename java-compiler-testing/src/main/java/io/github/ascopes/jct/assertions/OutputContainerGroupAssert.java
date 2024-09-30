/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
import org.jspecify.annotations.Nullable;

/**
 * Assertions for output container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class OutputContainerGroupAssert
    extends AbstractContainerGroupAssert<OutputContainerGroupAssert, OutputContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   * @throws AssertionError if the container group is null.
   */
  public OutputContainerGroupAssert(@Nullable OutputContainerGroup containerGroup) {
    super(containerGroup, OutputContainerGroupAssert.class);
  }

  /**
   * Get assertions to perform on package-oriented paths within this location.
   *
   * @return the package-oriented assertions.
   * @throws AssertionError if the container group is null.
   */
  public PackageContainerGroupAssert packages() {
    isNotNull();

    return new PackageContainerGroupAssert(actual);
  }

  /**
   * Get assertions to perform on module-oriented paths within this location.
   *
   * @return the module-oriented assertions.
   * @throws AssertionError if the container group is null.
   */
  public ModuleContainerGroupAssert modules() {
    isNotNull();

    return new ModuleContainerGroupAssert(actual);
  }
}
