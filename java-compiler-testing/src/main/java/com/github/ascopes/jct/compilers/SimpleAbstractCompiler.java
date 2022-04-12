/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.compilers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract compiler that compiles to a {@link SimpleCompilation} object.
 *
 * @param <A> the type of the class extending this class.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
public abstract class SimpleAbstractCompiler<A extends SimpleAbstractCompiler<A>>
    extends AbstractCompiler<A, SimpleCompilation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAbstractCompiler.class);

  private final Supplier<JavaCompiler> compilerSupplier;

  /**
   * Initialize this compiler.
   *
   * @param compilerSupplier the supplier of the compiler implementation to use internally.
   */
  protected SimpleAbstractCompiler(Supplier<JavaCompiler> compilerSupplier) {
    this.compilerSupplier = requireNonNull(compilerSupplier);
  }

  @Override
  protected final SimpleCompilation doCompile() throws IOException {
    var flags = buildFlags();
    var diagnosticListener = buildDiagnosticListener();

    try (var fileManager = applyLoggingToFileManager(buildJavaFileManager())) {
      var compilationUnits = discoverCompilationUnits(fileManager);
      LOGGER.debug("Discovered {} compilation units {}", compilationUnits.size(),
          compilationUnits);

      var writer = new TeeWriter(System.out);
      var task = buildCompilationTask(
          writer,
          fileManager,
          diagnosticListener,
          flags,
          compilationUnits
      );

      var result = runCompilationTask(task);
      var outputLines = writer.toString().lines().collect(Collectors.toList());

      return SimpleCompilation.builder()
          .warningsAsErrors(warningsAsErrors)
          .success(result)
          .outputLines(outputLines)
          .compilationUnits(Set.copyOf(compilationUnits))
          .diagnostics(diagnosticListener.getDiagnostics())
          .fileRepository(fileRepository)
          .build();
    }
  }

  protected final JavaCompiler createJsr199Compiler() {
    return compilerSupplier.get();
  }
}
