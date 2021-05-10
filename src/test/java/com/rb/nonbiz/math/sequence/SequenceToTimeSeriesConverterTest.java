package com.rb.nonbiz.math.sequence;

import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.time.Year;

import static com.rb.biz.types.collections.ts.YearlyTimeSeriesTest.yearlyTestTimeSeries;
import static com.rb.biz.types.collections.ts.YearlyTimeSeriesTest.yearlyTimeSeriesMatcher;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRange.singletonClosedRange;
import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;

public class SequenceToTimeSeriesConverterTest extends RBTest<SequenceToTimeSeriesConverter> {

  @Test
  public void generalCase() {
    assertThat(
        makeTestObject().toYearlyTimeSeries(
            DUMMY_LABEL,
            geometricProgressionBuilder()
                .setInitialValue(100.0)
                .setCommonRatio(2.0)
                .build(),
            closedRange(Year.of(2010), Year.of(2015))),
        yearlyTimeSeriesMatcher(
            yearlyTestTimeSeries(Year.of(2010), Year.of(2015),
                100.0, 200.0, 400.0, 800.0, 1_600.0, 3_200.0),
            f -> doubleAlmostEqualsMatcher(f, 1e-8)));
  }

  @Test
  public void singletonCase() {
    assertThat(
        makeTestObject().toYearlyTimeSeries(
            DUMMY_LABEL,
            geometricProgressionBuilder()
                .setInitialValue(100.0)
                .setCommonRatio(2.0)
                .build(),
            singletonClosedRange(Year.of(2010))),
        yearlyTimeSeriesMatcher(
            yearlyTestTimeSeries(Year.of(2010), Year.of(2010), 100.0),
            f -> doubleAlmostEqualsMatcher(f, 1e-8)));
  }

  @Override
  protected SequenceToTimeSeriesConverter makeTestObject() {
    return new SequenceToTimeSeriesConverter();
  }

}
