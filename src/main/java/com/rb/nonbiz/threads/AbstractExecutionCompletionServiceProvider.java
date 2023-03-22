package com.rb.nonbiz.threads;

import java.util.OptionalInt;
import java.util.concurrent.ExecutorCompletionService;

public interface AbstractExecutionCompletionServiceProvider {

  <T> ExecutorCompletionService<T> getExecutorCompletionService(OptionalInt maybeNumThreads);

}
