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

package com.github.ascopes.jct.testing.helpers;

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
import java.util.stream.Stream;

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
  public <T> CompletableFuture<? extends List<T>> awaitingAll(Collection<? extends Callable<T>> callables) {
    var futures = callables
        .stream()
        .map(callable -> CompletableFuture.supplyAsync(
            () -> { 
              try {
                return callable.call();
              } catch (Exception ex) {
                throw new CompletionException(ex);
              }
            },
            this
        ))
        .toArray(CompletableFuture[]::new);

    return CompletableFuture
        .allOf(futures)
        .thenApplyAsync(unused -> Stream
            .of(futures)
            .stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList()));
  }
}
