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

import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Argument provider for the {@link EcjCompilerTest} annotation.
 *
 * <p>Until Java 11 support is dropped, this is a reflective harness around
 * {@code EcjCompilersProviderJava17} which may not be on the classpath if the JVM in use
 * is older than Java 17. Once Java 11 support is dropped, this class can be replaced with
 * that harness directly.
 *
 * @author Ashley Scopes
 * @since TBC
 */
final class EcjCompilersProvider implements ArgumentsProvider, AnnotationConsumer<EcjCompilerTest> {

  private @Nullable Object impl;

  EcjCompilersProvider() {
    try {
      impl = getClass().getClassLoader()
          .loadClass(getClass().getPackageName() + ".EcjCompilersProviderJava17")
          .getConstructor()
          .newInstance();
    } catch (ReflectiveOperationException ex) {
      impl = null;
    }
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return impl == null
        ? Stream.empty()
        : invokeMethodOnImpl(impl, "provideArguments", context);
  }

  @Override
  public void accept(EcjCompilerTest annotation) {
    if (impl != null) {
      invokeMethodOnImpl(impl, "accept", annotation);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T invokeMethodOnImpl(Object impl, String name, Object... args) {
    try {
      return (T) Stream.of(impl.getClass().getMethods())
          .filter(m -> m.getName().equals(name))
          .findFirst()
          .orElseThrow()
          .invoke(args);
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException("Fatal error accessing ECJ frontend internals", ex);
    }
  }
}
