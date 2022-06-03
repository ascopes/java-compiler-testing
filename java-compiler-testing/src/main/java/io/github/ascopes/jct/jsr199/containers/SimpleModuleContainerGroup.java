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

import io.github.ascopes.jct.jsr199.ModuleLocation;
import io.github.ascopes.jct.jsr199.PathFileObject;
import io.github.ascopes.jct.paths.PathLike;
import io.github.ascopes.jct.utils.Lazy;
import io.github.ascopes.jct.utils.StringUtils;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Simple implementation of a {@link ModuleContainerGroup}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class SimpleModuleContainerGroup implements ModuleContainerGroup {

  private final Location location;
  private final Map<ModuleLocation, SimpleModuleModuleContainerGroup> modules;
  private final String release;
  private final Lazy<ContainerClassLoader> classLoaderLazy;

  /**
   * Initialize this container group.
   *
   * @param location the module-oriented location.
   * @param release  the release to use for Multi-Release JARs.
   * @throws UnsupportedOperationException if the {@code location} is not module-oriented, or is
   *                                       output-oriented.
   */
  public SimpleModuleContainerGroup(Location location, String release) {
    this.location = requireNonNull(location, "location");
    this.release = requireNonNull(release, "release");

    if (location.isOutputLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use output-oriented locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }

    if (!location.isModuleOrientedLocation()) {
      throw new UnsupportedOperationException(
          "Cannot use package-oriented locations such as "
              + StringUtils.quoted(location.getName())
              + " with this container"
      );
    }

    modules = new HashMap<>();
    classLoaderLazy = new Lazy<>(this::createClassLoader);
  }

  @Override
  @SuppressWarnings("resource")
  public void addModule(String module, Container container) {
    getOrCreateModule(module).addPackage(container);
  }

  @Override
  @SuppressWarnings("resource")
  public void addModule(String module, PathLike path) {
    getOrCreateModule(module).addPackage(path);
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
  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean contains(PathFileObject fileObject) {
    return Optional
        .ofNullable(modules.get(fileObject.getLocation()))
        .map(module -> module.contains(fileObject))
        .orElse(false);
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoaderLazy.access();
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public List<Set<Location>> getLocationsForModules() {
    return List.of(Set.copyOf(modules.keySet()));
  }

  @Override
  public Map<ModuleLocation, ? extends PackageContainerGroup> getModules() {
    return Map.copyOf(modules);
  }

  @Override
  public boolean hasLocation(ModuleLocation location) {
    return modules.containsKey(location);
  }

  @Override
  public <S> ServiceLoader<S> getServiceLoader(Class<S> service) {
    getClass().getModule().addUses(service);

    var finders = modules
        .values()
        .stream()
        .map(SimpleModuleModuleContainerGroup::getPackages)
        .flatMap(List::stream)
        .map(Container::getModuleFinder)
        .toArray(ModuleFinder[]::new);

    var composedFinder = ModuleFinder.compose(finders);
    var bootLayer = ModuleLayer.boot();
    var config = bootLayer
        .configuration()
        .resolveAndBind(ModuleFinder.of(), composedFinder, Collections.emptySet());

    var layer = bootLayer
        .defineModulesWithOneLoader(config, ClassLoader.getSystemClassLoader());

    return ServiceLoader.load(layer, service);
  }

  @Override
  public PackageContainerGroup getOrCreateModule(String moduleName) {
    return modules
        .computeIfAbsent(
            new ModuleLocation(location, moduleName),
            SimpleModuleModuleContainerGroup::new
        );
  }

  private ContainerClassLoader createClassLoader() {
    var moduleMapping = modules
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> entry.getKey().getModuleName(),
            entry -> entry.getValue().getPackages()
        ));

    return new ContainerClassLoader(location, moduleMapping);
  }

  /**
   * Wrapper around a location that lacks the constraints that
   * {@link SimplePackageContainerGroup} imposes.
   */
  private class SimpleModuleModuleContainerGroup
      extends AbstractPackageContainerGroup {

    private final Location location;

    private SimpleModuleModuleContainerGroup(Location location) {
      super(SimpleModuleContainerGroup.this.release);
      this.location = requireNonNull(location, "location");
    }

    @Override
    public Location getLocation() {
      return location;
    }
  }
}
