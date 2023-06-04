package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.stats.RBStatisticalSummary;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static com.rb.nonbiz.math.stats.RBStatisticalSummary.rbStatisticalSummary;
import static org.junit.Assert.*;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class SummaryStatisticsJsonApiConverterTest extends RBCommonsIntegrationTest<SummaryStatisticsJsonApiConverter> {

  @Test
  public void testToJson() {
    SummaryStatistics summaryStatistics = new SummaryStatistics();
    for(int i = -1 ; i < 21 ; i++) {
      summaryStatistics.addValue(i);
    }

    // create an RBStatisticalSummary in order to use its matcher
    RBStatisticalSummary<Integer> rbStatisticalSummary = rbStatisticalSummary(
        summaryStatistics,
        v -> (int) v);

    makeRealObject().toJsonObject(summaryStatistics);

  }

  @Override
  protected Class<SummaryStatisticsJsonApiConverter> getClassBeingTested() {
    return SummaryStatisticsJsonApiConverter.class;
  }

}
