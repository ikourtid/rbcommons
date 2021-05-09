package com.rb.nonbiz.reflection;

import com.rb.biz.investing.strategy.Investor;
import com.rb.biz.types.trading.BuyOrders;
import com.rb.biz.types.trading.Orders;
import com.rb.biz.types.trading.SellOrders;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.reflection.AllRbTopLevelClasses.unsafeTestOnlyAllRbTopLevelClasses;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class AllRbTopLevelClassesTest extends RBTestMatcher<AllRbTopLevelClasses> {

  @Override
  public AllRbTopLevelClasses makeTrivialObject() {
    return unsafeTestOnlyAllRbTopLevelClasses(emptyRBSet());
  }

  @Override
  public AllRbTopLevelClasses makeNontrivialObject() {
    return unsafeTestOnlyAllRbTopLevelClasses(rbSetOf(
        Investor.class,
        BuyOrders.class,
        SellOrders.class,
        Orders.class));
  }

  @Override
  public AllRbTopLevelClasses makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return unsafeTestOnlyAllRbTopLevelClasses(rbSetOf(
        Investor.class,
        BuyOrders.class,
        SellOrders.class,
        Orders.class));
  }

  @Override
  protected boolean willMatch(AllRbTopLevelClasses expected, AllRbTopLevelClasses actual) {
    return allRbTopLevelClassesMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<AllRbTopLevelClasses> allRbTopLevelClassesMatcher(AllRbTopLevelClasses expected) {
    return makeMatcher(expected,
        match(v -> v.getRawSet(), f -> rbSetEqualsMatcher(f)));
  }

}