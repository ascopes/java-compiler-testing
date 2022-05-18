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

package io.github.ascopes.jct.paths.v2.groups;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.intern.EnumerationAdapter;
import io.github.ascopes.jct.paths.v2.containers.Container;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class loader for package-oriented container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class PackageOrientedClassLoader extends ClassLoader {

  static {
    ClassLoader.registerAsParallelCapable();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(PackageOrientedContainerGroup.class);

  private final List<? extends Container> containers;

  /**
   * Initialize this class-loader.
   *
   * @param containers the containers to consider.
   */
  public PackageOrientedClassLoader(List<? extends Container> containers) {
    this.containers = requireNonNull(containers, "containers");
  }

  protected Class<?> findClass(String binaryName) throws ClassNotFoundException {
    try {
      for (var container : containers) {
        var clazz = container
            .getClassBinary(binaryName)
            .map(data -> defineClass(null, data, 0, data.length));

        if (clazz.isPresent()) {
          return clazz.get();
        }
      }

      throw new ClassNotFoundException("Class not found: " + binaryName);
    } catch (IOException ex) {
      throw new ClassNotFoundException("Class loading aborted for: " + binaryName, ex);
    }
  }

  @Override
  protected URL findResource(String resourcePath) {
    try {
      for (var container : containers) {
        var maybeResource = container.getResource(resourcePath);

        if (maybeResource.isPresent()) {
          return maybeResource.get();
        }
      }

    } catch (IOException ex) {
      // We ignore this, according to the spec for how this should be handled.
      LOGGER.warn(
          "Failed to look up resource {} because {}: {} - this will be ignored",
          resourcePath,
          ex.getClass().getName(),
          ex.getMessage()
      );
    }

    return null;
  }

  @Override
  protected Enumeration<URL> findResources(String resourcePath) throws IOException {
    var resources = new ArrayList<URL>();

    for (var container : containers) {
      container.getResource(resourcePath).ifPresent(resources::add);
    }

    return new EnumerationAdapter<>(resources.iterator());
  }
}
