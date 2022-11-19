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
package io.github.ascopes.acceptancetests.manifold

import io.github.ascopes.jct.compilers.JctCompiler
import org.junit.jupiter.api.condition.JRE

import static io.github.ascopes.jct.compilers.JctCompilerConfigurer.JctSimpleCompilerConfigurer
import static org.assertj.core.api.Assumptions.assumeThat

/**
 * Configurer that sets up Javac to invoke Manifold processors.
 */
final class ManifoldPluginConfigurer implements JctSimpleCompilerConfigurer {

  @Override
  void configure(JctCompiler compiler) {
    assumeThat(JRE.currentVersion())
        .as("Manifold accesses internal JRE components at runtime which breaks after JDK 15")
        .isLessThanOrEqualTo(JRE.JAVA_15)

    // TODO(ascopes): look into what is breaking this. Guess there is incompatibility somewhere.
    assumeThat(JRE.currentVersion())
        .as("Manifold triggers exceptions after JDK 11")
        .isLessThanOrEqualTo(JRE.JAVA_11)

    compiler.addCompilerOptions("-Xplugin:Manifold")
  }
}
