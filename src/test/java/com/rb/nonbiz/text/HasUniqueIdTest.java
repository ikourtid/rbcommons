package com.rb.nonbiz.text;

import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.text.UniqueIdTest.uniqueIdMatcher;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HasUniqueIdTest {

  private static class TestHasUniqueIdAndDouble implements HasUniqueId<TestHasUniqueIdAndDouble> {

    private final UniqueId<TestHasUniqueIdAndDouble> uniqueId;
    private final double value;

    private TestHasUniqueIdAndDouble(UniqueId<TestHasUniqueIdAndDouble> uniqueId, double value) {
      this.uniqueId = uniqueId;
      this.value = value;
    }

    @Override
    public UniqueId<TestHasUniqueIdAndDouble> getUniqueId() {
      return uniqueId;
    }

    public double getValue() {
      return value;
    }

  }

  @Test
  public void test_onlyByUniqueIdMatcher() {
    double e = 1e-9; // epsilon
    TestHasUniqueIdAndDouble obj = new TestHasUniqueIdAndDouble(uniqueId("a"), 1.1);
    assertTrue(
        "The same object has the same unique ID, so it will match itself",
        onlyByUniqueIdMatcher(obj).matches(obj));
    assertTrue(
        "Even if we have different values, there will still be a match, because we are only comparing the unique ID",
        onlyByUniqueIdMatcher(obj).matches(new TestHasUniqueIdAndDouble(uniqueId("a"), 3.3)));
    assertFalse(
        "Even if we have the same value, there will not be a match, because the unique IDs don't match",
        onlyByUniqueIdMatcher(obj).matches(new TestHasUniqueIdAndDouble(uniqueId("b"), 1.1)));
  }

  /**
   * Matches based only on the UniqueId portion of an object, not the entire object.
   */
  public static <H extends HasUniqueId<?>> TypeSafeMatcher<H> onlyByUniqueIdMatcher(H expected) {
    return makeMatcher(expected,
        match(v -> v.getUniqueId(), f -> uniqueIdMatcher(f)));
  }

}
