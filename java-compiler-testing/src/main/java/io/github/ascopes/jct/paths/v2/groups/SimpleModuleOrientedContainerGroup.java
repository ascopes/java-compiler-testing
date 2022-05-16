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

package io.github.ascopes.jct.paths.v2.groups;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.paths.ModuleLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Simple implementation of a {@link ModuleOrientedContainerGroup}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimpleModuleOrientedContainerGroup implements ModuleOrientedContainerGroup {

  private final Location location;
  private final Map<ModuleLocation, PackageOrientedContainerGroup> modules;
  private final String release;

  /**
   * Initialize this container group.
   *
   * @param location the module-oriented location.
   * @param release  the release to use for Multi-Release JARs.
   * @throws UnsupportedOperationException if the {@code location} is not module-oriented, or is
   *                                       output-oriented.
   */
  public SimpleModuleOrientedContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");
    this.release = requireNonNull(release, "release");

    if (location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use output-oriented locations with this container"
      );
    }

    if (!location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use package-oriented locations with this container"
      );
    }

    modules = new HashMap<>();
  }

  @Override
  public void close() throws IOException {
    var exceptions = new ArrayList<IOException>();
    for (var group : modules.values()) {
      try {
        group.close();
      } catch (IOException ex) {
        exceptions.add(ex);
      }
    }

    if (!exceptions.isEmpty()) {
      var ex = new IOException("One or more module groups failed to close");
      exceptions.forEach(ex::addSuppressed);
      throw ex;
    }
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public PackageOrientedContainerGroup forModule(String moduleName) {
    return modules
        .computeIfAbsent(
            new ModuleLocation(location, moduleName),
            loc -> new SimplePackageOrientedContainerGroup(loc, release)
        );
  }
}
