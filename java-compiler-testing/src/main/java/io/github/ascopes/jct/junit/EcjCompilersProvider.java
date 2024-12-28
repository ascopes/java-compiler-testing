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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.impl.EcjJctCompilerImpl;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Argument provider for the {@link EcjCompilerTest} annotation.
 *
 * @author Ashley Scopes
 * @since 5.0.0
 */
public final class EcjCompilersProvider extends AbstractCompilersProvider
    implements AnnotationConsumer<EcjCompilerTest> {

  /**
   * Initialise the provider.
   *
   * <p>This is only visible for testing purposes, users should have no need to
   * initialise this class directly.
   */
  EcjCompilersProvider() {
    // Visible for testing only.
  }

  @Override
  protected JctCompiler initializeNewCompiler() {
    return new EcjJctCompilerImpl();
  }

  @Override
  protected int minSupportedVersion() {
    return EcjJctCompilerImpl.getEarliestSupportedVersionInt();
  }

  @Override
  protected int maxSupportedVersion() {
    return EcjJctCompilerImpl.getLatestSupportedVersionInt();
  }

  @Override
  public void accept(EcjCompilerTest annotation) {
    var min = annotation.minVersion();
    var max = annotation.maxVersion();
    var configurers = annotation.configurers();
    var versioning = annotation.versionStrategy();
    configure(min, max, configurers, versioning);
  }
}
