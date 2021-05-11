package com.rb.nonbiz.collections;

import com.rb.nonbiz.testmatchers.Match;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.CaseInsensitiveStringFilter.caseInsensitiveStringFilter;
import static com.rb.nonbiz.collections.CaseInsensitiveStringFilter.emptyCaseInsensitiveStringFilter;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CaseInsensitiveStringFilterTest extends RBTestMatcher<CaseInsensitiveStringFilter> {

  @Test
  public void testContainsIgnoringCase() {
    CaseInsensitiveStringFilter filter = caseInsensitiveStringFilter("a", "bC");
    assertTrue(filter.containsIgnoringCase("a"));
    assertTrue(filter.containsIgnoringCase("A"));
    assertTrue(filter.containsIgnoringCase("bc"));
    assertTrue(filter.containsIgnoringCase("bC"));
    assertTrue(filter.containsIgnoringCase("Bc"));
    assertTrue(filter.containsIgnoringCase("BC"));

    assertFalse(filter.containsIgnoringCase(""));
    assertFalse(filter.containsIgnoringCase("A "));
    assertFalse(filter.containsIgnoringCase("a1"));
  }

  @Test
  public void testStringsMustBeUnique() {
    assertIllegalArgumentException( () -> caseInsensitiveStringFilter("a", "A"));
    assertIllegalArgumentException( () -> caseInsensitiveStringFilter("a", "A", "bb", "bB"));
    assertIllegalArgumentException( () -> caseInsensitiveStringFilter("abC", "Abc"));
  }

  @Override
  public CaseInsensitiveStringFilter makeTrivialObject() {
    return emptyCaseInsensitiveStringFilter();
  }

  @Override
  public CaseInsensitiveStringFilter makeNontrivialObject() {
    return caseInsensitiveStringFilter("ab", "cd", "ef");
  }

  @Override
  public CaseInsensitiveStringFilter makeMatchingNontrivialObject() {
    // Same object as above, but constructed differently, with strings that have different capitalization.
    return caseInsensitiveStringFilter("aB", "Cd", "EF");
  }

  @Override
  protected boolean willMatch(CaseInsensitiveStringFilter expected, CaseInsensitiveStringFilter actual) {
    return caseInsensitiveStringFilterMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<CaseInsensitiveStringFilter> caseInsensitiveStringFilterMatcher(
      CaseInsensitiveStringFilter expected) {
    return makeMatcher(expected,
        Match.match(v -> v.getCapitalizedStrings(), f -> rbSetEqualsMatcher(f)));
  }

}
