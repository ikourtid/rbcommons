package com.rb.biz.types.collections.ts;

import com.rb.nonbiz.collections.HasRbSet;
import com.rb.nonbiz.collections.RBSet;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

/**
 * The only prod class (as of Jan 2017) implementing HasRbSet is Eigendecomposition, but that's too clunky
 * to use in tests that need a HasRbSet, so I'm creating this one.
 */
public class TestHasRbSet<K> implements HasRbSet<K> {

  private final RBSet<K> rbSet;
  private final String string;

  /**
   * We intentionally do not create a static constructor like we do with every data class,
   * in order to make it even more noticeable that this is different and is not a prod class.
   */
  public TestHasRbSet(RBSet<K> rbSet, String string) {
    this.rbSet = rbSet;
    this.string = string;
  }

  @Override
  public RBSet<K> getRbSet() {
    return rbSet;
  }

  public String getString() {
    return string;
  }

  public static <K>TypeSafeMatcher<TestHasRbSet<K>> testHasRbSetMatcher(TestHasRbSet<K> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRbSet()),
        matchUsingEquals(v -> v.string));
  }

}
