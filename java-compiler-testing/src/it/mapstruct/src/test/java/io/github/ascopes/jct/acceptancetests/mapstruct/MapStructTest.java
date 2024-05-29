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
package io.github.ascopes.jct.acceptancetests.mapstruct;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.junit.JavacCompilerTest;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.util.stream.Stream;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;

class MapStructTest {

  @DisplayName("MapStruct generates expected mapping code")
  @JavacCompilerTest
  void mapStructGeneratesExpectedMappingCode(JctCompiler compiler) throws Throwable {
    try (final var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "flat");

      // When
      final var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

      final var classLoader = compilation.getFileManager()
          .getClassLoader(StandardLocation.CLASS_OUTPUT);
      final var packageName = "org.example";

      final var carTypeClass = classLoader.loadClass(packageName + ".CarType");
      final var carClass = classLoader.loadClass(packageName + ".Car");
      final var carMapperClass = classLoader.loadClass(packageName + ".CarMapper");

      final var car = carClass.getConstructor().newInstance();

      setAttr(car, "make", "VW Polo");
      setAttr(car, "type", getAttr(carTypeClass, "HATCHBACK"));
      setAttr(car, "numberOfSeats", 5);

      final var carMapper = getAttr(carMapperClass, "INSTANCE");
      final var carDto = callMethod(carMapper, "carToCarDto", car);

      assertSoftly(softly -> {
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("make", "VW Polo");
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("type", "HATCHBACK");
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("seatCount", 5);
      });
    }
  }

  @DisplayName("MapStruct generates expected mapping code for modules")
  @JavacCompilerTest(minVersion = 9)
  void mapStructGeneratesExpectedMappingCodeForModules(JctCompiler compiler) throws Throwable {
    try (final var workspace = Workspaces.newWorkspace()) {
      // Given
      workspace
          .createSourcePathPackage()
          .copyContentsFrom("src", "test", "resources", "code", "jpms");

      // When
      final var compilation = compiler.compile(workspace);

      // Then
      assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

      final var classLoader = compilation.getFileManager()
          .getClassLoader(StandardLocation.CLASS_OUTPUT);
      final var packageName = "org.example";

      final var carTypeClass = classLoader.loadClass(packageName + ".CarType");
      final var carClass = classLoader.loadClass(packageName + ".Car");
      final var carMapperClass = classLoader.loadClass(packageName + ".CarMapper");

      final var car = carClass.getConstructor().newInstance();

      setAttr(car, "make", "VW Polo");
      setAttr(car, "type", getAttr(carTypeClass, "HATCHBACK"));
      setAttr(car, "numberOfSeats", 5);

      final var carMapper = getAttr(carMapperClass, "INSTANCE");
      final var carDto = callMethod(carMapper, "carToCarDto", car);

      assertSoftly(softly -> {
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("make", "VW Polo");
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("type", "HATCHBACK");
        softly.assertThat(carDto).hasFieldOrPropertyWithValue("seatCount", 5);
      });
    }
  }

  private <T> T getAttr(Object obj, String name) throws Throwable {
    final var cls = obj instanceof Class<?>
        ? (Class<?>) obj
        : obj.getClass();

    final var instance = cls == obj
        ? obj
        : null;

    final var field = cls.getDeclaredField(name);
    field.setAccessible(true);

    @SuppressWarnings("unchecked")
    final var value = (T) field.get(instance);
    return value;
  }

  private void setAttr(Object obj, String name, Object value) throws Throwable {
    final var field = obj.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(obj, value);
  }

  private Object callMethod(Object obj, String method, Object... args) throws Throwable {
    final var cls = obj instanceof Class<?>
        ? (Class<?>) obj
        : obj.getClass();

    // Enforce reflective access to be allowed.
    cls.getModule().addOpens(cls.getPackageName(), getClass().getModule());
    cls.getModule().addExports(cls.getPackageName(), getClass().getModule());

    var methodObj = Stream.of(cls.getMethods())
        .filter(m -> m.getName().equals(method))
        .findAny()
        .orElseThrow();

    // Allow access to non-public members.
    methodObj.setAccessible(true);

    return obj instanceof Class<?>
        ? methodObj.invoke(null, args)
        : methodObj.invoke(obj, args);
  }
}
