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

import io.github.ascopes.jct.paths.RamPath;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An extension of the definition of a {@link DirectoryContainer} that is designed to hold a
 * {@link RamPath}.
 *
 * <p>This enables keeping a hard reference to the {@link RamPath} itself to prevent the
 * reference-counted in-memory file system from being garbage collected too early.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class RamPathContainer extends DirectoryContainer {
  // It is important to keep this reference alive, otherwise the RamPath may decide to close itself
  // before we use it if it gets garbage collected.
  private final RamPath ramPath;

  /**
   * Initialize this container.
   *
   * @param location the location to use.
   * @param ramPath the RAM path to initialize with.
   */
  public RamPathContainer(Location location, RamPath ramPath) {
    super(location, requireNonNull(ramPath, "ramPath").getPath());
    this.ramPath = ramPath;
  }

  /**
   * Get the RAM path that is being held.
   *
   * @return the RAM path.
   */
  @SuppressWarnings("unused")
  public RamPath getRamPath() {
    return ramPath;
  }
}
