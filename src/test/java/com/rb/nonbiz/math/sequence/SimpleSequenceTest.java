package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.math.sequence.SimpleSequence.Visitor;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.sequence.ArithmeticProgressionTest.arithmeticProgressionMatcher;
import static com.rb.nonbiz.math.sequence.GeometricProgressionTest.geometricProgressionMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class SimpleSequenceTest extends RBTestMatcher<SimpleSequence> {

  @Override
  public SimpleSequence makeTrivialObject() {
    return new ArithmeticProgressionTest().makeTrivialObject();
  }

  @Override
  public SimpleSequence makeNontrivialObject() {
    return new GeometricProgressionTest().makeNontrivialObject();
  }

  @Override
  public SimpleSequence makeMatchingNontrivialObject() {
    return new GeometricProgressionTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(SimpleSequence expected, SimpleSequence actual) {
    return doubleSequenceMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<SimpleSequence> doubleSequenceMatcher(SimpleSequence expected) {
    return generalVisitorMatcher(expected, f -> f.visit(new Visitor<VisitorMatchInfo<SimpleSequence>>() {
      @Override
      public VisitorMatchInfo<SimpleSequence> visitArithmeticProgression(ArithmeticProgression arithmeticProgression) {
        return visitorMatchInfo(1, arithmeticProgression,
            (MatcherGenerator<ArithmeticProgression>) f1 -> arithmeticProgressionMatcher(f1));
      }

      @Override
      public VisitorMatchInfo<SimpleSequence> visitGeometricProgression(GeometricProgression geometricProgression) {
        return visitorMatchInfo(2, geometricProgression,
            (MatcherGenerator<GeometricProgression>) f1 -> geometricProgressionMatcher(f1));
      }
    }));
  }

}
