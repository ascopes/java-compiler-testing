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

import io.github.ascopes.jct.workspaces.PathStrategy;
import io.github.ascopes.jct.workspaces.Workspace;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Annotation for a {@link Workspace} field in a test class. This will ensure it gets initialised
 * and closed correctly between tests.
 *
 * <p>Use static-fields to keep a workspace object alive for the duration of all the tests
 * (providing the same semantics as initialising and closing resources using the
 * {@link org.junit.jupiter.api.BeforeAll} and {@link org.junit.jupiter.api.AfterAll} annotations).
 * <p>
 * You must extend your test class with the {@link JctExtension} extension for this annotation to be
 * detected and handled.
 *
 * <p>Example usage:
 *
 * <pre><code>
 * {@literal @ExtendWith(JctExtension.class)}
 * class MyTest {
 *   {@literal @Managed}
 *   Workspace workspace;
 *
 *   {@literal @JavacCompilerTest}
 *   void myTest(JctCompiler compiler) {
 *     ...
 *     var compilation = compiler.compile(workspace);
 *     ...
 *   }
 * }
 * </code></pre>
 *
 * @author Ashley Scopes
 * @since 0.4.0
 */
@API(since = "0.4.0", status = Status.STABLE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Managed {

  /**
   * Get the path strategy to use for the workspace.
   *
   * @return the path strategy to use.
   */
  PathStrategy pathStrategy() default PathStrategy.RAM_DIRECTORIES;
}
