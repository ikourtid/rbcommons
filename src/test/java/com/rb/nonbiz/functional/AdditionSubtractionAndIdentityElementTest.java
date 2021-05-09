package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.Money.sumMoney;
import static com.rb.nonbiz.functional.AdditionSubtractionAndIdentityElement.additionSubtractionAndIdentityElement;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

// This is unusual, in that we can't make this be RBTestMatcher, because once we fix the type (e.g. Money),
// there is only one valid AdditionSubtractionAndIdentityElement we can generate.
// But even if we tweak it slightly (using an epsilon),
// I cannot get enough of them so that we can have a makeTrivialObject that does not match a makeNontrivialObject.
public class AdditionSubtractionAndIdentityElementTest {

  @Test
  public void testSimpleGroup() {
    // We are abusing the concept of group here, because we use this approximate equality by matching using 1e-8.
    DoubleFunction<AdditionSubtractionAndIdentityElement<Money>> maker = epsilon -> additionSubtractionAndIdentityElement(
        money(epsilon),
        (v1, v2) -> sumMoney(v1, v2, money(epsilon)),
        (v1, v2) -> v1.subtract(v2).add(money(epsilon)));

    assertThat(
        maker.apply(0),
        additionSubtractionAndIdentityElementMatcher(
            maker.apply(1e-9),
            money(100),
            money(70),
            f -> preciseValueMatcher(f, 1e-8)));
    // Unfortunately, I can't assert that, because the matcher itself checks that
    // identity minus identity 'matches' (almost equals) the identity, and because in this case (unlike above)
    // the matcher has a tighter epsilon, it will fail.
    //    assertThat(
    //        maker.apply(0),
    //        not(additionSubtractionAndIdentityElementMatcher(
    //            maker.apply(1e-7),
    //            money(100),
    //            money(70),
    //            f -> preciseValueMatcher(f, 1e-8))));
  }

  // value1 and value2 should both be non-zero elements, such that 0 < value2 < value1
  // and
  public static <T> TypeSafeMatcher<AdditionSubtractionAndIdentityElement<T>> additionSubtractionAndIdentityElementMatcher(
      AdditionSubtractionAndIdentityElement<T> expected,
      T value1,
      T value2,
      MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected, actual -> {
      assertFalse(
          "You set up this test incorrectly; if you want to create a matcher, use two different values",
          matcherGenerator.apply(value1).matches(value2));
      // Matchers don't normally have preconditions, but since we have a MatcherGenerator here,
      // let's confirm some of the basic group operations. The prod code cannot do that,
      // because MatcherGenerator is only applicable in test, and T is not guaranteed to implement #equals properly
      assertValid(expected, matcherGenerator);
      assertValid(actual, matcherGenerator);

      if (!matcherGenerator.apply(expected.getIdentityElement()).matches(actual.getIdentityElement())) {
        return false; // identity elements must match
      }
      T sharedIdentityElement = expected.getIdentityElement();
      assertThat(
          "For this matcher call to be general, do not use the same sample values",
          value1,
          not(matcherGenerator.apply(value2)));
      assertThat(
          "For this matcher call to be general, value1 should not be the identity",
          value1,
          not(matcherGenerator.apply(sharedIdentityElement)));
      assertThat(
          "For this matcher call to be general, value2 should not be the identity",
          value2,
          not(matcherGenerator.apply(sharedIdentityElement)));

      T expectedSum = expected.add(value1, value2);
      T actualSum = actual.add(value1, value2);

      T expectedDifference = expected.subtract(value1, value2);
      T actualDifference = actual.subtract(value1, value2);

      if (!matcherGenerator.apply(expectedSum).matches(actualSum)
          || !matcherGenerator.apply(expectedDifference).matches(actualDifference)) {
        return false;
      }

      assertThat(
          "For this matcher call to be general, make sure that value1 - value2 != value2",
          expectedDifference,
          not(matcherGenerator.apply(value2)));
      assertThat(
          "For this matcher call to be general, make sure that value1 - value2 != value2",
          expectedDifference,
          not(matcherGenerator.apply(value2)));
      return true;
    });
  }

  private static <T> void assertValid(
      AdditionSubtractionAndIdentityElement<T> additionSubtractionAndIdentityElement,
      MatcherGenerator<T> matcherGenerator) {
    T identityElement = additionSubtractionAndIdentityElement.getIdentityElement();
    assertThat(
        additionSubtractionAndIdentityElement.add(identityElement, identityElement),
        matcherGenerator.apply(identityElement));
    assertThat(
        additionSubtractionAndIdentityElement.subtract(identityElement, identityElement),
        matcherGenerator.apply(identityElement));
  }

}
