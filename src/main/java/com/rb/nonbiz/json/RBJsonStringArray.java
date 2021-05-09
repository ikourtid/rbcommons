package com.rb.nonbiz.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.rb.nonbiz.util.RBBuilder;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.json.RBJsonArrays.newJsonArrayWithExpectedSize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * JSON arrays don't have to have items of the same type in them.
 * This class enforces that all items are String objects.
 *
 * Classes in our system are normally immutable. JsonArray (not our class) is not.
 * The way we can make this class immutable is to return a new JsonArray every time we ask for one.
 * Otherwise, if there were a JsonArray private member, a caller could retrieve it and add to it.
 */
public class RBJsonStringArray {

  private final List<String> rawStringsList;

  private RBJsonStringArray(List<String> rawStringsList) {
    this.rawStringsList = rawStringsList;
  }

  public static RBJsonStringArray emptyRBJsonStringArray() {
    return new RBJsonStringArray(emptyList());
  }

  public static RBJsonStringArray singletonRBJsonStringArray(String item) {
    return new RBJsonStringArray(singletonList(item));
  }

  public static RBJsonStringArray rbJsonStringArray(String first, String second, String...rest) {
    List<String> rawStringsList = newArrayList();
    rawStringsList.add(first);
    rawStringsList.add(second);
    for (String item : rest) {
      rawStringsList.add(item);
    }
    return new RBJsonStringArray(rawStringsList);
  }

  public static RBJsonStringArray rbJsonStringArray(List<String> rawStringsList) {
    return new RBJsonStringArray(rawStringsList);
  }

  public JsonArray asJsonArray() {
    JsonArray jsonArray = newJsonArrayWithExpectedSize(rawStringsList.size());
    rawStringsList.forEach(v -> jsonArray.add(v));
    return jsonArray;
  }

  public int size() {
    return rawStringsList.size();
  }

  // do not use this; it's here to help the matcher
  @VisibleForTesting
  List<String> getRawStringsList() {
    return rawStringsList;
  }


  public static class RBJsonStringArrayBuilder implements RBBuilder<RBJsonStringArray> {

    private List<String> rawStringsList;

    private RBJsonStringArrayBuilder() {
      rawStringsList = newArrayList();
    }

    public static RBJsonStringArrayBuilder rbJsonStringArrayBuilder() {
      return new RBJsonStringArrayBuilder();
    }

    public int size() {
      return rawStringsList.size();
    }

    public RBJsonStringArrayBuilder add(String value) {
      rawStringsList.add(value);
      return this;
    }

    @Override
    public void sanityCheckContents() {
    }

    @Override
    public RBJsonStringArray buildWithoutPreconditions() {
      return rbJsonStringArray(rawStringsList);
    }

  }

}
