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
import io.github.ascopes.jct.compilers.impl.EcjJctCompilerImpl;
import io.github.ascopes.jct.utils.VisibleForTestingOnly;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Argument provider for the {@link EcjCompilerTest} annotation.
 *
 * <p>Note that this is highly experimental, and may break in strange and unexpected ways, or may
 * even be removed without notice. Use at your own risk.
 *
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = Status.EXPERIMENTAL)
public final class EcjCompilersProvider extends AbstractCompilersProvider
    implements AnnotationConsumer<EcjCompilerTest> {

  /**
   * Initialise the provider.
   *
   * <p>This is only visible for testing purposes, users should have no need to
   * initialise this class directly.
   */
  @VisibleForTestingOnly
  public EcjCompilersProvider() {
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
  public void accept(EcjCompilerTest javacCompilers) {
    // Super is needed here to prevent IntelliJ getting confused.
    super.configure(
        javacCompilers.minVersion(),
        javacCompilers.maxVersion(),
        javacCompilers.configurers(),
        javacCompilers.versionStrategy()
    );
  }
}
