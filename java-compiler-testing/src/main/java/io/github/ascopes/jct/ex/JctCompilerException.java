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
package io.github.ascopes.jct.ex;

import javax.annotation.concurrent.NotThreadSafe;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Exception that is thrown if the compiler fails to run and throws an unhandled exception.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
@NotThreadSafe
public final class JctCompilerException extends JctException {

  /**
   * Initialize the error.
   *
   * @param message the error message.
   */
  public JctCompilerException(String message) {
    super(message);
  }

  /**
   * Initialize the error.
   *
   * @param message the error message.
   * @param cause   the cause of the error.
   */
  public JctCompilerException(String message, Throwable cause) {
    super(message, cause);
  }
}
