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

import io.github.ascopes.jct.compilers.JctCompiler;
import java.util.function.BiConsumer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Strategy for setting a version on a JUnit compiler annotation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.STABLE)
public enum VersionStrategy {

  /**
   * Set the {@link JctCompiler#release release}.
   *
   * <p>This will define the source and target version to use, as well as providing information
   * to the compiler about supported APIs. This is generally the strategy you want to be using.
   */
  RELEASE(
      (compiler, version) -> compiler
          .release(version)
          .name(compiler.getName() + " (release = Java " + version + ")")
  ),

  /**
   * Set the {@link JctCompiler#source} source}.
   *
   * <p>This only sets the source version for the code being parsed. The target version that
   * defines the class file bytecode version will be left to the compiler default (usually
   * Java 1.5 or Java 8).
   *
   * <p>Use {@link #SOURCE_AND_TARGET} to set both the source and target.
   */
  SOURCE(
      (compiler, version) -> compiler
          .source(version)
          .name(compiler.getName() + " (source = Java " + version + ")")
  ),

  /**
   * Set the {@link JctCompiler#target} target}.
   *
   * <p>This only sets the target version for aby generated class file bytecode. The source
   * code version will be left as the compiler default (usually Java 1.5 or Java 8).
   *
   * <p>Use {@link #SOURCE_AND_TARGET} to set both the source and target.
   */
  TARGET(
      (compiler, version) -> compiler
          .target(version)
          .name(compiler.getName() + " (target = Java " + version + ")")
  ),

  /**
   * Set the {@link JctCompiler#source source} and {@link JctCompiler#target target}.
   *
   * <p>This is similar to using {@link #RELEASE} but allows you to override the core libraries
   * being used by the compiler. Generally you will want to use {@link #RELEASE} instead, as
   * it is more robust.
   */
  SOURCE_AND_TARGET(
      (compiler, version) -> compiler
          .source(version)
          .target(version)
          .name(compiler.getName() + " (source and target = Java " + version + ")")
  );

  /**
   * The default version strategy that the library will prefer to use.
   *
   * <p>This defaults to the {@link #RELEASE} strategy.
   *
   * @since 1.0.1
   */
  @API(since = "1.0.1", status = Status.STABLE)
  public static final VersionStrategy DEFAULT = RELEASE;

  private final BiConsumer<JctCompiler, Integer> versionSetter;

  VersionStrategy(BiConsumer<JctCompiler, Integer> versionSetter) {
    this.versionSetter = versionSetter;
  }

  /**
   * Set the given version on the compiler, according to the strategy in use.
   *
   * @param compiler the compiler to configure.
   * @param version  the version to set.
   */
  public void configureCompiler(JctCompiler compiler, int version) {
    versionSetter.accept(compiler, version);
  }
}
