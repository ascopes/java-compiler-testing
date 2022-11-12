/*
 * Copyright (C) 2022 - 2022 Ashley Scopes
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

import io.github.ascopes.jct.compilers.impl.JavacJctCompilerImpl;
import io.github.ascopes.jct.junit.JavacCompilerTest.JavacCompilersProvider;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Annotation that can be applied to a {@link org.junit.jupiter.params.ParameterizedTest} to enable
 * passing in a range of {@link JavacJctCompilerImpl} instances with specific configured versions
 * as the  first parameter.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
@ArgumentsSource(JavacCompilersProvider.class)
@Documented
@Inherited
@ParameterizedTest(name = "for compiler \"{0}\"")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
public @interface JavacCompilerTest {

  /**
   * Minimum version to use (inclusive).
   */
  int minVersion() default Integer.MIN_VALUE;

  /**
   * Maximum version to use (inclusive).
   */
  int maxVersion() default Integer.MAX_VALUE;

  /**
   * Whether we need to support modules or not.
   *
   * <p>Setting this to true will skip any versions of the compiler that do not support JPMS
   * modules.
   *
   * @return {@code true} if we need to support modules, or {@code false} if we do not.
   */
  boolean modules() default false;

  /**
   * Argument provider for the {@link JavacCompilerTest} annotation.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  final class JavacCompilersProvider
      extends AbstractCompilersProvider
      implements AnnotationConsumer<JavacCompilerTest> {

    JavacCompilersProvider() {
      super(
          version -> new JavacJctCompilerImpl("Javac " + version).release(version),
          8,
          9,
          JavacJctCompilerImpl.getLatestSupportedVersionInt()
      );
    }

    @Override
    public void accept(JavacCompilerTest javacCompilers) {
      configure(
          javacCompilers.minVersion(),
          javacCompilers.maxVersion(),
          javacCompilers.modules()
      );
    }
  }
}
