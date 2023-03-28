package com.rb.nonbiz.collections;

import org.junit.Test;

import static com.rb.nonbiz.collections.RBArrays.cutFromArray;
import static com.rb.nonbiz.collections.RBArrays.intArrayWithNCopies;
import static com.rb.nonbiz.collections.RBArrays.spliceIntoArray;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArrayMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBArraysTest {

  @Test
  public void cutFromArray_initialArrayTooSmall_throws() {
    assertIllegalArgumentException( () -> cutFromArray(new int[] {}, 0));
    assertIllegalArgumentException( () -> cutFromArray(new int[] { DUMMY_POSITIVE_INTEGER }, 0));
    int[] doesNotThrow = cutFromArray(new int[] { DUMMY_POSITIVE_INTEGER, DUMMY_POSITIVE_INTEGER }, 0);
  }

  @Test
  public void cutFromArray_passingWrongIndex_throws() {
    int[] arr = new int[] { 70, 71, 72 };
    assertIllegalArgumentException( () -> cutFromArray(arr, -999));
    assertIllegalArgumentException( () -> cutFromArray(arr, -1));
    int[] doesNotThrow;
    doesNotThrow = cutFromArray(arr, 0);
    doesNotThrow = cutFromArray(arr, 1);
    doesNotThrow = cutFromArray(arr, 2);
    assertIllegalArgumentException( () -> cutFromArray(arr, 3));
    assertIllegalArgumentException( () -> cutFromArray(arr, 999));
  }

  @Test
  public void cutFromArray_generalCase() {
    {
      int[] arr = new int[] { 70, 71 };
      assertThat(cutFromArray(arr, 0), intArrayMatcher(new int[] { 71 }));
      assertThat(cutFromArray(arr, 1), intArrayMatcher(new int[] { 70 }));
    }
    {
      int[] arr = new int[] { 70, 71, 72 };
      assertThat(cutFromArray(arr, 0), intArrayMatcher(new int[] { 71, 72 }));
      assertThat(cutFromArray(arr, 1), intArrayMatcher(new int[] { 70, 72 }));
      assertThat(cutFromArray(arr, 2), intArrayMatcher(new int[] { 70, 71 }));
    }
    {
      int[] arr = new int[] { 70, 71, 72, 73 };
      assertThat(cutFromArray(arr, 0), intArrayMatcher(new int[] { 71, 72, 73 }));
      assertThat(cutFromArray(arr, 1), intArrayMatcher(new int[] { 70, 72, 73 }));
      assertThat(cutFromArray(arr, 2), intArrayMatcher(new int[] { 70, 71, 73 }));
      assertThat(cutFromArray(arr, 3), intArrayMatcher(new int[] { 70, 71, 72 }));
    }
  }

  @Test
  public void spliceIntoArray_passingWrongIndex_throws() {
    assertIllegalArgumentException( () -> spliceIntoArray(new int[] { 71, 72, 73 }, -1, DUMMY_POSITIVE_INTEGER));
    int[] doesNotThrow;
    doesNotThrow = spliceIntoArray(new int[] { 71, 72, 73 }, 0, DUMMY_POSITIVE_INTEGER);
    doesNotThrow = spliceIntoArray(new int[] { 71, 72, 73 }, 1, DUMMY_POSITIVE_INTEGER);
    doesNotThrow = spliceIntoArray(new int[] { 71, 72, 73 }, 2, DUMMY_POSITIVE_INTEGER);
    doesNotThrow = spliceIntoArray(new int[] { 71, 72, 73 }, 3, DUMMY_POSITIVE_INTEGER);
    assertIllegalArgumentException( () -> spliceIntoArray(new int[] { 71, 72, 73 }, 4, DUMMY_POSITIVE_INTEGER));
  }

  @Test
  public void spliceIntoArray_generalCase() {
    {
      int[] arr = new int[] { 70 };
      assertThat(spliceIntoArray(arr, 0, 99), intArrayMatcher(new int[] { 99, 70 }));
      assertThat(spliceIntoArray(arr, 1, 99), intArrayMatcher(new int[] { 70, 99 }));
    }
    {
      int[] arr = new int[] { 70, 71, 72 };
      assertThat(spliceIntoArray(arr, 0, 99), intArrayMatcher(new int[] { 99, 70, 71, 72 }));
      assertThat(spliceIntoArray(arr, 1, 99), intArrayMatcher(new int[] { 70, 99, 71, 72 }));
      assertThat(spliceIntoArray(arr, 2, 99), intArrayMatcher(new int[] { 70, 71, 99, 72 }));
      assertThat(spliceIntoArray(arr, 3, 99), intArrayMatcher(new int[] { 70, 71, 72, 99 }));
    }
  }

  @Test
  public void testintArrayWithNCopies() {
    assertThat(
        intArrayWithNCopies(10, 4),
        intArrayMatcher(new int[] {4, 4, 4, 4, 4, 4, 4, 4, 4, 4}));
    assertThat(
        intArrayWithNCopies(4, 10),
        intArrayMatcher(new int[] {10, 10, 10, 10}));
    assertThat(
        intArrayWithNCopies(2, 0),
        intArrayMatcher(new int[] {0, 0}));
    assertThat(
        intArrayWithNCopies(4, -11),
        intArrayMatcher(new int[] {-11, -11, -11, -11}));

    // Negative and zero size not allowed.
    assertIllegalArgumentException( () -> intArrayWithNCopies(-2, 4));
    assertIllegalArgumentException( () -> intArrayWithNCopies(0, 4));
  }

}
