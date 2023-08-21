package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.math.sequence.ConstantSequence;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.checkDiscriminatorValue;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleUnknownClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.subclassDiscriminatorPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.math.sequence.ConstantSequence.constantSequence;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;

/**
 * Converts a {@link ConstantSequence} back and forth to JSON for our public API.
 */
public class ConstantSequenceJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "type",                subclassDiscriminatorPropertyDescriptor("constantSequence"),
          "constantValue",       simpleUnknownClassJsonApiPropertyDescriptor(jsonPropertySpecificDocumentation(
              "The class / type of the constant value. Usually a scalar (number), but does not have to be."))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(
      ConstantSequence<T> constantSequence,
      Function<T, JsonElement> serializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setString("type", "constantSequence")
            .setJsonElement("constantValue", serializer.apply(constantSequence.getConstantValue()))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> ConstantSequence<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> deserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);
    checkDiscriminatorValue(jsonObject, "type", "constantSequence");

    return constantSequence(deserializer.apply(getJsonElementOrThrow(jsonObject, "constantValue")));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ConstantSequence.class)
        .setSingleLineSummary(documentation(
            "A sequence (in the mathematical sense) of values that are constant for every index in the sequence."))
        .setLongDocumentation(documentation(
            "<b>constantValue</b> is the value of all elements in the sequence."))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "type",             jsonString("constantSequence"),
            "constantValue",    jsonDouble(1_000)))
        .build();
  }

}
