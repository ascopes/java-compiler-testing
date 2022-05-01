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

package io.github.ascopes.jct.testing.helpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Wrapper around a fixed thread pool that can be used in a try-with-resources block.
 *
 * @author Ashley Scopes
 * @since 0.0.1
 */
public class ThreadPool extends AbstractExecutorService implements AutoCloseable {

  private final ExecutorService executorService;

  /**
   * Initialize the closeable thread pool.
   *
   * @param threads the number of threads to open.
   */
  public ThreadPool(int threads) {
    executorService = Executors.newFixedThreadPool(threads);
  }

  @Override
  public void close() {
    executorService.shutdown();
  }

  @Override
  public void shutdown() {
    executorService.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return executorService.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return executorService.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executorService.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executorService.awaitTermination(timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    executorService.execute(command);
  }

  /**
   * Await all callables and return a future that completes when all callables complete.
   *
   * @param callables the callables to run in the pool.
   * @param <T>       the return type of the callables.
   * @return the list of futures.
   */
  public <T> CompletableFuture<List<T>> awaitingAll(Collection<? extends Callable<T>> callables) {
    var futures = new ArrayList<CompletableFuture<T>>();
    for (var callable : callables) {
      futures.add(CompletableFuture.supplyAsync(
          () -> {
            try {
              return callable.call();
            } catch (Exception ex) {
              throw new CompletionException(ex);
            }
          },
          this
      ));
    }

    return CompletableFuture
        .allOf(futures.toArray(CompletableFuture[]::new))
        .thenApplyAsync(unused -> futures
            .stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }

  /**
   * Annotation to tell junit to run these tests in series. This is useful if running tests in
   * parallel on GitHub actions or a similar CI environment, as use of a large number of threads may
   * cause thread starvation and fail the tests.
   *
   * <p>Anything using a large number of threads in this thread pool should be annotated with this
   * annotation for safety.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Isolated("com.github.ascopes.jct.testing.helpers.ThreadPool")
  public @interface RunTestsInIsolation {
  }
}
