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
package io.github.ascopes.jct.containers.impl;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An extension of the Java {@link URLClassLoader} that wraps around container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class ContainerGroupUrlClassLoader extends URLClassLoader {

  private ContainerGroupUrlClassLoader(String name, URL[] urls) {
    super(name, urls, ClassLoader.getSystemClassLoader());
  }

  /**
   * Create a class loader for a given package container group.
   *
   * @param group the package container group to wrap.
   * @return the class loader.
   */
  public static ContainerGroupUrlClassLoader createClassLoaderFor(PackageContainerGroup group) {
    return new ContainerGroupUrlClassLoader(
        "Packages within package group " + group.getLocation().getName(),
        urlsForPackageGroup(group)
            .distinct()
            .toArray(URL[]::new)
    );
  }

  /**
   * Create a class loader for a given module container group.
   *
   * @param group the module container group to wrap.
   * @return the class loader.
   */
  public static ContainerGroupUrlClassLoader createClassLoaderFor(ModuleContainerGroup group) {
    return new ContainerGroupUrlClassLoader(
        "Packages within module group " + group.getLocation().getName(),
        urlsForModuleGroup(group)
            .distinct()
            .toArray(URL[]::new)
    );
  }

  /**
   * Create a class loader for a given output container group.
   *
   * @param group the output container group to wrap.
   * @return the class loader.
   */
  public static ContainerGroupUrlClassLoader createClassLoaderFor(OutputContainerGroup group) {
    return new ContainerGroupUrlClassLoader(
        "Packages within output group " + group.getLocation().getName(),
        Stream.concat(urlsForPackageGroup(group), urlsForModuleGroup(group))
            .distinct()
            .toArray(URL[]::new)
    );
  }

  private static Stream<URL> urlsForPackageGroup(PackageContainerGroup group) {
    return group
        .getPackages()
        .stream()
        .map(Container::getPathWrapper)
        .map(PathWrapper::getUrl);
  }

  private static Stream<URL> urlsForModuleGroup(ModuleContainerGroup group) {
    return group
        .getModules()
        .values()
        .stream()
        .map(PackageContainerGroup::getPackages)
        .flatMap(Collection::stream)
        .map(Container::getPathWrapper)
        .map(PathWrapper::getUrl);
  }
}
