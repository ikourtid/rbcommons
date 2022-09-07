package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Iterators;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D;
import com.rb.nonbiz.collections.MutableDoubleIndexableArray2D;
import com.rb.nonbiz.json.JsonValidationInstructions;
import com.rb.nonbiz.json.JsonValidator;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.JsonValidationInstructions.JsonValidationInstructionsBuilder.jsonValidationInstructionsBuilder;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonDoubleArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.Strings.asSingleLine;

/**
 * Converts an {@link ImmutableDoubleIndexableArray2D} back and forth to JSON for our public API.
 */
public class ImmutableDoubleIndexableArray2DJsonApiConverter implements HasJsonApiDocumentation {

  private static final JsonValidationInstructions JSON_VALIDATION_INSTRUCTIONS = jsonValidationInstructionsBuilder()
      .setRequiredProperties(
          "rowKeys",    String.class,
          "columnKeys", String.class,
          "data",       Double.class)
      .hasNoOptionalProperties()
      .build();

  @Inject JsonValidator jsonValidator;

  public <R, C> JsonObject toJsonObject(
      ImmutableDoubleIndexableArray2D<R, C> array2D,
      Function<R, String> rowKeySerializer,
      Function<C, String> columnKeySerializer) {
    return jsonValidator.validate(
        rbJsonObjectBuilder()
            .setArray("rowKeys", jsonArray(
                array2D.getRowMapping().size(),
                IntStream
                    .range(0, array2D.getRowMapping().size())
                    .mapToObj(i -> jsonString(rowKeySerializer.apply(array2D.getRowMapping().getKey(i))))))
            .setArray("columnKeys", jsonArray(
                array2D.getColumnMapping().size(),
                IntStream
                    .range(0, array2D.getColumnMapping().size())
                    .mapToObj(i -> jsonString(columnKeySerializer.apply(array2D.getColumnMapping().getKey(i))))))
            .setArray("data", jsonArray(
                array2D.getRowMapping().size(),
                IntStream.range(0, array2D.getRowMapping().size())
                    .mapToObj(rowIndex -> jsonArray(
                        array2D.getColumnMapping().size(),
                        IntStream.range(0, array2D.getColumnMapping().size())
                            .mapToObj(columnIndex -> jsonDouble(array2D.getByIndex(rowIndex, columnIndex)))))))
            .build(),
        JSON_VALIDATION_INSTRUCTIONS);
  }

  public <R, C> ImmutableDoubleIndexableArray2D<R, C> fromJsonObject(
      JsonObject jsonObject,
      Function<String, R> rowKeyDeserializer,
      Function<String, C> columnKeyDeserializer) {
    jsonValidator.validate(jsonObject, JSON_VALIDATION_INSTRUCTIONS);

    ArrayIndexMapping<R> rowKeysMapping = simpleArrayIndexMapping(
        Iterators.transform(
            jsonObject.getAsJsonArray("rowKeys").iterator(),
            jsonElement -> rowKeyDeserializer.apply(jsonElement.getAsString())));
    ArrayIndexMapping<C> columnKeysMapping = simpleArrayIndexMapping(
        Iterators.transform(
            jsonObject.getAsJsonArray("columnKeys").iterator(),
            jsonElement -> columnKeyDeserializer.apply(jsonElement.getAsString())));

    double[][] rawArray = new double[rowKeysMapping.size()][columnKeysMapping.size()];
    MutableDoubleIndexableArray2D<R, C> mutableArray2D = mutableDoubleIndexableArray2D(
        rawArray,
        rowKeysMapping,
        columnKeysMapping);
    Iterator<JsonElement> rowIterator = jsonObject.getAsJsonArray("data").iterator();
    for (int r = 0; r < rowKeysMapping.size(); r++) {
      JsonArray row = rowIterator.next().getAsJsonArray();
      for (int c = 0; c < columnKeysMapping.size(); c++) {
        rawArray[r][c] = row.get(c).getAsDouble();
      }
    }
    return immutableDoubleIndexableArray2D(mutableArray2D);
  }

  @Override
  public JsonApiDocumentation getJsonApiDocumentation() {
    return jsonApiClassDocumentationBuilder()
        .setClass(ImmutableDoubleIndexableArray2D.class)
        .setSingleLineSummary(documentation(asSingleLine(
            "An indexable 2-D array is like a regular 2-D array, except that you can ",
            "also access it based on more meaningful keys - not just integer indices.")))
        .setLongDocumentation(documentation(asSingleLine(
            "Column keys must be unique amongst themselves; similarly for row keys.")))
        .setJsonValidationInstructions(JSON_VALIDATION_INSTRUCTIONS)
        .hasNoChildJsonApiConverters()
        .noTrivialSampleJsonSupplied()
        .setNontrivialSampleJson(jsonObject(
            "rowKeys",    jsonStringArray("a", "b", "c"),
            "columnKeys", jsonStringArray("100", "200"),
            "data", jsonArray(
                jsonDoubleArray(7.1, 7.2),
                jsonDoubleArray(8.1, 8.2),
                jsonDoubleArray(9.1, 9.2))))
        .build();
  }

}
