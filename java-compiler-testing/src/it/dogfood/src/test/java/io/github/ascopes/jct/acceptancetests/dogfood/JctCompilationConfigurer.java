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
package io.github.ascopes.jct.acceptancetests.dogfood;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import java.util.Locale;

/**
 * Configure the compiler to mimic the Maven settings for compiling JCT
 * that we use normally.
 *
 * @author Ashley Scopes
 */
public class JctCompilationConfigurer implements JctCompilerConfigurer<RuntimeException> {

  @Override
  public void configure(JctCompiler compiler) {
    compiler
        .failOnWarnings(false)
        .showWarnings(false)  // ignore spam about the testing module being hidden
        .locale(Locale.ENGLISH);
  }
}
