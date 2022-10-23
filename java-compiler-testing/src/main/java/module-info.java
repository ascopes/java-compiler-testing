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
/**
 * Java compiler testing facilities.
 */
module io.github.ascopes.jct {
  requires java.base;
  requires java.compiler;
  requires java.management;

  requires /* automatic */ jimfs;
  requires static jsr305;
  requires me.xdrop.fuzzywuzzy;
  requires static transitive org.apiguardian.api;
  requires static transitive org.junit.jupiter.params;
  requires /* automatic */ org.assertj.core;
  requires /* automatic */ org.slf4j;

  exports io.github.ascopes.jct.assertions;
  exports io.github.ascopes.jct.containers;
  exports io.github.ascopes.jct.compilers;
  exports io.github.ascopes.jct.compilers.javac;
  exports io.github.ascopes.jct.diagnostics;
  exports io.github.ascopes.jct.ex;
  exports io.github.ascopes.jct.junit;
  exports io.github.ascopes.jct.pathwrappers;

  // Junit annotation support.
  opens io.github.ascopes.jct.junit;

  ////////////////////
  /// TESTING ONLY ///
  ////////////////////

  opens io.github.ascopes.jct.annotations to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.assertions to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.compilers.javac to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.containers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.containers.impl to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.diagnostics to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.ex to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.pathwrappers to io.github.ascopes.jct.testing;
  opens io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;

  exports io.github.ascopes.jct.annotations to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.containers.impl to io.github.ascopes.jct.testing;
  exports io.github.ascopes.jct.utils to io.github.ascopes.jct.testing;
}
