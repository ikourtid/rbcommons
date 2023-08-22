package com.rb.nonbiz.math.sequence;

import com.rb.biz.types.Money;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.math.sequence.SimpleSequence.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.collections.PairTest.pairMatcher;
import static com.rb.nonbiz.math.sequence.ArithmeticProgressionTest.arithmeticProgressionMatcher;
import static com.rb.nonbiz.math.sequence.ConstantSequenceTest.constantSequenceMatcher;
import static com.rb.nonbiz.math.sequence.GeometricProgressionTest.geometricProgressionMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class SimpleSequenceTest extends RBTestMatcher<SimpleSequence<Pair<String, Money>>> {

  @Override
  public SimpleSequence<Pair<String, Money>> makeTrivialObject() {
    return new ArithmeticProgressionTest().makeTrivialObject();
  }

  @Override
  public SimpleSequence<Pair<String, Money>> makeNontrivialObject() {
    return new GeometricProgressionTest().makeNontrivialObject();
  }

  @Override
  public SimpleSequence<Pair<String, Money>> makeMatchingNontrivialObject() {
    return new GeometricProgressionTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(SimpleSequence<Pair<String, Money>> expected, SimpleSequence<Pair<String, Money>> actual) {
    return simpleSequenceMatcher(
        expected,
        f -> pairMatcher(f, f2 -> typeSafeEqualTo(f2), f3 -> preciseValueMatcher(f3, DEFAULT_EPSILON_1e_8)))
        .matches(actual);
  }

  public static <T> TypeSafeMatcher<SimpleSequence<T>> simpleSequenceMatcher(
      SimpleSequence<T> expected, MatcherGenerator<T> matcherGenerator) {
    return generalVisitorMatcher(expected, f -> f.visit(new Visitor<T, VisitorMatchInfo<SimpleSequence<T>>>() {
      @Override
      public VisitorMatchInfo<SimpleSequence<T>> visitConstantSequence(ConstantSequence<T> constantSequence) {
        return visitorMatchInfo(1, constantSequence,
            (MatcherGenerator<ConstantSequence<T>>) f1 -> constantSequenceMatcher(f1, matcherGenerator));
      }

      @Override
      public VisitorMatchInfo<SimpleSequence<T>> visitArithmeticProgression(ArithmeticProgression<T> arithmeticProgression) {
        return visitorMatchInfo(2, arithmeticProgression,
            (MatcherGenerator<ArithmeticProgression<T>>) f1 -> arithmeticProgressionMatcher(f1, matcherGenerator));
      }

      @Override
      public VisitorMatchInfo<SimpleSequence<T>> visitGeometricProgression(GeometricProgression<T> geometricProgression) {
        return visitorMatchInfo(3, geometricProgression,
            (MatcherGenerator<GeometricProgression<T>>) f1 -> geometricProgressionMatcher(f1, matcherGenerator));
      }
    }));
  }

}
