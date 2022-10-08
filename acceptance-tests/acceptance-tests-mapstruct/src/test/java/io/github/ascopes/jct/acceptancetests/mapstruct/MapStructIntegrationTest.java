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
package io.github.ascopes.jct.acceptancetests.mapstruct;

import static io.github.ascopes.jct.assertions.JctAssertions.assertThatCompilation;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.ascopes.jct.compilers.Compilable;
import io.github.ascopes.jct.junit.JavacCompilers;
import io.github.ascopes.jct.paths.RamPath;
import java.nio.file.Path;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;

@DisplayName("MapStruct integration tests")
class MapStructIntegrationTest {

  @DisplayName("MapStruct generates expected mapping code")
  @JavacCompilers
  @ParameterizedTest(name = "for {0}")
  void mapStructGeneratesExpectedMappingCode(Compilable<?, ?> compiler)
      throws ReflectiveOperationException {
    // Given
    var sources = RamPath
        .createPath("sources")
        .copyTreeFrom(
            Path.of("src", "test", "resources", "code"),
            "io/github/ascopes/jct/acceptancetests/mapstruct"
        );

    var compilation = compiler
        .addSourcePath(sources)
        .compile();

    assertThatCompilation(compilation).isSuccessfulWithoutWarnings();

    var classLoader = compilation.getFileManager().getClassLoader(StandardLocation.CLASS_OUTPUT);

    var packageName = "io.github.ascopes.jct.acceptancetests.mapstruct.";
    var carTypeClass = classLoader.loadClass(packageName + "CarType");
    var carClass = classLoader.loadClass(packageName + "Car");
    var carMapperClass = classLoader.loadClass(packageName + "CarMapper");

    var carType = carTypeClass.getField("HATCHBACK").get(null);
    var car = carClass.getConstructor().newInstance();
    carClass.getMethod("setMake", String.class).invoke(car, "VW Polo");
    carClass.getMethod("setType", carTypeClass).invoke(car, carType);
    carClass.getMethod("setNumberOfSeats", int.class).invoke(car, 5);

    var carMapper = carMapperClass.getField("INSTANCE").get(null);
    var carDto = carMapperClass.getDeclaredMethod("carToCarDto", carClass)
        .invoke(carMapper, car);

    assertSoftly(softly -> {
      softly.assertThat(carDto).hasFieldOrPropertyWithValue("make", "VW Polo");
      softly.assertThat(carDto).hasFieldOrPropertyWithValue("type", "HATCHBACK");
      softly.assertThat(carDto).hasFieldOrPropertyWithValue("seatCount", 5);
    });
  }
}
