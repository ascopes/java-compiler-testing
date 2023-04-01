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
 *   void myTest(JctCompiler&lt;?, ?&gt; compiler) {
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
public final class JctExtension implements
    Extension, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

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
    for (var field : getManagedStaticWorkspaceFields(context.getRequiredTestClass())) {
      initWorkspaceForField(field, null);
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    for (var instance : context.getRequiredTestInstances().getAllInstances()) {
      for (var field : getManagedInstanceWorkspaceFields(instance.getClass())) {
        initWorkspaceForField(field, instance);
      }
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    for (var field : getManagedStaticWorkspaceFields(context.getRequiredTestClass())) {
      closeWorkspaceForField(field, null);
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    for (var instance : context.getRequiredTestInstances().getAllInstances()) {
      for (var field : getManagedInstanceWorkspaceFields(instance.getClass())) {
        closeWorkspaceForField(field, instance);
      }
    }
  }

  private List<Field> getManagedStaticWorkspaceFields(Class<?> clazz) {
    // Do not recurse for static fields, as the state of any parent classes may be shared
    // with other classes running in parallel. Need to look up how JUnit expects us to handle that
    // case, if at all.

    var fields = new ArrayList<Field>();

    for (var field : clazz.getDeclaredFields()) {
      if (isWorkspaceField(field) && Modifier.isStatic(field.getModifiers())) {
        fields.add(field);
      }
    }

    return fields;
  }

  private List<Field> getManagedInstanceWorkspaceFields(Class<?> clazz) {
    // For instances, discover all the fields recursively in superclasses as well that are
    // non-static.

    var fields = new ArrayList<Field>();

    while (clazz != null) {
      for (var field : clazz.getDeclaredFields()) {
        if (isWorkspaceField(field) && !Modifier.isStatic(field.getModifiers())) {
          fields.add(field);
        }
      }
      clazz = clazz.getSuperclass();
    }

    return fields;
  }

  private boolean isWorkspaceField(Field field) {
    return field.getType().equals(Workspace.class)
        && field.isAnnotationPresent(Managed.class);
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

    field.setAccessible(true);
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

    field.setAccessible(true);
    ((Workspace) field.get(instance)).close();
  }
}
