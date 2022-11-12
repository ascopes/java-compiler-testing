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
open module io.github.ascopes.jct.testing {
  requires awaitility;
  requires transitive io.github.ascopes.jct;
  requires java.compiler;
  requires java.management;
  requires jimfs;
  requires net.bytebuddy;         // required for mockito to work with JPMS.
  requires net.bytebuddy.agent;   // required for mockito to work with JPMS.
  requires org.assertj.core;
  requires org.hamcrest;
  requires transitive org.junit.jupiter.api;
  requires transitive org.junit.jupiter.engine;
  requires transitive org.junit.jupiter.params;
  requires org.mockito;
  requires org.mockito.junit.jupiter;
  requires org.slf4j;
}
