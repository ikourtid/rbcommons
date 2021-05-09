package com.rb.nonbiz.text;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBTest.DUMMY_DOUBLE;
import static com.rb.nonbiz.text.ItemWithId.itemWithId;


public class ItemWithIdTest extends RBTestMatcher<ItemWithId<Double>> {

  @Test
  public void emptyId_throws() {
    assertIllegalArgumentException( () -> itemWithId("", DUMMY_DOUBLE));
  }

  @Override
  public ItemWithId<Double> makeTrivialObject() {
    return itemWithId("x", 0.0);
  }

  @Override
  public ItemWithId<Double> makeNontrivialObject() {
    return itemWithId("abc", -1.1);
  }

  @Override
  public ItemWithId<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return itemWithId("abc", -1.1 + e);
  }

  @Override
  protected boolean willMatch(ItemWithId<Double> expected, ItemWithId<Double> actual) {
    return itemWithIdMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T> TypeSafeMatcher<ItemWithId<T>> itemWithIdMatcher(
      ItemWithId<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getItem(), matcherGenerator));
  }

}
