/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
import static org.assertj.core.api.Assertions.assertThat;

import io.github.ascopes.jct.containers.ContainerGroup;
import java.util.List;
import java.util.ServiceLoader;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.jspecify.annotations.Nullable;

/**
 * Base assertions that can be performed on a container group.
 *
 * @param <I> the assertion implementation type.
 * @param <C> the container group type.
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class AbstractContainerGroupAssert<I extends AbstractContainerGroupAssert<I, C>, C extends ContainerGroup>
    extends AbstractAssert<I, C> {

  /**
   * Initialize the container group assertions.
   *
   * @param containerGroup the container group to assert upon.
   * @param selfType       the type of the assertion implementation to use.
   */
  @SuppressWarnings("DataFlowIssue")
  protected AbstractContainerGroupAssert(@Nullable C containerGroup, Class<?> selfType) {
    super(containerGroup, selfType);
  }

  /**
   * Get assertions to perform on the location of this container group.
   *
   * @return the assertions to perform.
   * @throws AssertionError if the object being asserted upon is null.
   */
  public LocationAssert location() {
    isNotNull();
    return new LocationAssert(actual.getLocation());
  }

  /**
   * Get assertions for the services loaded by the given service loader.
   *
   * @param type the class to look up in the service loader.
   * @param <T>  the service type.
   * @return the assertions across the resultant services that are loaded for the given class.
   * @throws AssertionError       if the object being asserted upon is null.
   * @throws NullPointerException if the provided class parameter is null.
   */
  public <T> AbstractListAssert<?, List<? extends T>, T, ? extends ObjectAssert<T>> services(
      Class<T> type
  ) {
    requireNonNull(type, "type must not be null");
    isNotNull();

    var items = actual.getServiceLoader(type)
        .stream()
        .map(ServiceLoader.Provider::get);
    return assertThat(items);
  }
}
