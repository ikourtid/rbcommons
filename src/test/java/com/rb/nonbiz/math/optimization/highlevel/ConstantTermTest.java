package com.rb.nonbiz.math.optimization.highlevel;

import org.junit.Test;

import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.zeroConstantTerm;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConstantTermTest {

  @Test
  public void testAdd() {
    assertAlmostEquals(
        constantTerm(2.2).add(constantTerm(-5.5)),
        constantTerm(-3.3),
        1e-8);
    assertAlmostEquals(
        constantTerm(-5.5).add(constantTerm(2.2)),
        constantTerm(-3.3),
        1e-8);
  }

  @Test
  public void testNegate() {
    assertAlmostEquals(
        constantTerm(2.2).negate(),
        constantTerm(-2.2),
        1e-8);
    assertAlmostEquals(
        constantTerm(-2.2).negate(),
        constantTerm(2.2),
        1e-8);
    assertAlmostEquals(
        zeroConstantTerm().negate(),
        zeroConstantTerm(),
        1e-8);
  }

  @Test
  public void testMultiply() {
    assertAlmostEquals(
        constantTerm(2.2).multiply(3),
        constantTerm(6.6),
        1e-8);
    assertAlmostEquals(
        constantTerm(2.2).multiply(-3),
        constantTerm(-6.6),
        1e-8);
    assertAlmostEquals(
        constantTerm(2.2).multiply(0),
        zeroConstantTerm(),
        1e-8);
  }

  @Test
  public void isZero() {
    assertTrue(zeroConstantTerm().isZero());
    assertTrue(constantTerm(0).isZero());
    assertFalse(constantTerm(-999).isZero());
    assertFalse(constantTerm(-1e-10).isZero());
    assertFalse(constantTerm(1e-10).isZero());
    assertFalse(constantTerm(999).isZero());
  }

}
