/*
 * Copyright (C) 2022 - 2023, the original author or authors.
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
package io.github.ascopes.jct.containers.impl;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.net.URL;
import java.net.URLClassLoader;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * An extension of the Java {@link URLClassLoader} that wraps around container groups.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
@SuppressWarnings("CommentedOutCode")
public final class PackageContainerGroupUrlClassLoader extends URLClassLoader {

  /**
   * Initialise this class loader.
   *
   * @param group the container group to use.
   */
  public PackageContainerGroupUrlClassLoader(PackageContainerGroup group) {
    super(
        "Packages within " + group.getLocation().getName(),
        extractUrls(group),
        ClassLoader.getSystemClassLoader()
    );
  }

  private static URL[] extractUrls(PackageContainerGroup group) {
    return group.getPackages()
        .stream()
        .map(Container::getPathRoot)
        .map(PathRoot::getUrl)
        .toArray(URL[]::new);
  }

  // TODO(ascopes): find a way to retain module information for modules that exist within this
  //   path. Currently we are discarding that information due to the nature of how URLClassLoader
  //   works internally.
  //
  // This would need something a bit more complicated to potentially mimic how the internal boot
  // classloaders currently work in the JVM.
  //
  // Module resolution for classloaders is a bit different to regular loading of classes.
  // As an example, this is how a classloader should be loading modules.
  //
  // var bootLayer = ModuleLayer.boot();
  //
  // var compiledCodeModuleConfig = Configuration.resolveAndBind(
  //     ModuleFinder.of(compilation.getFileManager()
  //         .getOutputContainerGroup(StandardLocation.CLASS_OUTPUT)
  //         .getPackages()
  //         .stream()
  //         .map(Container::getPathRoot)
  //         .map(PathRoot::getPath)
  //         .toArray(Path[]::new)),
  //     List.of(bootLayer.configuration()),
  //     ModuleFinder.of(),
  //     List.of("org.example")
  // );
  //
  // var compiledCodeController = ModuleLayer.defineModulesWithOneLoader(
  //     compiledCodeModuleConfig,
  //     List.of(bootLayer),
  //     getClass().getClassLoader()
  // );
  //
  // @SuppressWarnings("unchecked")
  // var someConfigurerCls = (Class<? extends JctCompilerConfigurer<?>>) compiledCodeController
  //     .layer()
  //     .findLoader("org.example")
  //     .loadClass("org.example.SomeConfigurer");
}
