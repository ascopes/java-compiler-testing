/*
 * Copyright (C) 2022 Ashley Scopes
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

package com.github.ascopes.jct.compilers.ecj;

import com.github.ascopes.jct.compilers.FlagBuilder;
import com.github.ascopes.jct.compilers.SimpleAbstractCompiler;
import java.util.concurrent.locks.ReentrantLock;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * Implementation of an ECJ compiler.
 *
 * <p>ECJ is unable to run in parallel correctly, so this class will ensure that
 * locks are acquired during the compilation process to prevent flaky tests in
 * environments that default to concurrent test execution.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
@API(since = "0.0.1", status = Status.INTERNAL)
public final class EcjCompiler extends SimpleAbstractCompiler<EcjCompiler> {
  // Annoyingly, ECJ seems to produce the following exception occasionally if run in
  // parallel. To avoid this, we lock the ECJ compiler globally.
  //
  // java.nio.file.FileSystemAlreadyExistsException
  //      at jdk.zipfs/...zipfs.ZipFileSystemProvider.newFileSystem(ZipFileSystemProvider.java:104)
  //      at java.base/java.nio.file.FileSystems.newFileSystem(FileSystems.java:339)
  //      at java.base/java.nio.file.FileSystems.newFileSystem(FileSystems.java:288)
  //      at ecj@3.29.0/...batch.ClasspathJep247Jdk12.initialize(ClasspathJep247Jdk12.java:132)
  //      at ecj@3.29.0/...compiler.batch.ClasspathJsr199.initialize(ClasspathJsr199.java:160)
  //      at ecj@3.29.0/...compiler.batch.FileSystem.<init>(FileSystem.java:228)
  //      at ecj@3.29.0/...compiler.batch.Main.getLibraryAccess(Main.java:3492)
  //      ...
  private static final ReentrantLock lock = new ReentrantLock();

  @Override
  protected String getName() {
    return "ecj";
  }

  @Override
  protected JavaCompiler createJsr199Compiler() {
    return new EclipseCompiler();
  }

  @Override
  protected FlagBuilder createFlagBuilder() {
    return new EcjFlagBuilder();
  }

  @Override
  protected Boolean runCompilationTask(CompilationTask task) {
    lock.lock();
    try {
      return super.runCompilationTask(task);
    } finally {
      lock.unlock();
    }
  }
}
