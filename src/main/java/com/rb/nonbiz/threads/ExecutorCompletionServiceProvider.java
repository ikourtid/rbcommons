package com.rb.nonbiz.threads;

import com.google.inject.Singleton;

import java.util.OptionalInt;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Some backtests run on all possible threads (12, on Iraklis's machine);
 * some only on 3 (which is the number that seems to work without causing problems in practice).
 * This allows you to control how many threads will be used, on a per backtest basis.
 */
@Singleton
public class ExecutorCompletionServiceProvider implements AbstractExecutionCompletionServiceProvider {

  // This will be 12 for a hexa-core machine and 8 for a quad-core.
  // This currently works (March 2019) because all machines this runs on have Intel processors with hyperthreading.
  public static final int NUM_CORES_INCLUDING_HYPERTHREADING = Runtime.getRuntime().availableProcessors();
  public static final int NUM_PHYSICAL_CORES = NUM_CORES_INCLUDING_HYPERTHREADING / 2;

  public static final int THREAD_POOL_SIZE = NUM_CORES_INCLUDING_HYPERTHREADING - 1;

  @Override
  public <T> ExecutorCompletionService<T> getExecutorCompletionService(OptionalInt maybeNumThreads) {
    int numThreads = maybeNumThreads.orElse(THREAD_POOL_SIZE);
    return new ExecutorCompletionService<>(
        numThreads == 0
                ? Executors.newSingleThreadExecutor()
                : Executors.newFixedThreadPool(numThreads));
  }

}
