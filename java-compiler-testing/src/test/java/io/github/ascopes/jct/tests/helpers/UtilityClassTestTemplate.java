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
package io.github.ascopes.jct.tests.helpers;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.github.ascopes.jct.utils.UtilityClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.TestAbortedException;

/**
 * Template for testing that a class is correctly formatted as being {@code static}-only.
 *
 * @author Ashley Scopes
 */
public interface UtilityClassTestTemplate {

  /**
   * Get the type to test.
   *
   * @return the type to test.
   */
  Class<?> getTypeBeingTested();

  /**
   * Open the module to allow reflection for the tests.
   */
  @BeforeEach
  default void openModuleForReflection() {
    var type = getTypeBeingTested();
    // We probably do not need to do this, but it saves hassle of adding this later
    // by ensuring we open the module so that we can inspect it with reflection.
    type.getModule().addOpens(type.getPackageName(), getClass().getModule());
  }

  @DisplayName("Class should extend UtilityClass")
  @Test
  default void testClassExtendsUtilityClass() {
    assertThat(getTypeBeingTested())
        .hasSuperclass(UtilityClass.class);
  }

  @DisplayName("Class should be final")
  @Test
  default void testClassIsFinal() {
    assertThat(getTypeBeingTested()).isFinal();
  }

  @DisplayName("Class should have a single constructor")
  @Test
  default void testClassSingleConstructor() {
    assertThatObject(getTypeBeingTested())
        .extracting(Class::getDeclaredConstructors, array(Constructor[].class))
        .hasSize(1);
  }

  @DisplayName("Class constructor should be private")
  @Test
  default void testClassConstructorIsPrivate() {
    assertThatObject(getSingleConstructor())
        .extracting(Constructor::getModifiers)
        .extracting(Modifier::isPrivate, BOOLEAN)
        .isTrue();
  }

  @DisplayName("Class constructor should take zero arguments")
  @Test
  default void testClassConstructorHasNoArguments() {
    assertThatObject(getSingleConstructor())
        .extracting(Constructor::getParameterCount)
        .isEqualTo(0);
  }

  @DisplayName("Class constructor should throw an UnsupportedOperationException")
  @Test
  default void testClassConstructorThrowsUnsupportedOperationException() {
    assertThatCode(() -> getSingleConstructor().newInstance())
        .cause()
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("this is a utility class that cannot be initialised or extended");
  }

  @DisplayName("All methods should be static")
  @TestFactory
  default Stream<DynamicTest> allMethodsShouldBeStatic() {
    return getAllMethods()
        .map(method -> {
          var name = methodName(method);
          return dynamicTest(name + " should be static", () -> assertThat(method)
              .as(name)
              .extracting(Method::getModifiers)
              .satisfies(modifiers -> assertThat(modifiers)
                  .as(Modifier.toString(modifiers))
                  .extracting(Modifier::isStatic, BOOLEAN)
                  .withFailMessage("expected method to have static modifier")
                  .isTrue()
              ));
        });
  }

  @DisplayName("All fields should be static")
  @TestFactory
  default Stream<DynamicTest> allFieldsShouldBeStatic() {
    return getAllFields()
        .map(field -> {
          var name = field.getType().getSimpleName() + " " + field.getName();
          return dynamicTest(name + " should be static", () -> assertThat(field)
              .as(name)
              .extracting(Field::getModifiers)
              .satisfies(modifiers -> assertThat(modifiers)
                  .as(Modifier.toString(modifiers))
                  .extracting(Modifier::isStatic, BOOLEAN)
                  .withFailMessage("expected field to have static modifier")
                  .isTrue()
              ));
        });
  }

  @DisplayName("All nested classes should be static")
  @TestFactory
  default Stream<DynamicTest> allNestedClassesShouldBeStatic() {
    return getAllNestedClasses()
        .map(clazz -> {
          var name = getTypeBeingTested().getSimpleName() + "." + clazz.getSimpleName();
          return dynamicTest(name + " should be static", () -> assertThat(clazz)
              .as(name)
              .isStatic());
        });
  }

  private Constructor<?> getSingleConstructor() {
    try {
      var type = getTypeBeingTested();
      var constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor;
    } catch (NoSuchMethodException ex) {
      throw new TestAbortedException(ex.getMessage(), ex);
    }
  }

  private Stream<Method> getAllMethods() {
    var declared = Stream.of(getTypeBeingTested().getDeclaredMethods());
    var inherited = Stream.of(getTypeBeingTested().getMethods());
    var objectMethods = Stream.of(Object.class.getMethods()).collect(Collectors.toSet());

    return Stream.concat(declared, inherited)
        .distinct()
        .filter(not(Method::isBridge))
        .filter(not(Method::isSynthetic))
        .filter(not(objectMethods::contains));
  }

  private Stream<Field> getAllFields() {
    var declared = Stream.of(getTypeBeingTested().getDeclaredFields());
    var inherited = Stream.of(getTypeBeingTested().getFields());
    var objectFields = Stream.of(Object.class.getFields()).collect(Collectors.toSet());

    return Stream.concat(declared, inherited)
        .distinct()
        .filter(not(Field::isSynthetic))
        .filter(not(Field::isEnumConstant))
        .filter(not(objectFields::contains));
  }

  private Stream<Class<?>> getAllNestedClasses() {
    var declared = Stream.of(getTypeBeingTested().getDeclaredClasses());
    var inherited = Stream.of(getTypeBeingTested().getClasses());
    var objectClasses = Stream.of(Object.class.getClasses()).collect(Collectors.toSet());

    return Stream.concat(declared, inherited)
        .distinct()
        .filter(not(Class::isSynthetic))
        .filter(not(objectClasses::contains));
  }

  private String methodName(Method method) {
    var sb = new StringBuilder(method.getReturnType().getSimpleName())
        .append(" ")
        .append(method.getName())
        .append("(");

    var firstParam = true;

    for (var param : method.getParameters()) {
      if (param.isImplicit()) {
        continue;
      }

      if (firstParam) {
        firstParam = false;
      } else {
        sb.append(", ");
      }

      sb.append(param.getType().getSimpleName());

      if (param.isVarArgs()) {
        sb.append("...");
      }

      if (param.isNamePresent()) {
        sb.append(" ").append(param.getName());
      }
    }

    return sb.append(")")
        .toString();
  }
}
