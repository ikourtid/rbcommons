package com.rb.nonbiz.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBOrderingPreconditions;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.json.RBJsonArrays.newJsonArrayWithExpectedSize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * JSON arrays don't have to have items of the same type in them.
 * This class enforces that all items are {@link LocalDate} objects.
 *
 * <p> Classes in our system are normally immutable. {@link JsonArray} (a Google / 3rd party class, not ours) is not.
 * The way we can make this class immutable is to return a new {@link JsonArray} every time we ask for one.
 * Otherwise, if there were a {@link JsonArray} private member, a caller could retrieve it and add to it.
 * This is less efficient, but safer. </p>
 */
public class RBJsonLocalDateArray {

  private final List<LocalDate> rawLocalDatesList;

  private RBJsonLocalDateArray(List<LocalDate> rawLocalDatesList) {
    this.rawLocalDatesList = rawLocalDatesList;
  }

  public static RBJsonLocalDateArray emptyRBJsonLocalDateArray() {
    return new RBJsonLocalDateArray(emptyList());
  }

  public static RBJsonLocalDateArray singletonRBJsonLocalDateArray(LocalDate item) {
    return new RBJsonLocalDateArray(singletonList(item));
  }

  public static RBJsonLocalDateArray rbJsonLocalDateArray(LocalDate first, LocalDate second, LocalDate...rest) {
    List<LocalDate> rawLocalDatesList = newArrayList();
    rawLocalDatesList.add(first);
    rawLocalDatesList.add(second);
    for (LocalDate item : rest) {
      rawLocalDatesList.add(item);
    }
    return new RBJsonLocalDateArray(rawLocalDatesList);
  }

  static RBJsonLocalDateArray rbJsonLocalDateArray(List<LocalDate> rawLocalDatesList) {
    return new RBJsonLocalDateArray(rawLocalDatesList);
  }

  public JsonArray asJsonArray() {
    JsonArray jsonArray = newJsonArrayWithExpectedSize(rawLocalDatesList.size());
    rawLocalDatesList.forEach(v -> jsonArray.add(v.toString()));
    return jsonArray;
  }

  public int size() {
    return rawLocalDatesList.size();
  }

  // do not use this; it's here to help the matcher
  @VisibleForTesting
  List<LocalDate> getRawLocalDatesList() {
    return rawLocalDatesList;
  }


  public static class RBJsonLocalDateArrayBuilder implements RBBuilder<RBJsonLocalDateArray> {

    private List<LocalDate> rawLocalDatesList;

    private RBJsonLocalDateArrayBuilder() {
      rawLocalDatesList = newArrayList();
    }

    public static RBJsonLocalDateArrayBuilder rbJsonLocalDateArrayBuilder() {
      return new RBJsonLocalDateArrayBuilder();
    }

    public int size() {
      return rawLocalDatesList.size();
    }

    public RBJsonLocalDateArrayBuilder add(LocalDate value) {
      rawLocalDatesList.add(value);
      return this;
    }

    @Override
    public void sanityCheckContents() {
      // RBPreconditions#checkIncreasing does the same in less code, but LocalDate implements
      // Comparable<ChronoLocalDate>, not Comparable<LocalDate>, so we can't use #checkIncreasing here.
      RBOrderingPreconditions.checkConsecutive(
          rawLocalDatesList,
          (v1, v2) -> v1.compareTo(v2) < 0,
          "Dates must be increasing but were not: %s",
          rawLocalDatesList);
    }

    @Override
    public RBJsonLocalDateArray buildWithoutPreconditions() {
      return rbJsonLocalDateArray(rawLocalDatesList);
    }

  }

}
