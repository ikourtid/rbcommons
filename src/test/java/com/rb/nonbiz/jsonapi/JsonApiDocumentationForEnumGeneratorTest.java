package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptor;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import static com.rb.nonbiz.json.JsonApiEnumDescriptor.jsonApiEnumDescriptor;
import static com.rb.nonbiz.jsonapi.JsonApiEnumDocumentationTest.jsonApiEnumDocumentationMatcher;
import static com.rb.nonbiz.testutils.Asserters.valueExplained;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonApiDocumentationForEnumGeneratorTest extends RBTest<JsonApiDocumentationForEnumGenerator> {

  @Test
  public void testTextGeneration() {
    JsonApiEnumDescriptor<TestEnumXYZ> jsonApiEnumDescriptor = jsonApiEnumDescriptor(
        TestEnumXYZ.class,
        rbEnumMapOf(
            TestEnumXYZ.X, documentation("explanation for x"),
            TestEnumXYZ.Y, documentation("explanation for y")));

    valueExplained("_X", TestEnumXYZ.X.toUniqueStableString());
    valueExplained("_Y", TestEnumXYZ.Y.toUniqueStableString());

    valueExplained("test documentation for X", TestEnumXYZ.X.getDocumentation().getAsString());
    valueExplained("test documentation for Y", TestEnumXYZ.Y.getDocumentation().getAsString());

    assertThat(
        makeTestObject().generate(
            documentation("Summary for XYZ."),
            documentation("Description for XYZ."),
            jsonApiEnumDescriptor),
        jsonApiEnumDocumentationMatcher(
            JsonApiEnumDocumentationBuilder.<TestEnumXYZ>jsonApiEnumDocumentationBuilder()
                .setJsonApiEnumDescriptor(jsonApiEnumDescriptor)
                .setSingleLineSummary(documentation("Summary for XYZ."))
                .setLongDocumentation(documentation(asSingleLine(
                    "<p> Description for XYZ. </p>\n",
                    "<p> The following values are valid:\n<ul>",
                    // see valueExplained above for what should go in here.
                    "<li> <strong>_X</strong> : explanation for x </li>\n",
                    "<li> <strong>_Y</strong> : explanation for y </li>\n",
                    "</ul></p>\n")))
                .build()));
  }

  @Override
  protected JsonApiDocumentationForEnumGenerator makeTestObject() {
    return new JsonApiDocumentationForEnumGenerator();
  }

}
