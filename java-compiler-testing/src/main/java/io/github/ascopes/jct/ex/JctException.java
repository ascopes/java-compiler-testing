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
package io.github.ascopes.jct.ex;

import org.jspecify.annotations.Nullable;

/**
 * Base for any exceptions thrown by JCT.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public abstract class JctException extends RuntimeException {

  /**
   * Initialise the exception with a message.
   *
   * @param message the message to initialise the exception with.
   */
  JctException(String message) {
    super(message);
  }

  /**
   * Initialise the exception with a message and a cause.
   *
   * @param message the message to initialise the exception with.
   * @param cause   the cause of the exception (or {@code null} if no cause exists).
   */
  JctException(String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
