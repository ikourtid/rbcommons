package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.types.HardAndSoftRange;
import com.rb.nonbiz.types.RBNumeric;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonObjectOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;

/**
 * Converts a {@link HardAndSoftRange} back and forth to a JSON array for our public API.
 */
public class HardAndSoftRangeJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "hardRange", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
          "softRange", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;
  @Inject RangeJsonApiConverter rangeJsonApiConverter;

  public <T extends RBNumeric<? super T>> JsonObject toJsonObject(
      HardAndSoftRange<T> hardAndSoftRange,
      Function<T, JsonPrimitive> serializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setJsonSubObject(
                "hardRange",
                rangeJsonApiConverter.toJsonObject(
                    hardAndSoftRange.getHardRange(),
                    serializer))
            .setJsonSubObject(
                "softRange",
                rangeJsonApiConverter.toJsonObject(
                    hardAndSoftRange.getSoftRange(),
                    serializer))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T extends RBNumeric<? super T>> HardAndSoftRange<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonPrimitive, T> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return hardAndSoftRange(
        rangeJsonApiConverter.fromJsonObject(
            getJsonObjectOrThrow(jsonObject, "hardRange"),
            deserializer),
        rangeJsonApiConverter.fromJsonObject(
            getJsonObjectOrThrow(jsonObject, "softRange"),
            deserializer));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(HardAndSoftRange.class)
        .setSingleLineSummary(documentation(asSingleLineWithNewlines(
            "A combination of an outer 'hard' range that an optimization solution must observe ",
            "and an inner 'soft' range that the optimization should observe.")))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "By 'should observe the soft limit', we mean ",
            "that if a value drifts outside the soft limit, it will not be allowed ",
            "to drift further, but will not be forced to immediate move in the other direction. <p />",
            "In contrast, if a value drifts outside a hard limit, the system will insist ",
            "that it move back to the soft limit.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasSingleChildJsonApiConverter(rangeJsonApiConverter)
        .setNontrivialSampleJson(jsonObject(
            "hardRange", jsonObject(
                "min", jsonDouble(0.111),
                "max", jsonDouble(0.444)),
            "softRange", jsonObject(
                "min", jsonDouble(0.222),
                "max", jsonDouble(0.333))))
        .build();
  }

}
