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
package io.github.ascopes.jct.filemanagers;

/**
 * Options for how to handle logging on special internal components.
 *
 * <p>This primarily exists to support debugging the JCT library itself.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public enum LoggingMode {

  /**
   * Enable basic logging.
   */
  ENABLED,

  /**
   * Enable logging and include stacktraces in the logs for each entry.
   */
  STACKTRACES,

  /**
   * Do not log anything.
   */
  DISABLED,
}
