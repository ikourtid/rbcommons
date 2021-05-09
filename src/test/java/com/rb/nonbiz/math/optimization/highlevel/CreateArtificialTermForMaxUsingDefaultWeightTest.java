package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMaxUsingDefaultWeight.createArtificialTermForMaxUsingDefaultWeight;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class CreateArtificialTermForMaxUsingDefaultWeightTest extends RBTestMatcher<CreateArtificialTermForMaxUsingDefaultWeight> {

  @Test
  public void negativeOrZeroValues_throws() {
    assertIllegalArgumentException( () -> createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(-0.1)));
    assertIllegalArgumentException( () -> createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(0.0)));
    CreateArtificialTermForMaxUsingDefaultWeight doesNotThrow = createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(1e-6));
  }

  @Override
  public CreateArtificialTermForMaxUsingDefaultWeight makeTrivialObject() {
    return createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(1.0));
  }

  @Override
  public CreateArtificialTermForMaxUsingDefaultWeight makeNontrivialObject() {
    return createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(0.1));
  }

  @Override
  public CreateArtificialTermForMaxUsingDefaultWeight makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return createArtificialTermForMaxUsingDefaultWeight(makeConstantsObject(0.1 + e));
  }

  @Override
  protected boolean willMatch(CreateArtificialTermForMaxUsingDefaultWeight expected,
                              CreateArtificialTermForMaxUsingDefaultWeight actual) {
    return createArtificialTermForMaxUsingDefaultWeightMatcher(expected).matches(actual);
  }

  private RBCommonsConstants makeConstantsObject(double value) {
    return new RBCommonsConstants() {
      @Override
      public double getDefaultWeightForMinAndMaxArtificialTerms() {
        return value;
      }
    };
  }

  public static TypeSafeMatcher<CreateArtificialTermForMaxUsingDefaultWeight> createArtificialTermForMaxUsingDefaultWeightMatcher(
      CreateArtificialTermForMaxUsingDefaultWeight expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getDefaultWeight(), 1e-8));
  }

}
