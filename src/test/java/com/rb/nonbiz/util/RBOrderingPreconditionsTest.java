package com.rb.nonbiz.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;
import java.util.function.BiConsumer;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.util.RBOrderingPreconditions.checkDoublesNotIncreasing;

public class RBOrderingPreconditionsTest {

  @Test
  public void testCheckDoublesNotIncreasing() {
    BiConsumer<Boolean, List<Double>> asserter = (willPass, doubleList) -> {
      if (!willPass) {
        assertIllegalArgumentException( () -> checkDoublesNotIncreasing(doubleList.iterator(), DEFAULT_EPSILON_1e_8, DUMMY_STRING));
        assertIllegalArgumentException( () -> checkDoublesNotIncreasing(doubleList, DEFAULT_EPSILON_1e_8, DUMMY_STRING));
        assertIllegalArgumentException( () -> checkDoublesNotIncreasing(doubleList, DEFAULT_EPSILON_1e_8));
      } else {
        // we'll call these and expect no exception, but there's no return value to look at.
        checkDoublesNotIncreasing(doubleList.iterator(), DEFAULT_EPSILON_1e_8, DUMMY_STRING);
        checkDoublesNotIncreasing(doubleList, DEFAULT_EPSILON_1e_8, DUMMY_STRING);
        checkDoublesNotIncreasing(doubleList, DEFAULT_EPSILON_1e_8);
      }
    };

    asserter.accept(true, ImmutableList.of(9.0, 8.0,        7.0));        // decreasing, so definitely not increasing
    asserter.accept(true, ImmutableList.of(9.0, 9.0,        7.0));        // 9 and 9; not decreasing, but also not increasing
    asserter.accept(true, ImmutableList.of(9.0, 9.0 + 1e-9, 7.0));        // increasing, but below epsilon
    asserter.accept(true, ImmutableList.of(9.0, 8.0,        8.0 + 1e-9)); // increasing, but below epsilon

    asserter.accept(true, ImmutableList.of(9.0, 9.0));
    asserter.accept(true, ImmutableList.of(9.0, 9.0 + 1e-9));
  }

}
