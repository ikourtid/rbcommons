package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.math.eigen.EigenExplainabilityFraction.eigenExplainabilityFraction;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityRestrictions.emptyEigenExplainabilityRestrictions;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityRestrictions.restrictBothNumEigenvectorsAndExplainability;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityRestrictions.restrictNumEigenvectors;
import static com.rb.nonbiz.math.eigen.EigenExplainabilityTest.DUMMY_EIGEN_EXPLAINABILITY_FRACTION;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalInt;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalPreciseValue;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class EigenExplainabilityRestrictionsTest extends RBTestMatcher<EigenExplainabilityRestrictions> {

  @Test
  public void maxNumEigenvectorsMustBeOneOrMore() {
    assertIllegalArgumentException( () -> restrictNumEigenvectors(-99));
    assertIllegalArgumentException( () -> restrictNumEigenvectors(-1));
    assertIllegalArgumentException( () -> restrictNumEigenvectors(0));

    assertIllegalArgumentException( () -> restrictBothNumEigenvectorsAndExplainability(-99, DUMMY_EIGEN_EXPLAINABILITY_FRACTION));
    assertIllegalArgumentException( () -> restrictBothNumEigenvectorsAndExplainability(-1, DUMMY_EIGEN_EXPLAINABILITY_FRACTION));
    assertIllegalArgumentException( () -> restrictBothNumEigenvectorsAndExplainability(0, DUMMY_EIGEN_EXPLAINABILITY_FRACTION));

    EigenExplainabilityRestrictions doesNotThrow;
    doesNotThrow = restrictNumEigenvectors(1);
    doesNotThrow = restrictNumEigenvectors(99);

    doesNotThrow = restrictBothNumEigenvectorsAndExplainability(1, DUMMY_EIGEN_EXPLAINABILITY_FRACTION);
    doesNotThrow = restrictBothNumEigenvectorsAndExplainability(99, DUMMY_EIGEN_EXPLAINABILITY_FRACTION);
  }

  @Override
  public EigenExplainabilityRestrictions makeTrivialObject() {
    return emptyEigenExplainabilityRestrictions();
  }

  @Override
  public EigenExplainabilityRestrictions makeNontrivialObject() {
    return restrictBothNumEigenvectorsAndExplainability(3, eigenExplainabilityFraction(0.2));
  }

  @Override
  public EigenExplainabilityRestrictions makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return restrictBothNumEigenvectorsAndExplainability(3, eigenExplainabilityFraction(0.2 + e));
  }

  @Override
  protected boolean willMatch(EigenExplainabilityRestrictions expected, EigenExplainabilityRestrictions actual) {
    return eigenExplainabilityRestrictionsMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<EigenExplainabilityRestrictions> eigenExplainabilityRestrictionsMatcher(
      EigenExplainabilityRestrictions expected) {
    return makeMatcher(expected,
        matchOptionalInt(v -> v.getMaxNumEigenvectors()),
        matchOptionalPreciseValue(v -> v.getMaxEigenExplainabilityFraction(), 1e-8));
  }

}
