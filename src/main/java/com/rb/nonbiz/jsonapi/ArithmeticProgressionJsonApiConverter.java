package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.math.sequence.ArithmeticProgression;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.subclassDiscriminatorPropertyDescriptor;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonDoubleOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.math.sequence.ArithmeticProgression.ArithmeticProgressionBuilder.arithmeticProgressionBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link ArithmeticProgression} back and forth to JSON for our public API.
 */
public class ArithmeticProgressionJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "type",                subclassDiscriminatorPropertyDescriptor("arithmeticProgression"),
          "initialValue",        simpleClassJsonApiPropertyDescriptor(Double.class),
          "commonDifference",    simpleClassJsonApiPropertyDescriptor(Double.class)))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(ArithmeticProgression arithmeticProgression) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setString("type",             "arithmeticProgression")
            .setDouble("initialValue",     arithmeticProgression.getInitialValue())
            .setDouble("commonDifference", arithmeticProgression.getCommonDifference())
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public ArithmeticProgression fromJsonObject(JsonObject jsonObject) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return arithmeticProgressionBuilder()
        .setInitialValue(    getJsonDoubleOrThrow(jsonObject, "initialValue"))
        .setCommonDifference(getJsonDoubleOrThrow(jsonObject, "commonDifference"))
        .build();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ArithmeticProgression.class)
        .setSingleLineSummary(documentation(asSingleLineWithNewlines(
            "A sequence of numbers that increases by a constant amount each time.")))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The first item in the sequence is specified by <b>initialValue</b>, and every subsequent item in the ",
            "sequence increases by <b>commonDifference</b>. ")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "type",             jsonString("arithmeticProgression"),
            "initialValue",     jsonDouble(10_000),
            "commonDifference", jsonDouble(500)))
        .build();
  }

}
