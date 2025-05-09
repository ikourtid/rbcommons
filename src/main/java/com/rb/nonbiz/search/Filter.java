package com.rb.nonbiz.search;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.RBOptionals;
import com.rb.nonbiz.text.Strings;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBOptionals.ifPresentOrElse;

/**
 * Represents a filter in a single collection of values T.
 *
 * <p> Right now (March 2024) we only support a filter with a single value,
 * but this could be expanded to a set of values later, if needed. </p>
 *
 * <p> Note that T should implement a non-trivial hashCode/equals
 * (i.e. it shouldn't be the default Object implementation of reference/pointer comparison). </p>
 */
public class Filter<T> {

  private final Optional<T> rawFilter;

  private Filter(Optional<T> rawFilter) {
    this.rawFilter = rawFilter;
  }

  public static <T> Filter<T> filter(T singleValue) {
    return new Filter<>(Optional.of(singleValue));
  }

  public static <T> Filter<T> noFilter() {
    return new Filter<>(Optional.empty());
  }

  public void ifPresent(Consumer<T> consumer) {
    rawFilter.ifPresent(consumer);
  }

  public void ifPresentOrElse(Consumer<T> consumer, Runnable emptyAction) {
    RBOptionals.ifPresentOrElse(rawFilter, consumer, emptyAction);
  }

  public <T1> T1 transformOrDefault(Function<T, T1> transformer, T1 defaultValue) {
    return transformOptional(rawFilter, transformer).orElse(defaultValue);
  }

  @VisibleForTesting
  Optional<T> getRawFilter() {
    return rawFilter;
  }

  @Override
  public String toString() {
    return Strings.format("[F %s F]", rawFilter);
  }

}
