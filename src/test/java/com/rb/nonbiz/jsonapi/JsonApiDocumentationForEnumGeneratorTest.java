package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.junit.Test;

import static com.rb.nonbiz.json.JsonApiEnumDescriptor.JavaEnumSerializationAndExplanation.javaEnumSerializationAndExplanation;
import static com.rb.nonbiz.json.JsonApiEnumDescriptor.jsonApiEnumDescriptor;
import static com.rb.nonbiz.jsonapi.JsonApiEnumDocumentationTest.jsonApiEnumDocumentationMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonApiDocumentationForEnumGeneratorTest extends RBTest<JsonApiDocumentationForEnumGenerator> {

  @Test
  public void testTextGeneration() {
    assertThat(
        makeTestObject().generate(
            label("Summary for XYZ."),
            "Description for XYZ.",
            jsonApiEnumDescriptor(
                TestEnumXYZ.class,
                enumMapOf(
                    TestEnumXYZ.X, javaEnumSerializationAndExplanation("_x", label("explanation for x")),
                    TestEnumXYZ.Y, javaEnumSerializationAndExplanation("_y", label("explanation for y"))))),
        jsonApiEnumDocumentationMatcher(
            JsonApiEnumDocumentationBuilder.<TestEnumXYZ>jsonApiEnumDocumentationBuilder()
                .setEnumClass(TestEnumXYZ.class)
                .setSingleLineSummary(label("Summary for XYZ."))
                .setLongDocumentation(asSingleLine(
                    "<p> Description for XYZ. </p>\n",
                    "<p> The following values are valid:\n<ul>",
                    "<li> <strong>_x</strong> : explanation for x </li>\n",
                    "<li> <strong>_y</strong> : explanation for y </li>\n",
                    "</ul></p>\n"))
                .build()));
  }

  @Override
  protected JsonApiDocumentationForEnumGenerator makeTestObject() {
    return new JsonApiDocumentationForEnumGenerator();
  }
  
}
