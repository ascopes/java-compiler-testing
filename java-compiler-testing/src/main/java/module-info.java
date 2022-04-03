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

module com.github.ascopes.jct {
  requires transitive java.compiler;
  requires java.management;

  requires ecj;
  requires jimfs;
  requires me.xdrop.fuzzywuzzy;
  requires org.apiguardian.api;
  requires transitive org.assertj.core;
  requires org.reflections;
  requires org.slf4j;

  exports com.github.ascopes.jct.assertions;
  exports com.github.ascopes.jct.compilations;
  exports com.github.ascopes.jct.compilers;
  exports com.github.ascopes.jct.diagnostics;
  exports com.github.ascopes.jct.paths;

  // Testing access only.
  exports com.github.ascopes.jct.intern to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.assertions to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.compilations to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.compilers to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.diagnostics to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.paths to com.github.ascopes.jct.testing;
  opens com.github.ascopes.jct.intern to com.github.ascopes.jct.testing;
}
