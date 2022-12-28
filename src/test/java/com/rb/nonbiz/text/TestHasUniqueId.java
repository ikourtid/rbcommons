package com.rb.nonbiz.text;

import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

/**
 * This class is for test purposes only, so we will not have to rely on some business logic object that
 * implements HasUniqueId.
 *
 * The value is a UnitFraction, which will make test code more general than using e.g. String,
 * since string matching are simpler (just using equals), whereas UnitFraction needs an epsilon matcher,
 * which is more general.
 */
public class TestHasUniqueId implements HasUniqueId<TestHasUniqueId> {

  private final UniqueId<TestHasUniqueId> uniqueId;
  private final UnitFraction value;

  private TestHasUniqueId(UniqueId<TestHasUniqueId> uniqueId, UnitFraction value) {
    this.uniqueId = uniqueId;
    this.value = value;
  }

  public static TestHasUniqueId testHasUniqueId(UniqueId<TestHasUniqueId> uniqueId, UnitFraction value) {
    return new TestHasUniqueId(uniqueId, value);
  }

  @Override
  public UniqueId<TestHasUniqueId> getUniqueId() {
    return uniqueId;
  }

  public UnitFraction getValue() {
    return value;
  }

  public static TypeSafeMatcher<TestHasUniqueId> testHasUniqueIdMatcher(TestHasUniqueId expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getUniqueId()),
        matchUsingAlmostEquals(v -> v.getValue(), DEFAULT_EPSILON_1e_8));
  }

}
