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
package io.github.ascopes.jct.repr;

import javax.tools.JavaFileManager.Location;
import org.assertj.core.presentation.Representation;
import org.jspecify.annotations.Nullable;

/**
 * Representation for a {@link Location location}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class LocationRepresentation implements Representation {

  private static final LocationRepresentation INSTANCE
      = new LocationRepresentation();

  /**
   * Get an instance of this location representation.
   *
   * @return the instance.
   */
  public static LocationRepresentation getInstance() {
    return INSTANCE;
  }

  private LocationRepresentation() {
    // Nothing to see here, move along now.
  }

  @Override
  public String toStringOf(@Nullable Object object) {
    return object == null ? "null" : ((Location) object).getName();
  }
}
