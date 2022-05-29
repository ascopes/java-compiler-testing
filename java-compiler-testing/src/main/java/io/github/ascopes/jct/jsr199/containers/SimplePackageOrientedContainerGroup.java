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

package io.github.ascopes.jct.jsr199.containers;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.utils.StringUtils;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A group of containers that relate to a specific location.
 *
 * <p>This mechanism enables the ability to have locations with more than one path in them,
 * which is needed to facilitate the Java compiler's distributed class path, module handling, and
 * other important features.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimplePackageOrientedContainerGroup extends AbstractPackageOrientedContainerGroup {

  private final Location location;

  /**
   * Initialize this group.
   *
   * @param location the location of the group.
   * @param release the release version to use for handling {@code Multi-Release} JARs in this
   *                location.
   */
  public SimplePackageOrientedContainerGroup(Location location, String release) {
    super(release);

    this.location = requireNonNull(location, "location");

    if (location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use output locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }

    if (location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use module-oriented locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }
  }

  @Override
  public Location getLocation() {
    return location;
  }
}
