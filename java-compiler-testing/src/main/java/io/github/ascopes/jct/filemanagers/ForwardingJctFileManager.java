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
package io.github.ascopes.jct.filemanagers;

import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;

/**
 * Abstract base for a {@link JctFileManager} that forwards all methods by default to a
 * delegate object.
 *
 * @param <M> the file manager implementation to delegate to.
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = API.Status.STABLE)
public abstract class ForwardingJctFileManager<M extends JctFileManager>
    extends ForwardingJavaFileManager<M>
    implements JctFileManager {

  /**
   * Creates a new instance of {@code ForwardingJavaFileManager}.
   *
   * @param fileManager delegate to this file manager
   */
  protected ForwardingJctFileManager(M fileManager) {
    super(fileManager);
  }

  @Override
  public void addPath(Location location, PathRoot path) {
    fileManager.addPath(location, path);
  }

  @Override
  public void addPaths(Location location, Collection<? extends PathRoot> paths) {
    fileManager.addPaths(location, paths);
  }

  @Override
  public void copyContainers(Location from, Location to) {
    fileManager.copyContainers(from, to);
  }

  @Override
  public void createEmptyLocation(Location location) {
    fileManager.createEmptyLocation(location);
  }

  @Override
  public String getEffectiveRelease() {
    return fileManager.getEffectiveRelease();
  }

  @Override
  public PackageContainerGroup getPackageContainerGroup(Location location) {
    return fileManager.getPackageContainerGroup(location);
  }

  @Override
  public Collection<PackageContainerGroup> getPackageContainerGroups() {
    return fileManager.getPackageContainerGroups();
  }

  @Override
  public ModuleContainerGroup getModuleContainerGroup(Location location) {
    return fileManager.getModuleContainerGroup(location);
  }

  @Override
  public Collection<ModuleContainerGroup> getModuleContainerGroups() {
    return fileManager.getModuleContainerGroups();
  }

  @Override
  public OutputContainerGroup getOutputContainerGroup(Location location) {
    return fileManager.getOutputContainerGroup(location);
  }

  @Override
  public Collection<OutputContainerGroup> getOutputContainerGroups() {
    return fileManager.getOutputContainerGroups();
  }

  // We have to override this method since the compiler will get confused that the JctFileManager
  // interface is promoting the return type of this method to a more specific type than the
  // ForwardingJavaFileManager class implements.
  @Override
  public Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<JavaFileObject.Kind> kinds,
      boolean recurse
  ) throws IOException {
    return fileManager.list(location, packageName, kinds, recurse);
  }
}
