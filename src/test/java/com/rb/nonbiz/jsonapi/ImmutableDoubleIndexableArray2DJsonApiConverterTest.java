package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2DTest.immutableDoubleIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class ImmutableDoubleIndexableArray2DJsonApiConverterTest
  extends RBTest<ImmutableDoubleIndexableArray2DJsonApiConverter> {

  @Test
  public void testBackAndForth() {
    ImmutableDoubleIndexableArray2D<String, Long> array2D = immutableDoubleIndexableArray2D(
        new double[][] {
            { 7.1, 7.2 },
            { 8.1, 8.2 },
            { 9.1, 9.2 }
        },
        simpleArrayIndexMapping("a", "b", "c"),
        simpleArrayIndexMapping(100L, 200L));
    JsonObject jsonObject = jsonObject(
        "rowKeys", jsonStringArray("a", "b", "c"),
        "columnKeys", jsonStringArray("100", "200"),
        "data", jsonArray(
            jsonDoubleArray(7.1, 7.2),
            jsonDoubleArray(8.1, 8.2),
            jsonDoubleArray(9.1, 9.2)));
    assertThat(
        makeTestObject().toJsonObject(
            array2D,
            rowKeyString -> rowKeyString,
            columnKeyLong -> Long.toString(columnKeyLong)),
        jsonObjectEpsilonMatcher(
            jsonObject));
    assertThat(
        makeTestObject().fromJsonObject(
            jsonObject,
            rowKey -> rowKey,
            columnKey -> Long.parseLong(columnKey)),
        immutableDoubleIndexableArray2DMatcher(
            array2D));
  }

  @Override
  protected ImmutableDoubleIndexableArray2DJsonApiConverter makeTestObject() {
    return new ImmutableDoubleIndexableArray2DJsonApiConverter();
  }

}
