package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.math.stats.StatisticalSummaryImpl;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link StatisticalSummaryImpl} back and forth to JSON for our public API.
 */
public class StatisticalSummaryImplJsonApiConverter extends HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "n", simpleClassJsonApiPropertyDescriptor(
              Long.class,
              jsonPropertySpecificDocumentation("The number of data points.")),
          "mean", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The mean (average) of the data points.")),
          "min", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The minimum data value in the data set.")),
          "max", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The maximum data value in the data set.")),
          "min", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The minimum data value in the data set.")),
          "standardDeviation", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The standard deviation of the data set.")),
          "variance", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The variance of the data set.")),
          "sum", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The sum of the values in the data set."))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(
      StatisticalSummaryImpl statisticalSummaryImpl) {
    return null;
  }

  public StatisticalSummaryImpl fromJsonObject(
      JsonObject jsonObject) {
    return null;
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(StatisticalSummaryImpl.class)
        .setSingleLineSummary(documentation(
            "Holds a statistical summary of for a data set."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The properties are: <p />",
            "The number of data points <b>n</b> (a long integer). <p />",
            "The <b>mean</b>; the mean (average) value of the data. <p />",
            "The <b>min</b>; the minimum value observed in the data set. <p />",
            "The <b>max</b>; the maximum value observed in the data set. <p />",
            "The <b>standardDeviation</b> of the data set. <p />",
            "The <b>variance</b> of the data set. <p />",
            "The <b>sum</b> of all the values in the data set. <p />",
            "All properties beside the number of points <b>n</b> are specified as doubles.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "n",                 jsonLong(234L),        // a long; the only non-double
            "mean",              jsonDouble(12.345),
            "min",               jsonDouble(-5.678),
            "max",               jsonDouble(67.890),
            "standardDeviation", jsonDouble(4.321),
            "variance",          jsonDouble(17.89),
            "sum",               jsonDouble(2_888.73)))  // = n * mean
        .build();
  }

}
