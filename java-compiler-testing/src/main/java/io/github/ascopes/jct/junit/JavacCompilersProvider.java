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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilers;
import io.github.ascopes.jct.compilers.impl.javac.JavacJctCompilerImpl;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Argument provider for the {@link JavacCompilerTest} annotation.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
final class JavacCompilersProvider extends AbstractCompilersProvider
    implements AnnotationConsumer<JavacCompilerTest> {

  /**
   * Initialise the provider.
   */
  JavacCompilersProvider() {
    // Nothing to do.
  }

  @Override
  protected JctCompiler initializeNewCompiler() {
    return JctCompilers.newPlatformCompiler();
  }

  @Override
  protected int minSupportedVersion() {
    return JavacJctCompilerImpl.getEarliestSupportedVersionInt();
  }

  @Override
  protected int maxSupportedVersion() {
    return JavacJctCompilerImpl.getLatestSupportedVersionInt();
  }

  @Override
  public void accept(JavacCompilerTest annotation) {
    var min = annotation.minVersion();
    var max = annotation.maxVersion();
    var configurers = annotation.configurers();
    var versioning = annotation.versionStrategy();
    configure(min, max, configurers, versioning);
  }
}
