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
package io.github.ascopes.jct.assertions;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.BooleanAssert;
import org.assertj.core.api.IntegerAssert;
import org.assertj.core.api.StringAssert;

/**
 * Assertions to perform on a {@link StackTraceElement stack trace frame}.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 * @deprecated I have put up a pull request for AssertJ to support this functionality in AssertJ
 *     Core. Once this is merged, this class will be removed from this API.
 */
@API(status = Status.EXPERIMENTAL)
@Deprecated(forRemoval = true)
public class StackTraceElementAssert
    extends AbstractAssert<StackTraceElementAssert, StackTraceElement> {

  /**
   * Initialize this assertion object.
   *
   * @param actual the stacktrace element to assert upon.
   */
  public StackTraceElementAssert(StackTraceElement actual) {
    super(actual, StackTraceElementAssert.class);
  }

  /**
   * Get assertions for the filename of the stack trace frame.
   *
   * @return the assertions for the file name.
   */
  public StringAssert fileName() {
    return new StringAssert(actual.getFileName());
  }

  /**
   * Get assertions for the line number of the stack trace frame.
   *
   * <p>The line number may be empty if the method is a {@link #nativeMethod() native method}.
   *
   * @return the assertions for the line number.
   */
  public MaybeAssert<IntegerAssert, Integer> lineNumber() {
    // Null for irrelevant values is less surprising than a negative value.
    return new MaybeAssert<>(
        actual.getLineNumber() > 0
            ? actual.getLineNumber()
            : null,
        IntegerAssert::new
    ).describedAs("line number %s", actual.getLineNumber());
  }

  /**
   * Get assertions for the module name of the stack trace frame.
   *
   * <p>The value may be null if not present.
   *
   * @return the assertions for the module name.
   */
  public StringAssert moduleName() {
    return new StringAssert(actual.getModuleName());
  }

  /**
   * Get assertions for the module version of the stack trace frame.
   *
   * <p>The value may be null if not present.
   *
   * @return the assertions for the module version.
   */
  public StringAssert moduleVersion() {
    return new StringAssert(actual.getModuleVersion());
  }

  /**
   * Get assertions for the name of the classloader of the class in the stack trace frame.
   *
   * @return the assertions for the classloader name.
   */
  public StringAssert classLoaderName() {
    return new StringAssert(actual.getClassLoaderName());
  }

  /**
   * Get assertions for the class name of the stack trace frame.
   *
   * @return the assertions for the class name.
   */
  public StringAssert className() {
    return new StringAssert(actual.getClassName());
  }

  /**
   * Get assertions for the method name of the stack trace frame.
   *
   * @return the assertions for the method name.
   */
  public StringAssert methodName() {
    return new StringAssert(actual.getMethodName());
  }

  /**
   * Get assertions for whether the frame is for a native (JNI) method or not.
   *
   * @return the assertions for the method nativity.
   */
  public BooleanAssert nativeMethod() {
    return new BooleanAssert(actual.isNativeMethod());
  }
}
