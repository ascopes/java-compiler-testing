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
package io.github.ascopes.jct.compilers;

/**
 * Function representing a configuration operation that can be applied to a compiler.
 *
 * <p>This can allow encapsulating common configuration logic across tests into a single place.
 *
 * <p>Implementations of this interface should declare the exception type that the implementation
 * can throw when invoked. If this is not a checked exception, or no case exists where an exception
 * could be thrown, then this can be set to {@link RuntimeException}.
 *
 * <p>The following demonstrates an example usage of this interface. The implementation configures
 * a specific annotation processor and sets an annotation processor flag.
 *
 * <pre><code>
 * class MyAnnotationProcessorConfigurer implements JctCompilerConfigurer&lt;RuntimeException&gt; {
 *    {@literal @Override}
 *    public void configure(JctCompiler compiler) {
 *      compiler
 *          .addAnnotationProcessors(new MyAnnotationProcessor())
 *          .addAnnotationProcessorOptions("MyAnnotationProcessor.debug=true");
 *    }
 * }
 * </code></pre>
 *
 * <p>...tests can then make use of this configurer directly:
 *
 * <pre><code>
 *    {@literal @Test}
 *    void theCompilationSucceedsAsExpected() {
 *      try (var workspace = Workspaces.newWorkspace()) {
 *        // Given
 *        ...
 *
 *        var compiler = JctCompilers
 *            .newPlatformCompiler()
 *            .release(17)
 *            .configure(new MyAnnotationProcessorConfigurer());
 *
 *        // When
 *        var compilation = compiler.compile(workspace);
 *
 *        // Then
 *        ...
 *      }
 *    }
 * </code></pre>
 *
 * <p>Since this is a functional interface, configurers can be lambda expressions, anonymous
 * objects, or method references.
 *
 * <pre><code>
 *   compiler
 *      .configure(c -&gt; c.release(11))
 *      .configure(this::configureFailures);
 * </code></pre>
 *
 * <p>The JUnit support allows for specifying these configurers in an annotation instead. This
 * will apply the configurer before passing it to the test as a parameter:
 *
 * <pre><code>
 *   {@literal @JavacCompilersTest(configurers = {MyAnnotationProcessorConfigurer.class})}
 *   void theCompilationSucceedsAsExpected(JctCompiler compiler) {
 *     // ...
 *   }
 * </code></pre>
 *
 * <p>Note that in this case, the configurer must be an outer class or a static nested class
 * rather than a Lambda expression, anonymous class, class instance, or nested class. It must also
 * have a single public no-arguments constructor in order to be accessible.
 *
 * @param <E> the exception that may be thrown by the configurer.
 * @author Ashley Scopes
 * @since 0.0.1
 */
@FunctionalInterface
public interface JctCompilerConfigurer<E extends Exception> {

  /**
   * Apply configuration logic to the given compiler.
   *
   * @param compiler the compiler.
   * @throws E any exception that may be thrown by the configurer.
   */
  void configure(JctCompiler compiler) throws E;
}
