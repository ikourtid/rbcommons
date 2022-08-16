package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.asset.InstrumentType;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor.pseudoEnumJsonApiPropertyDescriptor;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.jsonApiDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.jsonApiDocumentationMatcher;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonApiDocumentationForPseudoEnumGeneratorTest extends RBTest<JsonApiDocumentationForPseudoEnumGenerator> {

  @Test
  public void testTextGeneration() {
    assertThat(
        makeTestObject().generate(
            InstrumentType.class,
            label("One of several security types."),
            "This test only supports ETFs and stocks.",
            pseudoEnumJsonApiPropertyDescriptor(rbMapOf(
                "is_etf",   label("Must also have 'etf' key with a `EtfInstrumentType` in its contents"),
                "is_stock", label("Must also have 'stock' key with a `StockInstrumentType` in its contents")))),
        jsonApiDocumentationMatcher(
            jsonApiDocumentationBuilder()
                .setClass(InstrumentType.class)
                .setSingleLineSummary(label("One of several security types."))
                .setLongDocumentation(asSingleLine(
                    "<p> This test only supports ETFs and stocks. </p>\n",
                    "<p> The following values are valid:\n<ul>",
                    "<li> <strong>is_etf</strong> : Must also have 'etf' key with a `EtfInstrumentType` in its contents </li>\n",
                    "<li> <strong>is_stock</strong> : Must also have 'stock' key with a `StockInstrumentType` in its contents </li>\n",
                    "</ul></p>\n"))
                .hasNoJsonValidationInstructions()
                .hasNoChildNodes()
                .noTrivialSampleJsonSupplied()
                .noNontrivialSampleJsonSupplied()
                .build()));
  }

  @Override
  protected JsonApiDocumentationForPseudoEnumGenerator makeTestObject() {
    return new JsonApiDocumentationForPseudoEnumGenerator();
  }

}
