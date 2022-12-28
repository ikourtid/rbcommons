package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.stream.DoubleStream;

import static com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder.rbJsonDoubleArrayBuilder;
import static com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder.rbJsonDoubleArrayBuilderWithExpectedSize;
import static com.rb.nonbiz.json.RBJsonDoubleArray.emptyRBJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArray.rbJsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonDoubleArray.singletonRBJsonDoubleArray;
import static com.rb.nonbiz.testmatchers.Match.matchDoubleList;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertDoubleListsAlmostEqual;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class RBJsonDoubleArrayTest extends RBTestMatcher<RBJsonDoubleArray> {

  @Test
  public void testSingletonArray() {
    RBJsonDoubleArray singletonRBJsonDoubleArray = singletonRBJsonDoubleArray(1.23);
    assertEquals(1, singletonRBJsonDoubleArray.size());
    assertEquals(1.23, singletonRBJsonDoubleArray.getRawDoublesList().get(0), 1e-8);
  }

  @Test
  public void testVarargConstructor() {
    RBJsonDoubleArray rbJsonDoubleArray = rbJsonDoubleArray(1.23, 4.56, 7.89);
    assertResult(rbJsonDoubleArray);
  }

  @Test
  public void testRawArrayConstructor() {
    RBJsonDoubleArray rbJsonDoubleArray = rbJsonDoubleArray(new double[] {1.23, 4.56, 7.89});
    assertResult(rbJsonDoubleArray);
  }

  @Test
  public void testDoubleStreamConstructor() {
    RBJsonDoubleArray rbJsonDoubleArray = rbJsonDoubleArray(DoubleStream.of(1.23, 4.56, 7.89));
    assertResult(rbJsonDoubleArray);
  }

  private void assertResult(RBJsonDoubleArray rbJsonDoubleArray) {
    assertEquals(3, rbJsonDoubleArray.size());
    assertDoubleListsAlmostEqual(
        ImmutableList.of(1.23, 4.56, 7.89),
        rbJsonDoubleArray.getRawDoublesList(),
        DEFAULT_EPSILON_1e_8);
  }

  @Test
  public void testBuilder() {
    RBJsonDoubleArrayBuilder builder = rbJsonDoubleArrayBuilder()
        .add(1.23)
        .add(4.56)
        .add(7.89);
    assertBuilder(builder);
  }

  @Test
  public void testBuilderWithExpectedSize() {
    RBJsonDoubleArrayBuilder builder = rbJsonDoubleArrayBuilderWithExpectedSize(3)
        .add(1.23)
        .add(4.56)
        .add(7.89);
    assertBuilder(builder);
  }

  private void assertBuilder(RBJsonDoubleArrayBuilder builder) {
    assertEquals(3, builder.size());
    assertThat(
        builder.build(),
        rbJsonDoubleArrayMatcher(rbJsonDoubleArray(1.23, 4.56, 7.89), DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testAsJsonArray() {
    RBJsonDoubleArray rbJsonDoubleArray = rbJsonDoubleArray(DoubleStream.of(1.23, 4.56, 7.89));

    JsonArray expected = new JsonArray(3);
    expected.add(1.23);
    expected.add(4.56);
    expected.add(7.89);
    assertThat(
        rbJsonDoubleArray.asJsonArray(),
        jsonArrayMatcher(expected, DEFAULT_EPSILON_1e_8));
  }

  @Override
  public RBJsonDoubleArray makeTrivialObject() {
    return emptyRBJsonDoubleArray();
  }

  @Override
  public RBJsonDoubleArray makeNontrivialObject() {
    return rbJsonDoubleArrayBuilder()
        .add(1.1)
        .add(-7.7)
        .add(0)
        .build();
  }

  @Override
  public RBJsonDoubleArray makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbJsonDoubleArrayBuilder()
        .add(1.1 + e)
        .add(-7.7 + e)
        .add(0 + e)
        .build();
  }

  @Override
  protected boolean willMatch(RBJsonDoubleArray expected, RBJsonDoubleArray actual) {
    return rbJsonDoubleArrayMatcher(expected, DEFAULT_EPSILON_1e_8).matches(actual);
  }

  public static TypeSafeMatcher<RBJsonDoubleArray> rbJsonDoubleArrayMatcher(RBJsonDoubleArray expected, Epsilon epsilon) {
    return makeMatcher(expected,
        matchDoubleList(v -> v.getRawDoublesList(), epsilon));
  }

}
