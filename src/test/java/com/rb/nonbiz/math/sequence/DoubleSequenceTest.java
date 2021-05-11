package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.math.sequence.DoubleSequence.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.sequence.ArithmeticProgressionTest.arithmeticProgressionMatcher;
import static com.rb.nonbiz.math.sequence.GeometricProgressionTest.geometricProgressionMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class DoubleSequenceTest extends RBTestMatcher<DoubleSequence> {

  @Override
  public DoubleSequence makeTrivialObject() {
    return new ArithmeticProgressionTest().makeTrivialObject();
  }

  @Override
  public DoubleSequence makeNontrivialObject() {
    return new GeometricProgressionTest().makeNontrivialObject();
  }

  @Override
  public DoubleSequence makeMatchingNontrivialObject() {
    return new GeometricProgressionTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(DoubleSequence expected, DoubleSequence actual) {
    return doubleSequenceMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<DoubleSequence> doubleSequenceMatcher(DoubleSequence expected) {
    return generalVisitorMatcher(expected, f -> f.visit(new Visitor<VisitorMatchInfo<DoubleSequence>>() {
      @Override
      public VisitorMatchInfo<DoubleSequence> visitArithmeticProgression(ArithmeticProgression arithmeticProgression) {
        return visitorMatchInfo(1, arithmeticProgression,
            (MatcherGenerator<ArithmeticProgression>) f1 -> arithmeticProgressionMatcher(f1));
      }

      @Override
      public VisitorMatchInfo<DoubleSequence> visitGeometricProgression(GeometricProgression geometricProgression) {
        return visitorMatchInfo(2, geometricProgression,
            (MatcherGenerator<GeometricProgression>) f1 -> geometricProgressionMatcher(f1));
      }
    }));
  }

}
