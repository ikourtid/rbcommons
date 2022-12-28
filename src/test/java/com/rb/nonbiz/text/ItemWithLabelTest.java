package com.rb.nonbiz.text;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.ItemWithLabel.itemWithLabel;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class ItemWithLabelTest extends RBTestMatcher<ItemWithLabel<RBMap<String, Money>>> {

  @Override
  public ItemWithLabel<RBMap<String, Money>> makeTrivialObject() {
    return itemWithLabel("", emptyRBMap());
  }

  @Override
  public ItemWithLabel<RBMap<String, Money>> makeNontrivialObject() {
    return itemWithLabel("abc", rbMapOf(
        "a", money(1.1),
        "b", money(2.2)));
  }

  @Override
  public ItemWithLabel<RBMap<String, Money>> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return itemWithLabel("abc", rbMapOf(
        "a", money(1.1 + e),
        "b", money(2.2 + e)));
  }

  @Override
  protected boolean willMatch(ItemWithLabel<RBMap<String, Money>> expected,
                              ItemWithLabel<RBMap<String, Money>> actual) {
    return itemWithLabelMatcher(expected, f -> rbMapPreciseValueMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<ItemWithLabel<T>> itemWithLabelMatcher(
      ItemWithLabel<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        // By convention, we never match on human-readable items, because they don't affect behavior
        match(v -> v.getItem(), matcherGenerator));
  }

}
