package com.rb.nonbiz.functional;

/**
 * Java 8 only defines up to BiFunction.
 */
@FunctionalInterface
public interface QuintFunction<T1, T2, T3, T4, T5, R> {

  R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);

}
