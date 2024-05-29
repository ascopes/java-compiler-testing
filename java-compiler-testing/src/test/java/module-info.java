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

import org.jspecify.annotations.NullMarked;

@NullMarked
open module io.github.ascopes.jct.testing {
  requires awaitility;
  requires transitive io.github.ascopes.jct;
  requires java.compiler;
  requires java.management;
  requires com.github.marschall.memoryfilesystem;
  requires me.xdrop.fuzzywuzzy;
  requires net.bytebuddy;         // required for mockito to work with JPMS.
  requires net.bytebuddy.agent;   // required for mockito to work with JPMS.
  requires org.assertj.core;
  requires org.hamcrest;
  requires static org.jspecify;
  requires transitive org.junit.jupiter.api;
  requires transitive org.junit.jupiter.engine;
  requires transitive org.junit.jupiter.params;
  requires transitive org.junit.platform.commons;
  requires transitive org.junit.platform.engine;
  requires transitive org.junit.platform.testkit;
  requires org.mockito;
  requires org.mockito.junit.jupiter;
  requires org.slf4j;

  // Used for MemoryFileSystemProviderImplTest$MemoryFileSystemUrlHandlerProviderTest
  uses java.net.spi.URLStreamHandlerProvider;
}
