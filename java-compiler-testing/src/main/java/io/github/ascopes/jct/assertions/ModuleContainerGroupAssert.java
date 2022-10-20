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

import io.github.ascopes.jct.assertions.helpers.LocationRepresentation;
import io.github.ascopes.jct.compilers.ModuleLocation;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import java.util.stream.Collectors;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Assertions for module container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class ModuleContainerGroupAssert
    extends AbstractContainerGroupAssert<ModuleContainerGroupAssert, ModuleContainerGroup> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   */
  public ModuleContainerGroupAssert(ModuleContainerGroup containerGroup) {
    super(containerGroup, ModuleContainerGroupAssert.class);
  }

  /**
   * Assert that the given module does not exist.
   *
   * @param module the module name.
   * @return this assertion object for further assertion calls.
   * @throws AssertionError if the module exists.
   */
  public ModuleContainerGroupAssert withoutModule(String module) {
    var moduleGroup = actual.findModule(module);

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

  /**
   * Assert that the given module exists and then return assertions to perform on that module.
   *
   * @param module the module name.
   * @return the assertions to perform on the package container group.
   * @throws AssertionError if the module does not exist.
   */
  public PackageContainerGroupAssert withModule(String module) {
    var moduleGroup = actual.findModule(module);

    if (moduleGroup != null) {
      return new PackageContainerGroupAssert(moduleGroup);
    }

    var closestMatches = FuzzySearch
        .extractSorted(
            module,
            actual.getModules().keySet(),
            ModuleLocation::getModuleName,
            FUZZY_CUTOFF
        )
        .stream()
        .filter(it -> it.getScore() >= FUZZY_MIN_SCORE)
        .map(BoundExtractedResult::getReferent)
        .collect(Collectors.toList());

    var locationName = LocationRepresentation.getInstance().toStringOf(actual.getLocation());

    if (closestMatches.isEmpty()) {
      throw failure("No module in %s modules found named %s", locationName, module);
    } else {
      throw failure(
          "No module found named \"%s\" in %s. Did you mean...%s",
          locationName,
          module,
          closestMatches
              .stream()
              .map(ModuleLocation::getModuleName)
              .map("\n\t - "::concat)
              .collect(Collectors.joining())
      );
    }
  }
}
