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

package io.github.ascopes.jct.paths.v2.classloaders;

import static java.util.Objects.requireNonNull;

import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Exception that is thrown if class loading returned no results because the class did not exist on
 * the given paths.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class ClassMissingException extends ClassNotFoundException {

  private final String binaryName;
  private final Location location;

  /**
   * Initialize the exception.
   *
   * @param binaryName the binary name of the class that was not found.
   * @param location   the location that was being searched for the class.
   */
  public ClassMissingException(String binaryName, Location location) {
    super(
        String.format(
            "Class '%s' was not found in location '%s'",
            requireNonNull(binaryName, "binaryName"),
            requireNonNull(location, "location").getName()
        )
    );

    this.binaryName = binaryName;
    this.location = location;
  }

  /**
   * Get the binary name of the class that failed to be loaded.
   *
   * @return the binary name.
   */
  public String getBinaryName() {
    return binaryName;
  }

  /**
   * Get the location that the class was being loaded from.
   *
   * @return the location that the class was being loaded from.
   */
  public Location getLocation() {
    return location;
  }
}
