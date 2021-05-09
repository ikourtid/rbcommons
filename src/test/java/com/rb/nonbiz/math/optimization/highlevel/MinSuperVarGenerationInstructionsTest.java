package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingDefaultWeightTest.createArtificialTermForMinUsingDefaultWeightMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingSpecifiedWeightTest.createArtificialTermForMinUsingSpecifiedWeightMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMin.doNotCreateArtificialTermForMin;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMinTest.doNotCreateArtificialTermForMinMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class MinSuperVarGenerationInstructionsTest extends RBTestMatcher<MinSuperVarGenerationInstructions> {

  @Override
  public MinSuperVarGenerationInstructions makeTrivialObject() {
    return doNotCreateArtificialTermForMin();
  }

  @Override
  public MinSuperVarGenerationInstructions makeNontrivialObject() {
    return new CreateArtificialTermForMinUsingSpecifiedWeightTest().makeNontrivialObject();
  }

  @Override
  public MinSuperVarGenerationInstructions makeMatchingNontrivialObject() {
    return new CreateArtificialTermForMinUsingSpecifiedWeightTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(MinSuperVarGenerationInstructions expected,
                              MinSuperVarGenerationInstructions actual) {
    return minSuperVarGenerationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<MinSuperVarGenerationInstructions> minSuperVarGenerationInstructionsMatcher(
      MinSuperVarGenerationInstructions expected) {
    return generalVisitorMatcher(expected, instructions -> instructions.visit(
        new Visitor<VisitorMatchInfo<MinSuperVarGenerationInstructions>>() {
          @Override
          public VisitorMatchInfo<MinSuperVarGenerationInstructions> visitDoNotCreateArtificialTermForMin(
              DoNotCreateArtificialTermForMin doNotCreateArtificialTermForMin) {
            return visitorMatchInfo(1, doNotCreateArtificialTermForMin,
                (MatcherGenerator<DoNotCreateArtificialTermForMin>) f -> doNotCreateArtificialTermForMinMatcher(f));
          }

          @Override
          public VisitorMatchInfo<MinSuperVarGenerationInstructions> visitCreateArtificialTermForMinUsingDefaultWeight(
              CreateArtificialTermForMinUsingDefaultWeight createArtificialTermForMinUsingDefaultWeight) {
            return visitorMatchInfo(2, createArtificialTermForMinUsingDefaultWeight,
                (MatcherGenerator<CreateArtificialTermForMinUsingDefaultWeight>) f -> createArtificialTermForMinUsingDefaultWeightMatcher(f));
          }

          @Override
          public VisitorMatchInfo<MinSuperVarGenerationInstructions> visitCreateArtificialTermForMinUsingSpecifiedWeight(
              CreateArtificialTermForMinUsingSpecifiedWeight createArtificialTermForMinUsingSpecifiedWeight) {
            return visitorMatchInfo(3, createArtificialTermForMinUsingSpecifiedWeight,
                (MatcherGenerator<CreateArtificialTermForMinUsingSpecifiedWeight>) f -> createArtificialTermForMinUsingSpecifiedWeightMatcher(f));
          }
        }));
  }

}
