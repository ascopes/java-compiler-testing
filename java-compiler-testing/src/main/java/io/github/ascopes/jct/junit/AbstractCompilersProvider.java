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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.Compiler;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * Base for defining a compiler-supplying arguments provider for Junit Jupiter parameterised test
 * support.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class AbstractCompilersProvider implements ArgumentsProvider {

  private final IntFunction<? extends Compiler<?, ?>> compilerFactory;
  private final int minCompilerVersionWithoutModules;
  private final int minCompilerVersionWithModules;
  private final int maxCompilerVersion;

  // Configured values by JUnit 5.
  private volatile int minVersion;
  private volatile int maxVersion;

  /**
   * Initialise this provider.
   *
   * @param compilerFactory                  a function taking a compiler version and returning the
   *                                         corresponding compiler.
   * @param minCompilerVersionWithoutModules the minimum version to allow when we don't require
   *                                         module support.
   * @param minCompilerVersionWithModules    the minimum version to allow when we require module
   *                                         support.
   * @param maxCompilerVersion               the maximum version of the compiler to support.
   */
  protected AbstractCompilersProvider(
      IntFunction<? extends Compiler<?, ?>> compilerFactory,
      int minCompilerVersionWithoutModules,
      int minCompilerVersionWithModules,
      int maxCompilerVersion
  ) {
    this.compilerFactory = compilerFactory;
    this.minCompilerVersionWithoutModules = minCompilerVersionWithoutModules;
    this.minCompilerVersionWithModules = minCompilerVersionWithModules;
    this.maxCompilerVersion = maxCompilerVersion;
    minVersion = 0;
    maxVersion = Integer.MAX_VALUE;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return IntStream
        .rangeClosed(minVersion, maxVersion)
        .mapToObj(compilerFactory)
        .map(Arguments::of);
  }

  /**
   * Configure this provider with parameters from annotations.
   *
   * @param min     the inclusive minimum compiler version to use.
   * @param max     the inclusive maximum compiler version to use.
   * @param modules whether the compiler version must support modules.
   */
  protected final void configure(int min, int max, boolean modules) {
    min = Math.max(min, modules ? minCompilerVersionWithModules : minCompilerVersionWithoutModules);
    max = Math.min(max, maxCompilerVersion);

    if (min > max) {
      throw new IllegalArgumentException(
          "Cannot set min version to a version higher than the max version"
      );
    }

    minVersion = min;
    maxVersion = max;
  }
}
