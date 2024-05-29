/*
 * Copyright (C) 2022 - 2024, the original author or authors.
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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.jspecify.annotations.Nullable;

/**
 * Assertions to perform on a {@link StackTraceElement stack trace frame}.
 *
 * <p>This type is a placeholder and will be replaced when AssertJ releases changes to
 * support assertions on stack traces.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(status = Status.EXPERIMENTAL)
public final class StackTraceElementAssert
    extends AbstractAssert<StackTraceElementAssert, StackTraceElement> {

  /**
   * Initialize this assertion object.
   *
   * @param actual the stacktrace element to assert upon.
   */
  @SuppressWarnings("DataFlowIssue")
  public StackTraceElementAssert(@Nullable StackTraceElement actual) {
    super(actual, StackTraceElementAssert.class);
  }

  /**
   * Get assertions for the filename of the stack trace frame.
   *
   * @return the assertions for the file name.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> fileName() {
    isNotNull();

    return assertThat(actual.getFileName())
        .as("file name");
  }

  /**
   * Get assertions for the line number of the stack trace frame.
   *
   * <p>The line number may be non-positive if the method is a
   * {@link #nativeMethod() native method}.
   *
   * @return the assertions for the line number.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractIntegerAssert<?> lineNumber() {
    isNotNull();

    return assertThat(actual.getLineNumber())
        .as("line number");
  }

  /**
   * Get assertions for the module name of the stack trace frame.
   *
   * <p>The value may be null if not present.
   *
   * @return the assertions for the module name.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> moduleName() {
    isNotNull();

    return assertThat(actual.getModuleName())
        .as("module name");
  }

  /**
   * Get assertions for the module version of the stack trace frame.
   *
   * <p>The value may be null if not present.
   *
   * @return the assertions for the module version.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> moduleVersion() {
    isNotNull();

    return assertThat(actual.getModuleVersion())
        .as("module version");
  }

  /**
   * Get assertions for the name of the classloader of the class in the stack trace frame.
   *
   * @return the assertions for the classloader name.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> classLoaderName() {
    isNotNull();

    return assertThat(actual.getClassLoaderName())
        .as("class loader name");
  }

  /**
   * Get assertions for the class name of the stack trace frame.
   *
   * @return the assertions for the class name.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> className() {
    isNotNull();

    return assertThat(actual.getClassName())
        .as("class name");
  }

  /**
   * Get assertions for the method name of the stack trace frame.
   *
   * @return the assertions for the method name.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractStringAssert<?> methodName() {
    isNotNull();

    return assertThat(actual.getMethodName())
        .as("method name");
  }

  /**
   * Get assertions for whether the frame is for a native (JNI) method or not.
   *
   * @return the assertions for the method nativity.
   * @throws AssertionError if the stack trace element is null.
   */
  public AbstractBooleanAssert<?> nativeMethod() {
    isNotNull();

    return assertThat(actual.isNativeMethod())
        .as("native method");
  }
}
