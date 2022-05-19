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

import io.github.ascopes.jct.paths.v2.PathFileObject;
import java.io.Closeable;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base container group interface.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public interface ContainerGroup extends Closeable {

  /**
   * Determine whether this group contains the given file object anywhere.
   *
   * @param fileObject the file object to look for.
   * @return {@code true} if the file object is contained in this group, or {@code false} otherwise.
   */
  boolean contains(PathFileObject fileObject);

  /**
   * Get a class loader for this group of containers.
   *
   * <p>Note that adding additional containers to this group after accessing this class loader
   * may result in the class loader being destroyed or re-created.
   *
   * @return the class loader, if the implementation provides one. If no class loader is available,
   *     then an empty optional is returned instead.
   */
  Optional<ClassLoader> getClassLoader();

  /**
   * Get the location of this container group.
   *
   * @return the location.
   */
  Location getLocation();

  /**
   * Get a service loader for the given service class.
   *
   * @param service the service class to get.
   * @param <S>     the service class type.
   * @return the service loader, if this location supports loading plugins. If not, an empty
   *     optional is returned instead.
   * @throws UnsupportedOperationException if the container group does not provide this
   *                                       functionality.
   */
  <S> Optional<ServiceLoader<S>> getServiceLoader(Class<S> service);
}
