package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Range;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ClosedUnitFractionRanges;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;

public class ClosedUnitFractionRangesJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .hasNoRequiredProperties()
      .setOptionalProperties(rbMapOf(
          "min", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
          "max", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
      .build();

  @Inject JsonValidator jsonValidator;
  @Inject RangeJsonApiConverter rangeJsonApiConverter;

  public <C extends Comparable<? super C>> JsonObject toJsonObject(
      ClosedUnitFractionRanges<C> closedUnitFractionRanges,
      Function<C, JsonPrimitive> serializer) {
    return null;
  }

  public <C extends Comparable<? super C>> Range<C> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonPrimitive, C> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);
    return closedUnitFractionRange();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ClosedUnitFractionRanges.class)
        .build();
  }

}
