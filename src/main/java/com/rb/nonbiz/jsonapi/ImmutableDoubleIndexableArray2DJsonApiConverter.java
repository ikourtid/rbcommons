package com.rb.nonbiz.jsonapi;

import com.google.common.collect.Iterators;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D;
import com.rb.nonbiz.collections.MutableDoubleIndexableArray2D;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.MutableDoubleIndexableArray2D.mutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiDocumentation.JsonApiDocumentationBuilder.jsonApiDocumentationBuilder;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.text.Strings.asSingleLine;

/**
 * Converts an {@link ImmutableDoubleIndexableArray2D} back and forth to JSON for our public API.
 */
public class ImmutableDoubleIndexableArray2DJsonApiConverter implements HasJsonApiDocumentation {

  public <R, C> JsonObject toJsonObject(
      ImmutableDoubleIndexableArray2D<R, C> array2D,
      Function<R, String> rowKeySerializer,
      Function<C, String> columnKeySerializer) {
    return jsonObject(
        "rowKeys", jsonArray(
            array2D.getRowMapping().size(),
            IntStream
                .range(0, array2D.getRowMapping().size())
                .mapToObj(i -> jsonString(rowKeySerializer.apply(array2D.getRowMapping().getKey(i))))),
        "columnKeys", jsonArray(
            array2D.getColumnMapping().size(),
            IntStream
                .range(0, array2D.getColumnMapping().size())
                .mapToObj(i -> jsonString(columnKeySerializer.apply(array2D.getColumnMapping().getKey(i))))),
        "data", jsonArray(
            array2D.getRowMapping().size(),
            IntStream.range(0, array2D.getRowMapping().size())
                .mapToObj(rowIndex -> jsonArray(
                    array2D.getColumnMapping().size(),
                    IntStream.range(0, array2D.getColumnMapping().size())
                        .mapToObj(columnIndex -> jsonDouble(array2D.getByIndex(rowIndex, columnIndex)))))));
  }

  public <R, C> ImmutableDoubleIndexableArray2D<R, C> fromJsonObject(
      JsonObject jsonObject,
      Function<String, R> rowKeyDeserializer,
      Function<String, C> columnKeyDeserializer) {
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
    return jsonApiDocumentationBuilder()
        .setClass(ImmutableDoubleIndexableArray2D.class)
        .setSingleLineSummary(label(asSingleLine(
            "An indexable 2-D array is like a regular 2-D array, except that you can ",
            "also access it based on more meaningful keys - not just integer indices.")))
        .setDocumentationHtml("FIXME IAK / FIXME SWA JSONDOC")
        .hasNoChildNodes()
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

}
