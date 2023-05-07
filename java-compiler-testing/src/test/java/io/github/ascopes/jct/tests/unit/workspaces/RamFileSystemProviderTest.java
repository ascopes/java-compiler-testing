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
package io.github.ascopes.jct.tests.unit.workspaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.workspaces.RamFileSystemProvider;
import io.github.ascopes.jct.workspaces.impl.MemoryFileSystemProvider;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * {@link RamFileSystemProvider} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("RamFileSystemProvider tests")
@Isolated("messes with global ServiceLoader")
@SuppressWarnings({"Java9UndeclaredServiceUsage", "removal"})
class RamFileSystemProviderTest {

  @DisplayName(".getInstance() returns the first service provider instance if present")
  @Test
  void getInstanceReturnsTheFirstServiceProviderInstance() {
    // Given
    RamFileSystemProvider customRamProvider = mock();
    RamFileSystemProvider result;

    try (var serviceLoaderCls = mockStatic(ServiceLoader.class)) {
      ServiceLoader<RamFileSystemProvider> serviceLoader = mock();

      serviceLoaderCls.when(() -> ServiceLoader.load(RamFileSystemProvider.class))
          .thenReturn(serviceLoader);
      when(serviceLoader.findFirst())
          .thenReturn(Optional.of(customRamProvider));

      // When
      result = RamFileSystemProvider.getInstance();

      // Then
      serviceLoaderCls.verify(() -> ServiceLoader.load(RamFileSystemProvider.class));
      serviceLoaderCls.verifyNoMoreInteractions();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }

    // AssertJ needs to use service loaders internally, so we cannot keep it mocked for
    // any longer than needed.
    assertThat(result).isSameAs(customRamProvider);
  }

  @DisplayName(".getInstance() returns the default implementation if no provider is present")
  @Test
  void getInstanceReturnsTheDefaultImplementationIfNoProviderIsPresent() {
    // Given
    RamFileSystemProvider result;

    try (var serviceLoaderCls = mockStatic(ServiceLoader.class)) {
      ServiceLoader<RamFileSystemProvider> serviceLoader = mock();

      serviceLoaderCls.when(() -> ServiceLoader.load(RamFileSystemProvider.class))
          .thenReturn(serviceLoader);
      when(serviceLoader.findFirst())
          .thenReturn(Optional.empty());

      // When
      result = RamFileSystemProvider.getInstance();

      // Then
      serviceLoaderCls.verify(() -> ServiceLoader.load(RamFileSystemProvider.class));
      serviceLoaderCls.verifyNoMoreInteractions();

      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }

    // AssertJ needs to use service loaders internally, so we cannot keep it mocked for
    // any longer than needed.
    assertThat(result)
        .isSameAs(MemoryFileSystemProvider.getInstance());
  }
}
