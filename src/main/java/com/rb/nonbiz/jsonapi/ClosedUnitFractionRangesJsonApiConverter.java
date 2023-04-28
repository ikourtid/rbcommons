package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ClosedUnitFractionRanges;
import com.rb.nonbiz.types.ClosedUnitFractionRange;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedUnitFractionRanges.closedUnitFractionRanges;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_CLASS_OF_JSON_PROPERTY;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonPercentage;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjects.jsonObjectToRBMap;
import static com.rb.nonbiz.json.RBJsonObjects.rbMapToJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassWithNonFixedPropertiesDocumentation.JsonApiClassWithNonFixedPropertiesDocumentationBuilder.jsonApiClassWithNonFixedPropertiesDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;

/**
 * Converts a {@link ClosedUnitFractionRanges} back and forth to JSON for our public API.
 */
public class ClosedUnitFractionRangesJsonApiConverter implements HasJsonApiDocumentation {

  @Inject RangeJsonApiConverter rangeJsonApiConverter;

  public <C extends Comparable<? super C>> JsonObject toJsonObject(
      ClosedUnitFractionRanges<C> closedUnitFractionRanges,
      Function<C, String> keySerializer) {
    return rbMapToJsonObject(
        closedUnitFractionRanges.getRawMap(),
        keySerializer,
        v -> rangeJsonApiConverter.toJsonObject(
            v.asClosedRangeOfUnitFraction().asRange(),
            unitFraction -> jsonPercentage(unitFraction)));
  }

  public <C extends Comparable<? super C>> ClosedUnitFractionRanges<C> fromJsonObject(
      JsonObject jsonObject,
      Function<String, C> keyDeserializer) {
    return closedUnitFractionRanges(
        jsonObjectToRBMap(
            jsonObject,
            keyDeserializer,
            jsonElement -> closedUnitFractionRange(
                rangeJsonApiConverter.fromJsonObject(
                    jsonElement.getAsJsonObject(),
                    v -> unitFractionInPct(v.getAsDouble())))));
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassWithNonFixedPropertiesDocumentationBuilder()
        .setClassBeingDocumented(ClosedUnitFractionRanges.class)
        .setKeyClass(UNKNOWN_CLASS_OF_JSON_PROPERTY)
        .setValueClass(ClosedUnitFractionRange.class)
        .setSingleLineSummary(documentation(
            "Holds a map of keys to `ClosedUnitFractionRange`s."))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The keys can be of any type, but the values must be `ClosedUnitFractionRange`s.")))
        .hasSingleChildJsonApiConverter(rangeJsonApiConverter)
        .setNontrivialSampleJson(jsonObject(
            "US_Software", jsonObject(
                "min", jsonDouble(10.0),
                "max", jsonDouble(30.0)),
            "US_Financials", jsonObject(
                "min", jsonDouble(15.0),
                "max", jsonDouble(35.0))))
        .build();
  }

}
