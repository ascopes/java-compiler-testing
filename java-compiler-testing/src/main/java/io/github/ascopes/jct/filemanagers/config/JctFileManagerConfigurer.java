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
package io.github.ascopes.jct.filemanagers.config;

import io.github.ascopes.jct.filemanagers.JctFileManager;

/**
 * Interface for a {@link JctFileManager} configurer.
 *
 * <p>Configurers are designed to be chained to enable applying sets of operation to file managers
 * during creation.
 *
 * <p>The configurer itself must return a {@link JctFileManager} instance. This will usually
 * be the same value that is passed in the input parameter, as most configurers will only
 * want to mutate an existing file manager. Configurers may instead opt to return a different
 * file manager as the result, enabling wrapping the input in proxies or delegating
 * implementations.
 *
 * <p>Configurers may also decide to not run at all by marking themselves as being disabled,
 * which will result in them being skipped by any configurer chain or file manager that
 * respects this attribute.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@FunctionalInterface
public interface JctFileManagerConfigurer {

  /**
   * Configure the file manager implementation.
   *
   * @param fileManager the file manager implementation.
   * @return the new file manager (this may be the same as the input file manager).
   */
  JctFileManager configure(JctFileManager fileManager);

  /**
   * Determine if this configurer is enabled or not.
   *
   * <p>Default implementations are enabled automatically unless otherwise
   * specified.
   *
   * @return {@code true} if enabled, {@code false} if disabled.
   */
  default boolean isEnabled() {
    return true;
  }
}
