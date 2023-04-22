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
package io.github.ascopes.jct.assertions;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.filemanagers.ModuleLocation;
import io.github.ascopes.jct.repr.LocationRepresentation;
import io.github.ascopes.jct.utils.StringUtils;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for module container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class ModuleContainerGroupAssert
    extends AbstractContainerGroupAssert<ModuleContainerGroupAssert, ModuleContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   */
  public ModuleContainerGroupAssert(@Nullable ModuleContainerGroup containerGroup) {
    super(containerGroup, ModuleContainerGroupAssert.class);
  }

  /**
   * Assert that the given module exists and then return assertions to perform on that module.
   *
   * @param module the module name.
   * @return the assertions to perform on the package container group.
   * @throws AssertionError       if the container group is null or if the module does not exist.
   * @throws NullPointerException if the module parameter is null.
   */
  public PackageContainerGroupAssert moduleExists(String module) {
    requireNonNull(module, "module must not be null");
    isNotNull();

    var moduleGroup = actual.getModule(module);

    if (moduleGroup != null) {
      return new PackageContainerGroupAssert(moduleGroup);
    }

    throw failure(StringUtils.resultNotFoundWithFuzzySuggestions(
        module,
        module,
        actual.getModules().keySet(),
        ModuleLocation::getModuleName,
        ModuleLocation::getModuleName,
        "module"
    ));
  }

  /**
   * Assert that the given module does not exist.
   *
   * @param module the module name.
   * @return this assertion object for further assertion calls.
   * @throws AssertionError       if the container group is null or if the module exists.
   * @throws NullPointerException if the module parameter is null.
   */
  public ModuleContainerGroupAssert moduleDoesNotExist(String module) {
    requireNonNull(module, "module must not be null");
    isNotNull();

    var moduleGroup = actual.getModule(module);

    if (moduleGroup == null) {
      return this;
    }

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    throw failure(
        "Expected module %s to not exist in %s but it did",
        module,
        locationName
    );
  }
}
