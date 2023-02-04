/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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

import static java.util.stream.Collectors.toMap;

import java.lang.module.FindException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility for discovering modules in a given path.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class ModuleDiscoverer extends UtilityClass {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleDiscoverer.class);

  private ModuleDiscoverer() {
    // Static-only class.
  }

  /**
   * Find all modules that exist in a given path.
   *
   * <p>This will only discover modules that contain a {@code module-info.class}
   * or are an {@code Automatic-Module} in an accessible {@code MANIFEST.MF}.
   *
   * @param path the path to look within.
   * @return a map of module names to the path of the module's package root.
   */
  public static Map<String, Path> findModulesIn(Path path) {
    try {
      // TODO(ascopes): should I deal with sources here too? How should I do that?
      return ModuleFinder
          .of(path)
          .findAll()
          .stream()
          .collect(toMap(nameExtractor(), pathExtractor()));
    } catch (FindException ex) {
      LOGGER.debug("Failed to find modules in {}, will ignore this error", path, ex);
      return Map.of();
    }
  }

  private static Function<ModuleReference, String> nameExtractor() {
    return ref -> ref.descriptor().name();
  }

  private static Function<ModuleReference, Path> pathExtractor() {
    return ref -> Path.of(ref.location().orElseThrow());
  }
}
