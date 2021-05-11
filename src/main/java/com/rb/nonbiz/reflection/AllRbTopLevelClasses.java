package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * Represents all top-level (non-inner classes) in the com.rb package in the rb module, including test ones
 * when this is run from test.
 *
 * Ideally we'd use something smarter, where we do some pre-indexing. There are libraries that do this. However,
 * I used a Google library that does this stuff in a simple fashion. If this code ever becomes time-critical,
 * we can upgrade this. But right now (May 2021) we only use this as a one-off for determining what files to include
 * in the jar files we give to clients who get a binary license.
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
