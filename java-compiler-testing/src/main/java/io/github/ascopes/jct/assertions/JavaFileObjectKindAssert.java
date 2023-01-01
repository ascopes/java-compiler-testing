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
package io.github.ascopes.jct.assertions;

import static org.assertj.core.api.Assertions.assertThat;

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
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public final class JavaFileObjectKindAssert
    extends AbstractEnumAssert<JavaFileObjectKindAssert, Kind> {

  /**
   * Initialize this assertion type.
   *
   * @param value the value to assert on.
   */
  public JavaFileObjectKindAssert(Kind value) {
    super(value, JavaFileObjectKindAssert.class, "kind");
  }

  /**
   * Assert that the kind is a {@link Kind#SOURCE}.
   *
   * @return this assertion object.
   */
  public JavaFileObjectKindAssert isSource() {
    return isOneOf(Kind.SOURCE);
  }

  /**
   * Assert that the kind is a {@link Kind#CLASS}.
   *
   * @return this assertion object.
   */
  public JavaFileObjectKindAssert isClass() {
    return isOneOf(Kind.CLASS);
  }

  /**
   * Assert that the kind is an {@link Kind#HTML HTML source}.
   *
   * @return this assertion object.
   */
  public JavaFileObjectKindAssert isHtml() {
    return isOneOf(Kind.HTML);
  }

  /**
   * Assert that the kind is {@link Kind#OTHER some other unknown kind}.
   *
   * @return this assertion object.
   */
  public JavaFileObjectKindAssert isOther() {
    return isOneOf(Kind.OTHER);
  }

  /**
   * Perform an assertion on the file extension of the kind.
   *
   * @return the assertions for the file extension of the kind.
   */
  public AbstractStringAssert<?> extension() {
    return assertThat(actual.extension);
  }
}
