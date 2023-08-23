package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.math.sequence.GeometricProgression;
import com.rb.nonbiz.types.PositiveMultiplier;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.checkDiscriminatorValue;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.subclassDiscriminatorPropertyDescriptor;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonDoubleOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.math.sequence.GeometricProgression.geometricProgression;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;

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

  public <T> JsonObject toJsonObject(
      GeometricProgression<T> geometricProgression,
      Function<T, JsonElement> elementSerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setString("type", "geometricProgression")
            .setJsonElement("initialValue",  elementSerializer.apply(geometricProgression.getInitialValue()))
            .setImpreciseValue("commonRatio", geometricProgression.getCommonRatio())
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> GeometricProgression<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> deserializer,
      BiFunction<T, PositiveMultiplier, T> nextItemGenerator) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);
    checkDiscriminatorValue(jsonObject, "type", "geometricProgression");

    PositiveMultiplier commonRatio = positiveMultiplier(getJsonDoubleOrThrow(jsonObject, "commonRatio"));
    return geometricProgression(
        deserializer.apply(getJsonElementOrThrow(jsonObject, "initialValue")),
        commonRatio,
        v -> nextItemGenerator.apply(v, commonRatio));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(GeometricProgression.class)
        .setSingleLineSummary(documentation(
            "A sequence of numbers that increases by a constant ratio each time."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The first item in the sequence is specified by <b>initialValue</b>, and every subsequent item in the ",
            "sequence is its previous item times <b>commonRatio</b>. <p />",

            "Note that this is more general than a single number; it could be any object with a number in it. ",
            "If so, then any subsequent items in the sequence will be identical to the initial value, except for ",
            "the number in the object. <p />")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "type",         jsonString("geometricProgression"),
            "initialValue", jsonDouble(10_000),
            "commonRatio",  jsonDouble(1.08))) // 8% higher each time
        .build();
  }

}
