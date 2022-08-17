package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;
import com.rb.nonbiz.json.RBJsonObjectBuilder;
import com.rb.nonbiz.text.HumanReadableDocumentation;

import java.util.ArrayList;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonApiPropertyDescriptor.SimpleClassJsonApiPropertyDescriptor.simpleClassJsonApiPropertyDescriptor;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToList;
import static com.rb.nonbiz.json.RBJsonArrays.listToJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonArrayOrThrow;
import static com.rb.nonbiz.json.RBJsonObjects.jsonArrayToSimpleArrayIndexMapping;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;

/**
 * Converts an {@link ImmutableIndexableArray1D} back and forth to JSON for our public API.
 *
 * This does not implement JsonRoundTripConverter because we need to supply serializers and deserializers.
 */
public class ImmutableIndexableArray1DJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(rbMapOf(
          "keys", simpleClassJsonApiPropertyDescriptor(String.class),
          "data", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <K, V> JsonObject toJsonObject(
      ImmutableIndexableArray1D<K, V> array1D,
      Function<K, JsonElement> keySerializer,
      Function<V, JsonElement> valueSerializer) {
    RBJsonObjectBuilder rbJsonObjectBuilder = rbJsonObjectBuilder();
    ArrayList<K> keyList   = newArrayListWithExpectedSize(array1D.size());
    ArrayList<V> valueList = newArrayListWithExpectedSize(array1D.size());
    array1D.forEachEntry( (key, value) -> {
      keyList.add(key);
      valueList.add(value);
    });
    return jsonValidator.validate(
        rbJsonObjectBuilder
            // we can't use a JsonObject map of key -> data because the order has to be preserved
            .setArray("keys", listToJsonArray(keyList,   k -> keySerializer.apply(k)))
            .setArray("data", listToJsonArray(valueList, v -> valueSerializer.apply(v)))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <K, V> ImmutableIndexableArray1D<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, K> keyDeserializer,
      Function<JsonElement, V> valueDeserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    @SuppressWarnings("unchecked")
    V[] dataArray = (V[]) jsonArrayToList(
        getJsonArrayOrThrow(jsonObject, "data"),
        jsonValueElement -> valueDeserializer.apply(jsonValueElement)).toArray();

    return immutableIndexableArray1D(
        jsonArrayToSimpleArrayIndexMapping(
            getJsonArrayOrThrow(jsonObject, "keys"),
            jsonKeyElement -> keyDeserializer.apply(jsonKeyElement)),
        dataArray);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ImmutableIndexableArray1D.class)
        .setSingleLineSummary(humanReadableDocumentation(asSingleLine(
            "An indexable 1-D array is like a regular 1-D array, except that you can ",
            "also access it based on more meaningful keys - not just an integer index.")))
        .setLongDocumentation(humanReadableDocumentation("FIXME IAK / FIXME SWA JSONDOC"))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildNodes()
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }
}
