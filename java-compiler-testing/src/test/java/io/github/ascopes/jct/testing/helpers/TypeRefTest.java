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
package io.github.ascopes.jct.testing.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link TypeRef}.
 *
 * @author Ashley Scopes
 */
@DisplayName("TypeRef helper tests")
class TypeRefTest {

  @DisplayName("Concrete types are reifiable")
  @MethodSource("reifiedConcreteTypes")
  @ParameterizedTest(name = "{0} should reify to {1}")
  <T> void concreteTypesAreReifiable(TypeRef<T> ref, Class<T> expected) {
    assertThat(ref.getRawType()).isEqualTo(expected);
  }

  @DisplayName("Parameterized types are raw-reifiable")
  @MethodSource("reifiedParameterizedTypes")
  @ParameterizedTest(name = "{0} should reify to raw {1}")
  <T> void parameterizedTypesAreRawReifiable(TypeRef<T> ref, Class<T> expected) {
    assertThat(ref.getRawType()).isEqualTo(expected);
  }

  @DisplayName("Raw references cannot be reified")
  @SuppressWarnings("rawtypes")
  @Test
  void rawReferencesCannotBeReified() {
    assertThatThrownBy(() -> new TypeRef() {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "No type information found, only found class of type %s",
            TypeRef.class.getName()
        );
  }

  @DisplayName("Type-erased references cannot be reified")
  @Test
  <T> void typeErasedReferencesCannotBeReified() {
    assertThatThrownBy(() -> new TypeRef<T>() {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot reify non-concrete type");
  }

  @DisplayName("Covariant erased references cannot be reified")
  @Test
  <T extends CharSequence> void covariantErasedReferencesCannotBeReified() {
    assertThatThrownBy(() -> new TypeRef<T>() {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot reify non-concrete type");
  }

  @DisplayName("Intersection erased references cannot be reified")
  @Test
  <T extends CharSequence & Runnable> void intersectionErasedReferencesCannotBeReified() {
    assertThatThrownBy(() -> new TypeRef<T>() {})
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot reify non-concrete type");
  }

  @SuppressWarnings("rawtypes")
  static Stream<Arguments> reifiedConcreteTypes() {
    return Stream.of(
        of(new TypeRef<String>() {}, String.class),
        of(new TypeRef<Integer>() {}, Integer.class),
        of(new TypeRef<>() {}, Object.class),
        of(new TypeRef<TypeRef>() {}, TypeRef.class),
        of(new TypeRef<ArrayList>() {}, ArrayList.class)
    );
  }

  static Stream<Arguments> reifiedParameterizedTypes() {
    return Stream.of(
        of(new TypeRef<ArrayList<Integer>>() {}, ArrayList.class),
        of(new TypeRef<ArrayList<ArrayList<String>>>() {}, ArrayList.class),
        of(new TypeRef<TypeRef<String>>() {}, TypeRef.class),
        of(new TypeRef<TypeRef<TypeRef<Integer>>>() {}, TypeRef.class),
        of(new TypeRef<BiFunction<String, Integer, Long>>() {}, BiFunction.class)
    );
  }
}
