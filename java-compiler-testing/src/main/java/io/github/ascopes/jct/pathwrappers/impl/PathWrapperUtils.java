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
package io.github.ascopes.jct.pathwrappers.impl;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.utils.StringUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Common helper methods for dealing with path wrappers.
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class PathWrapperUtils {

  private PathWrapperUtils() {
    throw new UnsupportedOperationException("static-only class");
  }

  /**
   * Obtain a URL for the given path.
   *
   * @param path the path to obtain a URL for.
   * @return the URL.
   * @throws IllegalArgumentException if the path does not support being represented as a URL.
   */
  public static URL retrieveRequiredUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException ex) {
      // Many compiler tools make use of the URLs provided by the URL classloader, so not being
      // able to support this is somewhat problematic for us.
      // While we can implement a classloader that does not require URLs, this can lead to some
      // annotation processor libraries being unable to function correctly. To avoid the confusion,
      // we enforce that URLs must be able to be generated for the path. Users are far less likely
      // to want to use custom FileSystem objects that do not have URL protocol handlers than they
      // are to want to assume that our classloaders we expose are instances of URLClassLoader.
      throw new IllegalArgumentException(
          "Cannot obtain a URL for the given path "
              + StringUtils.quoted(path)
              + ", this is likely due to no URL protocol implementation for this file system. "
              + "Unfortunately, this feature is required for consistent classloading to be "
              + "available to the compiler.",
          ex
      );
    }
  }


  /**
   * Assert that the given name is a valid name for a directory, and that it does not contain
   * potentially dangerous characters such as double-dots or slashes that could be used to escape
   * the directory we are running from.
   *
   * @param name the directory name to check.
   * @throws IllegalArgumentException if the name is invalid.
   * @throws NullPointerException     if the name is {@code null}.
   */
  public static void assertValidRootName(@Nullable String name) {
    Objects.requireNonNull(name, "name");

    if (name.isBlank()) {
      throw new IllegalArgumentException("Directory name cannot be blank");
    }

    if (!name.equals(name.trim())) {
      throw new IllegalArgumentException("Directory name cannot begin or end in spaces");
    }

    if (name.contains("/") || name.contains("\\") || name.contains("..")) {
      throw new IllegalArgumentException("Invalid file name provided");
    }
  }
}
