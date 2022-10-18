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

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.annotations.Nullable;
import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.ex.ClassLoadingFailedException;
import io.github.ascopes.jct.ex.ClassMissingException;
import io.github.ascopes.jct.utils.ToStringBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class loader that can load from modules and packages held within various implementations of
 * {@link Container}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class ContainerGroupClassLoaderImpl extends ClassLoader {

  static {
    ClassLoader.registerAsParallelCapable();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerGroupClassLoaderImpl.class);
  private static final List<Container> NO_PACKAGES = List.of();
  private static final Map<String, ? extends List<Container>> NO_MODULES = Map.of();

  private final Location location;
  private final List<? extends Container> packageContainers;
  private final Map<String, ? extends List<? extends Container>> moduleContainers;

  /**
   * Create a container class loader for a set of packages.
   *
   * @param location          the location that the containers are for.
   * @param packageContainers the package containers.
   */
  public ContainerGroupClassLoaderImpl(Location location,
      List<? extends Container> packageContainers) {
    this(location, packageContainers, NO_MODULES);
  }

  /**
   * Create a container class loader for a set of modules.
   *
   * @param location         the location that the containers are for.
   * @param moduleContainers the module containers.
   */
  public ContainerGroupClassLoaderImpl(
      Location location,
      Map<String, ? extends List<Container>> moduleContainers
  ) {
    this(location, NO_PACKAGES, moduleContainers);
  }

  /**
   * Create a container class loader for a set of packages and modules.
   *
   * @param location          the location that the containers are for.
   * @param packageContainers the package containers.
   * @param moduleContainers  the module containers.
   */
  public ContainerGroupClassLoaderImpl(
      Location location,
      List<? extends Container> packageContainers,
      Map<String, ? extends List<Container>> moduleContainers
  ) {
    this.location = requireNonNull(location, "location");
    this.packageContainers = requireNonNull(packageContainers, "packageContainers");
    this.moduleContainers = requireNonNull(moduleContainers, "moduleContainers");
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attribute("location", location)
        .toString();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    var module = ModuleHandle.tryExtract(name);

    if (module != null) {
      var moduleContainers = this.moduleContainers.get(module.getModuleName());

      if (moduleContainers != null) {
        var moduleClass = loadClassFrom(moduleContainers, module.getRest());

        if (moduleClass != null) {
          return moduleClass;
        }
      }
    }

    var packageClass = loadClassFrom(packageContainers, name);

    if (packageClass != null) {
      return packageClass;
    }

    throw new ClassMissingException(name, location);
  }

  @Nullable
  @Override
  protected URL findResource(String resourcePath) {
    try {
      var resources = findResources(resourcePath);
      return resources.hasMoreElements()
          ? resources.nextElement()
          : null;
    } catch (IOException ex) {
      // We have to return null in this case. I don't think this is overly useful though, so
      // log some diagnostic information to indicate what the issue is.
      LOGGER.warn(
          "Failed to find resource '{}' in {}. Will return null for this case",
          resourcePath,
          location.getName(),
          ex
      );
      return null;
    }
  }

  @Override
  protected Enumeration<URL> findResources(String resourcePath) throws IOException {
    resourcePath = removeLeadingForwardSlash(resourcePath);

    var resources = new ArrayList<URL>();

    var module = ModuleHandle.tryExtract(resourcePath);
    if (module != null) {
      var moduleContainers = this.moduleContainers.get(module.getModuleName());

      if (moduleContainers != null) {
        var trimmedResourcePath = module.getRest();

        for (var container : moduleContainers) {
          var resource = container.getResource(trimmedResourcePath);

          if (resource == null) {
            continue;
          }

          LOGGER.trace(
              "Found resource '{}' in module container {} for module {} within {}",
              resource,
              container,
              module.getModuleName(),
              location.getName()
          );

          resources.add(resource);
        }
      }
    }

    for (var container : packageContainers) {
      var resource = container.getResource(resourcePath);

      if (resource == null) {
        continue;
      }

      LOGGER.trace(
          "Found resource '{}' in package container {} within {}",
          resource,
          container,
          location.getName()
      );

      resources.add(resource);
    }

    return new EnumerationAdapter<>(resources.iterator());
  }

  @Nullable
  private Class<?> loadClassFrom(
      List<? extends Container> containers,
      String binaryName
  ) throws ClassLoadingFailedException {
    try {
      for (var container : containers) {
        var data = container.getClassBinary(binaryName);
        if (data == null) {
          continue;
        }

        var clazz = defineClass(null, data, 0, data.length);

        LOGGER.trace("Found class {} in {} for {}", binaryName, container, location.getName());
        return clazz;
      }

      LOGGER.trace("Class {} not found in {}", binaryName, location.getName());

      return null;
    } catch (IOException ex) {
      throw new ClassLoadingFailedException(binaryName, location, ex);
    }
  }

  private static String removeLeadingForwardSlash(String name) {
    var index = 0;
    while (index < name.length() && name.charAt(index) == '/') {
      ++index;
    }

    return name.substring(index);
  }
}
