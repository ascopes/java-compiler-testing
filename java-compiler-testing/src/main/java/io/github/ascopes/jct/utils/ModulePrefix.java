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

package io.github.ascopes.jct.utils;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Helper class that extracts a module name from a string as a prefix and holds the result.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class ModulePrefix {

  private final String original;
  private final String moduleName;
  private final String rest;

  private ModulePrefix(String original, String moduleName, String rest) {
    this.original = requireNonNull(original, "original");
    this.moduleName = requireNonNull(moduleName, "moduleName");
    this.rest = requireNonNull(rest, "rest");
  }

  /**
   * Get the original input string.
   *
   * @return the original input string.
   */
  public String getOriginal() {
    return original;
  }

  /**
   * Get the extracted module name.
   *
   * @return the module name.
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * Get the rest of the string minus the module name.
   *
   * @return the rest of the string.
   */
  public String getRest() {
    return rest;
  }

  /**
   * Try and extract a module name from the given string.
   *
   * @param original the string to operate on.
   * @return the extracted prefix, or an empty module if no module was found.
   * @throws IllegalArgumentException if the input string starts with a forward slash.
   */
  public static Optional<ModulePrefix> tryExtract(String original) {
    if (original.startsWith("/")) {
      throw new IllegalArgumentException(
          "Absolute paths are not supported (got '" + original + "')");
    }

    // If we have a valid module name at the start, we should check in that location first.
    var firstSlash = original.indexOf('/');

    if (firstSlash == -1) {
      return Optional.empty();
    }

    var moduleName = original.substring(0, firstSlash);
    var restOfPath = original.substring(firstSlash + 1);
    return Optional.of(new ModulePrefix(original, moduleName, restOfPath));
  }
}
