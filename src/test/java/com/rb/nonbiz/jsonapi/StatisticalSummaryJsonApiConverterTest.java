package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.junit.Test;

import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.StatisticalSummaryImplBuilder.statisticalSummaryImplBuilder;
import static com.rb.nonbiz.math.stats.StatisticalSummaryTest.statisticalSummaryMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

public class StatisticalSummaryJsonApiConverterTest
    extends RBCommonsIntegrationTest<StatisticalSummaryJsonApiConverter> {

  @Test
  public void testRoundTrip() {
    JsonApiTestData<StatisticalSummary> statisticalSummaryJsonApiTestData = jsonApiTestData(
        f -> statisticalSummaryMatcher(f, DEFAULT_EPSILON_1e_8),

        jsonApiPair(
            statisticalSummaryImplBuilder()
                .setN(                234L)
                .setMean(             12.345)
                .setMin(              -5.678)
                .setMax(              67.890)
                .setStandardDeviation( 4.321)
                .build(),
            jsonObject(
                "n",                 jsonLong(  234L),
                "mean",              jsonDouble(12.345),
                "min",               jsonDouble(-5.678),
                "max",               jsonDouble(67.890),
                "standardDeviation", jsonDouble( 4.321),
                "variance",          jsonDouble(18.671041),
                "sum",               jsonDouble(2_888.73))));

    statisticalSummaryJsonApiTestData.testRoundTripConversions(
        statisticalSummaryImpl -> makeRealObject().toJsonObject(statisticalSummaryImpl),
        jsonObject             -> makeRealObject().fromJsonObject(jsonObject));
  }

  @Override
  protected Class<StatisticalSummaryJsonApiConverter> getClassBeingTested() {
    return StatisticalSummaryJsonApiConverter.class;
  }

}
