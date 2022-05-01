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

package io.github.ascopes.jct.assertions;

import io.github.ascopes.jct.compilers.Compilation;
import io.github.ascopes.jct.compilers.TraceDiagnostic;
import io.github.ascopes.jct.paths.ModuleLocation;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

/**
 * Assertions to apply to compilation output.
 *
 * @param <C> the implementation of the compilation.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class CompilationAssert<C extends Compilation>
    extends AbstractObjectAssert<CompilationAssert<C>, C> {

  /**
   * Initialize this set of assertions.
   *
   * @param compilation the compilation type.
   */
  protected CompilationAssert(C compilation) {
    super(compilation, CompilationAssert.class);
  }

  /**
   * Assert that the compilation succeeded.
   *
   * @return this object.
   */
  public CompilationAssert<C> isSuccessful() {
    if (actual.isSuccessful()) {
      return myself;
    }

    Predicate<Kind> filter = actual.isFailOnWarnings()
        ? kind -> kind == Kind.ERROR || kind == Kind.MANDATORY_WARNING || kind == Kind.WARNING
        : kind -> kind == Kind.ERROR;

    var errors = actual
        .getDiagnostics()
        .stream()
        .filter(it -> filter.test(it.getKind()))
        .collect(Collectors.toList());

    if (errors.isEmpty()) {
      throw failureWithActualExpected(
          "failed",
          "succeeded",
          "Expected successful compilation but it failed without any error diagnostics"
      );
    }

    throw failureWithActualExpected(
        "failed",
        "succeeded",
        "Expected successful compilation but it failed with errors:%n%s",
        new DiagnosticCollectionRepresentation().toStringOf(errors)
    );
  }

  /**
   * Assert that the compilation succeeded without warnings or errors.
   *
   * @return this object.
   */
  public CompilationAssert<C> isSuccessfulWithoutWarnings() {
    isSuccessful();
    warnings().withFailMessage("Expected no warnings").isEmpty();
    mandatoryWarnings().withFailMessage("Expected no mandatory warnings").isEmpty();
    return myself;
  }

  /**
   * Assert that the compilation failed.
   *
   * @return this object.
   */
  public CompilationAssert<C> isFailure() {
    if (actual.isFailure()) {
      return myself;
    }

    throw failureWithActualExpected(
        "succeeded",
        "failed",
        "Expected failed compilation but it succeeded"
    );
  }

  /**
   * Get assertions to perform on the collection of diagnostics that were returned.
   *
   * @return the assertions to perform across the diagnostics.
   */
  public DiagnosticListAssert diagnostics() {
    return DiagnosticListAssert.assertThatDiagnostics(actual.getDiagnostics());
  }

  /**
   * Get assertions to perform on all diagnostics that match the given predicate.
   *
   * @param predicate the predicate to filter diagnostics by.
   * @return the assertions to perform across the filtered diagnostics.
   */
  public DiagnosticListAssert diagnostics(
      Predicate<? super TraceDiagnostic<? extends JavaFileObject>> predicate
  ) {
    return actual
        .getDiagnostics()
        .stream()
        .filter(predicate)
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            DiagnosticListAssert::assertThatDiagnostics
        ));
  }

  /**
   * Get assertions to perform on all error diagnostics.
   *
   * @return the assertions to perform across the error diagnostics.
   */
  public DiagnosticListAssert errors() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.ERROR);
  }

  /**
   * Get assertions to perform on all warning diagnostics.
   *
   * @return the assertions to perform across the warning diagnostics.
   */
  public DiagnosticListAssert warnings() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.WARNING);
  }

  /**
   * Get assertions to perform on all mandatory warning diagnostics.
   *
   * @return the assertions to perform across the warning diagnostics.
   */
  public DiagnosticListAssert mandatoryWarnings() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.MANDATORY_WARNING);
  }

  /**
   * Get assertions to perform on all note diagnostics.
   *
   * @return the assertions to perform across the note diagnostics.
   */
  public DiagnosticListAssert notes() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.NOTE);
  }

  /**
   * Get assertions to perform on all {@link Kind#OTHER}-kinded diagnostics.
   *
   * @return the assertions to perform across the {@link Kind#OTHER}-kinded diagnostics.
   */
  public DiagnosticListAssert otherDiagnostics() {
    return diagnostics(diagnostic -> diagnostic.getKind() == Kind.OTHER);
  }

  /**
   * Get assertions to perform on the compiler log output.
   *
   * @return the assertions to perform on the compiler log output.
   */
  public ListAssert<String> outputLines() {
    return Assertions.assertThat(actual.getOutputLines());
  }

  /**
   * Get assertions to perform on a given location.
   *
   * @param location the location to perform assertions on.
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert location(Location location) {
    var locationManager = actual
        .getFileRepository()
        .getManager(location)
        .orElse(null);

    return PathLocationManagerAssert.assertThatLocation(locationManager);
  }

  /**
   * Get assertions to perform on a given location of a module.
   *
   * @param location   the location to perform assertions on.
   * @param moduleName the module name within the location to perform assertions on.
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert location(Location location, String moduleName) {
    var locationManager = actual
        .getFileRepository()
        .getManager(new ModuleLocation(location, moduleName))
        .orElse(null);

    return PathLocationManagerAssert.assertThatLocation(locationManager);
  }

  /**
   * Perform assertions on the class output roots.
   *
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert classOutput() {
    return location(StandardLocation.CLASS_OUTPUT);
  }

  /**
   * Perform assertions on the class output roots for a given module name.
   *
   * @param moduleName the name of the module.
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert classOutput(String moduleName) {
    return location(StandardLocation.CLASS_OUTPUT, moduleName);
  }

  /**
   * Perform assertions on the native header outputs.
   *
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert nativeHeaders() {
    return location(StandardLocation.NATIVE_HEADER_OUTPUT);
  }

  /**
   * Perform assertions on the native header outputs for a given module name.
   *
   * @param moduleName the name of the module.
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert nativeHeaders(String moduleName) {
    return location(StandardLocation.NATIVE_HEADER_OUTPUT, moduleName);
  }

  /**
   * Perform assertions on the generated source outputs.
   *
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert generatedSources() {
    return location(StandardLocation.SOURCE_OUTPUT);
  }

  /**
   * Perform assertions on the generated source outputs for a given module name.
   *
   * @param moduleName the name of the module.
   * @return the assertions to perform.
   */
  public PathLocationManagerAssert generatedSources(String moduleName) {
    return location(StandardLocation.SOURCE_PATH, moduleName);
  }

  /**
   * Create a new assertion object.
   *
   * @param compilation the compilation to assert on.
   * @param <C>         the compilation type.
   * @return the compilation assertions to use.
   */
  public static <C extends Compilation> CompilationAssert<C> assertThatCompilation(C compilation) {
    return new CompilationAssert<>(compilation);
  }
}
