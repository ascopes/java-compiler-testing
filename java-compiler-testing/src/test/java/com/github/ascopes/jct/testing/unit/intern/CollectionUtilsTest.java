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

package com.github.ascopes.jct.testing.unit.intern;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.ascopes.jct.intern.CollectionUtils;
import com.github.ascopes.jct.testing.helpers.StaticClassTestTemplate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CollectionUtils} tests.
 *
 * @author Ashley Scopes
 */
class CollectionUtilsTest implements StaticClassTestTemplate {

  @Override
  public Class<?> getTypeBeingTested() {
    return CollectionUtils.class;
  }

  @DisplayName("combineOneOrMore(T, T...) returns the expected value")
  @Test
  void combineOneOrMoreReturnsTheExpectedValue() {
    var foo = new Object();
    var bar = new Object();
    var baz = new Object();
    var bork = new Object();

    assertThat(CollectionUtils.combineOneOrMore(foo))
        .isEqualTo(List.of(foo));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar))
        .isEqualTo(List.of(foo, bar));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar, baz))
        .isEqualTo(List.of(foo, bar, baz));

    assertThat(CollectionUtils.combineOneOrMore(foo, bar, baz, bork))
        .isEqualTo(List.of(foo, bar, baz, bork));
  }

  @DisplayName("combineTwoOrMore(T, T...) returns the expected value")
  @Test
  void combineTwoOrMoreReturnsTheExpectedValue() {
    var foo = new Object();
    var bar = new Object();
    var baz = new Object();
    var bork = new Object();
    var qux = new Object();

    assertThat(CollectionUtils.combineTwoOrMore(foo, bar))
        .isEqualTo(List.of(foo, bar));

    assertThat(CollectionUtils.combineTwoOrMore(foo, bar, baz))
        .isEqualTo(List.of(foo, bar, baz));

    assertThat(CollectionUtils.combineTwoOrMore(foo, bar, baz, bork))
        .isEqualTo(List.of(foo, bar, baz, bork));

    assertThat(CollectionUtils.combineTwoOrMore(foo, bar, baz, bork, qux))
        .isEqualTo(List.of(foo, bar, baz, bork, qux));
  }
}
