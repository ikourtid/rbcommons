package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.RBCategoryMap;
import com.rb.nonbiz.json.JsonPropertySpecificDocumentation;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor.rbMapJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_CLASS_OF_JSON_PROPERTY;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;

/**
 * Converts an {@link RBCategoryMap} back and forth to JSON for our public API.
 */
public class RBCategoryMapJsonApiConverter implements HasJsonApiDocumentation {

  static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "valueRegardlessOfCategory", simpleClassJsonApiPropertyDescriptor(
              UNKNOWN_CLASS_OF_JSON_PROPERTY,
              jsonPropertySpecificDocumentation("The value V that is valid regardless of category")),
          "categoryMap", rbMapJsonApiPropertyDescriptor(
              simpleClassJsonApiPropertyDescriptor(
                  UNKNOWN_CLASS_OF_JSON_PROPERTY,
                  jsonPropertySpecificDocumentation("The key type of the category map.")),
              simpleClassJsonApiPropertyDescriptor(
                  UNKNOWN_CLASS_OF_JSON_PROPERTY,
                  jsonPropertySpecificDocumentation("The value type of the category map.")))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <K, V> JsonObject toJsonObject(
      RBCategoryMap<K, V> rbCategoryMap,
      Function<K, String> keySerializer,
      Function<V, JsonElement> valueSerializer) {
    return null;
  }

  public <K, V> RBCategoryMap<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      Function<JsonElement, V> valueDeserializer) {

    return null;
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return null;
  }

}
