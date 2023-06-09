package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.RBCategoryMap;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBCategoryMap.rbCategoryMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.RBMapJsonApiPropertyDescriptor.rbMapJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonPropertySpecificDocumentation.jsonPropertySpecificDocumentation;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_CLASS_OF_JSON_PROPERTY;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonObjectOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjects.jsonObjectToRBMap;
import static com.rb.nonbiz.json.RBJsonObjects.rbMapToJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

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
              simpleClassJsonApiPropertyDescriptor(UNKNOWN_CLASS_OF_JSON_PROPERTY),
              simpleClassJsonApiPropertyDescriptor(UNKNOWN_CLASS_OF_JSON_PROPERTY))))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <K, V> JsonObject toJsonObject(
      RBCategoryMap<K, V> rbCategoryMap,
      Function<K, String> keySerializer,
      Function<V, JsonElement> valueSerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setJsonElement(
                "valueRegardlessOfCategory",
                valueSerializer.apply(rbCategoryMap.getValueRegardlessOfCategory()))
            .setJsonSubObject(
                "categoryMap",
                rbMapToJsonObject(
                    rbCategoryMap.getRawMap(),
                    keySerializer,
                    valueSerializer))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <K, V> RBCategoryMap<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      Function<JsonElement, V> valueDeserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    return rbCategoryMap(
        valueDeserializer.apply(
            getJsonElementOrThrow(
                jsonObject, "valueRegardlessOfCategory")),
        jsonObjectToRBMap(
            getJsonObjectOrThrow(jsonObject, "categoryMap"),
            keyDeserializer,
            valueDeserializer));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(RBCategoryMap.class)
        .setSingleLineSummary(documentation(asSingleLineWithNewlines(
            "Like an `RBMap` of categories, with an additional data member",
            "that applies regardless of category.")))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "For example, we could store the trading notional by side (buy/sell), additionally",
            "holding the total trading notional (buy + sell) which doesn't apply to either category",
            "by itself. <p />",
            "The category map is stored under the <b>categoryMap</p> property. <p />",
            "The value that applies regardless of category is stored under the ",
            "<b>valueRegardlessOfCategory</b> property.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "valueRegardlessOfCategory", jsonInteger(1_100),
            "categoryMap", jsonObject(
                "TLH",        jsonInteger(100),
                "invest",     jsonInteger(300),
                "withdrawal", jsonInteger(700))))
        .build();
  }

}
