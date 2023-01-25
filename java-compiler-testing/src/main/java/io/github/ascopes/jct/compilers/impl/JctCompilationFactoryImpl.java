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
import io.github.ascopes.jct.utils.IterableUtils;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a compilation factory that performs the actual compilation of user
 * provided sources and configurations from the JCT API descriptors.
 *
 * @author Ashley Scopes
 * @since 0.0.1 (0.0.1-M7)
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@Immutable
@ThreadSafe
public final class JctCompilationFactoryImpl implements JctCompilationFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctCompilationFactoryImpl.class);

  private final JctCompiler<?, ?> compiler;

  public JctCompilationFactoryImpl(JctCompiler<?, ?> compiler) {
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
      return createCheckedCompilation(flags, fileManager, jsr199Compiler, classNames);
    } catch (JctCompilerException ex) {
      // Fall through, do not rewrap these.
      throw ex;
    } catch (Exception ex) {
      throw new JctCompilerException(
          "Failed to perform compilation, an unexpected exception was raised", ex
      );
    }
  }

  private JctCompilation createCheckedCompilation(
      List<String> flags,
      JctFileManager fileManager,
      JavaCompiler jsr199Compiler,
      @Nullable Collection<String> classNames
  ) throws Exception {
    var compilationUnits = findCompilationUnits(fileManager);

    // Do not close stdout, it breaks test engines, especially IntellIJ.
    @WillNotClose
    var writer = new TeeWriter(new OutputStreamWriter(System.out, compiler.getLogCharset()));

    var diagnosticListener = new TracingDiagnosticListener<>(
        compiler.getDiagnosticLoggingMode() != LoggingMode.DISABLED,
        compiler.getDiagnosticLoggingMode() == LoggingMode.STACKTRACES
    );

    var task = jsr199Compiler.getTask(
        writer,
        fileManager,
        diagnosticListener,
        flags,
        classNames,
        compilationUnits
    );

    var processors = compiler.getAnnotationProcessors();
    if (!processors.isEmpty()) {
      task.setProcessors(processors);
    }

    LOGGER.info("Starting compilation");

    var start = System.nanoTime();
    var success = requireNonNull(
        task.call(), "Compiler task .call() method returned null unexpectedly!"
    );
    var delta = (System.nanoTime() - start) / 1_000_000L;

    LOGGER
        .atInfo()
        .setMessage("Compilation {} after approximately {}ms")
        .addArgument(() -> success ? "completed successfully" : "failed")
        .addArgument(delta)
        .log();

    return JctCompilationImpl
        .builder()
        .compilationUnits(compilationUnits)
        .fileManager(fileManager)
        .outputLines(writer.toString().lines().collect(toList()))
        .diagnostics(diagnosticListener.getDiagnostics())
        .success(success)
        .failOnWarnings(compiler.isFailOnWarnings())
        .build();
  }

  private Set<JavaFileObject> findCompilationUnits(JctFileManager fileManager) throws IOException {
    Collection<Location> locations = IterableUtils
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

    var objects = new HashSet<JavaFileObject>();

    for (var location : locations) {
      objects.addAll(fileManager.list(location, "", Set.of(Kind.SOURCE), true));
    }

    if (objects.isEmpty()) {
      throw new JctCompilerException(
          "No compilation units were found. Did you forget to add something?"
      );
    }

    return objects;
  }
}
