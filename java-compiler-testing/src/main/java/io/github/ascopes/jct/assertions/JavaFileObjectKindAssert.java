/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Nullable;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractStringAssert;

/**
 * Assertions for an individual {@link Kind Java file object kind}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JavaFileObjectKindAssert
    extends AbstractEnumAssert<JavaFileObjectKindAssert, Kind> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  public JavaFileObjectKindAssert(@Nullable Kind value) {
    super(value, JavaFileObjectKindAssert.class);
  }

  /**
   * Assert that the kind is a {@link Kind#SOURCE}.
   *
   * @return this assertion object.
   * @throws AssertionError if the kind is null.
   */
  public JavaFileObjectKindAssert isSource() {
    return isAnyOf(Kind.SOURCE);
  }

  /**
   * Assert that the kind is a {@link Kind#CLASS}.
   *
   * @return this assertion object.
   * @throws AssertionError if the kind is null.
   */
  public JavaFileObjectKindAssert isClass() {
    return isAnyOf(Kind.CLASS);
  }

  /**
   * Assert that the kind is an {@link Kind#HTML HTML source}.
   *
   * @return this assertion object.
   * @throws AssertionError if the kind is null.
   */
  public JavaFileObjectKindAssert isHtml() {
    return isAnyOf(Kind.HTML);
  }

  /**
   * Assert that the kind is {@link Kind#OTHER some other unknown kind}.
   *
   * @return this assertion object.
   * @throws AssertionError if the kind is null.
   */
  public JavaFileObjectKindAssert isOther() {
    return isAnyOf(Kind.OTHER);
  }

  /**
   * Perform an assertion on the file extension of the kind.
   *
   * @return the assertions for the file extension of the kind.
   * @throws AssertionError if the kind is null.
   */
  public AbstractStringAssert<?> extension() {
    isNotNull();

    return assertThat(actual.extension);
  }
}
