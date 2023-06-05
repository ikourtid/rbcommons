package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonDoubleOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonLongOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.math.stats.StatisticalSummaryImpl.StatisticalSummaryImplBuilder.statisticalSummaryImplBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link StatisticalSummary} back and forth to JSON for our public API.
 */
public class StatisticalSummaryJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "n", simpleClassJsonApiPropertyDescriptor(
              Long.class,
              jsonPropertySpecificDocumentation("The number of data points.")),
          "mean", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The mean (average) value of the data points.")),
          "min", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The minimum value in the data set.")),
          "max", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The maximum value in the data set.")),
          "standardDeviation", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The standard deviation of the data set."))))
      // the following properties are derived, so if they're not present in JSON we can still construct the Java object
      .setOptionalProperties(rbMapOf(
          "variance", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The variance of the data set.")),
          "sum", simpleClassJsonApiPropertyDescriptor(
              Double.class,
              jsonPropertySpecificDocumentation("The sum of the values in the data set."))))
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(
      StatisticalSummary statisticalSummary) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setLong(  "n",                 statisticalSummary.getN())
            .setDouble("mean",              statisticalSummary.getMean())
            .setDouble("min",               statisticalSummary.getMin())
            .setDouble("max",               statisticalSummary.getMax())
            .setDouble("standardDeviation", statisticalSummary.getStandardDeviation())
            .setDouble("variance",          statisticalSummary.getVariance())
            .setDouble("sum",               statisticalSummary.getSum())
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public StatisticalSummary fromJsonObject(
      JsonObject jsonObject) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    // return a StatisticalSummaryImpl, which implements StatisticalSummary
    return statisticalSummaryImplBuilder()
        .setN(                getJsonLongOrThrow(  jsonObject, "n"))
        .setMean(             getJsonDoubleOrThrow(jsonObject, "mean"))
        .setMin(              getJsonDoubleOrThrow(jsonObject, "min"))
        .setMax(              getJsonDoubleOrThrow(jsonObject, "max"))
        .setStandardDeviation(getJsonDoubleOrThrow(jsonObject, "standardDeviation"))
        // we don't read in 'variance' or 'sum' because they are derived quantities
        .build();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(StatisticalSummary.class)
        .setSingleLineSummary(documentation(
            "Holds a statistical summary of a data set."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The properties are: <p />",
            "The number of data points <b>n</b> (a long integer). <p />",
            "The <b>mean</b> (average) value of the data. <p />",
            "The <b>min</b>; the minimum value observed in the data set. <p />",
            "The <b>max</b>; the maximum value observed in the data set. <p />",
            "The <b>standardDeviation</b> of the data set. <p />",
            "The <b>variance</b> of the data set. <p />",
            "The <b>sum</b> of all the values in the data set. <p />",
            "All properties beside the number of points <b>n</b> are specified as doubles.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "n",                 jsonLong(  234L),        // a long; the only non-double
            "mean",              jsonDouble(12.345),
            "min",               jsonDouble(-5.678),
            "max",               jsonDouble(67.890),
            "standardDeviation", jsonDouble( 4.321),
            "variance",          jsonDouble(18.671_041),  // variance = stdDev * stdDev
            "sum",               jsonDouble(2_888.73)))   // sum      = n * mean
        .build();
  }

}
