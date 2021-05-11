package com.rb.nonbiz.text;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.text.UniqueId.uniqueIdWithoutSpaces;
import static org.junit.Assert.assertEquals;

/**
 * This doesn't have to be {@code <Integer>}; the type inside a UniqueId only matters
 * when we try to attach that UniqueId to an object.
 */
public class UniqueIdTest extends RBTestMatcher<UniqueId<Integer>> {

  @Test
  public void implementsEquals() {
    assertEquals(makeTrivialObject(), makeTrivialObject());
    assertEquals(makeNontrivialObject(), makeNontrivialObject());
  }

  @Test
  public void emptyStringIsNotAllowed() {
    assertIllegalArgumentException( () -> uniqueId(""));
  }

  @Test
  public void spacesAreNotAllowed() {
    assertIllegalArgumentException( () -> uniqueIdWithoutSpaces("a b"));
    assertIllegalArgumentException( () -> uniqueIdWithoutSpaces(" b"));
    assertIllegalArgumentException( () -> uniqueIdWithoutSpaces("a "));
    assertIllegalArgumentException( () -> uniqueIdWithoutSpaces(" "));

    UniqueId<Integer> doesNotThrow;
    doesNotThrow = uniqueId("a b");
    doesNotThrow = uniqueId(" b");
    doesNotThrow = uniqueId("a ");
    doesNotThrow = uniqueId(" ");
  }

  @Override
  public UniqueId<Integer> makeTrivialObject() {
    return uniqueId("a");
  }

  @Override
  public UniqueId<Integer> makeNontrivialObject() {
    return uniqueId("a_b_c");
  }

  @Override
  public UniqueId<Integer> makeMatchingNontrivialObject() {
    return uniqueId("a_b_c");
  }

  @Override
  protected boolean willMatch(UniqueId<Integer> expected, UniqueId<Integer> actual) {
    return uniqueIdMatcher(expected).matches(actual);
  }

  // UniqueId implements equals, but tests use matchers everywhere, so we created uniqueIdMatcher
  public static <T> TypeSafeMatcher<UniqueId<T>> uniqueIdMatcher(UniqueId<T> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getStringId()));
  }

}
