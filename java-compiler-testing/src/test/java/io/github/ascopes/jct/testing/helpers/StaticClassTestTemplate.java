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

package io.github.ascopes.jct.testing.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

/**
 * Template for testing that a class is correctly formatted as being {@code static}-only.
 *
 * @author Ashley Scopes
 */
public interface StaticClassTestTemplate {

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

  /**
   * Test that the type is static-only.
   */
  @DisplayName("This class should be marked as 'static'-only")
  @Order(Integer.MIN_VALUE)
  @TestFactory
  default Stream<DynamicTest> ensureStaticClass() {
    return Stream.of(
        test(
            "{name} should be final",
            () -> assertThat(getTypeBeingTested()).isFinal()
        ),
        test(
            "{name} should have a single constructor",
            () -> assertThatObject(getTypeBeingTested())
                .extracting(Class::getDeclaredConstructors, array(Constructor[].class))
                .hasSize(1)
        ),
        test(
            "{name}'s constructor should be private",
            () -> assertThatObject(getSingleConstructor())
                .extracting(Constructor::getModifiers)
                .extracting(Modifier::isPrivate, BOOLEAN)
                .isTrue()
        ),
        test(
            "{name}'s constructor should take zero arguments",
            () -> assertThatObject(getSingleConstructor())
                .extracting(Constructor::getParameterCount)
                .isEqualTo(0)
        ),
        test(
            "{name}'s constructor should raise an UnsupportedOperationException",
            () -> assertThatCode(() -> getSingleConstructor().newInstance())
                .cause()
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("static-only class")
        )
    );
  }

  private Constructor<?> getSingleConstructor() throws NoSuchMethodException {
    var type = getTypeBeingTested();
    var constructor = type.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor;
  }

  private DynamicTest test(String nameTemplate, Executable test) {
    return dynamicTest(
        nameTemplate.replace("{name}", getTypeBeingTested().getSimpleName()),
        test
    );
  }
}
