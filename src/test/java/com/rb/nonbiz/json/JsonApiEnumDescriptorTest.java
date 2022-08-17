package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.JavaEnumSerializationAndExplanationTest.javaEnumSerializationAndExplanationMatcher;
import static com.rb.nonbiz.json.JsonApiEnumDescriptor.JavaEnumSerializationAndExplanation.javaEnumSerializationAndExplanation;
import static com.rb.nonbiz.json.JsonApiEnumDescriptor.jsonApiEnumDescriptor;
import static com.rb.nonbiz.json.JsonApiEnumDescriptor.simpleJsonApiEnumDescriptor;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonApiEnumDescriptorTest extends RBTestMatcher<JsonApiEnumDescriptor<TestEnumXYZ>> {

  @Test
  public void test_simpleJsonApiEnumDescriptor() {
    assertThat(
        simpleJsonApiEnumDescriptor(TestEnumXYZ.class),
        jsonApiEnumDescriptorMatcher(
            jsonApiEnumDescriptor(
                TestEnumXYZ.class,
                enumMapOf(
                    TestEnumXYZ.X, javaEnumSerializationAndExplanation("X", humanReadableDocumentation("test documentation for X")),
                    TestEnumXYZ.Y, javaEnumSerializationAndExplanation("Y", humanReadableDocumentation("test documentation for Y")),
                    TestEnumXYZ.Z, javaEnumSerializationAndExplanation("Z", humanReadableDocumentation("test documentation for Z"))))));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeTrivialObject() {
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        singletonEnumMap(
            TestEnumXYZ.Y, javaEnumSerializationAndExplanation("_y", humanReadableDocumentation("explanation for y"))));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeNontrivialObject() {
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        enumMapOf(
            TestEnumXYZ.X, javaEnumSerializationAndExplanation("_x", humanReadableDocumentation("explanation for x")),
            TestEnumXYZ.Z, javaEnumSerializationAndExplanation("_z", humanReadableDocumentation("explanation for z"))));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        enumMapOf(
            TestEnumXYZ.X, javaEnumSerializationAndExplanation("_x", humanReadableDocumentation("explanation for x")),
            TestEnumXYZ.Z, javaEnumSerializationAndExplanation("_z", humanReadableDocumentation("explanation for z"))));
  }

  @Override
  protected boolean willMatch(JsonApiEnumDescriptor<TestEnumXYZ> expected, JsonApiEnumDescriptor<TestEnumXYZ> actual) {
    return jsonApiEnumDescriptorMatcher(expected).matches(actual);
  }

  public static <E extends Enum<E>> TypeSafeMatcher<JsonApiEnumDescriptor<E>> jsonApiEnumDescriptorMatcher(
      JsonApiEnumDescriptor<E> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getEnumClass()),
        match(           v -> v.getValidValuesToExplanations(), f -> enumMapMatcher(f,
            f2 -> javaEnumSerializationAndExplanationMatcher(f2))));
  }

}
