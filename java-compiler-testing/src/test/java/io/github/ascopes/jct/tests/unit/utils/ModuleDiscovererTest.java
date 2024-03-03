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
package io.github.ascopes.jct.tests.unit.utils;

import static io.github.ascopes.jct.tests.helpers.Fixtures.someModuleName;
import static io.github.ascopes.jct.tests.helpers.Fixtures.somePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.ascopes.jct.utils.ModuleDiscoverer;
import io.github.ascopes.jct.utils.ModuleDiscoverer.ModuleCandidate;
import io.github.ascopes.jct.workspaces.PathRoot;
import io.github.ascopes.jct.workspaces.impl.WrappingDirectoryImpl;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link ModuleDiscoverer} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("ModuleDiscoverer tests")
class ModuleDiscovererTest {

  @DisplayName("Modules are discovered")
  @Test
  void modulesAreDiscovered() {
    // Given
    var moduleFinder = mock(ModuleFinder.class);
    try (var moduleFinderStatic = mockStatic(ModuleFinder.class)) {
      moduleFinderStatic.when(() -> ModuleFinder.of(any()))
          .thenReturn(moduleFinder);

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
          .satisfiesOnlyOnce(assertCandidateEquals(module1))
          .satisfiesOnlyOnce(assertCandidateEquals(module2))
          .satisfiesOnlyOnce(assertCandidateEquals(module3))
          .hasSize(3);
    }
  }

  @DisplayName("Module discovery errors fail silently")
  @Test
  void moduleDiscoveryErrorsFailSilently() {
    // Given
    var moduleFinder = mock(ModuleFinder.class);
    try (var moduleFinderStatic = mockStatic(ModuleFinder.class)) {
      moduleFinderStatic.when(() -> ModuleFinder.of(any()))
          .thenReturn(moduleFinder);

      when(moduleFinder.findAll()).thenThrow(FindException.class);

      // When
      var results = ModuleDiscoverer.findModulesIn(somePath());

      // Then
      assertThat(results).isEmpty();
    }
  }

  static ModuleReference someModuleRef(String name, Path path) {
    var descriptor = mock(ModuleDescriptor.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
    when(descriptor.name()).thenReturn(name);

    var ref = mock(ModuleReference.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
    when(ref.location()).thenReturn(Optional.of(path.toUri()));

    when(ref.descriptor()).thenReturn(descriptor);

    return ref;
  }

  static Consumer<ModuleCandidate> assertCandidateEquals(ModuleReference reference) {
    return moduleCandidate -> assertSoftly(softly -> {
      softly.assertThat(moduleCandidate.getName())
          .as("name")
          .isEqualTo(reference.descriptor().name());
      softly.assertThat(moduleCandidate.getPath())
          .as("path")
          .isEqualTo(Path.of(reference.location().orElseThrow()));
      softly.assertThat(moduleCandidate.getDescriptor())
          .as("descriptor")
          .isEqualTo(reference.descriptor());
      softly.assertThat(moduleCandidate.createPathRoot())
          .as("pathRoot")
          .isInstanceOf(WrappingDirectoryImpl.class)
          .extracting(PathRoot::getPath)
          .as("pathRoot -> path")
          .isEqualTo(Path.of(reference.location().orElseThrow()));
    });
  }

  @DisplayName("ModuleCandidate tests")
  @Nested
  class ModuleCandidateTest {
    @DisplayName("The name is set")
    @Test
    void theNameIsSet() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);

      // When
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate.getName()).as("name").isEqualTo(name);
    }

    @DisplayName("The path is set")
    @Test
    void thePathIsSet() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);

      // When
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate.getPath()).as("path").isEqualTo(path);
    }

    @DisplayName("The descriptor is set")
    @Test
    void theDescriptorIsSet() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);

      // When
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate.getDescriptor()).as("descriptor").isEqualTo(ref.descriptor());
    }

    @DisplayName("The PathRoot is created as expected")
    @Test
    void thePathRootIsCreated() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);

      // When
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate.createPathRoot())
          .as("pathRoot")
          .isInstanceOf(WrappingDirectoryImpl.class)
          .extracting(PathRoot::getPath)
          .as("pathRoot -> path")
          .isEqualTo(path);
    }

    @DisplayName("Equal candidates are considered equal")
    @Test
    void equalCandidatesAreConsideredEqual() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref1 = someModuleRef(name, path);
      var ref2 = someModuleRef(name, path);

      // When
      var candidate1 = new ModuleCandidate(name, path, ref1.descriptor());
      var candidate2 = new ModuleCandidate(name, path, ref2.descriptor());

      // Then
      assertThat(candidate1).isEqualTo(candidate2);
      assertThat(candidate1).hasSameHashCodeAs(candidate2);
    }

    @DisplayName("Inequal candidates are considered inequal")
    @Test
    void inequalCandidatesAreConsideredInequal() {
      // Given
      var name1 = someModuleName();
      var name2 = someModuleName();
      var path1 = somePath();
      var path2 = somePath();
      var ref1 = someModuleRef(name1, path1);
      var ref2 = someModuleRef(name2, path2);

      // When
      var candidate1 = new ModuleCandidate(name1, path1, ref1.descriptor());
      var candidate2 = new ModuleCandidate(name2, path2, ref2.descriptor());

      // Then
      assertThat(candidate1).isNotEqualTo(candidate2);
      assertThat(candidate1).doesNotHaveSameHashCodeAs(candidate2);
    }

    @DisplayName("Candidates are not equal to null")
    @Test
    void candidatesAreNotEqualToNull() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      // (Purposely using isNotEqual here rather than isNull, so do not
      // change it).
      assertThat(candidate).isNotEqualTo(null);
    }

    @DisplayName("Candidates are not equal to random objects")
    @Test
    void candidatesAreNotEqualToRandomObjects() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate).isNotEqualTo(new Object());
    }

    @DisplayName("Candidates have the expected toString representation")
    @Test
    void candidatesHaveExpectedToStringRepresentation() {
      // Given
      var name = someModuleName();
      var path = somePath();
      var ref = someModuleRef(name, path);
      var candidate = new ModuleCandidate(name, path, ref.descriptor());

      // Then
      assertThat(candidate)
          .hasToString(
              "ModuleCandidate{name=\"%s\", path=\"%s\"}",
              name,
              // Replace handles string escaping on Windows paths
              path.toString().replace("\\", "\\\\")
          );
    }
  }
}
