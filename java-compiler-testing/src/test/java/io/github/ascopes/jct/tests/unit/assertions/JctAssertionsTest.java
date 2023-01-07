/*
 * Copyright (C) 2022 - 2023 Ashley Scopes
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
package io.github.ascopes.jct.tests.unit.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import io.github.ascopes.jct.assertions.DiagnosticKindAssert;
import io.github.ascopes.jct.assertions.JavaFileObjectAssert;
import io.github.ascopes.jct.assertions.JavaFileObjectKindAssert;
import io.github.ascopes.jct.assertions.JctCompilationAssert;
import io.github.ascopes.jct.assertions.JctAssertions;
import io.github.ascopes.jct.assertions.LocationAssert;
import io.github.ascopes.jct.assertions.ModuleContainerGroupAssert;
import io.github.ascopes.jct.assertions.OutputContainerGroupAssert;
import io.github.ascopes.jct.assertions.PackageContainerGroupAssert;
import io.github.ascopes.jct.assertions.PathFileObjectAssert;
import io.github.ascopes.jct.assertions.TraceDiagnosticAssert;
import io.github.ascopes.jct.assertions.TraceDiagnosticListAssert;
import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link JctAssertions} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("JctAssertions test")
class JctAssertionsTest {

  @DisplayName("Assertion helper methods return the expected result")
  @MethodSource("assertionCases")
  @ParameterizedTest(name = "{0} returns an instance of {3}")
  <T, A extends AbstractAssert<A, T>> void assertionHelperMethodsReturnTheExpectedResult(
      @SuppressWarnings("unused") String name,
      ThrowingFunction<T, A> method,
      T inputObject,
      Class<A> returnType
  ) throws Throwable {
    // When
    var result = method.apply(inputObject);

    // Then
    assertThat(result)
        .isInstanceOf(returnType)
        .satisfies(assertion -> assertion.isSameAs(inputObject));
  }

  static Stream<Arguments> assertionCases() {
    return Stream.of(
        arg("assertThat", JctCompilation.class, JctCompilationAssert.class),
        arg("assertThatCompilation", JctCompilation.class, JctCompilationAssert.class),
        arg("assertThat", ModuleContainerGroup.class, ModuleContainerGroupAssert.class),
        arg(
            "assertThatContainerGroup",
            ModuleContainerGroup.class,
            ModuleContainerGroupAssert.class
        ),
        arg("assertThat", OutputContainerGroup.class, OutputContainerGroupAssert.class),
        arg(
            "assertThatContainerGroup",
            OutputContainerGroup.class,
            OutputContainerGroupAssert.class
        ),
        arg("assertThat", PackageContainerGroup.class, PackageContainerGroupAssert.class),
        arg(
            "assertThatContainerGroup",
            PackageContainerGroup.class,
            PackageContainerGroupAssert.class
        ),
        arg("assertThat", TraceDiagnostic.class, TraceDiagnosticAssert.class),
        arg("assertThatDiagnostic", TraceDiagnostic.class, TraceDiagnosticAssert.class),
        arg("assertThatDiagnostics", List.class, TraceDiagnosticListAssert.class),
        arg("assertThat", JavaFileObject.class, JavaFileObjectAssert.class),
        arg("assertThatFileObject", JavaFileObject.class, JavaFileObjectAssert.class),
        arg("assertThat", PathFileObject.class, PathFileObjectAssert.class),
        arg("assertThatFileObject", PathFileObject.class, PathFileObjectAssert.class),
        arg("assertThat", Diagnostic.Kind.class, DiagnosticKindAssert.class),
        arg("assertThatKind", Diagnostic.Kind.class, DiagnosticKindAssert.class),
        arg("assertThat", JavaFileObject.Kind.class, JavaFileObjectKindAssert.class),
        arg("assertThatKind", JavaFileObject.Kind.class, JavaFileObjectKindAssert.class),
        arg("assertThat", Location.class, LocationAssert.class),
        arg("assertThatLocation", Location.class, LocationAssert.class)
    );
  }

  static <T, R> Arguments arg(
      String name,
      Class<T> inputType,
      Class<R> returnType
  ) {
    @SuppressWarnings("unchecked")
    ThrowingFunction<T, R> fn = input -> {
      var method = JctAssertions.class.getDeclaredMethod(name, inputType);
      return (R) method.invoke(null, input);
    };

    return arguments(
        name + "(" + inputType.getSimpleName() + ")",
        fn,
        mock(inputType),
        returnType
    );
  }

  @FunctionalInterface
  interface ThrowingFunction<T, R> {
    R apply(T t) throws Throwable;
  }
}
