package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.function.DoubleFunction;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts an Apache {@code SummaryStatistics} to JSON for our public API.
 *
 * <p> This does not provide a {@code fromJsonObject()} method because Apache
 * does not provide a constructor given all the summary statistics; the user
 * has to use .add() point-by-point instead. </p>
 */
public class SummaryStatisticsJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties(rbMapOf(
          "min", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
          "max", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(
      SummaryStatistics summaryStatistics) {
    return rbJsonObjectBuilder()
        .setLong(  "n",            summaryStatistics.getN())
        .setDouble("min",          summaryStatistics.getMin())
        .setDouble("max",          summaryStatistics.getMax())
        .setDouble("mean",         summaryStatistics.getMean())
        .setDouble("stdDev",       summaryStatistics.getStandardDeviation())
        .setDouble("variance",     summaryStatistics.getVariance())
        .setDouble("sum",          summaryStatistics.getSum())
        .setDouble("sumOfSquares", summaryStatistics.getSumsq())
        .build();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(StatisticalSummary.class)
        .setSingleLineSummary(documentation(
            "A set of statistics about some data."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "Holds several standard summary statistics. <p />",
            "The property <b>n</b> holds the number of data points. ",
            "The <b>min</b> and <b>max</b> hold the minimum and maximum",
            "values seen in the data. <p />",
            "The <b>mean</b> holds the data mean (average), and the",
            "<b>variance</b> holds the variance. <p />",
            "The <b>stdDev</b> holds the standard deviation.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "n",        jsonInteger(211),
            "min",      jsonDouble(-2.43),
            "max",      jsonDouble(127.45),
            "mean",     jsonDouble(45.33),
            "stdDev",   jsonDouble(31.20),
            "variance", jsonDouble(1_222.45),
            "sumOfSquares", jsonDouble(257_842)))
        .build();
  }

}
