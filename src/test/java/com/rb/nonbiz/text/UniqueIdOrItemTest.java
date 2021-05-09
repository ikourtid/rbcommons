package com.rb.nonbiz.text;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.UniqueIdOrItem.Visitor;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBEitherMatchers.eitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.text.UniqueId.uniqueIdWithoutSpaces;
import static com.rb.nonbiz.text.UniqueIdOrItem.itemInsteadOfUniqueId;
import static com.rb.nonbiz.text.UniqueIdTest.uniqueIdMatcher;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.junit.Assert.assertEquals;

public class UniqueIdOrItemTest extends RBTestMatcher<UniqueIdOrItem<TestHasUniqueId>> {

  @Test
  public void testVisitor() {
    UniqueIdOrItem.Visitor<TestHasUniqueId, Integer> visitor = new Visitor<TestHasUniqueId, Integer>() {
      @Override
      public Integer visitIsUniqueId(UniqueId<TestHasUniqueId> uniqueId) {
        return uniqueId.getStringId().length();
      }

      @Override
      public Integer visitActualItem(TestHasUniqueId actualItem) {
        return (int) (actualItem.getValue().doubleValue() * 100);
      }
    };

    assertEquals(1, visitor.visitIsUniqueId(uniqueIdWithoutSpaces("a")).intValue());
    assertEquals(5, visitor.visitIsUniqueId(uniqueIdWithoutSpaces("abcde")).intValue());

    assertEquals(78, visitor.visitActualItem(testHasUniqueId(uniqueId(DUMMY_STRING), unitFraction(0.78))).intValue());
  }

  @Override
  public UniqueIdOrItem<TestHasUniqueId> makeTrivialObject() {
    // Strange - even though the IDE says this is not needed, the IDE itself gives a build error if we don't
    // make the generic type explicit here.
    return UniqueIdOrItem.<TestHasUniqueId>uniqueIdInsteadOfItem(uniqueId("x"));
  }

  @Override
  public UniqueIdOrItem<TestHasUniqueId> makeNontrivialObject() {
    return itemInsteadOfUniqueId(testHasUniqueId(uniqueId("abc"), unitFraction(0.123)));
  }

  @Override
  public UniqueIdOrItem<TestHasUniqueId> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return itemInsteadOfUniqueId(testHasUniqueId(uniqueId("abc"), unitFraction(0.123 + e)));
  }

  @Override
  protected boolean willMatch(UniqueIdOrItem<TestHasUniqueId> expected, UniqueIdOrItem<TestHasUniqueId> actual) {
    return uniqueIdOrItemMatcher(expected, f -> testHasUniqueIdMatcher(f)).matches(actual);
  }

  public static <T extends HasUniqueId<T>> TypeSafeMatcher<UniqueIdOrItem<T>> uniqueIdOrItemMatcher(
      UniqueIdOrItem<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawEither(), f -> eitherMatcher(f, f2 -> uniqueIdMatcher(f2), matcherGenerator)));
  }

}
