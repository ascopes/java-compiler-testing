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

open module com.github.ascopes.jct.testing {
  requires awaitility;
  requires com.github.ascopes.jct;
  requires ecj;
  requires java.compiler;
  requires java.management;
  requires transitive net.bytebuddy;         // required for mockito to work with JPMS.
  requires transitive net.bytebuddy.agent;   // required for mockito to work with JPMS.
  requires transitive org.assertj.core;
  requires transitive org.junit.jupiter;
  requires transitive org.junit.platform.suite;
  requires org.mockito;
  requires org.mockito.junit.jupiter;
  requires org.slf4j;

  exports com.github.ascopes.jct.testing;
}