/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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
package io.github.ascopes.jct.containers;

import io.github.ascopes.jct.workspaces.PathRoot;
import java.util.List;
import javax.tools.JavaFileManager.Location;

/**
 * A base definition for an output-oriented container group.
 *
 * <p>These can behave as if they are module-oriented, or non-module-oriented.
 * It is down to the implementation to mediate access between modules and their files.
 *
 * <p>Operations on modules should first {@link #getModule(String) get} or
 * {@link #getOrCreateModule(String) create} the module, and then operate on that sub-container
 * group. Operations on non-module packages should operate on this container group directly.
 *
 * <p>Note that each container group will usually only support one package container group
 * in the outputs. This is due to the JSR-199 API not providing a method for specifying which output
 * location to write files to for legacy-style packages.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public interface OutputContainerGroup extends PackageContainerGroup, ModuleContainerGroup {

  /**
   * {@inheritDoc}
   *
   * <p>Note that this implementation will only ever allow a single container in the package
   * container groups. If a container is already present, then an exception will be thrown.
   *
   * @param path the path to add.
   * @throws IllegalStateException if a package already exists in this location.
   */
  @Override
  void addPackage(PathRoot path);

  /**
   * {@inheritDoc}
   *
   * <p>Note that this implementation will only ever allow a single container in the package
   * container groups. If a container is already present, then an exception will be thrown.
   *
   * @param container the container to add.
   * @throws IllegalStateException if a package already exists in this location.
   */
  @Override
  void addPackage(Container container);

  /**
   * Get the output-oriented location.
   *
   * @return the output-oriented location.
   */
  @Override
  Location getLocation();

  /**
   * {@inheritDoc}
   *
   * <p>Note that this implementation will only ever return one container in the list due to
   * JSR-199 limitations.
   *
   * @return the list containing the package container, if it exists. Otherwise, an empty list is
   *     returned instead.
   */
  @Override
  List<Container> getPackages();
}
