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
package io.github.ascopes.jct.junit;

import io.github.ascopes.jct.workspaces.Workspace;
import io.github.ascopes.jct.workspaces.Workspaces;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit5 extension that will manage the lifecycle of {@link Managed}-annotated {@link Workspace}
 * fields within JUnit5 test classes.
 *
 * <pre><code>
 * {@literal @ExtendWith(JctExtension.class)}
 * class MyTest {
 *   {@literal @Managed}
 *   Workspace workspace;
 *
 *   {@literal @JavacCompilerTest}
 *   void myTest(JctCompiler compiler) {
 *     // Given
 *     workspace
 *        .createSourcePathPackage()
 *        ...;
 *
 *     // When
 *     var compilation = compiler.compile(workspace);
 *
 *     // Then
 *     ...
 *   }
 * }
 * </code></pre>
 *
 * @author Ashley Scopes
 * @since 0.4.0
 */
@API(since = "0.4.0", status = Status.STABLE)
public final class JctExtension
    implements Extension, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(JctExtension.class);

  /**
   * Initialise this extension.
   *
   * <p>You shouldn't ever need to call this directly. See the class description for an example
   * of how to use this.
   */
  public JctExtension() {
    // Nothing to do.
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    for (var field : getManagedWorkspaceFields(context.getRequiredTestClass(), true)) {
      initWorkspaceForField(field, null);
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    for (var instance : context.getRequiredTestInstances().getAllInstances()) {
      for (var field : getManagedWorkspaceFields(instance.getClass(), false)) {
        initWorkspaceForField(field, instance);
      }
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    for (var field : getManagedWorkspaceFields(context.getRequiredTestClass(), true)) {
      closeWorkspaceForField(field, null);
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    for (var instance : context.getRequiredTestInstances().getAllInstances()) {
      for (var field : getManagedWorkspaceFields(instance.getClass(), false)) {
        closeWorkspaceForField(field, instance);
      }
    }
  }

  private List<Field> getManagedWorkspaceFields(Class<?> clazz, boolean wantStatic) {
    var fields = new ArrayList<Field>();

    @Nullable
    Class<?> currentClass = clazz;

    do {
      for (var field : currentClass.getDeclaredFields()) {
        var isWorkspace = field.getType().equals(Workspace.class);
        var isManaged = field.isAnnotationPresent(Managed.class);
        var isDesiredScope = Modifier.isStatic(field.getModifiers()) == wantStatic;

        if (isWorkspace && isManaged && isDesiredScope) {
          field.setAccessible(true);
          fields.add(field);
        }
      }

      // Only recurse if we are checking instance scope. We don't manage annotated fields
      // in superclasses that are static as we cannot guarantee they are not shared with a
      // different class running in parallel.
      currentClass = wantStatic
          ? null
          : currentClass.getSuperclass();
  
    } while (currentClass != null);

    return fields;
  }

  private void initWorkspaceForField(Field field, @Nullable Object instance) throws Exception {
    LOGGER
        .atTrace()
        .setMessage("Initialising workspace for field in {}: {} {} on instance {}")
        .addArgument(() -> field.getDeclaringClass().getSimpleName())
        .addArgument(() -> field.getType().getSimpleName())
        .addArgument(field::getName)
        .addArgument(instance)
        .log();

    var managedWorkspace = field.getAnnotation(Managed.class);
    var workspace = Workspaces.newWorkspace(managedWorkspace.pathStrategy());
    field.set(instance, workspace);
  }

  private void closeWorkspaceForField(Field field, @Nullable Object instance) throws Exception {
    LOGGER
        .atTrace()
        .setMessage("Closing workspace for field in {}: {} {} on instance {}")
        .addArgument(() -> field.getDeclaringClass().getSimpleName())
        .addArgument(() -> field.getType().getSimpleName())
        .addArgument(field::getName)
        .addArgument(instance)
        .log();

    var workspace = (Workspace) field.get(instance);
    workspace.close();
  }
}
