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

/**
 * Java compiler testing facilities.
 */
module io.github.ascopes.jct {
  requires java.base;
  requires java.compiler;
  requires java.management;

  requires ecj;
  requires jimfs;
  requires static jsr305;
  requires static org.apiguardian.api;
  requires static org.junit.jupiter.params;
  requires org.assertj.core;
  requires org.reflections;
  requires org.slf4j;

  exports io.github.ascopes.jct.assertions;
  exports io.github.ascopes.jct.compilers;
  exports io.github.ascopes.jct.compilers.ecj;
  exports io.github.ascopes.jct.compilers.javac;
  exports io.github.ascopes.jct.junit;
  exports io.github.ascopes.jct.jsr199;
  exports io.github.ascopes.jct.jsr199.containers;
  exports io.github.ascopes.jct.jsr199.diagnostics;
  exports io.github.ascopes.jct.paths;

  // Junit annotation support.
  opens io.github.ascopes.jct.junit;

  ////////////////////
  /// TESTING ONLY ///
  ////////////////////

  exports io.github.ascopes.jct.assertions.repr to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.assertions to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.assertions.repr to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.ecj to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.javac to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.jsr199 to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.jsr199.containers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.jsr199.diagnostics to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.paths to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.annotations to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.annotations to io.github.ascopes.jct.testing;
}
