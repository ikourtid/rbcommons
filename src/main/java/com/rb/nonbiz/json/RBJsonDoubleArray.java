package com.rb.nonbiz.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBBuilder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.json.RBJsonArrays.newJsonArrayWithExpectedSize;
import static com.rb.nonbiz.text.Strings.formatListInExistingOrder;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * JSON arrays don't have to have items of the same type in them.
 * This class enforces that all items are doubles.
 *
 * <p> Classes in our system are normally immutable. {@link JsonArray} (a Google / 3rd party class, not ours) is not.
 * The way we can make this class immutable is to return a new {@link JsonArray} every time we ask for one.
 * Otherwise, if there were a {@link JsonArray} private member, a caller could retrieve it and add to it.
 * This is less efficient, but safer. </p>
 *
 * <p> Another advantage is that, once we know this can only hold doubles, we can have a test matcher that uses an epsilon. </p>
 */
public class RBJsonDoubleArray {

  private final List<Double> rawDoublesList;

  private RBJsonDoubleArray(List<Double> rawDoublesList) {
    this.rawDoublesList = rawDoublesList;
  }

  public static RBJsonDoubleArray emptyRBJsonDoubleArray() {
    return new RBJsonDoubleArray(emptyList());
  }

  public static RBJsonDoubleArray singletonRBJsonDoubleArray(double item) {
    return new RBJsonDoubleArray(singletonList(item));
  }

  public static RBJsonDoubleArray rbJsonDoubleArray(double first, double second, double...rest) {
    List<Double> rawDoublesList = newArrayListWithExpectedSize(2 + rest.length);
    rawDoublesList.add(first);
    rawDoublesList.add(second);
    for (double item : rest) {
      rawDoublesList.add(item);
    }
    return new RBJsonDoubleArray(rawDoublesList);
  }

  public static RBJsonDoubleArray rbJsonDoubleArray(double[] items) {
    return rbJsonDoubleArray(Arrays.stream(items));
  }

  public static RBJsonDoubleArray rbJsonDoubleArray(DoubleStream items) {
    return new RBJsonDoubleArray(items.boxed().collect(Collectors.toList()));
  }

  public static RBJsonDoubleArray rbJsonDoubleArray(Iterator<Double> itemsIterator) {
    return new RBJsonDoubleArray(newArrayList(itemsIterator));
  }

  public static RBJsonDoubleArray rbJsonDoubleArray(Iterable<Double> items) {
    return new RBJsonDoubleArray(newArrayList(items.iterator()));
  }

  public JsonArray asJsonArray() {
    JsonArray jsonArray = newJsonArrayWithExpectedSize(rawDoublesList.size());
    rawDoublesList.forEach(v -> jsonArray.add(v));
    return jsonArray;
  }

  public int size() {
    return rawDoublesList.size();
  }

  // do not use this; it's here to help the matcher
  @VisibleForTesting
  List<Double> getRawDoublesList() {
    return rawDoublesList;
  }

  @Override
  public String toString() {
    return Strings.format("[RBJDA %s RBJDA]", formatListInExistingOrder(rawDoublesList));
  }


  public static class RBJsonDoubleArrayBuilder implements RBBuilder<RBJsonDoubleArray> {

    private final List<Double> rawDoublesList;

    private RBJsonDoubleArrayBuilder(List<Double> rawDoublesList) {
      this.rawDoublesList = newArrayList();
    }

    public static RBJsonDoubleArrayBuilder rbJsonDoubleArrayBuilder() {
      return new RBJsonDoubleArrayBuilder(newArrayList());
    }

    public static RBJsonDoubleArrayBuilder rbJsonDoubleArrayBuilderWithExpectedSize(int expectedSize) {
      return new RBJsonDoubleArrayBuilder(newArrayListWithExpectedSize(expectedSize));
    }

    public int size() {
      return rawDoublesList.size();
    }

    public RBJsonDoubleArrayBuilder add(double value) {
      rawDoublesList.add(value);
      return this;
    }

    @Override
    public void sanityCheckContents() {}

    @Override
    public RBJsonDoubleArray buildWithoutPreconditions() {
      return rbJsonDoubleArray(rawDoublesList.stream().mapToDouble(v -> v));
    }

  }

}
