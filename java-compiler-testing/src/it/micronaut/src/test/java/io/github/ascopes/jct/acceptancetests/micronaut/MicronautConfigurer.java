/*
 * Copyright (C) 2022 - 2025, the original author or authors.
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
package io.github.ascopes.jct.acceptancetests.micronaut;

import io.github.ascopes.jct.compilers.JctCompiler;
import io.github.ascopes.jct.compilers.JctCompilerConfigurer;
import io.micronaut.annotation.processing.AggregatingTypeElementVisitorProcessor;
import io.micronaut.annotation.processing.BeanDefinitionInjectProcessor;
import io.micronaut.annotation.processing.ConfigurationMetadataProcessor;
import io.micronaut.annotation.processing.PackageConfigurationInjectProcessor;
import io.micronaut.annotation.processing.TypeElementVisitorProcessor;


/**
 * Micronaut annotation processor configurer.
 *
 * @author Ashley Scopes
 */
public class MicronautConfigurer implements JctCompilerConfigurer<RuntimeException> {

  @Override
  public void configure(JctCompiler compiler) {
    compiler.addAnnotationProcessors(
        new AggregatingTypeElementVisitorProcessor(),
        new BeanDefinitionInjectProcessor(),
        new ConfigurationMetadataProcessor(),
        new PackageConfigurationInjectProcessor(),
        new TypeElementVisitorProcessor()
    );
  }
}
