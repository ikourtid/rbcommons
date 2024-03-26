package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder;
import com.rb.nonbiz.json.RBJsonLocalDateArray.RBJsonLocalDateArrayBuilder;
import com.rb.nonbiz.json.RBJsonStringArray.RBJsonStringArrayBuilder;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.time.LocalDate;
import java.util.Optional;

import static com.rb.nonbiz.json.RBJsonDoubleArray.RBJsonDoubleArrayBuilder.rbJsonDoubleArrayBuilder;
import static com.rb.nonbiz.json.RBJsonLocalDateArray.RBJsonLocalDateArrayBuilder.rbJsonLocalDateArrayBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonStringArray.RBJsonStringArrayBuilder.rbJsonStringArrayBuilder;

/**
 * Represents a daily timeseries as a collection of (x, y) points, expressed in JSON format, with separate x and y vectors,
 * which is the way our 3rd party chart library (Plot.ly) expects to see them.
 *
 * <p> The y vector is restricted to be a number (double). </p>
 */
public class JsonPointList {

  private final RBJsonLocalDateArray xCoordinates;
  private final RBJsonDoubleArray yCoordinates;
  private final Optional<RBJsonStringArray> textLabels;
  private final int sharedSize;

  private JsonPointList(
      RBJsonLocalDateArray xCoordinates,
      RBJsonDoubleArray yCoordinates,
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

  public RBJsonDoubleArray getYCoordinates() {
    return yCoordinates;
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
        "y", getYCoordinates().asJsonArray());
    getTextLabels().ifPresent(labels ->
        jsonObject.add("text", labels.asJsonArray()));
    return jsonObject;
  }

  @Override
  public String toString() {
    return Strings.format("[JA %s %s %s JA]", xCoordinates, yCoordinates, textLabels);
  }


  public static class JsonPointListBuilder implements RBBuilder<JsonPointList> {

    private final RBJsonLocalDateArrayBuilder xBuilder;
    private final RBJsonDoubleArrayBuilder yBuilder;
    private final RBJsonStringArrayBuilder textLabelsBuilder;
    private boolean hasTextLabels;

    private JsonPointListBuilder() {
      xBuilder = rbJsonLocalDateArrayBuilder();
      yBuilder = rbJsonDoubleArrayBuilder();
      textLabelsBuilder = rbJsonStringArrayBuilder();
      hasTextLabels = false;
    }

    public static JsonPointListBuilder jsonPointListBuilder() {
      return new JsonPointListBuilder();
    }

    public JsonPointListBuilder addPoint(LocalDate date, double value) {
      return addPoint(date, value, "");
    }

    public JsonPointListBuilder addPoint(LocalDate date, double value, String textLabel) {
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
    public JsonPointList buildWithoutPreconditions() {
      return new JsonPointList(
          xBuilder.build(),
          yBuilder.build(),
          hasTextLabels
              ? Optional.of(textLabelsBuilder.build())
              : Optional.empty(),
          xBuilder.size());
    }

  }

}
