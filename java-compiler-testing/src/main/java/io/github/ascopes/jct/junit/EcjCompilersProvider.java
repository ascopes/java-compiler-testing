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
import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Argument provider for the {@link EcjCompilerTest} annotation.
 *
 * <p>This implementation attempts to be "aware" of whether ECJ can be loaded by the current JVM
 * or not. If it cannot be loaded, no tests will be emitted.
 *
 * @author Ashley Scopes
 * @since TBC
 */
final class EcjCompilersProvider
    extends AbstractCompilersProvider
    implements AnnotationConsumer<EcjCompilerTest> {

  private static final String ECJ_JCT_COMPILERS_IMPL_FQN
      = "io.github.ascopes.jct.compilers.impl.ecj.EcjJctCompilerImpl";

  private static final Logger log = LoggerFactory.getLogger(EcjCompilersProvider.class);

  private final ClassLoader classLoader;

  EcjCompilersProvider() {
    this(EcjCompilersProvider.class.getClassLoader());
  }

  EcjCompilersProvider(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void accept(EcjCompilerTest annotation) {
    tryLoadEcjClass()
        .ifPresentOrElse(cls -> configureForJdk17(annotation), this::configureForJdk11);
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return tryLoadEcjClass()
        .map(cls -> super.provideArguments(context))
        .orElseGet(Stream::empty);
  }

  @Override
  protected JctCompiler initializeNewCompiler() {
    try {
      return (JctCompiler) tryLoadEcjClass()
          .map(Class::getDeclaredConstructors)
          .map(Stream::of)
          .orElseThrow(this::ecjImplementationNotFound)
          .findFirst()
          .orElseThrow()
          .newInstance();
    } catch (Exception ex) {
      throw reflectionFailure("initialising ECJ frontend classes", ex);
    }
  }

  @Override
  protected int minSupportedVersion() {
    try {
      return (int) tryLoadEcjClass()
          .map(Class::getDeclaredMethods)
          .map(Stream::of)
          .orElseThrow(this::ecjImplementationNotFound)
          .filter(m -> Modifier.isStatic(m.getModifiers()))
          .filter(m -> m.getName().equals("getEarliestSupportedVersionInt"))
          .findFirst()
          .orElseThrow()
          .invoke(null);
    } catch (Exception ex) {
      throw reflectionFailure("get earliest supported language version for ECJ", ex);
    }
  }

  @Override
  protected int maxSupportedVersion() {
    try {
      return (int) tryLoadEcjClass()
          .map(Class::getDeclaredMethods)
          .map(Stream::of)
          .orElseThrow(this::ecjImplementationNotFound)
          .filter(m -> Modifier.isStatic(m.getModifiers()))
          .filter(m -> m.getName().equals("getLatestSupportedVersionInt"))
          .findFirst()
          .orElseThrow()
          .invoke(null);
    } catch (Exception ex) {
      throw reflectionFailure("get latest supported language version for ECJ", ex);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private Optional<Class<?>> tryLoadEcjClass() {
    try {
      return Optional.of(classLoader.loadClass(ECJ_JCT_COMPILERS_IMPL_FQN));
    } catch (ClassNotFoundException ex) {
      return Optional.empty();
    }
  }

  private NoClassDefFoundError ecjImplementationNotFound() {
    return new NoClassDefFoundError(
        "ECJ implementation not found (perhaps you are running on a Java version older "
            + "than Java 17?");
  }

  private IllegalStateException reflectionFailure(String description, Exception cause) {
    return new IllegalStateException(
        "Failed performing operation \"" + description + "\". This is a bug.",
        cause
    );
  }

  private void configureForJdk17(EcjCompilerTest annotation) {
    var min = annotation.minVersion();
    var max = annotation.maxVersion();
    var configurers = annotation.configurers();
    var versioning = annotation.versionStrategy();
    configure(min, max, configurers, versioning);
  }

  private void configureForJdk11() {
    log.info("ECJ tests will be skipped as your JDK does not support ECJ.");
  }
}

