package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.biz.jsonapi.JsonTicker;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.MutableIidMap;
import com.rb.nonbiz.types.PreciseValue;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addToJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.jsonApiDocumentationBuilder;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static java.util.Comparator.comparing;

/**
 * This makes for shorter, easier-to-read JSON for cases where we're dealing with an IidMap of precise values.
 * This allows for precise values to be written as a number, instead of a full object (which we'd see if we
 * were to use a generic Gson conversion).
 *
 * Example: instead of a map (in this case, of unit fractions) looking like this:
 *
 * rawMap
 *    111:
 *       value: 0.002
 *    222:
 *       value: 0.05988
 *
 * ... it will look like this:
 *
 * rawMap
 *    111: 0.002
 *    222: 0.05988
 *
 * ... where 111 and 222 are the numeric values for the instrument IDs that are the keys in the iid map.
 */
public class PreciseValueIidMapJsonConverter implements HasJsonApiDocumentation {

  @Inject PreciseValueJsonApiConverter preciseValueJsonApiConverter;

  public <V extends PreciseValue<? super V>> JsonObject toJsonObject(IidMap<V> map, JsonTickerMap jsonTickerMap) {
    JsonObject jsonObject = new JsonObject();
    // Using forEachIidSortedEntry instead of forEachEntry because JsonObject seems to be printing out key-value pairs
    // in the order they got added. This makes the serialization of pretty JSON deterministic,
    // so it's easier to read the JSON files, which will use increasing InstrumentId order wherever applicable.
    map.forEachKeySortedEntry( (iid, value) ->
            // #addToJsonObject will throw on duplicate keys; no need to check for duplicates here
            addToJsonObject(
                jsonObject,
                jsonTickerMap.getJsonTickerOrThrow(iid).getFreeFormTicker(),
                jsonBigDecimal(value)),
        comparing(iid -> jsonTickerMap.getJsonTickerOrThrow(iid).toString()));
    return jsonObject;
  }

  public <V extends PreciseValue<? super V>> IidMap<V> fromJsonObject(
      JsonObject jsonObject, JsonTickerMap jsonTickerMap, Function<BigDecimal, V> bigDecimalConverter) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(jsonObject.entrySet().size());
    jsonObject.entrySet().forEach(entry -> {
      JsonTicker jsonTicker = jsonTicker(entry.getKey());
      JsonElement valueAsJsonElement = entry.getValue();
      V preciseValue = preciseValueJsonApiConverter.fromJsonBigDecimal(
          valueAsJsonElement, jsonTicker, jsonTickerMap, bigDecimalConverter);
      mutableMap.putAssumingAbsent(jsonTickerMap.getInstrumentIdOrThrow(jsonTicker), preciseValue);
    });
    return newIidMap(mutableMap);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiDocumentationBuilder()
        .setClass(IidMap.class)
        .setSingleLineSummary(label(asSingleLine(
            "An IidMap of PreciseValues. That is, an RBMap with keys that are InstrumentIds ",
            "and values that are PreciseValues")))
        .setDocumentationHtml("FIXME IAK / FIXME SWA JSONDOC")
        .hasChildNode(preciseValueJsonApiConverter)
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

}
