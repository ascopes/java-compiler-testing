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

import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility for discovering modules in a given path.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public final class ModuleDiscoverer {

  private static final Logger log = LoggerFactory.getLogger(ModuleDiscoverer.class);

  private ModuleDiscoverer() {
    // Static-only class.
  }

  /**
   * Find all compiled modules that exist in a given path.
   *
   * <p>This will only discover modules that contain a {@code module-info.class}
   * or are an {@code Automatic-Module} in an accessible {@code MANIFEST.MF}.
   *
   * @param path the path to look within.
   * @return a set of candidate modules.
   */
  public static Set<ModuleCandidate> findModulesIn(Path path) {
    try {
      return ModuleFinder
          .of(path)
          .findAll()
          .stream()
          .map(module -> new ModuleCandidate(
              module.descriptor().name(),
              Path.of(module.location().orElseThrow()),
              module.descriptor()
          ))
          .collect(Collectors.toUnmodifiableSet());
    } catch (FindException ex) {
      log.debug("Failed to find modules in {}, will ignore this error", path, ex);
      return Set.of();
    }
  }

  /**
   * Representation of a candidate module that was discovered.
   *
   * @since 3.0.2
   */
  public static final class ModuleCandidate {

    private final String name;
    private final Path path;
    private final ModuleDescriptor descriptor;

    /**
     * Initialise this module.
     *
     * @param name       the module name.
     * @param path       the path to the module.
     * @param descriptor the descriptor of the module.
     */
    public ModuleCandidate(String name, Path path, ModuleDescriptor descriptor) {
      this.name = name;
      this.path = path;
      this.descriptor = descriptor;
    }

    /**
     * Get the module name.
     *
     * @return the module name.
     */
    public String getName() {
      return name;
    }

    /**
     * Get the module path.
     *
     * @return the module path.
     */
    public Path getPath() {
      return path;
    }

    /**
     * Construct a new {@link PathRoot} for this module.
     *
     * @return the path root.
     */
    public PathRoot createPathRoot() {
      return new WrappingDirectoryImpl(path);
    }

    /**
     * Get the module descriptor.
     *
     * @return the module descriptor.
     */
    public ModuleDescriptor getDescriptor() {
      return descriptor;
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (other instanceof ModuleCandidate that) {
        return name.equals(that.name) && path.equals(that.path);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, path);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .attribute("name", name)
          .attribute("path", path)
          .toString();
    }
  }
}
