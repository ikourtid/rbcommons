package com.rb.nonbiz.json;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.JsonApiEnumDescriptor.jsonApiEnumDescriptor;
import static com.rb.nonbiz.json.JsonApiEnumDescriptor.simpleJsonApiEnumDescriptor;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonApiEnumDescriptorTest extends RBTestMatcher<JsonApiEnumDescriptor<TestEnumXYZ>> {

  @Test
  public void test_simpleJsonApiEnumDescriptor() {
    assertThat(
        simpleJsonApiEnumDescriptor(TestEnumXYZ.class),
        jsonApiEnumDescriptorMatcher(
            jsonApiEnumDescriptor(
                TestEnumXYZ.class,
                rbEnumMapOf(
                    TestEnumXYZ.X, documentation("test documentation for X"),
                    TestEnumXYZ.Y, documentation("test documentation for Y"),
                    TestEnumXYZ.Z, documentation("test documentation for Z")))));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeTrivialObject() {
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        singletonRBEnumMap(
            TestEnumXYZ.Y, documentation("explanation for y")));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeNontrivialObject() {
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        rbEnumMapOf(
            TestEnumXYZ.X, documentation("explanation for x"),
            TestEnumXYZ.Z, documentation("explanation for z")));
  }

  @Override
  public JsonApiEnumDescriptor<TestEnumXYZ> makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        rbEnumMapOf(
            TestEnumXYZ.X, documentation("explanation for x"),
            TestEnumXYZ.Z, documentation("explanation for z")));
  }

  @Override
  protected boolean willMatch(JsonApiEnumDescriptor<TestEnumXYZ> expected, JsonApiEnumDescriptor<TestEnumXYZ> actual) {
    return jsonApiEnumDescriptorMatcher(expected).matches(actual);
  }

  public static <E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>>
  TypeSafeMatcher<JsonApiEnumDescriptor<E>> jsonApiEnumDescriptorMatcher(
      JsonApiEnumDescriptor<E> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getEnumClass()),
        match(           v -> v.getValidValuesToExplanations(), f -> rbEnumMapMatcher(f,
            f2 -> humanReadableDocumentationMatcher(f2))));
  }

}
