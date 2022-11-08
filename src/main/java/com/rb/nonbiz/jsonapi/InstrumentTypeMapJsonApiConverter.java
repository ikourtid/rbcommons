package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.biz.types.asset.InstrumentTypeMap;
import com.rb.biz.types.asset.InstrumentTypeMap.InstrumentTypeMapBuilder;
import com.rb.nonbiz.collections.IidMapWithDefaultsByInstrumentType;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.JsonValidationInstructions.UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonElementOrDefault;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;
import static com.rb.nonbiz.text.Strings.asSingleLineWithNewlines;

/**
 * Convert a {@link InstrumentTypeMap} back and forth to JSON for our public API.
 */
public class InstrumentTypeMapJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS =
      jsonValidationInstructionsBuilder()
          .hasNoRequiredProperties()
          .setOptionalProperties(rbMapOf(
              "etf",               UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
              "stock",             UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
              "mutualFund",        UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR,
              "structuredProduct", UNKNOWN_DATA_CLASS_JSON_API_DESCRIPTOR))
          .build();

  @Inject JsonValidator jsonValidator;

  public <T> JsonObject toJsonObject(
      InstrumentTypeMap<T> instrumentTypeMap,
      Predicate<T> onlyIncludeIf,
      Function<T, JsonElement> valueSerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setIf("etf",               instrumentTypeMap.getValueForEtfs(),               onlyIncludeIf, valueSerializer)
            .setIf("stock",             instrumentTypeMap.getValueForStocks(),             onlyIncludeIf, valueSerializer)
            .setIf("mutualFund",        instrumentTypeMap.getValueForMutualFunds(),        onlyIncludeIf, valueSerializer)
            .setIf("structuredProduct", instrumentTypeMap.getValueForStructuredProducts(), onlyIncludeIf, valueSerializer)
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <T> InstrumentTypeMap<T> fromJsonObject(
      JsonObject jsonObject,
      Function<JsonElement, T> ifPresent,
      T valueIfMissing) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);
    return InstrumentTypeMapBuilder.<T>instrumentTypeMapBuilder()
        .setValueForEtfs(              getJsonElementOrDefault(jsonObject, "etf",               ifPresent, valueIfMissing))
        .setValueForStocks(            getJsonElementOrDefault(jsonObject, "stock",             ifPresent, valueIfMissing))
        .setValueForMutualFunds(       getJsonElementOrDefault(jsonObject, "mutualFund",        ifPresent, valueIfMissing))
        .setValueForStructuredProducts(getJsonElementOrDefault(jsonObject, "structuredProduct", ifPresent, valueIfMissing))
        .build();
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(InstrumentTypeMap.class)
        .setSingleLineSummary(documentation(asSingleLine(
            "A set of objects, one for each of the 4 supported instrument type.")))
        .setLongDocumentation(documentation(asSingleLineWithNewlines(
            "The currently supported instrument types are",
            "<b>stock</b>, <b>etf</b>, <b>mutualFund</b>,",
            "and <b>structuredProduct</b>.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .setNontrivialSampleJson(jsonObject(
            "stock",             jsonDouble(100),
            "etf",               jsonDouble(200),
            "mutualFund",        jsonDouble(300),
            "structuredProduct", jsonDouble(400)))
        .build();
  }

}
