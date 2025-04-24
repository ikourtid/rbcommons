package com.rb.nonbiz.collections;

public class IidMapWithGroupings<V, S extends HasIidSet> {

  private final IidMap<V> iidMap;
  private final IidGroupings<S> iidGroupings;

  private IidMapWithGroupings(IidMap<V> iidMap, IidGroupings<S> iidGroupings) {
    this.iidMap = iidMap;
    this.iidGroupings = iidGroupings;
  }

  public static <V, S extends HasIidSet> IidMapWithGroupings<V, S> iidMapWithGroupings(
      IidMap<V> iidMap, IidGroupings<S> iidGroupings) {
    return new IidMapWithGroupings<>(iidMap, iidGroupings);
  }



}
