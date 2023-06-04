package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.math.stats.StatisticalSummaryImpl;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.jsonapi.JsonApiTestPair.jsonApiTestPair;
import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.statisticalSummaryImpl;
import static com.rb.nonbiz.math.stats.StatisticalSummaryImplTest.statisticalSummaryImplMatcher;

public class StatisticalSummaryImplJsonApiConverterTest
    extends RBCommonsIntegrationTest<StatisticalSummaryImplJsonApiConverter> {

  @Test
  public void testRoundTrip() {
    JsonApiTestData<StatisticalSummaryImpl> statisticalSummaryJsonApiTestData = jsonApiTestData(
        f -> statisticalSummaryImplMatcher(f),

        jsonApiTestPair(
            statisticalSummaryImpl(
                234L,
                12.345,
                -5.678,
                67.890,
                4.321,
                17.89,
                2_888.73),
            jsonObject(
                "n",                 jsonLong(  234L),
                "mean",              jsonDouble(12.345),
                "min",               jsonDouble(-5.678),
                "max",               jsonDouble(67.890),
                "standardDeviation", jsonDouble( 4.321),
                "variance",          jsonDouble(17.89),
                "sum",               jsonDouble(2_888.73))));

    statisticalSummaryJsonApiTestData.testRoundTripConversions(
        statisticalSummaryImpl -> makeRealObject().toJsonObject(statisticalSummaryImpl),
        jsonObject             -> makeRealObject().fromJsonObject(jsonObject));
  }

  @Override
  protected Class<StatisticalSummaryImplJsonApiConverter> getClassBeingTested() {
    return StatisticalSummaryImplJsonApiConverter.class;
  }

}
