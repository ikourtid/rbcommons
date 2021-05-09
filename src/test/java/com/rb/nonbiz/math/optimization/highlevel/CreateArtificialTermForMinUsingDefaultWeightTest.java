package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.CreateArtificialTermForMinUsingDefaultWeight.createArtificialTermForMinUsingDefaultWeight;
import static com.rb.nonbiz.testmatchers.Match.matchUsingDoubleAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class CreateArtificialTermForMinUsingDefaultWeightTest extends RBTestMatcher<CreateArtificialTermForMinUsingDefaultWeight> {

  @Test
  public void negativeOrZeroValues_throws() {
    assertIllegalArgumentException( () -> createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(-0.1)));
    assertIllegalArgumentException( () -> createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(0.0)));
    CreateArtificialTermForMinUsingDefaultWeight doesNotThrow = createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(1e-6));
  }

  @Override
  public CreateArtificialTermForMinUsingDefaultWeight makeTrivialObject() {
    return createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(1.0));
  }

  @Override
  public CreateArtificialTermForMinUsingDefaultWeight makeNontrivialObject() {
    return createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(0.1));
  }

  @Override
  public CreateArtificialTermForMinUsingDefaultWeight makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return createArtificialTermForMinUsingDefaultWeight(makeConstantsObject(0.1 + e));
  }

  @Override
  protected boolean willMatch(CreateArtificialTermForMinUsingDefaultWeight expected,
                              CreateArtificialTermForMinUsingDefaultWeight actual) {
    return createArtificialTermForMinUsingDefaultWeightMatcher(expected).matches(actual);
  }

  private RBCommonsConstants makeConstantsObject(double value) {
    return new RBCommonsConstants() {
      @Override
      public double getDefaultWeightForMinAndMaxArtificialTerms() {
        return value;
      }
    };
  }

  public static TypeSafeMatcher<CreateArtificialTermForMinUsingDefaultWeight> createArtificialTermForMinUsingDefaultWeightMatcher(
      CreateArtificialTermForMinUsingDefaultWeight expected) {
    return makeMatcher(expected,
        matchUsingDoubleAlmostEquals(v -> v.getDefaultWeight(), 1e-8));
  }

}
