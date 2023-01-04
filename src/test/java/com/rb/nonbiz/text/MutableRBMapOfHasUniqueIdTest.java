package com.rb.nonbiz.text;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.text.MutableRBMapOfHasUniqueId.newMutableRBMapOfHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

// This test class is not generic, but the publicly exposed static matcher is.
public class MutableRBMapOfHasUniqueIdTest extends RBTestMatcher<MutableRBMapOfHasUniqueId<TestHasUniqueId, Double>> {

  @Override
  public MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> makeTrivialObject() {
    return newMutableRBMapOfHasUniqueId();
  }

  @Override
  public MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> makeNontrivialObject() {
    MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> mutableMap = newMutableRBMapOfHasUniqueId();
    mutableMap.putAssumingAbsent(testHasUniqueId(uniqueId("a"), unitFraction(0.11)), 1.1);
    mutableMap.putAssumingAbsent(testHasUniqueId(uniqueId("b"), unitFraction(0.22)), 2.2);
    return mutableMap;
  }

  @Override
  public MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> mutableMap = newMutableRBMapOfHasUniqueId();
    mutableMap.putAssumingAbsent(testHasUniqueId(uniqueId("a"), unitFraction(0.11 + e)), 1.1);
    mutableMap.putAssumingAbsent(testHasUniqueId(uniqueId("b"), unitFraction(0.22 + e)), 2.2);
    return mutableMap;
  }

  @Override
  protected boolean willMatch(MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> expected,
                              MutableRBMapOfHasUniqueId<TestHasUniqueId, Double> actual) {
    return mutableRBMapOfHasUniqueIdMatcher(
        expected,
        f1 -> testHasUniqueIdMatcher(f1),
        f2 -> doubleAlmostEqualsMatcher(f2, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <K extends HasUniqueId<K>, V> TypeSafeMatcher<MutableRBMapOfHasUniqueId<K, V>> mutableRBMapOfHasUniqueIdMatcher(
      MutableRBMapOfHasUniqueId<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        matchRBMap(v -> newRBMap(v.getRawMap()), f -> pairMatcher(f, keyMatcherGenerator, valueMatcherGenerator)));
  }

}
