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
package io.github.ascopes.jct.compilers.impl;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import io.github.ascopes.jct.compilers.JctCompilation;
import io.github.ascopes.jct.compilers.JctCompilationFactory;
import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.diagnostics.TeeWriter;
import io.github.ascopes.jct.diagnostics.TracingDiagnosticListener;
import io.github.ascopes.jct.ex.JctCompilerException;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import io.github.ascopes.jct.filemanagers.LoggingMode;
import io.github.ascopes.jct.filemanagers.PathFileObject;
import io.github.ascopes.jct.utils.IterableUtils;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a compilation factory that performs the actual compilation of user
 * provided sources and configurations from the JCT API descriptors.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class JctCompilationFactoryImpl implements JctCompilationFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctCompilationFactoryImpl.class);
  private static final String THE_ROOT_PACKAGE = "";
  
  private final JctCompiler compiler;

  public JctCompilationFactoryImpl(JctCompiler compiler) {
    this.compiler = compiler;
  }

  @Override
  public JctCompilation createCompilation(
      List<String> flags,
      JctFileManager fileManager,
      JavaCompiler jsr199Compiler,
      @Nullable Collection<String> classNames
  ) {
    try {
      var compilationUnits = findFilteredCompilationUnits(fileManager, classNames);

      if (compilationUnits.isEmpty()) {
        throw new JctCompilerException("No compilation units were found in the given workspace");
      }

      // Note: we do not close stdout, it breaks test engines, especially IntelliJ.
      var writer = TeeWriter.wrapOutputStream(System.out, compiler.getLogCharset());

      var diagnosticListener = new TracingDiagnosticListener<>(
          compiler.getDiagnosticLoggingMode() != LoggingMode.DISABLED,
          compiler.getDiagnosticLoggingMode() == LoggingMode.STACKTRACES
      );

      var task = jsr199Compiler.getTask(
          writer,
          fileManager,
          diagnosticListener,
          flags,
          null,
          compilationUnits
      );

      var processors = compiler.getAnnotationProcessors();
      if (!processors.isEmpty()) {
        task.setProcessors(processors);
      }

      task.setLocale(compiler.getLocale());

      LOGGER
          .atInfo()
          .setMessage("Starting compilation with {} (found {} compilation units)")
          .addArgument(compiler::getName)
          .addArgument(compilationUnits::size)
          .log();

      var start = System.nanoTime();
      var success = requireNonNull(
          task.call(),
          () -> "Compiler " + compiler.getName()
              + " task .call() method returned null unexpectedly!"
      );
      var delta = (System.nanoTime() - start) / 1_000_000L;

      // Ensure we commit the writer contents to the wrapped output stream in full.
      writer.flush();

      LOGGER
          .atInfo()
          .setMessage("Compilation with {} {} after approximately {}ms (roughly {} classes/sec)")
          .addArgument(compiler::getName)
          .addArgument(() -> success ? "completed successfully" : "failed")
          .addArgument(delta)
          .addArgument(() -> String.format("%.2f", 1000.0 * compilationUnits.size() / delta))
          .log();

      return JctCompilationImpl
          .builder()
          .arguments(flags)
          .compilationUnits(Set.copyOf(compilationUnits))
          .fileManager(fileManager)
          .outputLines(writer.getContent().lines().collect(toList()))
          .diagnostics(diagnosticListener.getDiagnostics())
          .success(success)
          .failOnWarnings(compiler.isFailOnWarnings())
          .build();

    } catch (JctCompilerException ex) {
      // Rethrow JctCompilerExceptions -- we don't want to wrap these again.
      throw ex;

    } catch (Exception ex) {
      throw new JctCompilerException(
          "Failed to perform compilation, an unexpected exception was raised", ex
      );
    }
  }

  private Collection<JavaFileObject> findFilteredCompilationUnits(
      JctFileManager fileManager,
      @Nullable Collection<String> classNames
  ) throws IOException {
    var compilationUnits = findCompilationUnits(fileManager);

    if (classNames == null) {
      return compilationUnits;
    }

    if (classNames.isEmpty()) {
      throw new JctCompilerException("The list of explicit class names to compile is empty");
    }

    return filterCompilationUnitsByBinaryNames(compilationUnits, classNames);
  }

  private Collection<JavaFileObject> findCompilationUnits(
      JctFileManager fileManager
  ) throws IOException {
    var locations = IterableUtils
        .flatten(fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH));

    if (locations.isEmpty()) {
      LOGGER.info(
          "No multi-module sources found, will use the source path to find classes to compile"
      );
      locations = List.of(StandardLocation.SOURCE_PATH);
    } else {
      LOGGER.info(
          "Multi-module sources found, will use the module source path to find classes to compile"
      );
    }

    // Use a linked hash set to retain order for consistency.
    var objects = new LinkedHashSet<JavaFileObject>();

    for (var location : locations) {
      objects.addAll(fileManager.list(location, THE_ROOT_PACKAGE, Set.of(Kind.SOURCE), true));
    }

    return objects;
  }

  private Collection<JavaFileObject> filterCompilationUnitsByBinaryNames(
      Collection<JavaFileObject> compilationUnits,
      Collection<String> classNames
  ) {
    var binaryNamesToCompilationUnits = compilationUnits
        .stream()
        // Assumption that we always use this class internally. Technically unsafe, but we don't
        // care too much as the implementation should conform to this anyway. We just cannot enforce
        // it due to covariance rules.
        .map(PathFileObject.class::cast)
        .filter(fo -> classNames.contains(fo.getBinaryName()))
        .collect(Collectors.toMap(PathFileObject::getBinaryName, JavaFileObject.class::cast));

    for (var className : classNames) {
      var compilationUnit = binaryNamesToCompilationUnits.get(className);
      if (compilationUnit == null) {
        throw new JctCompilerException("No compilation unit matching " + className 
            + " found in the provided sources");
      }
    }

    LOGGER.atDebug()
        .setMessage("Filtered {} candidate compilation units down to {} final compilation units")
        .addArgument(compilationUnits::size)
        .addArgument(binaryNamesToCompilationUnits::size)
        .log();

    return binaryNamesToCompilationUnits.values();
  }
}
