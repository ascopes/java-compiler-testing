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

import static io.github.ascopes.jct.fixtures.Fixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.ContainerGroup;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractListAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link AbstractContainerGroupAssert} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("AbstractContainerGroupAssert tests")
class AbstractContainerGroupAssertTest {

  @DisplayName("AbstractContainerGroupAssert.location(...) tests")
  @Nested
  class LocationTest {

    @DisplayName(".location() throws an AssertionError if the container group is null")
    @Test
    void locationThrowsAnAssertionErrorIfTheContainerGroupIsNull() {
      // Given
      var assertions = new AssertionImpl(null);

      // Then
      assertThatThrownBy(assertions::location)
          .isInstanceOf(AssertionError.class)
          .hasMessageEndingWith("Expecting actual not to be null");
    }

    @DisplayName(".location() returns the location assertions")
    @Test
    void locationReturnsTheLocationAssertions() {
      // Given
      var containerGroup = mock(ContainerGroup.class);
      var location = someLocation();
      when(containerGroup.getLocation()).thenReturn(location);

      var assertions = new AssertionImpl(containerGroup);

      // When
      var locationAssertions = assertions.location();

      // Then
      assertThat(locationAssertions)
          .isNotNull()
          .isInstanceOf(LocationAssert.class);

      locationAssertions
          .isSameAs(location);
    }
  }

  @DisplayName("AbstractContainerGroupAssert.services(...) tests")
  @Nested
  class ServicesTest {

    @DisplayName(".services() throws a NullPointerException if the class parameter is null")
    @Test
    void servicesThrowsNullPointerExceptionIfClassParameterIsNull() {
      // Given
      var assertions = new AssertionImpl(mock(ContainerGroup.class));

      // Then
      assertThatThrownBy(() -> assertions.services(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageEndingWith("type must not be null");
    }

    @DisplayName(".services() throws an AssertionError if the container group is null")
    @Test
    void servicesThrowsAnAssertionErrorIfTheContainerGroupIsNull() {
      // Given
      var assertions = new AssertionImpl(null);

      // Then
      assertThatThrownBy(() -> assertions.services(SomeServiceType.class))
          .isInstanceOf(AssertionError.class)
          .hasMessageEndingWith("Expecting actual not to be null");
    }

    @DisplayName(".services() returns a list assertion across the available services")
    @Test
    void servicesReturnsListAssertionAcrossTheAvailableServices() {
      // Given
      var impl1 = mock(SomeServiceType.class);
      var impl2 = mock(SomeServiceType.class);
      var impl3 = mock(SomeServiceType.class);

      ServiceLoader<SomeServiceType> serviceLoader = mock();
      when(serviceLoader.stream()).then(ctx -> Stream.of(impl1, impl2, impl3)
          .map(ServiceLoaderProvider::new));

      var containerGroup = mock(ContainerGroup.class);
      when(containerGroup.getServiceLoader(any())).then(ctx -> serviceLoader);

      var assertions = new AssertionImpl(containerGroup);

      // When
      var servicesAssertions = assertions.services(SomeServiceType.class);

      // Then
      assertThat(servicesAssertions)
          .isNotNull()
          .isInstanceOf(AbstractListAssert.class);
      servicesAssertions
          .containsExactly(impl1, impl2, impl3);
      verify(containerGroup)
          .getServiceLoader(SomeServiceType.class);
      verifyNoMoreInteractions(containerGroup);
    }
  }

  interface SomeServiceType {}

  static final class AssertionImpl
      extends AbstractContainerGroupAssert<AssertionImpl, ContainerGroup> {

    AssertionImpl(@Nullable ContainerGroup containerGroup) {
      super(containerGroup, AssertionImpl.class);
    }
  }

  static class ServiceLoaderProvider<T> implements ServiceLoader.Provider<T> {
    private final T service;

    ServiceLoaderProvider(T service) {
      this.service = service;
    }

    @Override
    public T get() {
      return service;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends T> type() {
      return (Class<T>) service.getClass();
    }
  }
}
