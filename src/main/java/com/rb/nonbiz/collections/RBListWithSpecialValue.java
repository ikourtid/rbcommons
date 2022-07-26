package com.rb.nonbiz.collections;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.List;
import java.util.OptionalInt;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This is like a plain {@code List<T>}, except with the semantics that a single item may be singled out as somehow being special.
 * It's possible to have no such 'special' item, but we can't have more than one.
 */
public class RBListWithSpecialValue<T> {

  private final List<T> rawList;
  private final OptionalInt indexOfSpecialValue;

  private RBListWithSpecialValue(List<T> rawList, OptionalInt indexOfSpecialValue) {
    this.rawList = rawList;
    this.indexOfSpecialValue = indexOfSpecialValue;
  }

  public static <T> RBListWithSpecialValue<T> emptyRBListWithSpecialValue() {
    return new RBListWithSpecialValue<>(emptyList(), OptionalInt.empty());
  }

  public static <T> RBListWithSpecialValue<T> rbListWithoutSpecialValue(List<T> list) {
    return new RBListWithSpecialValue<>(list, OptionalInt.empty());
  }

  public static <T> RBListWithSpecialValue<T> singletonRBListWithoutSpecialValue(T item) {
    return new RBListWithSpecialValue<>(singletonList(item), OptionalInt.empty());
  }

  public static <T> RBListWithSpecialValue<T> singletonRBListWithSpecialValue(T item) {
    return new RBListWithSpecialValue<>(singletonList(item), OptionalInt.of(0));
  }

  public static <T> RBListWithSpecialValue<T> rbListWithSpecialValueSpecified(
      OptionalInt indexOfSpecialValue, List<T> rawList) {
    indexOfSpecialValue.ifPresent(v ->
        RBPreconditions.checkInRange(v, Range.closed(0, rawList.size() - 1)));
    RBPreconditions.checkArgument(
        rawList.stream().noneMatch(item -> item == null),
        "You cannot have nulls (i.e. the special isSpecialValue()) in the list of items when using this constructor");
    return new RBListWithSpecialValue<>(rawList, indexOfSpecialValue);
  }

  /**
   * This is handy and expedient, although somewhat ugly.
   * If null appears after any item, it means that that item is the 'special' one.
   * The nice thing is that this allows for a compact instantiation of a RBListWithSpecialValue
   *
   * You might say - "Why not a CTOR that takes an optional int and a list of values? The 'null' seems convoluted."
   *
   * That's a valid alternative, and it was my original design.
   * However, the number is hard to read if there are many choices for a knob,
   * whereas the special value is easier to read inline. In particular, if we remove / add choices
   * (often in a temporary branch - but still), it is easier to forget to update the int, whereas adding or deleting
   * lines won't have that problem.
   */
  @SafeVarargs
  public static <T> RBListWithSpecialValue<T> rbListWithSpecialValue(T first, T second, T... rest) {
    List<T> rawList = newArrayListWithExpectedSize(2 + (rest == null ? 0 : rest.length));
    RBPreconditions.checkArgument(
        first != null,
        "The special null value must follow a non-null item, but we have null in the 1st position");
    OptionalInt indexOfSpecialValue = OptionalInt.empty();
    rawList.add(first);
    if (second == null) {
      indexOfSpecialValue = OptionalInt.of(0);
    } else {
      rawList.add(second);
    }
    // When we want to denote 'second' as the special item, 'rest' will be a null array, not an empty array.
    // So we need to handle that separately.
    if (rest == null) {
      RBPreconditions.checkArgument(
          !indexOfSpecialValue.isPresent(),
          "We have multiple 'null' markers, in locations 0 and 1");
      return new RBListWithSpecialValue<>(rawList, OptionalInt.of(1));
    } else {
      for (int i = 0; i < rest.length; i++) {
        if (rest[i] == null) {
          int index = i + 1;
          indexOfSpecialValue.ifPresent( v -> {
                throw new IllegalArgumentException(Strings.format(
                    "We have multiple 'null' markers, in locations %s and %s", v, index));
              });
          indexOfSpecialValue = OptionalInt.of(index);
        } else {
          rawList.add(rest[i]);
        }
      }
    }
    return new RBListWithSpecialValue<>(rawList, indexOfSpecialValue);
  }

  // This is easier to read than null, and more explicit
  public static <T> T isSpecialValue() {
    return null;
  }

  public List<T> getRawList() {
    return rawList;
  }

  public OptionalInt getIndexOfSpecialValue() {
    return indexOfSpecialValue;
  }

  @Override
  public String toString() {
    return Strings.format("[RBLWSV %s %s RBLWSV]", getIndexOfSpecialValue(), getRawList());
  }

}
