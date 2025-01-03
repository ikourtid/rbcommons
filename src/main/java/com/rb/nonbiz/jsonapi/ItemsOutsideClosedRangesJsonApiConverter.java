package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ItemsOutsideClosedRanges;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ItemsOutsideClosedRanges.itemsOutsideClosedRanges;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjects.jsonObjectToRBMap;
import static com.rb.nonbiz.json.RBJsonObjects.rbMapToJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Converts a {@link ItemsOutsideClosedRanges} to / from JSON for our JSON APIs.
 */
public class ItemsOutsideClosedRangesJsonApiConverter implements HasJsonApiDocumentation {

  // Can't use JsonValidator, because the keys aren't fixed here
  @Inject ValueOutsideClosedRangeJsonApiConverter valueOutsideClosedRangeJsonApiConverter;

  public <K, V extends Comparable<? super V>> JsonObject toJsonObject(
      ItemsOutsideClosedRanges<K, V> itemsOutsideClosedRanges,
      Function<K, String> keySerializer,
      Function<V, JsonPrimitive> valueSerializer) {
    return rbMapToJsonObject(
        itemsOutsideClosedRanges.getRawMap(),
        keySerializer,
        v -> valueOutsideClosedRangeJsonApiConverter.toJsonObject(v, valueSerializer));
  }

  public <K, V extends Comparable<? super V>> ItemsOutsideClosedRanges<K, V> fromJsonObject(
      JsonObject jsonObject,
      Function<String, K> keyDeserializer,
      Function<JsonPrimitive, V> valueDeserializer) {
    return itemsOutsideClosedRanges(
        jsonObjectToRBMap(
            jsonObject,
            keyDeserializer,
            v -> valueOutsideClosedRangeJsonApiConverter.fromJsonObject(v.getAsJsonObject(), valueDeserializer)));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ItemsOutsideClosedRangesJsonApiConverter.class)
        .setSingleLineSummary(documentation(
            "A map of keys to a pair of: a value (typically numeric), and a `ClosedRange` that does not contain it"))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            asSingleLine(
                "Useful e.g. for representing stocks and their starting position % that's outside a ",
                "designated `ClosedRange`. "))))
        .hasNoJsonValidationInstructions()
        .hasSingleChildJsonApiConverter(valueOutsideClosedRangeJsonApiConverter)
        .setNontrivialSampleJson(jsonObject(
            "A", jsonObject(
                "value", jsonDouble(1.7),
                "range", jsonObject(
                    "min", jsonDouble(2.7),
                    "max", jsonDouble(3.7))),
            "B", jsonObject(
                "value", jsonDouble(1.8),
                "range", jsonObject(
                    "min", jsonDouble(2.8),
                    "max", jsonDouble(3.8)))))
        .build();
  }

}
