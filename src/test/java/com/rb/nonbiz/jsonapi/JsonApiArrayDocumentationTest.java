package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.testutils.RBTestMatcher;

import static com.rb.nonbiz.jsonapi.HasJsonApiDocumentationTest.hasJsonApiDocumentationMatcher;
import static com.rb.nonbiz.jsonapi.JsonApiArrayDocumentation.JsonApiArrayDocumentationBuilder.jsonApiArrayDocumentationBuilder;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentationTest.testJsonApiClassDocumentationWithSeed;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;

import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class JsonApiArrayDocumentationTest extends RBTestMatcher<JsonApiArrayDocumentation> {

  @Override
  public JsonApiArrayDocumentation makeTrivialObject() {
    // Except for the documentation, the fields below are not realistic, but rbcommons does not have access
    // to rbbizinfra, where a lot of our business logic classes lie, so it's hard to make this realistic.
    // The realistic example would have been
    //   .setClassBeingDocumented(SingleInstrumentOrderedTaxLots.class)
    //   .setClassOfArrayItems(TaxLot.class)
    return jsonApiArrayDocumentationBuilder()
        .setClassBeingDocumented(ClosedRange.class)
        .setClassOfArrayItems(UnitFraction.class)
        .setSingleLineSummary(documentation("s"))
        .setLongDocumentation(documentation("l"))
        .hasNoJsonApiConverter()
        .build();
  }

  @Override
  public JsonApiArrayDocumentation makeNontrivialObject() {
    // Except for the documentation, the fields below are not realistic, but rbcommons does not have access
    // to rbbizinfra, where a lot of our business logic classes lie, so it's hard to make this realistic.
    // The realistic example would have been
    //   .setClassBeingDocumented(SingleInstrumentOrderedTaxLots.class)
    //   .setClassOfArrayItems(TaxLot.class)
    return jsonApiArrayDocumentationBuilder()
        .setClassBeingDocumented(ClosedRange.class)
        .setClassOfArrayItems(UnitFraction.class)
        .setSingleLineSummary(documentation("s"))
        .setLongDocumentation(documentation("l"))
        .hasJsonApiConverter( () -> testJsonApiClassDocumentationWithSeed(BigDecimal.class, ""))
        .build();
  }

  @Override
  public JsonApiArrayDocumentation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return jsonApiArrayDocumentationBuilder()
        .setClassBeingDocumented(ClosedRange.class)
        .setClassOfArrayItems(UnitFraction.class)
        .setSingleLineSummary(documentation("s"))
        .setLongDocumentation(documentation("l"))
        .hasJsonApiConverter( () -> testJsonApiClassDocumentationWithSeed(BigDecimal.class, ""))
        .build();
  }

  @Override
  protected boolean willMatch(JsonApiArrayDocumentation expected, JsonApiArrayDocumentation actual) {
    return jsonApiArrayDocumentationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonApiArrayDocumentation> jsonApiArrayDocumentationMatcher(
      JsonApiArrayDocumentation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getClassBeingDocumented()),
        matchUsingEquals(v -> v.getClassOfArrayItems()),
        match(           v -> v.getSingleLineSummary(),     f -> humanReadableDocumentationMatcher(f)),
        match(           v -> v.getLongDocumentation(),     f -> humanReadableDocumentationMatcher(f)),
        matchOptional(   v -> v.getChildJsonApiConverter(), f -> hasJsonApiDocumentationMatcher(f)));
  }

}
