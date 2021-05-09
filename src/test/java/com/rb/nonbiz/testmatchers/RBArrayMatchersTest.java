package com.rb.nonbiz.testmatchers;

import org.junit.Test;

import static com.rb.nonbiz.testmatchers.RBArrayMatchers.array2DMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.array3DMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArray2DMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArray2DMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBArrayMatchersTest {

  @Test
  public void testArray2D() {
    // These are intentionally constructed so that the 1st item subarray always has the same size (3),
    // in order to catch a bug.
    Integer[][] nonJagged = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
    Integer[][] jagged    = { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9 } };
    assertThat(nonJagged, array2DMatcher(nonJagged, f -> typeSafeEqualTo(f)));
    // We really shouldn't have jagged arrays anywhere. It's very confusing.
    // Still, the matcher should work in that case.
    assertThat(jagged, array2DMatcher(jagged, f -> typeSafeEqualTo(f)));
    assertThat(
        jagged,
        not(array2DMatcher(nonJagged, f -> typeSafeEqualTo(f))));
    assertThat(
        nonJagged,
        not(array2DMatcher(jagged, f -> typeSafeEqualTo(f))));
  }

  @Test
  public void testIntArray2D() {
    // These are intentionally constructed so that the 1st item subarray always has the same size (3),
    // in order to catch a bug.
    int[][] nonJagged = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
    int[][] jagged    = { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9 } };
    assertThat(nonJagged, intArray2DMatcher(nonJagged));
    // We really shouldn't have jagged arrays anywhere. It's very confusing.
    // Still, the matcher should work in that case.
    assertThat(jagged, intArray2DMatcher(jagged));
    assertThat(
        jagged,
        not(intArray2DMatcher(nonJagged)));
    assertThat(
        nonJagged,
        not(intArray2DMatcher(jagged)));
  }

  @Test
  public void testDoubleArray2D() {
    double e = 1e-8; // epsilon
    // These are intentionally constructed so that the 1st item subarray always has the same size (3),
    // in order to catch a bug.
    double[][] nonJagged = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
    double[][] jagged    = { { 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9 } };
    assertThat(nonJagged, doubleArray2DMatcher(nonJagged, e));
    // We really shouldn't have jagged arrays anywhere. It's very confusing.
    // Still, the matcher should work in that case.
    assertThat(jagged, doubleArray2DMatcher(jagged, e));
    assertThat(
        jagged,
        not(doubleArray2DMatcher(nonJagged, e)));
    assertThat(
        nonJagged,
        not(doubleArray2DMatcher(jagged, e)));
  }

  @Test
  public void testArray3D() {
    // These are intentionally constructed so that the 1st item subarray always has the same size (3),
    // in order to catch a bug.
    Integer[][][] nonJagged = {
        { { 17, 27, 37 }, { 47, 57, 67 }, { 77, 87, 97 } },
        { { 18, 28, 38 }, { 48, 58, 68 }, { 78, 88, 98 } },
        { { 19, 29, 39 }, { 49, 59, 69 }, { 79, 89, 99 } },
    };
    Integer[][][] jagged = {
        { { 17, 27, 37 }, { 47, 57 }, { 67, 77, 87, 97 } },
        { { 18, 28, 38 }, { 48, 58 }, { 68, 78, 88, 98 } },
        { { 19, 29, 39 }, { 49, 59 }, { 69, 79, 89, 99 } },
    };
    assertThat(nonJagged, array3DMatcher(nonJagged, f -> typeSafeEqualTo(f)));
    // We really shouldn't have jagged arrays anywhere. It's very confusing.
    // Still, the matcher should work in that case.
    assertThat(jagged, array3DMatcher(jagged, f -> typeSafeEqualTo(f)));
    assertThat(
        jagged,
        not(array3DMatcher(nonJagged, f -> typeSafeEqualTo(f))));
    assertThat(
        nonJagged,
        not(array3DMatcher(jagged, f -> typeSafeEqualTo(f))));
  }

}
