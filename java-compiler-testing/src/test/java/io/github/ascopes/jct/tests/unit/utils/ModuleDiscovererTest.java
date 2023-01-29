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
package io.github.ascopes.jct.tests.unit.utils;

import static io.github.ascopes.jct.tests.helpers.Fixtures.somePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.utils.ModuleDiscoverer;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * {@link ModuleDiscoverer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ModuleDiscoverer tests")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModuleDiscovererTest {

  @Mock
  MockedStatic<ModuleFinder> moduleFinderStatic;

  @Mock
  ModuleFinder moduleFinder;

  @BeforeEach
  void setUp() {
    moduleFinderStatic.when(() -> ModuleFinder.of(any()))
        .thenReturn(moduleFinder);
  }

  @DisplayName("Modules are discovered")
  @Test
  void modulesAreDiscovered() {
    // Given
    var path1 = somePath();
    var module1 = someModuleRef("foo.bar", path1);
    var path2 = somePath();
    var module2 = someModuleRef("baz.bork", path2);
    var path3 = somePath();
    var module3 = someModuleRef("qux.quxx", path3);
    when(moduleFinder.findAll()).thenReturn(Set.of(module1, module2, module3));

    var path = somePath();

    // When
    var results = ModuleDiscoverer.findModulesIn(path);

    // Then
    moduleFinderStatic.verify(() -> ModuleFinder.of(path));
    assertThat(results)
        .containsEntry("foo.bar", path1)
        .containsEntry("baz.bork", path2)
        .containsEntry("qux.quxx", path3)
        .hasSize(3);
  }

  @DisplayName("Module discovery errors fail silently")
  @Test
  void moduleDiscoveryErrorsFailSilently() {
    // Given
    when(moduleFinder.findAll()).thenThrow(FindException.class);

    // When
    var results = ModuleDiscoverer.findModulesIn(somePath());

    // Then
    assertThat(results).isEmpty();
  }

  static ModuleReference someModuleRef(String name, Path path) {
    var descriptor = mock(ModuleDescriptor.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
    when(descriptor.name()).thenReturn(name);

    var ref = mock(ModuleReference.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
    when(ref.location()).thenReturn(Optional.of(path.toUri()));

    when(ref.descriptor()).thenReturn(descriptor);

    return ref;
  }
}
