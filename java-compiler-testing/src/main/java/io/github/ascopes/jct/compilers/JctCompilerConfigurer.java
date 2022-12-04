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
package io.github.ascopes.jct.compilers;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Function representing a configuration operation that can be applied to a compiler.
 *
 * <p>This can allow encapsulating common configuration logic across tests into a single place.
 *
 * <p>If your configurer should not throw checked exceptions, you can just use
 * {@link JctCompilerConfigurer.JctSimpleCompilerConfigurer the simple configurer interface}
 * instead.
 *
 * @param <T> the exception that may be thrown by the configurer.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface JctCompilerConfigurer<T extends Exception> {

  /**
   * Apply configuration logic to the given compiler.
   *
   * @param compiler the compiler.
   * @throws T any exception that may be thrown by the configurer.
   */
  void configure(JctCompiler<?, ?> compiler) throws T;

  /**
   * A compiler configurer interface that has no type parameters and cannot throw checked
   * exceptions.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.EXPERIMENTAL)
  @FunctionalInterface
  interface JctSimpleCompilerConfigurer extends JctCompilerConfigurer<RuntimeException> {

    @Override
    void configure(JctCompiler<?, ?> compiler);
  }
}
