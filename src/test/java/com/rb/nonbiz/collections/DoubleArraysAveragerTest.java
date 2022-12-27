package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static org.hamcrest.MatcherAssert.assertThat;

public class DoubleArraysAveragerTest extends RBTest<DoubleArraysAverager> {

  private final boolean IGNORE_AVERAGE_OF_SINGLE_ITEM = true;
  private final boolean ALLOW_AVERAGE_OF_SINGLE_ITEM = false;

  @Test
  public void noArrays_returnsEmpty() {
    assertOptionalEmpty(makeAverage(IGNORE_AVERAGE_OF_SINGLE_ITEM));
    assertOptionalEmpty(makeAverage(ALLOW_AVERAGE_OF_SINGLE_ITEM));
  }

  @Test
  public void singleArray_allowsAverageOfOneItem_returnsSame() {
    assertThat(
        makeAverage(ALLOW_AVERAGE_OF_SINGLE_ITEM, new double[] { 1.1, 2.2 }),
        nonEmptyOptionalMatcher(
            doubleArrayMatcher(new double[] { 1.1, 2.2 },
                epsilon(1e-12))));
  }

  @Test
  public void singleArray_ignoresAverageOfOneItem_returnsEmpty() {
    assertOptionalEmpty(
        makeAverage(IGNORE_AVERAGE_OF_SINGLE_ITEM, new double[] { 1.1, 2.2 }));
  }

  @Test
  public void emptyArrays_throws() {
    assertIllegalArgumentException( () -> makeAverage(IGNORE_AVERAGE_OF_SINGLE_ITEM, new double[] {}, new double[] {}));
    assertIllegalArgumentException( () -> makeAverage(ALLOW_AVERAGE_OF_SINGLE_ITEM, new double[] {}, new double[] {}));
  }

  @Test
  public void arraySizeMismatch_throws() {
    assertIllegalArgumentException( () -> makeAverage(
        IGNORE_AVERAGE_OF_SINGLE_ITEM,
        new double[] { DUMMY_DOUBLE },
        new double[] { DUMMY_DOUBLE, DUMMY_DOUBLE }));
    assertIllegalArgumentException( () -> makeAverage(
        ALLOW_AVERAGE_OF_SINGLE_ITEM,
        new double[] { DUMMY_DOUBLE },
        new double[] { DUMMY_DOUBLE, DUMMY_DOUBLE }));
  }

  @Test
  public void twoArrays_returnsAverage() {
    for (boolean ignoreAverageOfSingleItem : rbSetOf(true, false)) {
      assertThat(
          makeAverage(
              ignoreAverageOfSingleItem,
              new double[] { 1.1 },
              new double[] { 2.2 }),
          nonEmptyOptionalMatcher(
              doubleArrayMatcher(
                  new double[] { doubleExplained(1.65, (1.1 + 2.2) / 2) },
                  DEFAULT_EPSILON_1e_8)));
      assertThat(
          makeAverage(
              ignoreAverageOfSingleItem,
              new double[] { 1.1, 3.3, 5.5 },
              new double[] { 2.2, 4.4, 6.6 }),
          nonEmptyOptionalMatcher(doubleArrayMatcher(
              new double[] {
                  doubleExplained(1.65, (1.1 + 2.2) / 2),
                  doubleExplained(3.85, (3.3 + 4.4) / 2),
                  doubleExplained(6.05, (5.5 + 6.6) / 2),
              },
              DEFAULT_EPSILON_1e_8)));
    }
  }

  @Test
  public void generalCase_threeArrays_includesNegatives_returnsAverage() {
    for (boolean ignoreAverageOfSingleItem : rbSetOf(true, false)) {
      assertThat(
          makeAverage(
              ignoreAverageOfSingleItem,
              new double[] { -1.1, -3.3, -5.5 },
              new double[] { 2.2, 4.4, 6.6 },
              new double[] { 1.9, 4.9, 7.9 }),
          nonEmptyOptionalMatcher(doubleArrayMatcher(
              new double[] {
                  doubleExplained(1, (-1.1 + 2.2 + 1.9) / 3),
                  doubleExplained(2, (-3.3 + 4.4 + 4.9) / 3),
                  doubleExplained(3, (-5.5 + 6.6 + 7.9) / 3),
              },
              DEFAULT_EPSILON_1e_8)));
    }
  }

  private Optional<double[]> makeAverage(boolean ignoreAverageOfSingleItem, double[]...arrays) {
    return makeTestObject().calculateAverageArray(Arrays.asList(arrays).iterator(), ignoreAverageOfSingleItem);
  }

  @Override
  protected DoubleArraysAverager makeTestObject() {
    return new DoubleArraysAverager();
  }

}
