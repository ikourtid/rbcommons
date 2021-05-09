package com.rb.nonbiz.math.optimization.highlevel;

import com.rb.nonbiz.math.optimization.general.RawVariable;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.zeroConstantHighLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressionTest.highLevelVarExpressionMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpressions.highLevelVarExpressionOfOptionals;
import static com.rb.nonbiz.types.Weighted.weighted;
import static org.hamcrest.MatcherAssert.assertThat;

public class HighLevelVarExpressionsTest {

  @Test
  public void testHighLevelVarExpressionOfOptionals() {
    RawVariable x = rawVariable("x", 0);
    RawVariable y = rawVariable("y", 2);
    assertThat(
        highLevelVarExpressionOfOptionals(
        weighted(Optional.of(x), 1.1),
        weighted(Optional.empty(), 2.2),
        weighted(Optional.of(y), 3.3)),
        highLevelVarExpressionMatcher(highLevelVarExpression(1.1, x, 3.3, y)));
    assertThat(
        highLevelVarExpressionOfOptionals(
            weighted(Optional.empty(), 1.1),
            weighted(Optional.empty(), 2.2),
            weighted(Optional.empty(), 3.3)),
        highLevelVarExpressionMatcher(zeroConstantHighLevelVarExpression()));
    // This usage isn't ideal, but at least it would work as expected
    assertThat(
        highLevelVarExpressionOfOptionals(),
        highLevelVarExpressionMatcher(zeroConstantHighLevelVarExpression()));
  }
  
}
