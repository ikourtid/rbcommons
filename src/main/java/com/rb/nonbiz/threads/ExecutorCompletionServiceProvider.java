package com.rb.nonbiz.threads;

import com.google.inject.Singleton;

import java.util.OptionalInt;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Some backtests or daytests run on all possible threads (12, on Iraklis's machine);
 * some may need fewer, if they are resource-heavy (e.g. maybe we can't fit 12 backtests in memory at
 * the same time).
 * This allows you to control how many threads will be used, on a per daytest or backtest basis.
 * Of course, the concept is more general; it just so happens that currently (Mar 2023) we only use this
 * for running daytests and backtests in parallel.
 */
@Singleton
public class ExecutorCompletionServiceProvider implements AbstractExecutionCompletionServiceProvider {

  // This will be 12 for a hexa-core machine and 8 for a quad-core.
  // This currently works (March 2023) because all machines this runs on have Intel processors with hyperthreading.
  public static final int NUM_CORES_INCLUDING_HYPERTHREADING = Runtime.getRuntime().availableProcessors();
  public static final int NUM_PHYSICAL_CORES = Math.max(1, NUM_CORES_INCLUDING_HYPERTHREADING / 2);

  public static final int THREAD_POOL_SIZE = Math.max(1, NUM_CORES_INCLUDING_HYPERTHREADING - 1);

  @Override
  public <T> ExecutorCompletionService<T> getExecutorCompletionService(OptionalInt maybeNumThreads) {
    int numThreads = maybeNumThreads.orElse(THREAD_POOL_SIZE);
    return new ExecutorCompletionService<>(
        numThreads == 0
                ? Executors.newSingleThreadExecutor()
                : Executors.newFixedThreadPool(numThreads));
  }

}
