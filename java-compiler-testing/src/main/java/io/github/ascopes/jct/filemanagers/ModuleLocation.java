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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.ex.JctIllegalInputException;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.tools.JavaFileManager.Location;
import org.jspecify.annotations.Nullable;

/**
 * Handle that represents the location of a module.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ModuleLocation implements Location {

  private final Location parent;
  private final String moduleName;
  private final String name;

  /**
   * Initialize the location.
   *
   * @param parent     the parent location.
   * @param moduleName the module name.
   * @throws JctIllegalInputException if the parent location is not an output location or a
   *                                  module-oriented location.
   */
  public ModuleLocation(Location parent, String moduleName) {
    Objects.requireNonNull(parent, "parent");
    Objects.requireNonNull(moduleName, "moduleName");

    if (!parent.isOutputLocation() && !parent.isModuleOrientedLocation()) {
      throw new JctIllegalInputException(
          "The parent of a module location must be either an output location "
              + "or be module-oriented, but got "
              + parent.getName()
      );
    }

    this.parent = parent;
    this.moduleName = moduleName;
    name = parent.getName() + "[" + moduleName + "]";
  }

  /**
   * Get the parent location.
   *
   * @return the parent location.
   */
  public Location getParent() {
    return parent;
  }

  /**
   * Get the module name.
   *
   * @return the module name.
   */
  public String getModuleName() {
    return moduleName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isOutputLocation() {
    return parent.isOutputLocation();
  }

  @Override
  public boolean isModuleOrientedLocation() {
    // Module locations cannot be module-oriented, but their parents can be.
    return false;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (!(other instanceof ModuleLocation)) {
      return false;
    }

    var that = (ModuleLocation) other;

    return parent.equals(that.parent)
        && moduleName.equals(that.moduleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parent, moduleName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("parent", parent)
        .attribute("moduleName", moduleName)
        .toString();
  }

  /**
   * Attempt to upcast a given location to a ModuleLocation if it is an instance of
   * ModuleLocation.
   *
   * @param location the location to attempt to upcast.
   * @return an optional containing the upcast location if it is a module location,
   *     or an empty optional if not.
   * @since 1.1.5
   */
  public static Optional<ModuleLocation> upcast(Location location) {
    return Optional.of(location)
        .filter(ModuleLocation.class::isInstance)
        .map(ModuleLocation.class::cast);
  }
}
