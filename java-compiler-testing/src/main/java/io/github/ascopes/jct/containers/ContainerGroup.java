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

import io.github.ascopes.jct.filemanagers.PathFileObject;
import java.io.Closeable;
import java.util.ServiceLoader;
import javax.tools.JavaFileManager.Location;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Base container group interface.
 *
 * <p>Closing an implementation of this interface will free up any internal resources that have
 * been opened. Resources passed to the implementation in an already-open state will not be closed.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public interface ContainerGroup extends Closeable {

  /**
   * Determine whether this group contains the given file object anywhere.
   *
   * @param fileObject the file object to look for.
   * @return {@code true} if the file object is contained in this group, or {@code false} otherwise.
   */
  boolean contains(PathFileObject fileObject);

  /**
   * Get the location of this container group.
   *
   * @return the location.
   */
  Location getLocation();

  /**
   * Get the effective Java release being targeted by the compiler that owns this container group.
   *
   * <p>This is used to calculate Multi-Release JAR packages correctly.
   *
   * @return the effective release version.
   */
  String getRelease();

  /**
   * Get a service loader for the given service class, or throw an exception if the operation is not
   * supported.
   *
   * @param service the service class to get.
   * @param <S>     the service class type.
   * @return the service loader.
   * @throws UnsupportedOperationException if the container group does not provide this
   *                                       functionality.
   */
  <S> ServiceLoader<S> getServiceLoader(Class<S> service);
}
