package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.immutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1DTest.testImmutableIndexableArray1D;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonLongArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static org.hamcrest.MatcherAssert.assertThat;

public class ImmutableIndexableArray1DJsonApiConverterTest
    extends RBTest<ImmutableIndexableArray1DJsonApiConverter> {

  @Test
  public void testBackAndForth() {
    ImmutableIndexableArray1D<String, Long> array1D = testImmutableIndexableArray1D(
        "a", 100L,
        "b", 200L,
        "c", 300L);

    JsonObject jsonObject1D = jsonObject(
        "keys", jsonStringArray("a", "b", "c"),
        "data", jsonLongArray(100L, 200L, 300L));

    assertThat(
        makeTestObject().toJsonObject(
            array1D,
            k -> jsonString(k),
            v -> jsonLong(v)),
        jsonObjectEpsilonMatcher(
            jsonObject1D));

    assertThat(
        makeTestObject().fromJsonObject(
            jsonObject1D,
            k -> k.getAsString(),
            v -> v.getAsLong()),
        immutableIndexableArray1DMatcher(
            array1D,
            key   -> typeSafeEqualTo(key),
            value -> typeSafeEqualTo(value)));
  }

  @Override
  protected ImmutableIndexableArray1DJsonApiConverter makeTestObject() {
    return makeRealObject(ImmutableIndexableArray1DJsonApiConverter.class);
  }

}