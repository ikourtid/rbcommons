package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingDefaultWeightTest.createArtificialTermForMaxUsingDefaultWeightMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingSpecifiedWeightTest.createArtificialTermForMaxUsingSpecifiedWeightMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMax.doNotCreateArtificialTermForMax;
import static com.rb.nonbiz.math.optimization.highlevel.DoNotCreateArtificialTermForMaxTest.doNotCreateArtificialTermForMaxMatcher;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.VisitorMatchInfo.visitorMatchInfo;
import static com.rb.nonbiz.testmatchers.RBVisitorMatchers.generalVisitorMatcher;

public class MaxSuperVarGenerationInstructionsTest extends RBTestMatcher<MaxSuperVarGenerationInstructions> {

  @Override
  public MaxSuperVarGenerationInstructions makeTrivialObject() {
    return doNotCreateArtificialTermForMax();
  }

  @Override
  public MaxSuperVarGenerationInstructions makeNontrivialObject() {
    return new CreateArtificialTermForMaxUsingSpecifiedWeightTest().makeNontrivialObject();
  }

  @Override
  public MaxSuperVarGenerationInstructions makeMatchingNontrivialObject() {
    return new CreateArtificialTermForMaxUsingSpecifiedWeightTest().makeMatchingNontrivialObject();
  }

  @Override
  protected boolean willMatch(MaxSuperVarGenerationInstructions expected,
                              MaxSuperVarGenerationInstructions actual) {
    return maxSuperVarGenerationInstructionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<MaxSuperVarGenerationInstructions> maxSuperVarGenerationInstructionsMatcher(
      MaxSuperVarGenerationInstructions expected) {
    return generalVisitorMatcher(expected, instructions -> instructions.visit(
        new Visitor<VisitorMatchInfo<MaxSuperVarGenerationInstructions>>() {
          @Override
          public VisitorMatchInfo<MaxSuperVarGenerationInstructions> visitDoNotCreateArtificialTermForMax(
              DoNotCreateArtificialTermForMax doNotCreateArtificialTermForMax) {
            return visitorMatchInfo(1, doNotCreateArtificialTermForMax,
                (MatcherGenerator<DoNotCreateArtificialTermForMax>) f -> doNotCreateArtificialTermForMaxMatcher(f));
          }

          @Override
          public VisitorMatchInfo<MaxSuperVarGenerationInstructions> visitCreateArtificialTermForMaxUsingDefaultWeight(
              CreateArtificialTermForMaxUsingDefaultWeight createArtificialTermForMaxUsingDefaultWeight) {
            return visitorMatchInfo(2, createArtificialTermForMaxUsingDefaultWeight,
                (MatcherGenerator<CreateArtificialTermForMaxUsingDefaultWeight>) f -> createArtificialTermForMaxUsingDefaultWeightMatcher(f));
          }

          @Override
          public VisitorMatchInfo<MaxSuperVarGenerationInstructions> visitCreateArtificialTermForMaxUsingSpecifiedWeight(
              CreateArtificialTermForMaxUsingSpecifiedWeight createArtificialTermForMaxUsingSpecifiedWeight) {
            return visitorMatchInfo(3, createArtificialTermForMaxUsingSpecifiedWeight,
                (MatcherGenerator<CreateArtificialTermForMaxUsingSpecifiedWeight>) f -> createArtificialTermForMaxUsingSpecifiedWeightMatcher(f));
          }
        }));
  }

}
