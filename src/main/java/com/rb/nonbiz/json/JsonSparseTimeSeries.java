package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.json.RBJsonLocalDateArray.RBJsonLocalDateArrayBuilder;
import com.rb.nonbiz.json.RBJsonStringArray.RBJsonStringArrayBuilder;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonLocalDateArray.RBJsonLocalDateArrayBuilder.rbJsonLocalDateArrayBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonStringArray.RBJsonStringArrayBuilder.rbJsonStringArrayBuilder;

/**
 * Represents a collection of (date, y) points, expressed in JSON format.
 *
 * <p> It is called 'sparse' because it is suited for representing cases where the dates aren't consecutive, i.e.
 * there are gaps in the dates. It also allows for y values of any type, not just numbers. </p>
 *
 * <p> This is a more general version of JsonPointList. </p>
 */
public class JsonSparseTimeSeries {

  private final RBJsonLocalDateArray xCoordinates;
  private final List<JsonElement> yCoordinates;
  private final Optional<RBJsonStringArray> textLabels;
  private final int sharedSize;

  private JsonSparseTimeSeries(
      RBJsonLocalDateArray xCoordinates,
      List<JsonElement> yCoordinates,
      Optional<RBJsonStringArray> textLabels,
      int sharedSize) {
    this.xCoordinates = xCoordinates;
    this.yCoordinates = yCoordinates;
    this.textLabels = textLabels;
    this.sharedSize = sharedSize;
  }

  public RBJsonLocalDateArray getXCoordinates() {
    return xCoordinates;
  }

  public JsonArray getYCoordinates() {
    return jsonArray(yCoordinates);
  }

  public Optional<RBJsonStringArray> getTextLabels() {
    return textLabels;
  }

  public boolean isEmpty() {
    return sharedSize == 0;
  }

  public JsonObject asJsonObject() {
    JsonObject jsonObject = jsonObject(
        "x", getXCoordinates().asJsonArray(),
        "y", getYCoordinates());
    getTextLabels().ifPresent(labels ->
        jsonObject.add("text", labels.asJsonArray()));
    return jsonObject;
  }

  @Override
  public String toString() {
    return Strings.format("[JA %s %s %s JA]", xCoordinates, yCoordinates, textLabels);
  }


  public static class JsonSparseTimeSeriesBuilder implements RBBuilder<JsonSparseTimeSeries> {

    private final RBJsonLocalDateArrayBuilder xBuilder;
    private final List<JsonElement> yBuilder;
    private final RBJsonStringArrayBuilder textLabelsBuilder;
    private boolean hasTextLabels;

    private JsonSparseTimeSeriesBuilder() {
      xBuilder = rbJsonLocalDateArrayBuilder();
      yBuilder = newArrayList();
      textLabelsBuilder = rbJsonStringArrayBuilder();
      hasTextLabels = false;
    }

    public static JsonSparseTimeSeriesBuilder jsonSparseTimeSeriesBuilder() {
      return new JsonSparseTimeSeriesBuilder();
    }

    public JsonSparseTimeSeriesBuilder addPoint(LocalDate date, JsonElement value) {
      return addPoint(date, value, "");
    }

    public JsonSparseTimeSeriesBuilder addPoint(LocalDate date, JsonElement value, String textLabel) {
      xBuilder.add(date);
      yBuilder.add(value);
      textLabelsBuilder.add(textLabel);
      if (!textLabel.isEmpty()) {
        hasTextLabels = true;
      }
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBSimilarityPreconditions.checkAllSame(
          ImmutableList.of(xBuilder.size(), yBuilder.size(), textLabelsBuilder.size()),
          "All arrays must have the same size");
    }

    @Override
    public JsonSparseTimeSeries buildWithoutPreconditions() {
      return new JsonSparseTimeSeries(
          xBuilder.build(),
          yBuilder,
          hasTextLabels
              ? Optional.of(textLabelsBuilder.build())
              : Optional.empty(),
          xBuilder.size());
    }

  }

}
