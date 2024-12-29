/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.containers.ModuleContainerGroup;
import io.github.ascopes.jct.containers.OutputContainerGroup;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.UtilityClass;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import org.jspecify.annotations.Nullable;

/**
 * Helper class to provide fluent creation of assertions for compilations.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public final class JctAssertions extends UtilityClass {

  private JctAssertions() {
    // Disallow initialisation.
  }

  /**
   * Perform an assertion on a compilation.
   *
   * <p>This is a shorthand alias for {@link #assertThatCompilation(JctCompilation)}. If you are
   * using AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param compilation the compilation to assert on.
   * @return the assertion.
   */
  public static JctCompilationAssert assertThat(@Nullable JctCompilation compilation) {
    return assertThatCompilation(compilation);
  }

  /**
   * Perform an assertion on a module container group.
   *
   * <p>This is a shorthand alias for {@link #assertThatContainerGroup(ModuleContainerGroup)}. If
   * you are using AssertJ assertions in your tests with static imports, you may wish to use that
   * instead to prevent name conflicts.
   *
   * @param moduleContainerGroup the module container group to assert on.
   * @return the assertion.
   */
  public static ModuleContainerGroupAssert assertThat(
      @Nullable ModuleContainerGroup moduleContainerGroup
  ) {
    return assertThatContainerGroup(moduleContainerGroup);
  }

  /**
   * Perform an assertion on an output container group.
   *
   * <p>This is a shorthand alias for {@link #assertThatContainerGroup(OutputContainerGroup)}. If
   * you are using AssertJ assertions in your tests with static imports, you may wish to use that
   * instead to prevent name conflicts.
   *
   * @param outputContainerGroup the output container group to assert on.
   * @return the assertion.
   */
  public static OutputContainerGroupAssert assertThat(
      @Nullable OutputContainerGroup outputContainerGroup
  ) {
    return assertThatContainerGroup(outputContainerGroup);
  }

  /**
   * Perform an assertion on a package container group.
   *
   * <p>This is a shorthand alias for {@link #assertThatContainerGroup(PackageContainerGroup)}. If
   * you are using AssertJ assertions in your tests with static imports, you may wish to use that
   * instead to prevent name conflicts.
   *
   * @param packageContainerGroup the package container group to assert on.
   * @return the assertion.
   */
  public static PackageContainerGroupAssert assertThat(
      @Nullable PackageContainerGroup packageContainerGroup
  ) {
    return assertThatContainerGroup(packageContainerGroup);
  }

  /**
   * Perform an assertion on a diagnostic.
   *
   * <p>This is a shorthand alias for {@link #assertThatDiagnostic(TraceDiagnostic)}. If
   * you are using AssertJ assertions in your tests with static imports, you may wish to use that
   * instead to prevent name conflicts.
   *
   * @param diagnostic the diagnostic to assert on.
   * @return the assertion.
   */
  public static TraceDiagnosticAssert assertThat(
      @Nullable TraceDiagnostic<? extends JavaFileObject> diagnostic
  ) {
    return assertThatDiagnostic(diagnostic);
  }

  /**
   * Perform an assertion on a Java file object.
   *
   * <p>This is a shorthand alias for {@link #assertThatFileObject(JavaFileObject)}. If you are
   * using AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param fileObject the file object to assert on.
   * @return the assertion.
   */
  public static JavaFileObjectAssert assertThat(@Nullable JavaFileObject fileObject) {
    return assertThatFileObject(fileObject);
  }

  /**
   * Perform an assertion on a Path-based Java file object.
   *
   * <p>This is a shorthand alias for {@link #assertThatFileObject(PathFileObject)}. If you are
   * using AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param fileObject the file object to assert on.
   * @return the assertion.
   */
  public static PathFileObjectAssert assertThat(@Nullable PathFileObject fileObject) {
    return assertThatFileObject(fileObject);
  }

  /**
   * Perform an assertion on a diagnostic kind.
   *
   * <p>This is a shorthand alias for {@link #assertThatKind(Diagnostic.Kind)}. If you are using
   * AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param kind the diagnostic kind to assert on.
   * @return the assertion.
   */
  public static DiagnosticKindAssert assertThat(Diagnostic.@Nullable Kind kind) {
    return assertThatKind(kind);
  }

  /**
   * Perform an assertion on a Java file object kind.
   *
   * <p>This is a shorthand alias for {@link #assertThatKind(JavaFileObject.Kind)}. If you are
   * using AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param kind the Java file object kind to assert on.
   * @return the assertion.
   */
  public static JavaFileObjectKindAssert assertThat(JavaFileObject.@Nullable Kind kind) {
    return assertThatKind(kind);
  }

  /**
   * Perform an assertion on a location.
   *
   * <p>This is a shorthand alias for {@link #assertThatLocation(Location)}. If you are using
   * AssertJ assertions in your tests with static imports, you may wish to use that instead to
   * prevent name conflicts.
   *
   * @param location the location to assert on.
   * @return the assertion.
   */
  public static LocationAssert assertThat(@Nullable Location location) {
    return assertThatLocation(location);
  }

  /**
   * Perform an assertion on a compilation.
   *
   * @param compilation the compilation to assert on.
   * @return the assertion.
   */
  public static JctCompilationAssert assertThatCompilation(@Nullable JctCompilation compilation) {
    return new JctCompilationAssert(compilation);
  }

  /**
   * Perform an assertion on a module container group.
   *
   * @param moduleContainerGroup the module container group to assert on.
   * @return the assertion.
   */
  public static ModuleContainerGroupAssert assertThatContainerGroup(
      @Nullable ModuleContainerGroup moduleContainerGroup
  ) {
    return new ModuleContainerGroupAssert(moduleContainerGroup);
  }

  /**
   * Perform an assertion on an output container group.
   *
   * @param outputContainerGroup the output container group to assert on.
   * @return the assertion.
   */
  public static OutputContainerGroupAssert assertThatContainerGroup(
      @Nullable OutputContainerGroup outputContainerGroup
  ) {
    return new OutputContainerGroupAssert(outputContainerGroup);
  }

  /**
   * Perform an assertion on a package container group.
   *
   * @param packageContainerGroup the package container group to assert on.
   * @return the assertion.
   */
  public static PackageContainerGroupAssert assertThatContainerGroup(
      @Nullable PackageContainerGroup packageContainerGroup
  ) {
    return new PackageContainerGroupAssert(packageContainerGroup);
  }

  /**
   * Perform an assertion on a diagnostic.
   *
   * @param diagnostic the diagnostic to assert on.
   * @return the assertion.
   */
  public static TraceDiagnosticAssert assertThatDiagnostic(
      @Nullable TraceDiagnostic<? extends JavaFileObject> diagnostic
  ) {
    return new TraceDiagnosticAssert(diagnostic);
  }

  /**
   * Perform an assertion on a list of diagnostics.
   *
   * @param diagnostics the diagnostics to assert on.
   * @return the assertion.
   */
  public static TraceDiagnosticListAssert assertThatDiagnostics(
      @Nullable List<? extends TraceDiagnostic<? extends JavaFileObject>> diagnostics
  ) {
    return new TraceDiagnosticListAssert(diagnostics);
  }

  /**
   * Perform an assertion on a Java file object.
   *
   * @param fileObject the file object to assert on.
   * @return the assertion.
   */
  public static JavaFileObjectAssert assertThatFileObject(@Nullable JavaFileObject fileObject) {
    return new JavaFileObjectAssert(fileObject);
  }

  /**
   * Perform an assertion on a Path-based Java file object.
   *
   * @param fileObject the file object to assert on.
   * @return the assertion.
   */
  public static PathFileObjectAssert assertThatFileObject(@Nullable PathFileObject fileObject) {
    return new PathFileObjectAssert(fileObject);
  }

  /**
   * Perform an assertion on a diagnostic kind.
   *
   * @param kind the diagnostic kind to assert on.
   * @return the assertion.
   */
  public static DiagnosticKindAssert assertThatKind(Diagnostic.@Nullable Kind kind) {
    return new DiagnosticKindAssert(kind);
  }

  /**
   * Perform an assertion on a Java file object kind.
   *
   * @param kind the Java file object kind to assert on.
   * @return the assertion.
   */
  public static JavaFileObjectKindAssert assertThatKind(JavaFileObject.@Nullable Kind kind) {
    return new JavaFileObjectKindAssert(kind);
  }

  /**
   * Perform an assertion on a location.
   *
   * @param location the location to assert on.
   * @return the assertion.
   */
  public static LocationAssert assertThatLocation(@Nullable Location location) {
    return new LocationAssert(location);
  }
}

