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

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

import io.github.ascopes.jct.jsr199.diagnostics.TraceDiagnostic;
import io.github.ascopes.jct.utils.IterableUtils;
import io.github.ascopes.jct.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractListAssert;

//@formatter:off
/**
 * Assertions for a list of diagnostics.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public class DiagnosticListAssert extends AbstractListAssert<
    DiagnosticListAssert,
    List<? extends TraceDiagnostic<? extends JavaFileObject>>,
    TraceDiagnostic<? extends JavaFileObject>,
    DiagnosticAssert
>
//@formatter:on
{

  /**
   * Initialize this assertion.
   *
   * @param traceDiagnostics the diagnostics to perform assertions on.
   */
  public DiagnosticListAssert(
      List<? extends TraceDiagnostic<? extends JavaFileObject>> traceDiagnostics
  ) {
    super(traceDiagnostics, DiagnosticListAssert.class);
    setCustomRepresentation(DiagnosticListRepresentation.getInstance());
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the {@link Kind#ERROR}
   * kind.
   *
   * @return the assertion object for {@link Kind#ERROR} diagnostics.
   */
  public DiagnosticListAssert errors() {
    return filteringBy(kind(Kind.ERROR));
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the {@link Kind#WARNING} or
   * {@link Kind#MANDATORY_WARNING} kind.
   *
   * @return the assertion object for {@link Kind#WARNING} and {@link Kind#MANDATORY_WARNING}
   *     diagnostics.
   */
  public DiagnosticListAssert warnings() {
    return filteringBy(kind(Kind.WARNING, Kind.MANDATORY_WARNING));
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the {@link Kind#WARNING}
   * kind.
   *
   * @return the assertion object for {@link Kind#WARNING} diagnostics.
   */
  public DiagnosticListAssert customWarnings() {
    return filteringBy(kind(Kind.WARNING));
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the
   * {@link Kind#MANDATORY_WARNING} kind.
   *
   * @return the assertion object for {@link Kind#MANDATORY_WARNING} diagnostics.
   */
  public DiagnosticListAssert mandatoryWarnings() {
    return filteringBy(kind(Kind.MANDATORY_WARNING));
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the {@link Kind#NOTE}
   * kind.
   *
   * @return the assertion object for {@link Kind#NOTE} diagnostics.
   */
  public DiagnosticListAssert notes() {
    return filteringBy(kind(Kind.NOTE));
  }

  /**
   * Get a {@link DiagnosticListAssert} across all diagnostics that have the {@link Kind#OTHER}
   * kind.
   *
   * @return the assertion object for {@link Kind#OTHER} diagnostics.
   */
  public DiagnosticListAssert others() {
    return filteringBy(kind(Kind.OTHER));
  }

  /**
   * Get a {@link DiagnosticListAssert} that contains diagnostics corresponding to any of the given
   * {@link Kind kinds}.
   *
   * @param kind      the first kind to match.
   * @param moreKinds additional kinds to match.
   * @return the assertion object for the filtered diagnostics.
   */
  public DiagnosticListAssert filterByKinds(Kind kind, Kind... moreKinds) {
    return filteringBy(kind(kind, moreKinds));
  }

  /**
   * Get a {@link DiagnosticListAssert} that contains diagnostics corresponding to none of the given
   * {@link Kind kinds}.
   *
   * @param kind      the first kind to ensure are not matched.
   * @param moreKinds additional kinds to ensure are not matched.
   * @return the assertion object for the filtered diagnostics.
   */
  public DiagnosticListAssert filterExceptKinds(Kind kind, Kind... moreKinds) {
    return filteringBy(not(kind(kind, moreKinds)));
  }

  /**
   * Assert that this list has no {@link Kind#ERROR} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoErrors() {
    return hasNoKinds(Kind.ERROR);
  }

  /**
   * Assert that this list has no {@link Kind#ERROR}, {@link Kind#WARNING}, or
   * {@link Kind#MANDATORY_WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoErrorsOrWarnings() {
    return hasNoKinds(Kind.ERROR, Kind.WARNING, Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#WARNING} or {@link Kind#MANDATORY_WARNING}
   * diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoWarnings() {
    return hasNoKinds(Kind.WARNING, Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoCustomWarnings() {
    return hasNoKinds(Kind.WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#MANDATORY_WARNING} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoMandatoryWarnings() {
    return hasNoKinds(Kind.MANDATORY_WARNING);
  }

  /**
   * Assert that this list has no {@link Kind#NOTE} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoNotes() {
    return hasNoKinds(Kind.NOTE);
  }

  /**
   * Assert that this list has no {@link Kind#OTHER} diagnostics.
   *
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoOtherDiagnostics() {
    return hasNoKinds(Kind.OTHER);
  }

  /**
   * Assert that this list has no diagnostics matching any of the given kinds.
   *
   * @param kind the first kind to check for.
   * @param moreKinds any additional kinds to check for.
   * @return this assertion object for further call chaining.
   */
  public DiagnosticListAssert hasNoKinds(Kind kind, Kind... moreKinds) {
    return filteringBy(kind(kind, moreKinds))
        .withFailMessage(() -> {
          var allKindsString = IterableUtils
              .asList(kind, moreKinds)
              .stream()
              .map(next -> next.name().toLowerCase(Locale.ROOT).replace('_', ' '))
              .collect(Collectors.collectingAndThen(
                  Collectors.toUnmodifiableList(),
                  names -> StringUtils.toWordedList(names, ", ", ", or ")
              ));

          return String.format("Expected no %s diagnostics", allKindsString);
        });
  }

  /**
   * Filter diagnostics by a given predicate and return an assertion object that applies to all
   * diagnostics that match that predicate.
   *
   * @param predicate the predicate to match.
   * @return the assertion object for the diagnostics that match.
   */
  public DiagnosticListAssert filteringBy(
      Predicate<TraceDiagnostic<? extends JavaFileObject>> predicate
  ) {
    requireNonNull(predicate, "predicate");

    return actual
        .stream()
        .filter(predicate)
        .collect(Collectors.collectingAndThen(
            Collectors.toUnmodifiableList(),
            DiagnosticListAssert::new
        ));
  }

  @Override
  protected DiagnosticAssert toAssert(
      TraceDiagnostic<? extends JavaFileObject> value,
      String description
  ) {
    return new DiagnosticAssert(value);
  }

  @Override
  protected DiagnosticListAssert newAbstractIterableAssert(
      Iterable<? extends TraceDiagnostic<? extends JavaFileObject>> iterable
  ) {
    var list = new ArrayList<TraceDiagnostic<? extends JavaFileObject>>();
    iterable.forEach(list::add);
    return new DiagnosticListAssert(list);
  }

  private Predicate<TraceDiagnostic<? extends JavaFileObject>> kind(Kind kind, Kind... moreKinds) {
    return diagnostic -> {
      var actualKind = diagnostic.getKind();
      return actualKind.equals(kind) || Arrays.asList(moreKinds).contains(actualKind);
    };
  }
}
