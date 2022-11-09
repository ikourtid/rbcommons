package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.types.SignedFraction;
import com.rb.nonbiz.types.WeightedBySignedFraction;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonBigDecimalOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static com.rb.nonbiz.types.WeightedBySignedFraction.weightedBySignedFraction;

/**
 * Converts a {@link WeightedBySignedFraction} back and forth to JSON for our public API.
 *
 * <p> Note that this converts a {@link WeightedBySignedFraction} to a JSON object that contains
 * a double keyed by "weight" and a JsonElement keyed by "item". That is, the "item"
 * is a JSON element and can be of any JSON type: boolean, String, double, Object, or Array. </p>
 *
 * <p> This does not implement JsonRoundTripConverter because we need to supply serializers
 * and deserializers. </p>
 *
 * @see SignedFraction
 */
public class WeightedBySignedFractionJsonApiConverter implements HasJsonApiDocumentation{

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "item",   UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
          "weight", simpleClassJsonApiPropertyDescriptor(SignedFraction.class)))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(
      WeightedBySignedFraction<T> weightedBySignedFraction,
      Function<T, JsonElement> serializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setPreciseValue(
                "weight",
                weightedBySignedFraction.getWeight())
            // translates a <T> to a JsonElement (a JSON string, double, object, etc.)
            .setJsonElement(
                "item",
                serializer.apply(weightedBySignedFraction.getItem()))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> WeightedBySignedFraction<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return weightedBySignedFraction(
        // translates a JsonElement (a JSON string, double, object, etc.) to a <T>
        deserializer.apply(jsonObject.get("item")),
        signedFraction(getJsonBigDecimalOrThrow(jsonObject, "weight")));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(WeightedBySignedFraction.class)
        .setSingleLineSummary(documentation("A single item with a `SignedFraction` weight."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The <b>weight</b> is a `SignedFraction`, that is, any number, either positive, negative, or zero,",
            "and of any magnitude. <p />",
            "`SignedFraction`s are used in similar contexts to `UnitFraction`s, which are constrained to be in",
            "the range [0.0, 1.0]. `SignedFraction`s, however, are allowed to go outside of those bounds.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "weight", jsonDouble(-123.45),
            "item",   jsonString("sample item")))
        .build();
  }

}
