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
package io.github.ascopes.jct.tests.unit.containers.impl;

import static io.github.ascopes.jct.tests.helpers.Fixtures.oneOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.ascopes.jct.containers.Container;
import io.github.ascopes.jct.containers.PackageContainerGroup;
import io.github.ascopes.jct.containers.impl.PackageContainerGroupUrlClassLoader;
import io.github.ascopes.jct.tests.helpers.Fixtures;
import io.github.ascopes.jct.tests.helpers.Fixtures.TempFileSystem;
import io.github.ascopes.jct.workspaces.PathRoot;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PackageContainerGroupUrlClassLoader} tests.
 *
 * @author Ashley Scopes
 */
@DisplayName("PackageContainerGroupUrlClassLoader tests")
class PackageContainerGroupUrlClassLoaderTest {

  private static final SomeClassFile FOO_BAR_BAZ = new SomeClassFile("foo.bar", "Baz");
  private static final SomeClassFile FOO_BAR_BORK = new SomeClassFile("foo.bar", "Bork");
  private static final SomeClassFile DOH_RAY_ME_FAH = new SomeClassFile("doh.ray.me", "Fah");
  private static final SomeClassFile ORG_EXAMPLE_TEST = new SomeClassFile("org.example", "Test");
  private static final SomeClassFile SOMETHING_ELSE = new SomeClassFile("something", "Else");

  @DisplayName("The class loader is a URLClassLoader")
  @Test
  void classLoaderIsUrlClassLoader() {
    // Given
    var group = mock(PackageContainerGroup.class);
    when(group.getLocation()).thenReturn(oneOf(StandardLocation.class));
    when(group.getPackages()).thenReturn(List.of());
    var classLoader = new PackageContainerGroupUrlClassLoader(group);

    // Then
    assertThat(classLoader).isInstanceOf(URLClassLoader.class);
  }

  @DisplayName("The class loader has the expected name")
  @Test
  void classLoaderHasExpectedName() {
    // Given
    var location = oneOf(StandardLocation.class);
    var group = mock(PackageContainerGroup.class);
    when(group.getLocation()).thenReturn(location);
    when(group.getPackages()).thenReturn(List.of());
    var classLoader = new PackageContainerGroupUrlClassLoader(group);

    // Then
    assertThat(classLoader.getName())
        .isEqualTo("Packages within %s", location.getName());
  }

  @DisplayName("Classes can get loaded from multiple paths")
  @Test
  void classesCanGetLoadedFromMultiplePaths() throws IOException {
    try (
        var fs1 = Fixtures.someTemporaryFileSystem();
        var fs2 = Fixtures.someTemporaryFileSystem();
        var fs3 = Fixtures.someTemporaryFileSystem()
    ) {
      // Given
      var container1 = createClassInTempFs(fs1, FOO_BAR_BAZ);
      var container2 = createClassInTempFs(fs1, FOO_BAR_BORK);
      var container3 = createClassInTempFs(fs2, DOH_RAY_ME_FAH);
      var container4 = createClassInTempFs(fs3, ORG_EXAMPLE_TEST);
      var container5 = createClassInTempFs(fs3, SOMETHING_ELSE);

      var group = mock(PackageContainerGroup.class);
      when(group.getLocation()).thenReturn(oneOf(StandardLocation.class));
      when(group.getPackages())
          .thenReturn(List.of(container1, container2, container3, container4, container5));

      // When
      var classLoader = new PackageContainerGroupUrlClassLoader(group);

      // Then
      var classFiles = List.of(
          FOO_BAR_BAZ, FOO_BAR_BORK, DOH_RAY_ME_FAH, ORG_EXAMPLE_TEST, SOMETHING_ELSE
      );
      assertThat(classFiles)
          .allSatisfy(classFile -> {
            var cls = classLoader.loadClass(classFile.qualifiedName);
            var obj = cls.getConstructor().newInstance();
            var method = cls.getDeclaredMethod("getName");
            var result = method.invoke(obj);

            assertThat(result)
                .isInstanceOf(String.class)
                .asString()
                .isEqualTo(classFile.qualifiedName);
          });
    }
  }

  @DisplayName("Classes get cached as expected")
  @Test
  void classesGetCachedAsExpected() throws IOException, ReflectiveOperationException {
    try (var fs = Fixtures.someTemporaryFileSystem()) {
      // Given
      var container = createClassInTempFs(fs, FOO_BAR_BAZ);

      var group = mock(PackageContainerGroup.class);
      when(group.getLocation()).thenReturn(oneOf(StandardLocation.class));
      when(group.getPackages()).thenReturn(List.of(container));

      // When
      var classLoader = new PackageContainerGroupUrlClassLoader(group);

      // Then
      var fooBarBazCls1 = classLoader.loadClass(FOO_BAR_BAZ.qualifiedName);
      var fooBarBazCls2 = classLoader.loadClass(FOO_BAR_BAZ.qualifiedName);
      assertThat(fooBarBazCls1).isAssignableFrom(fooBarBazCls2);
      assertThat(fooBarBazCls2).isAssignableFrom(fooBarBazCls1);
    }
  }

  private static Container createClassInTempFs(
      TempFileSystem fs,
      SomeClassFile classFile
  ) throws IOException {
    classFile.writeClassFileToBaseDir(fs.getRootPath());

    var pathRoot = mock(PathRoot.class);
    when(pathRoot.getUrl()).thenReturn(fs.getRootPath().toUri().toURL());

    var container = mock(Container.class);
    when(container.getPathRoot()).thenReturn(pathRoot);

    return container;
  }

  /**
   * Since Mockito struggles to mock classloading mechanisms, we make real class files to use in our
   * test cases. This reduces the dependencies on other JCT components for this test.
   */
  private static final class SomeClassFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeClassFile.class);

    private final String packageName;
    private final String className;
    private final String sourceFileName;
    private final String classFileName;
    private final String qualifiedName;
    private volatile byte[] cachedClassFile;

    public SomeClassFile(String packageName, String className) {
      this.packageName = packageName;
      this.className = className;
      sourceFileName = className + ".java";
      classFileName = className + ".class";
      qualifiedName = packageName + "." + className;
    }

    public synchronized void writeClassFileToBaseDir(Path path) throws IOException {
      if (cachedClassFile == null) {
        compileFile();
      }

      var packageDir = path;
      for (var part : packageName.split("\\.")) {
        packageDir = packageDir.resolve(part);
      }

      Files.createDirectories(packageDir);

      var outputFile = packageDir.resolve(classFileName);

      LOGGER.trace(
          "Using cached class file for {}.{}, writing to {}",
          packageName,
          className,
          outputFile.toUri()
      );

      try (var stream = Files.newOutputStream(outputFile)) {
        stream.write(cachedClassFile);
      }
    }

    private void compileFile() throws IOException {
      var dir = Files.createTempDirectory(getClass().getSimpleName());

      try {
        var packageDir = dir;
        for (var part : packageName.split("\\.")) {
          packageDir = packageDir.resolve(part);
        }

        LOGGER.trace("Compiling {}.{} in temp dir {}", packageName, className, packageDir);

        Files.createDirectories(packageDir);
        try (var sourceStream = Files.newOutputStream(packageDir.resolve(sourceFileName))) {
          var fileLines = String.join(
              "\n",
              "package " + packageName + ";",
              "public class " + className + " {",
              "  public String getName() {",
              "    return \"" + qualifiedName + "\";",
              "  }",
              "}"
          );

          sourceStream.write(fileLines.getBytes(StandardCharsets.UTF_8));
        }

        var compiler = ToolProvider.getSystemJavaCompiler();
        var sfm = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        sfm.setLocation(StandardLocation.SOURCE_PATH, List.of(dir.toFile()));
        sfm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(dir.toFile()));

        var options = List.of("--release", "" + SourceVersion.latestSupported().ordinal());
        var compilationUnits = sfm.getJavaFileObjects(packageDir.resolve(sourceFileName));

        var task = compiler.getTask(null, sfm, null, options, null, compilationUnits);

        if (!task.call()) {
          throw new IllegalArgumentException("Compilation failed");
        }

        cachedClassFile = Files.readAllBytes(packageDir.resolve(classFileName));
        LOGGER.trace(
            "{}.{} compiled to {} bytes of binary data",
            packageName,
            className,
            cachedClassFile.length
        );
      } finally {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            LOGGER.trace("Deleting file {}", file);
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc)
              throws IOException {
            LOGGER.trace("Deleting directory {}", dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }
  }
}
