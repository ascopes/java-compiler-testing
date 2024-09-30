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
package io.github.ascopes.jct.ex;

/**
 * Exception that is raised if an internal feature is not implemented.
 *
 * @author Ashley Scopes
 * @since 1.1.0
 */
public final class JctNotImplementedException extends JctException {

  /**
   * Initialise the exception.
   *
   * @param message the message to report.
   */
  public JctNotImplementedException(String message) {
    super(message);
  }
}
