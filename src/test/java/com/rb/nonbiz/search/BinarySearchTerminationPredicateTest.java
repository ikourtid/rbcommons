package com.rb.nonbiz.search;

import org.junit.Test;

import java.math.BigDecimal;

import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.onlyTerminateBasedOnX;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.onlyTerminateBasedOnY;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.terminateBasedOnXandY;
import static com.rb.nonbiz.search.BinarySearchTerminationPredicate.terminateBasedOnXorY;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_BIG_DECIMAL;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Unlike almost all data classes, we won't extend RBTestMatcher,
// because this data class mostly consists of predicates, and lambdas cannot be matched,
// so there's no point in trying to match on a BinarySearchTerminationPredicate.
public class BinarySearchTerminationPredicateTest {

  @Test
  public void testOnlyX() {
    BinarySearchTerminationPredicate<Double, BigDecimal> predicate = onlyTerminateBasedOnX( (x1, x2) -> (x2 - x1) < 7);
    BigDecimal dummyY = DUMMY_BIG_DECIMAL;
    assertFalse(predicate.test(10.0, 17.1, dummyY, dummyY));
    assertTrue( predicate.test(10.0, 16.9, dummyY, dummyY));
  }

  @Test
  public void testOnlyY() {
    BinarySearchTerminationPredicate<Double, BigDecimal> predicate = onlyTerminateBasedOnY( (y1, y2) -> y1.compareTo(y2) > 0);
    double dummyX = DUMMY_DOUBLE;
    assertFalse(predicate.test(dummyX, dummyX, BigDecimal.ONE, BigDecimal.TEN));
    assertFalse(predicate.test(dummyX, dummyX, BigDecimal.ONE, BigDecimal.ONE));
    assertTrue( predicate.test(dummyX, dummyX, BigDecimal.ONE, BigDecimal.ZERO));
  }

  @Test
  public void testXorY() {
    BinarySearchTerminationPredicate<Double, BigDecimal> predicate = terminateBasedOnXorY(
        (x1, x2) -> (x2 - x1) < 7,
        (y1, y2) -> y1.compareTo(y2) > 0);

    // X part of predicate is false
    assertFalse(predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.TEN));  // Y part of predicate is false
    assertFalse(predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.ONE));  // Y part of predicate is false
    assertTrue( predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.ZERO)); // Y part of predicate is true

    // X part of predicate is true
    assertTrue(predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.TEN));  // Y part of predicate is false
    assertTrue(predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.ONE));  // Y part of predicate is false
    assertTrue(predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.ZERO)); // Y part of predicate is true
  }

  @Test
  public void testXandY() {
    BinarySearchTerminationPredicate<Double, BigDecimal> predicate = terminateBasedOnXandY(
        (x1, x2) -> (x2 - x1) < 7,
        (y1, y2) -> y1.compareTo(y2) > 0);

    // X part of predicate is false
    assertFalse(predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.TEN));  // Y part of predicate is false
    assertFalse(predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.ONE));  // Y part of predicate is false
    assertFalse(predicate.test(10.0, 17.1, BigDecimal.ONE, BigDecimal.ZERO)); // Y part of predicate is true

    // X part of predicate is true
    assertFalse(predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.TEN));  // Y part of predicate is false
    assertFalse(predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.ONE));  // Y part of predicate is false
    assertTrue( predicate.test(10.0, 16.9, BigDecimal.ONE, BigDecimal.ZERO)); // Y part of predicate is true
  }

}
