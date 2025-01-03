package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ValueOutsideClosedRange;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.ValueOutsideClosedRange.valueOutsideClosedRange;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonPercentage;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonObjectOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonPrimitiveOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link ValueOutsideClosedRange} back and forth to JSON for our public API.
 */
public class ValueOutsideClosedRangeJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "value", simpleClassJsonApiPropertyDescriptor(String.class, jsonPropertySpecificDocumentation(
              asSingleLineWithNewlines(
                  "The value that's outside the range."))),
          "range", simpleClassJsonApiPropertyDescriptor(String.class, jsonPropertySpecificDocumentation(
              asSingleLineWithNewlines(
                  "The range that the value falls outside of.")))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;
  @Inject RangeJsonApiConverter rangeJsonApiConverter;

  public <T extends Comparable<? super T>> JsonObject toJsonObject(
      ValueOutsideClosedRange<T> itemsOutsideClosedRanges,
      Function<T, JsonPrimitive> valueSerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setJsonPrimitive("value", valueSerializer.apply(itemsOutsideClosedRanges.getValue()))
            .setJsonSubObject("range", rangeJsonApiConverter.toJsonObject(
                itemsOutsideClosedRanges.getClosedRange().asRange(),
                valueSerializer))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T extends Comparable<? super T>> ValueOutsideClosedRange<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonPrimitive, T> valueDeserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);
    return valueOutsideClosedRange(
        valueDeserializer.apply(getJsonPrimitiveOrThrow(jsonObject, "value")),
        closedRange(rangeJsonApiConverter.fromJsonObject(
            getJsonObjectOrThrow(jsonObject, "range"),
            valueDeserializer)));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ValueOutsideClosedRange.class)
        .setSingleLineSummary(documentation(
            "A value (typically numeric), and a range that does not contain it"))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The <b>value</b> can be e.g. an initial portfolio position as a percentage, ",
            "and the <b>range</b> can be the numeric range that the position must be within, but isn't.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasSingleChildJsonApiConverter(rangeJsonApiConverter)
        .setNontrivialSampleJson(jsonObject(
            "value", jsonPercentage(1.1),
            "range", jsonObject(
                "min", jsonPercentage(2.8),
                "max", jsonPercentage(3.9))))
        .build();  }

}
