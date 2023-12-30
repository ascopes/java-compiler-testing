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
  private static final String ROOT_PACKAGE = "";
  
  private final JctCompiler compiler;

  public JctCompilationFactoryImpl(JctCompiler compiler) {
    this.compiler = requireNonNull(compiler);
  }

  @Override
  public JctCompilation createCompilation(
      List<String> flags,
      JctFileManager fileManager,
      JavaCompiler jsr199Compiler,
      @Nullable Collection<String> classNames
  ) {
    try {
      final var startPreparation = System.nanoTime();
      var compilationUnits = findFilteredCompilationUnits(fileManager, classNames);

      if (compilationUnits.isEmpty()) {
        throw new JctCompilerException("No compilation units were found in the given workspace");
      }

      // Note: we do not close stdout, it breaks test engines, especially IntelliJ.
      var writer = TeeWriter.wrapOutputStream(System.out, compiler.getLogCharset());

      var diagnosticListener = new TracingDiagnosticListener<>(
          /* enabled */ compiler.getDiagnosticLoggingMode() != LoggingMode.DISABLED,
          /* stackTraces */ compiler.getDiagnosticLoggingMode() == LoggingMode.STACKTRACES
      );

      // We work out the classes to annotation process rather than relying on the
      // compiler to do this, as we retain more control by doing so.
      var task = jsr199Compiler.getTask(
          writer,
          fileManager,
          diagnosticListener,
          flags,
          /* classes */ null,
          compilationUnits
      );

      var processors = compiler.getAnnotationProcessors();
      if (!processors.isEmpty()) {
        task.setProcessors(processors);
      }

      task.setLocale(compiler.getLocale());

      var preparationExecutionTimeMs = timeDeltaMs(startPreparation);

      LOGGER
          .atInfo()
          .setMessage("Starting compilation with {} (found {} compilation units in approx {}ms)")
          .addArgument(compiler::getName)
          .addArgument(compilationUnits::size)
          .addArgument(preparationExecutionTimeMs)
          .log();

      var startCompilation = System.nanoTime();
      var success = requireNonNull(
          task.call(),
          () -> "Compiler " + compiler.getName()
              + " task .call() method returned null unexpectedly!"
      );
      var compilationExecutionTimeMs = timeDeltaMs(startCompilation);

      // Ensure we commit the writer contents to the wrapped output stream in full.
      writer.flush();

      LOGGER
          .atInfo()
          .setMessage("Compilation with {} {} after approximately {}ms (roughly {} classes/sec)")
          .addArgument(compiler::getName)
          .addArgument(() -> success ? "completed successfully" : "failed")
          .addArgument(compilationExecutionTimeMs)
          .addArgument(() -> String.format(
              "%.2f", 
              (1000.0 * compilationUnits.size()) / compilationExecutionTimeMs
          ))
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

    } finally {
      // Compiler invovation is a pretty heavy operation, so we advise the JVM that
      // it might be a good idea to reclaim resources now rather than doing it later
      // during another compilation. This may help keep compiler invocation times
      // more consistent between test cases.
      System.gc();
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
    // If we use modules, we may have more than one module location to search for.
    var deepLocations = fileManager.listLocationsForModules(StandardLocation.MODULE_SOURCE_PATH);
    var locations = IterableUtils.flatten(deepLocations);

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
    var fileObjects = new LinkedHashSet<JavaFileObject>();
    var kinds = Set.of(Kind.SOURCE);

    for (var location : locations) {
      var nextFileObjects = fileManager.list(location, ROOT_PACKAGE, kinds, true);
      fileObjects.addAll(nextFileObjects);
    }

    return fileObjects;
  }

  private Collection<JavaFileObject> filterCompilationUnitsByBinaryNames(
      Collection<JavaFileObject> compilationUnits,
      Collection<String> classNames
  ) {
    var binaryNamesToCompilationUnits = compilationUnits
        .stream()
        .map(this::forceUpcastJavaFileObject)
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
        .setMessage("Filtered {} candidate compilation units down to {} via class whitelist {}")
        .addArgument(compilationUnits::size)
        .addArgument(binaryNamesToCompilationUnits::size)
        .addArgument(classNames)
        .log();

    return binaryNamesToCompilationUnits.values();
  }

  // We need to access the binary name of each file object. This forces us
  // to cast to PathFileObject in an unsafe way as the standard JavaFileObject
  // interface does not expose this information consistently.
  // All JCT implementations should be using PathFileObject types internally
  // anyway, so this should be fine as a hack for now. In the future I may decide
  // to add an additional set of methods to PathFileObject to expose searching
  // for PathFileObjects directly to prevent the cast back to JavaFileObject that
  // makes us need this hack.
  private PathFileObject forceUpcastJavaFileObject(JavaFileObject jfo) {
    assert jfo instanceof PathFileObject
        : "Unexpected state: JavaFileObject " + jfo + " was not a PathFileObject!";
    return (PathFileObject) jfo;
  }

  private static long timeDeltaMs(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }
}
