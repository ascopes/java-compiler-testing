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

import javax.tools.JavaFileObject;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * Assertions for {@link JavaFileObject Java file objects}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public final class JavaFileObjectAssert
    extends AbstractJavaFileObjectAssert<JavaFileObjectAssert, JavaFileObject> {

  /**
   * Create a new instance of this assertion object.
   *
   * @param actual the Java file object to assert upon.
   */
  public JavaFileObjectAssert(@Nullable JavaFileObject actual) {
    super(actual, JavaFileObjectAssert.class);
  }
}
