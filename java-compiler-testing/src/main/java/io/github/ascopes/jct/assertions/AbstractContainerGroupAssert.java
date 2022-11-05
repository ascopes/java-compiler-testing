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
package io.github.ascopes.jct.assertions;

import static java.util.Objects.requireNonNull;

import io.github.ascopes.jct.containers.ContainerGroup;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.assertj.core.api.ObjectAssert;

/**
 * Base assertions that can be performed on a container group.
 *
 * @param <I> the assertion implementation type.
 * @param <C> the container group type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractContainerGroupAssert<I extends AbstractContainerGroupAssert<I, C>, C extends ContainerGroup>
    extends AbstractAssert<I, C> {

  protected static final int FUZZY_CUTOFF = 5;

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   * @param selfType       the type of the assertion implementation to use.
   */
  protected AbstractContainerGroupAssert(C containerGroup, Class<?> selfType) {
    super(containerGroup, selfType);
  }

  /**
   * Get assertions to perform on the class loader associated with this container group.
   *
   * @return the assertions to perform.
   */
  public ClassLoaderAssert classLoader() {
    return new ClassLoaderAssert(actual.getClassLoader());
  }

  /**
   * Get assertions to perform on the location of this container group.
   *
   * @return the assertions to perform.
   */
  public LocationAssert location() {
    return new LocationAssert(actual.getLocation());
  }

  /**
   * Get assertions for the services loaded by the given service loader.
   *
   * @param clazz the class to look up in the service loader.
   * @param <T>   the service type.
   * @return the assertions across the resultant services that are loaded for the given class.
   */
  public <T> AbstractListAssert<?, List<? extends T>, T, ObjectAssert<T>> serviceLoader(
      Class<T> clazz
  ) {
    requireNonNull(clazz, "class must not be null");

    var items = new ArrayList<T>();
    actual.getServiceLoader(clazz).iterator().forEachRemaining(items::add);

    return FactoryBasedNavigableListAssert.assertThat(items, ObjectAssert::new);
  }
}
