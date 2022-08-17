package com.rb.nonbiz.jsonapi;

import com.rb.biz.types.asset.InstrumentType;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import org.junit.Test;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor.pseudoEnumJsonApiPropertyDescriptor;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.jsonApiDocumentationMatcher;
import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonApiDocumentationForPseudoEnumGeneratorTest extends RBTest<JsonApiDocumentationForPseudoEnumGenerator> {

  @Test
  public void testTextGeneration() {
    assertThat(
        makeTestObject().generate(
            InstrumentType.class,
            humanReadableDocumentation("One of several security types."),
            humanReadableDocumentation("This test only supports ETFs and stocks."),
            pseudoEnumJsonApiPropertyDescriptor(rbMapOf(
                "is_etf",   humanReadableDocumentation("Must also have 'etf' key with a `EtfInstrumentType` in its contents"),
                "is_stock", humanReadableDocumentation("Must also have 'stock' key with a `StockInstrumentType` in its contents")))),
        jsonApiDocumentationMatcher(
            jsonApiClassDocumentationBuilder()
                .setClass(InstrumentType.class)
                .setSingleLineSummary(humanReadableDocumentation("One of several security types."))
                .setLongDocumentation(humanReadableDocumentation(asSingleLine(
                    "<p> This test only supports ETFs and stocks. </p>\n",
                    "<p> The following values are valid:\n<ul>",
                    "<li> <strong>is_etf</strong> : Must also have 'etf' key with a `EtfInstrumentType` in its contents </li>\n",
                    "<li> <strong>is_stock</strong> : Must also have 'stock' key with a `StockInstrumentType` in its contents </li>\n",
                    "</ul></p>\n")))
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
