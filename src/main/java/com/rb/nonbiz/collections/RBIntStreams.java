package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RBIntStreams {

  /**
   * If e.g. we have a range of numbers e.g. 0, 1, 2, 3, 4, 5, 6, and groupSize is 4, then this will return the result
   * of run the 'reducer' function twice, with these arguments:
   * first time:  group number = 0, int stream = [0, 1, 2, 3]
   * second time: group number = 1, int stream = [4, 5, 6]
   *
   * It is good to separate out the iteration logic from whatever business logic wants to group things,
   * because (a) we can reuse it and (b) we don't need to retest it.
   */
  public static <V> Stream<V> reduceInGroups(ClosedRange<Integer> range, int groupSize, BiFunction<Integer, IntStream, V> reducer) {
    RBPreconditions.checkArgument(groupSize >= 1);
    int lower = range.lowerEndpoint();
    int upper = range.upperEndpoint();
    int rangeWidthInclusive = upper - lower + 1;
    int numGroupsNeeded = (int) Math.ceil( ((double) rangeWidthInclusive) / groupSize);

    return IntStream
        .range(0, numGroupsNeeded) // intentional integer division
        .mapToObj(groupNo -> {
          int groupStart = lower + groupNo * groupSize;
          return reducer.apply(groupNo, IntStream.rangeClosed(groupStart, Math.min(groupStart + groupSize - 1, upper)));
        });
  }

  /**
   * Similar to above, except that instead of passing an IntStream, we only pass the first and last index.
   * first time:  group number = 0, group bounds (inclusive) = 0, 3
   * second time: group number = 1, group bounds (inclusive) = 4, 6
   *
   * This isn't really related to an IntStream, but it is similar to the previous method, so we'll keep it here.
   */
  public static <V> Stream<V> reduceInGroups(
      ClosedRange<Integer> range, int groupSize, TriFunction<Integer, Integer, Integer, V> reducer) {
    RBPreconditions.checkArgument(groupSize >= 1);
    int lower = range.lowerEndpoint();
    int upper = range.upperEndpoint();
    int rangeWidthInclusive = upper - lower + 1;
    int numGroupsNeeded = (int) Math.ceil( (double) rangeWidthInclusive / groupSize);

    return IntStream
        .range(0, numGroupsNeeded) // intentional integer division
        .mapToObj(groupNo -> {
          int groupStart = lower + groupNo * groupSize;
          return reducer.apply(groupNo, groupStart, Math.min(groupStart + groupSize - 1, upper));
        });
  }

}
