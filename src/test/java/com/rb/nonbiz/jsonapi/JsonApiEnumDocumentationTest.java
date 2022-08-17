package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptorTest;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.json.JsonApiEnumDescriptorTest.jsonApiEnumDescriptorMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;
import static com.rb.nonbiz.text.HumanReadableLabelTest.humanReadableLabelMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;

public class JsonApiEnumDocumentationTest extends RBTestMatcher<JsonApiEnumDocumentation<TestEnumXYZ>> {

  @Override
  public JsonApiEnumDocumentation<TestEnumXYZ> makeTrivialObject() {
    return JsonApiEnumDocumentationBuilder.<TestEnumXYZ>jsonApiEnumDocumentationBuilder()
        .setJsonApiEnumDescriptor(new JsonApiEnumDescriptorTest().makeTrivialObject())
        .setSingleLineSummary(humanReadableDocumentation("x"))
        .setLongDocumentation(humanReadableDocumentation("y"))
        .build();
  }

  @Override
  public JsonApiEnumDocumentation<TestEnumXYZ> makeNontrivialObject() {
    return JsonApiEnumDocumentationBuilder.<TestEnumXYZ>jsonApiEnumDocumentationBuilder()
        .setJsonApiEnumDescriptor(new JsonApiEnumDescriptorTest().makeNontrivialObject())
        .setSingleLineSummary(humanReadableDocumentation("Summary for TestEnumXYZ"))
        .setLongDocumentation(humanReadableDocumentation("Long documentation for TestEnumXYZ"))
        .build();
  }

  @Override
  public JsonApiEnumDocumentation<TestEnumXYZ> makeMatchingNontrivialObject() {
    return JsonApiEnumDocumentationBuilder.<TestEnumXYZ>jsonApiEnumDocumentationBuilder()
        .setJsonApiEnumDescriptor(new JsonApiEnumDescriptorTest().makeMatchingNontrivialObject())
        .setSingleLineSummary(humanReadableDocumentation("Summary for TestEnumXYZ"))
        .setLongDocumentation(humanReadableDocumentation("Long documentation for TestEnumXYZ"))
        .build();
  }

  @Override
  protected boolean willMatch(
      JsonApiEnumDocumentation<TestEnumXYZ> expected,
      JsonApiEnumDocumentation<TestEnumXYZ> actual) {
    return jsonApiEnumDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiEnumDocumentation<? extends Enum<?>>> jsonApiEnumDocumentationMatcher(
      JsonApiEnumDocumentation<? extends Enum<?>> expected) {
    return makeMatcher(expected,
        match(v -> v.getJsonApiEnumDescriptor(), f -> jsonApiEnumDescriptorMatcher(f)),
        match(v -> v.getSingleLineSummary(),     f -> humanReadableDocumentationMatcher(f)),
        match(v -> v.getLongDocumentation(),     f -> humanReadableDocumentationMatcher(f)));
  }

  public static <E extends Enum<E>> TypeSafeMatcher<JsonApiEnumDocumentation<E>>
  jsonApiEnumDocumentationOfSameTypeMatcher(JsonApiEnumDocumentation<E> expected) {
    return makeMatcher(expected, actual ->
        jsonApiEnumDocumentationMatcher(expected).matches(actual));
  }

}
