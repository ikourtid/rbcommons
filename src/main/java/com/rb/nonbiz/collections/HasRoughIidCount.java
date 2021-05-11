package com.rb.nonbiz.collections;

/**
 * In order to create IidMaps that do not have too much unwasted space, it's good to have an idea (even roughly)
 * of how many instrument IDs there are in a data class. For example, if we take GroupedAggregateValues for
 * instrument class A (with stocks A1 and A2) and B (with stocks B1, B2, B3), and want to convert to
 * UngroupedAggregateValues, it helps to know that there are 5 instruments, so we can preallocate the MutableIidMap
 * accordingly.
 *
 * By making an interface out of this, it means that we:
 * a) enforce some uniformity b/c all methods will be called getRoughIidCount.
 *    This may or may not be the same as size() (whenever size() is present); we don’t enforce any relationship.
 *    E.g. GroupedAggregateValues#size could be the # of instrument classes, OR asset classes, but likely not the same
 *    as the # of instrument IDs.
 * b) can use as a shorthand getNumInstrumentsInMapValues and other such handy methods, in order to keep the code shorter.
 */
public interface HasRoughIidCount {

  int getRoughIidCount();

  static <K, V extends HasRoughIidCount> int getRoughIidCountInMapValues(RBMap<K, V> map) {
    // This is cleaner with streams, but let's keep it in the way we know is fast,
    // as it may be somewhat in the critical path.
    int roughIidCount = 0;
    for (V v : map.values()) {
      roughIidCount += v.getRoughIidCount();
    }
    return roughIidCount;
  }

}
