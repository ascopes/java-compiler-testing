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
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.pathwrappers.PathWrapper;
import java.net.URL;
import java.net.URLClassLoader;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An extension of the Java {@link URLClassLoader} that wraps around container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class PackageContainerGroupUrlClassLoader extends URLClassLoader {

  /**
   * Initialise this class loader.
   *
   * @param group the container group to use.
   */
  public PackageContainerGroupUrlClassLoader(PackageContainerGroup group) {
    super(
        "Packages within " + group.getLocation().getName(),
        group
            .getPackages()
            .stream()
            .map(Container::getPathWrapper)
            .map(PathWrapper::getUrl)
            .distinct()
            .toArray(URL[]::new),
        ClassLoader.getSystemClassLoader()
    );
  }
}
