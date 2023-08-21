package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.math.sequence.GeometricProgression;

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
import static com.rb.nonbiz.math.sequence.GeometricProgression.GeometricProgressionBuilder.geometricProgressionBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link GeometricProgression} back and forth to JSON for our public API.
 */
public class GeometricProgressionJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "type",         subclassDiscriminatorPropertyDescriptor("geometricProgression"),
          "initialValue", simpleClassJsonApiPropertyDescriptor(Double.class),
          "commonRatio",  simpleClassJsonApiPropertyDescriptor(Double.class)))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public JsonObject toJsonObject(GeometricProgression geometricProgression) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setString("type",         "geometricProgression")
            .setDouble("initialValue", geometricProgression.getInitialValue())
            .setDouble("commonRatio",  geometricProgression.getCommonRatio())
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public GeometricProgression fromJsonObject(JsonObject jsonObject) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return geometricProgressionBuilder()
        .setInitialValue(getJsonDoubleOrThrow(jsonObject, "initialValue"))
        .setCommonRatio( getJsonDoubleOrThrow(jsonObject, "commonRatio"))
        .build();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(GeometricProgression.class)
        .setSingleLineSummary(documentation(asSingleLineWithNewlines(
            "A sequence of numbers that increases by a constant ratio each time.")))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The first item in the sequence is specified by <b>initialValue</b>, and every subsequent item in the ",
            "sequence is its previous item times <b>commonRatio</b>. ")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "type",         jsonString("geometricProgression"),
            "initialValue", jsonDouble(10_000),
            "commonRatio",  jsonDouble(1.08))) // 8% higher each time
        .build();
  }

}
