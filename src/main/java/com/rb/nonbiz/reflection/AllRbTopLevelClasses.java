package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A typesafe wrapper representing all top-level (non-inner classes) in the com.rb package in the rb module,
 * including test ones when this is run from test.
 *
 * <p> Ideally we'd use something smarter, where we do some pre-indexing. There are libraries that do this. However,
 * I used a Google library that does this stuff in a simple fashion. If this code ever becomes time-critical,
 * we can upgrade this. </p>
 */
public class AllRbTopLevelClasses {

  private final RBSet<Class<?>> rawSet;

  private AllRbTopLevelClasses(RBSet<Class<?>> rawSet) {
    this.rawSet = rawSet;
  }

  public static AllRbTopLevelClasses allRbTopLevelClasses(RBSet<Class<?>> rawSet) {
    RBPreconditions.checkArgument(
        rawSet.size() >= 3_700,
        "On 03-05-2021 we had 3,773 classes. It's unlikely this number went down, so there's probably some error.");
    return new AllRbTopLevelClasses(rawSet);
  }

  /**
   * Useful in tests so we can skip preconditions.
   */
  public static AllRbTopLevelClasses unsafeTestOnlyAllRbTopLevelClasses(RBSet<Class<?>> rawSet) {
    return new AllRbTopLevelClasses(rawSet);
  }

  public RBSet<Class<?>> getRawSet() {
    return rawSet;
  }

}
