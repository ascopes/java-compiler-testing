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
package io.github.ascopes.jct.filemanagers.impl;

import io.github.ascopes.jct.filemanagers.ForwardingJctFileManager;
import io.github.ascopes.jct.filemanagers.JctFileManager;
import java.io.IOException;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * A wrapper around a regular {@link JctFileManager} that intercepts and fixes some of the inputs
 * and return values that ECJ can produce. This is needed since ECJ does not fully adhere to the
 * JSR-199 specification for valid parameter types and behaviours.
 *
 * @author Ashley Scopes
 * @since TBC
 */
@API(since = "TBC", status = API.Status.INTERNAL)
public final class EcjJctFileManagerImpl extends ForwardingJctFileManager<JctFileManager> {

  /**
   * Initialise this wrapper.
   *
   * @param fileManager delegate to this file manager
   */
  public EcjJctFileManagerImpl(JctFileManager fileManager) {
    super(fileManager);
  }

  @Override
  public JavaFileObject getJavaFileForInput(
      Location location,
      String className,
      Kind kind
  ) throws IOException {
    className = fixClassOrPackageName(className);
    return super.getJavaFileForInput(location, className, kind);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location,
      String className,
      Kind kind,
      @Nullable FileObject sibling
  ) throws IOException {
    className = fixClassOrPackageName(className);
    return super.getJavaFileForOutput(location, className, kind, sibling);
  }

  @Override
  public FileObject getFileForInput(
      Location location,
      String packageName,
      String relativeName
  ) throws IOException {
    packageName = fixClassOrPackageName(packageName);
    return super.getFileForInput(location, packageName, relativeName);
  }

  @Override
  public FileObject getFileForOutput(
      Location location,
      String packageName,
      String relativeName,
      FileObject sibling
  ) throws IOException {
    packageName = fixClassOrPackageName(packageName);
    return super.getFileForOutput(location, packageName, relativeName, sibling);
  }

  @Nullable
  @Override
  public Set<JavaFileObject> list(
      Location location,
      String packageName,
      Set<Kind> kinds,
      boolean recurse
  ) throws IOException {
    // ECJ passes invalid locations into this method, so we need to handle those explicitly.
    if (location.isModuleOrientedLocation()) {
      return Set.of();
    }

    packageName = fixClassOrPackageName(packageName);
    return super.list(location, packageName, kinds, recurse);
  }

  private String fixClassOrPackageName(String providedBinaryName) {
    // ECJ passes around forward-slashes in binary names rather than periods. This is incorrect
    // and will confuse the JctFileManagerImpl implementation if we do not fix it.
    return providedBinaryName.replace('/', '.');
  }
}
