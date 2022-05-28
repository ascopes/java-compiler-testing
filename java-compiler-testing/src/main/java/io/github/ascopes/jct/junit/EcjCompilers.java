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

package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.compilers.ecj.EcjCompiler;
import io.github.ascopes.jct.junit.EcjCompilers.EcjCompilersProvider;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Annotation that can be applied to a {@link org.junit.jupiter.params.ParameterizedTest} to enable
 * passing in a range of {@link EcjCompiler} instances with specific configured versions as the
 * first parameter.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.EXPERIMENTAL)
@ArgumentsSource(EcjCompilersProvider.class)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface EcjCompilers {

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
   * Argument provider for the {@link EcjCompilers} annotation.
   *
   * @author Ashley Scopes
   * @since 0.0.1
   */
  @API(since = "0.0.1", status = Status.INTERNAL)
  final class EcjCompilersProvider extends AbstractCompilersProvider implements
      AnnotationConsumer<EcjCompilers> {

    EcjCompilersProvider() {
      super(
          version -> new EcjCompiler("ECJ " + version).release(version),
          8,
          9,
          EcjCompiler.getMaxVersion()
      );
    }

    @Override
    public void accept(EcjCompilers ecjCompilers) {
      configure(ecjCompilers.minVersion(), ecjCompilers.maxVersion(), ecjCompilers.modules());
    }
  }
}
