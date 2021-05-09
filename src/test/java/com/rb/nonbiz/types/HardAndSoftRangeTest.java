package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.Money;
import com.rb.biz.types.SignedMoney;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.DoubleFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rbNumericRangeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_MONEY;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;

public class HardAndSoftRangeTest extends RBTestMatcher<HardAndSoftRange<SignedMoney>> {

  public static <T extends RBNumeric<? super T>> HardAndSoftRange<T> testHardAndSoftRangeWithSeed(
      DoubleFunction<T> instantiator, double seed) {
    return hardAndSoftRange(
        Range.openClosed(instantiator.apply(-12 + seed), instantiator.apply(35 + seed)),
        Range.closed(    instantiator.apply(-11 + seed), instantiator.apply(33 + seed)));
  }

  @Test
  public void testAllValidCases() {
    // We typically only have tests for cases that make preconditions break, but in this case there are many valid
    // permutations, so let's check those explicitly as well.
    HardAndSoftRange<Money> doesNotThrow;
    // The second (soft) range cannot have "open" endpoints. This is because the semantics are that if we
    // exceed the hard range, we should move back to within the (tighter) soft range - but if that endpoint is not
    // a valid value, then we can't do that.
    doesNotThrow = hardAndSoftRange(Range.atLeast(    money(14)), Range.atLeast(money(15)));
    doesNotThrow = hardAndSoftRange(Range.greaterThan(money(14)), Range.atLeast(money(15)));

    doesNotThrow = hardAndSoftRange(Range.atMost(     money(26)), Range.atMost( money(25)));
    doesNotThrow = hardAndSoftRange(Range.lessThan(   money(26)), Range.atMost( money(25)));

    doesNotThrow = hardAndSoftRange(Range.closed(    money(14), money(26)), Range.closed(money(15), money(25)));
    doesNotThrow = hardAndSoftRange(Range.openClosed(money(14), money(26)), Range.closed(money(15), money(25)));
    doesNotThrow = hardAndSoftRange(Range.closedOpen(money(14), money(26)), Range.closed(money(15), money(25)));
    doesNotThrow = hardAndSoftRange(Range.open(      money(14), money(26)), Range.closed(money(15), money(25)));
  }

  @Test
  public void emptyRangeEitherHardOrSoft_throws() {
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.<Money>all(),       Range.all()));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.all(),              Range.atLeast(DUMMY_MONEY)));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.all(),              Range.atMost( DUMMY_MONEY)));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost( money(26)), Range.all()));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atLeast(money(14)), Range.all()));
  }

  @Test
  public void singletonHardRange_throws() {
    // a singleton hard range cannot enclose a soft range
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.singleton(money(20)), Range.closed(money(19), money(21))));
    // if hard and soft are both singleton ranges, then the hard will not "safely" enclose the soft
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.singleton(money(20)), Range.singleton(money(20))));
  }

  @Test
  public void singletonSoftRange_throws() {
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.closed( money(14), money(25)), Range.singleton(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atLeast(money(14)),            Range.singleton(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(            money(25)), Range.singleton(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.all(),                         Range.singleton(money(15))));
  }

  @Test
  public void softRangeHasOpenEndpoints_throws() {
    rbSetOf(
        Range.closed(    money(14), money(26)),
        Range.closedOpen(money(14), money(26)),
        Range.openClosed(money(14), money(26)),
        Range.open(      money(14), money(26))).forEach(validHardRange -> {
      assertIllegalArgumentException( () -> hardAndSoftRange(validHardRange, Range.closedOpen(money(15), money(25))));
      assertIllegalArgumentException( () -> hardAndSoftRange(validHardRange, Range.openClosed(money(15), money(25))));
      assertIllegalArgumentException( () -> hardAndSoftRange(validHardRange, Range.open(      money(15), money(25))));

      HardAndSoftRange<Money> doesNotThrow = hardAndSoftRange(validHardRange, Range.closed(money(15), money(25)));
    });

    rbSetOf(
        Range.atMost(  money(26)),
        Range.lessThan(money(26))).forEach(validHardRange -> {
      assertIllegalArgumentException( () -> hardAndSoftRange( validHardRange, Range.lessThan(money(25))));
      HardAndSoftRange<Money> doesNotThrow = hardAndSoftRange(validHardRange, Range.atMost(  money(25)));
    });

    rbSetOf(
        Range.atLeast(    money(14)),
        Range.greaterThan(money(14))).forEach(validHardRange -> {
      assertIllegalArgumentException( () ->  hardAndSoftRange(validHardRange, Range.greaterThan(money(15))));
      HardAndSoftRange<Money> doesNotThrow = hardAndSoftRange(validHardRange, Range.atLeast(    money(15)));
    });
  }

  @Test
  public void softRangeNotEnclosedByHard_throws() {
    rbSetOf(
        Range.closed(    money(14), money(26)),
        Range.open(      money(14), money(26)),
        Range.closedOpen(money(14), money(26)),
        Range.openClosed(money(14), money(26)))
        .forEach(hardRange -> {
          assertIllegalArgumentException( () ->   hardAndSoftRange(hardRange, Range.closed(money(15), money(27))));
          HardAndSoftRange<Money> doesNotThrow = hardAndSoftRange(hardRange, Range.closed(money(15), money(25)));
        });
  }

  @Test
  public void softRangeNotSafelyEnclosedByHard_throws() {
    rbSetOf(
        Range.closed(    money(14), money(26)),
        Range.open(      money(14), money(26)),
        Range.closedOpen(money(14), money(26)),
        Range.openClosed(money(14), money(26)))
        .forEach(hardRange -> {
          // each end of the soft range must be at least "epsilon" inside the hard range
          assertIllegalArgumentException( () -> hardAndSoftRange(hardRange, Range.closed(money(14), money(25))));
          assertIllegalArgumentException( () -> hardAndSoftRange(hardRange, Range.closed(money(15), money(26))));
          HardAndSoftRange<Money> doesNotThrow;
          doesNotThrow = hardAndSoftRange(hardRange, Range.closed(money(15),   money(25.9)));
          doesNotThrow = hardAndSoftRange(hardRange, Range.closed(money(14.1), money(25)));
        });
  }

  @Test
  public void existenceOfEndpointsOnEachSideMustMatch() {
    // Hard range has 2 endpoints; soft range only has one
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.closed(money(14), money(26)), Range.atLeast(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.open(  money(14), money(26)), Range.atLeast(money(15))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.closed(money(14), money(26)), Range.atMost( money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.open(  money(14), money(26)), Range.atMost( money(15))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.closed(money(14), money(26)), Range.all())); // Range.all is always invalid

    // Hard range only has lower endpoint; soft range either has both, or only upper
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atLeast(    money(14)), Range.atMost(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.greaterThan(money(14)), Range.atMost(money(15))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atLeast(money(14)), Range.closed(money(15), money(26))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atLeast(money(14)), Range.all())); // Range.all is always invalid

    // Hard range only has upper endpoint; soft range either has both, or only lower
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(  money(26)), Range.atLeast(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.lessThan(money(26)), Range.atLeast(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(  money(26)), Range.atLeast(money(15))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.lessThan(money(26)), Range.atLeast(money(15))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(  money(26)), Range.closed(money(15), money(25))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.lessThan(money(26)), Range.closed(money(15), money(25))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(  money(26)), Range.closed(money(15), money(25))));
    assertIllegalArgumentException( () -> hardAndSoftRange(Range.lessThan(money(26)), Range.closed(money(15), money(25))));

    assertIllegalArgumentException( () -> hardAndSoftRange(Range.atMost(money(26)), Range.all())); // Range.all is always invalid
  }

  @Override
  public HardAndSoftRange<SignedMoney> makeTrivialObject() {
    return hardAndSoftRange(Range.atMost(signedMoney(1)), Range.atMost(signedMoney(0)));
  }

  @Override
  public HardAndSoftRange<SignedMoney> makeNontrivialObject() {
    return testHardAndSoftRangeWithSeed(v -> signedMoney(v), ZERO_SEED);
  }

  @Override
  public HardAndSoftRange<SignedMoney> makeMatchingNontrivialObject() {
    return testHardAndSoftRangeWithSeed(v -> signedMoney(v), EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(HardAndSoftRange<SignedMoney> expected, HardAndSoftRange<SignedMoney> actual) {
    return hardAndSoftRangeMatcher(expected).matches(actual);
  }

  public static <T extends RBNumeric<? super T>> TypeSafeMatcher<HardAndSoftRange<T>> hardAndSoftRangeMatcher(
      HardAndSoftRange<T> expected) {
    return makeMatcher(expected,
        match(v -> v.getHardRange(), f -> rbNumericRangeMatcher(f, 1e-8)),
        match(v -> v.getSoftRange(), f -> rbNumericRangeMatcher(f, 1e-8)));
  }

}
