package com.rb.nonbiz.json;

import org.junit.Test;

import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArrayTest.rbJsonDoubleArrayMatcher;
import static com.rb.nonbiz.json.RBJsonDoubleArrays.convertClosedRangeToRBJsonDoubleArray;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBJsonDoubleArraysTest {

  @Test
  public void testConvertClosedRangeToJsonDoubleArray() {
    assertThat(
        convertClosedRangeToRBJsonDoubleArray(closedRange(signedMoney(-1.1), signedMoney(3.3))),
        rbJsonDoubleArrayMatcher(
            rbJsonDoubleArray(-1.1, 3.3),
            DEFAULT_EPSILON_1e_8));
  }

}
