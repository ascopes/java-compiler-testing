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
package io.github.ascopes.jct.compilers;

import io.github.ascopes.jct.utils.ToStringBuilder;
import java.util.Objects;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;


/**
 * Handle that represents the location of a module.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class ModuleLocation implements Location {

  private final Location parent;
  private final String moduleName;
  private final String name;

  /**
   * Initialize the location.
   *
   * @param parent     the parent location.
   * @param moduleName the module name.
   * @throws IllegalArgumentException if the parent location is not an output location or a
   *                                  module-oriented location.
   */
  public ModuleLocation(Location parent, String moduleName) {
    Objects.requireNonNull(parent);
    Objects.requireNonNull(moduleName);

    if (!parent.isOutputLocation() && !parent.isModuleOrientedLocation()) {
      throw new IllegalArgumentException(
          "The parent of a module location must be either an output location or be module-oriented:"
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
  public boolean equals(Object other) {
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
}
