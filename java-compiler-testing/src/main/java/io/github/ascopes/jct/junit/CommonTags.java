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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.utils.UtilityClass;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Common tag strings used in annotations in this package.
 *
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = Status.INTERNAL)
final class CommonTags extends UtilityClass {
  static final String JAVA_COMPILER_TESTING_TEST = "java-compiler-testing-test";

  static final String ECJ_TEST = "ecj-test";
  static final String JAVAC_TEST = "javac-test";
}
