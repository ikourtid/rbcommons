package com.rb.nonbiz.text;

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.AssetId;
import com.rb.biz.types.asset.AssetId.AssetIdVisitor;
import com.rb.biz.types.asset.CashId;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.Deviations;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.ImmutableIndexableArray1D;
import com.rb.nonbiz.collections.NonZeroDeviations;
import com.rb.nonbiz.collections.Partition;
import com.rb.nonbiz.collections.RBIterators;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.types.UnitFraction;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasters.displaySymbol;
import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isWhitespace;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByKey;

/**
 * Various versions of Java's {@link String#format} methods, with the advantage that {@link Strings#format}
 * doesn't throw an exception if the format doesn't match its arguments.
 */
public class Strings {

  /**
   * Java's String.format will throw if the arguments don't match the format - e.g. String.format("%s %s", "A")
   * A safer thing to use is this method, which will not throw, and print something reasonable if there are
   * missing or extra arguments after the format string.
   *
   * This is copied from Guava preconditions class, where it is package private (and therefore we can't just call it).
   */
  public static String format(String template, Object... args) {
    template = String.valueOf(template);
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;

    int i;
    int placeholderStart;
    for (i = 0; i < args.length; templateStart = placeholderStart + 2) {
      placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }

      builder.append(template.substring(templateStart, placeholderStart));
      builder.append(args[i++]);
    }

    builder.append(template.substring(templateStart));
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);

      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }

      builder.append(']');
    }

    return builder.toString();
  }

  public static String formatDoubleArray(double[] doubleArray) {
    return sizePrefix(doubleArray.length) + formatDoubleArray("%f", doubleArray, ", ");
  }

  public static String formatDoubleArray(String format, double[] doubleArray, String joinPattern) {
    return sizePrefix(doubleArray.length) + Joiner.on(joinPattern).join(Arrays
        .stream(doubleArray)
        .mapToObj(d -> String.format(format, d))
        .iterator());
  }

  public static String toBasisPoints(double value, int numDecimalDigits) {
    return toBasisPoints(value, numDecimalDigits, true);
  }

  public static String formatNDigits(double value, int numDecimalDigits) {
    return String.format("%." + numDecimalDigits + "f", value);
  }

  public static String toBasisPoints(double value, int numDecimalDigits, boolean includeSuffix) {
    return includeSuffix
        ? String.format("%." + numDecimalDigits + "f bps", 10_000 * value)
        : String.format("%." + numDecimalDigits + "f", 10_000 * value);
  }

  public static <K, V> String formatMapInKeyOrder(RBMap<K, V> map, Comparator<K> keyComparator, String separator) {
    return formatMapInKeyOrder(map, keyComparator, separator, v -> v.toString());
  }

  public static <K, V> String formatMapInKeyOrder(
      RBMap<K, V> map, Comparator<K> keyComparator, String separator, Function<V, String> valueTransformer) {
    return formatSortedMapHelper(map, separator, (e1, e2) -> keyComparator.compare(e1.getKey(), e2.getKey()), valueTransformer);
  }

  public static <K, V> String formatMapInValueOrder(RBMap<K, V> map, Comparator<V> valueComparator, String separator) {
    return formatMapInValueOrder(map, valueComparator, separator, v -> v.toString());
  }

  public static <K, V> String formatMapInValueOrder(
      RBMap<K, V> map, Comparator<V> valueComparator, String separator, Function<V, String> valueTransformer) {
    return formatSortedMapHelper(
        map, separator, (e1, e2) -> valueComparator.compare(e1.getValue(), e2.getValue()), valueTransformer);
  }

  public static <K extends Comparable<? super K>, V> String formatMap(RBMap<K, V> map, String separator) {
    return formatSortedMapHelper(map, separator, comparing(e -> e.getKey()));
  }

  public static <K, V extends PrintsInstruments> String formatMapWhereValuesPrintInstruments(
      RBMap<K, V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatMapHelper(
        map,
        key -> key.toString(),
        value -> value.toString(instrumentMaster, date));
  }

  public static <K extends Comparable<? super K>, V extends PrintsInstruments> String
  formatMapInKeyOrderWhereValuesPrintInstruments(
      RBMap<K, V> map, String separator, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatSortedMapHelper(
        map,
        separator,
        comparingByKey(),
        value -> value.toString(instrumentMaster, date));
  }

  private static <K, V> String formatMapHelper(
      RBMap<K, V> map, Function<K, String> keyTransformer, Function<V, String> valueTransformer) {
    return sizePrefix(map.size()) + Joiner.on(" , ").join(map.entrySet()
        .stream()
        .map(entry -> Strings.format("%s = %s",
            keyTransformer.apply(entry.getKey()),
            valueTransformer.apply(entry.getValue())))
        .iterator());
  }

  public static <K extends PrintsInstruments, V extends PrintsInstruments>
  String formatMapOfPrintsInstrumentsToPrintsInstruments(
      RBMap<K, V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatMapHelper(
        map,
        key -> key.toString(instrumentMaster, date),
        value -> value.toString(instrumentMaster, date));
  }

  public static <K extends PrintsInstruments, V extends PrintsInstruments>
  String formatIndexableArrayOfPrintsInstrumentsToPrintsInstruments(
      ImmutableIndexableArray1D<K, V> indexableArray1D, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIndexableArray1DHelper(
        indexableArray1D,
        key -> key.toString(instrumentMaster, date),
        value -> value.toString(instrumentMaster, date));
  }

  public static <K, V extends PrintsInstruments>
  String formatIndexableArray1DWhereValuesPrintInstruments(
      ImmutableIndexableArray1D<K, V> indexableArray1D, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIndexableArray1DHelper(
        indexableArray1D,
        key -> key.toString(),
        value -> value.toString(instrumentMaster, date));
  }

  public static <K extends PrintsInstruments, V>
  String formatIndexableArray1DWhereKeysPrintInstruments(
      ImmutableIndexableArray1D<K, V> indexableArray1D, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIndexableArray1DHelper(
        indexableArray1D,
        key -> key.toString(instrumentMaster, date),
        value -> value.toString());
  }

  private static <K, V> String formatIndexableArray1DHelper(
      ImmutableIndexableArray1D<K, V> indexableArray1D,
      Function<K, String> keyTransformer,
      Function<V, String> valueTransformer) {
    return sizePrefix(indexableArray1D.size()) + Joiner.on(" , ").join(
        IntStream.range(0, indexableArray1D.size())
            .mapToObj(i -> Strings.format("%s = %s",
                keyTransformer.apply(indexableArray1D.getKey(i)),
                valueTransformer.apply(indexableArray1D.getByIndex(i))))
            .iterator());
  }

  private static <K, V> String formatSortedMapHelper(
      RBMap<K, V> map, String separator, Comparator<Entry<K, V>> entryComparator) {
    return formatSortedMapHelper(map, separator, entryComparator, v -> v.toString());
  }

  private static <K, V> String formatSortedMapHelper(
      RBMap<K, V> map, String separator, Comparator<Entry<K, V>> entryComparator, Function<V, String> valueTransformer) {
    return sizePrefix(map.size()) + Joiner.on(separator).join(map.entrySet()
        .stream()
        .sorted(entryComparator)
        .map(entry -> Strings.format("%s = %s", entry.getKey(), valueTransformer.apply(entry.getValue())))
        .iterator());
  }

  public static <V> String formatIidMap(IidMap<V> map) {
    return formatIidMap(map, NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  public static <V> String formatIidMap(IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIidMap(map, instrumentMaster, date, false);
  }

  public static <V> String formatIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    StringBuilder sb = new StringBuilder();
    sb.append(sizePrefix(map.size()));
    map.forEachIidSortedEntry((instrumentId, value) ->
        sb.append(Strings.format("%s = %s ",
            displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds), value)));
    return sb.toString();
  }

  public static <V> String formatIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date, Function<V, String> valueTransformer) {
    return formatIidMap(map, instrumentMaster, date, valueTransformer, false);
  }

  public static <V> String formatIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date, Function<V, String> valueTransformer,
      boolean printInstrumentIds) {
    StringBuilder sb = new StringBuilder();
    sb.append(sizePrefix(map.size()));
    map.forEachIidSortedEntry((instrumentId, value) ->
        sb.append(Strings.format("%s = %s ",
            displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds), valueTransformer.apply(value))));
    return sb.toString();
  }

  public static <V extends PrintsInstruments> String formatIidMapOfPrintsInstruments(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return formatIidMapOfPrintsInstruments(map, instrumentMaster, date, false);
  }

  public static <V extends PrintsInstruments> String formatIidMapOfPrintsInstruments(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return formatIidMapOfPrintsInstruments(map, " , ", instrumentMaster, date, printInstrumentIds);
  }

  public static <V extends PrintsInstruments> String formatIidMapOfPrintsInstruments(
      IidMap<V> map, String joiningString, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return sizePrefix(map.size()) + Joiner.on(joiningString).join(
        map.toIidSortedTransformedEntriesStream(
            (instrumentId, value) -> Strings.format("%s = %s",
                displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds),
                value.toString(instrumentMaster, date)))
            .iterator());
  }

  public static <V extends PrintsInstruments> String formatSortedIidMapOfPrintsInstruments(
      IidMap<V> map, Comparator<V> comparator, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return sizePrefix(map.size()) + Joiner.on(" , ").join(
        map.toSortedTransformedEntriesStream(
            (instrumentId, value) -> Strings.format("%s = %s",
                displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds),
                value.toString(instrumentMaster, date)),
            comparator)
            .iterator());
  }

  public static <V> String formatSortedIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date,
      String joinString, Comparator<V> comparator) {
    return formatSortedIidMap(map, instrumentMaster, date, joinString, comparator, false);
  }

  public static <V> String formatSortedIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date,
      String joinString, Comparator<V> comparator, boolean printInstrumentIds) {
    return formatSortedIidMap(map, instrumentMaster, date, joinString, comparator, v -> v.toString(), printInstrumentIds);
  }

  public static <V> String formatSortedIidMap(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date,
      String joinString, Comparator<V> comparator, Function<V, String> valueToStringConverter,
      boolean printsInstrumentIds) {
    return sizePrefix(map.size()) + Joiner.on(joinString).join(map
        .instrumentIdStream()
        .sorted(comparing(instrumentId -> map.getOrThrow(instrumentId), comparator))
        .map(instrumentId -> Strings.format("%s = %s",
            displaySymbol(instrumentId, instrumentMaster, date, printsInstrumentIds),
            valueToStringConverter.apply(map.getOrThrow(instrumentId))))
        .iterator());
  }

  /**
   * Prints in sorted instrument order, but does not print the InstrumentId in the IidMap. This is useful in cases where
   * the value in the map will print the InstrumentId (or corresponding Symbol) anyway, such as with SellOrders.
   */
  public static <V extends PrintsInstruments> String formatValuesOnlyIidMapOfPrintsInstruments(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return sizePrefix(map.size())
        + formatValuesOnlyIidMapOfPrintsInstrumentsWithoutSize(map, instrumentMaster, date);
  }

  /**
   * Prints in sorted instrument order, but does not print the InstrumentId in the IidMap. This is useful in cases where
   * the value in the map will print the InstrumentId (or corresponding Symbol) anyway, such as with SellOrders.
   */
  public static <V extends PrintsInstruments> String formatValuesOnlyIidMapOfPrintsInstrumentsWithoutSize(
      IidMap<V> map, InstrumentMaster instrumentMaster, LocalDate date) {
    return Joiner.on(" , ").join(
        map.toIidSortedTransformedEntriesStream(
            (instrumentId, value) -> value.toString(instrumentMaster, date))
            .iterator());
  }

  public static <T extends PrintsInstruments> String formatCollectionOfPrintsInstruments(
      Collection<T> values, InstrumentMaster instrumentMaster, LocalDate date) {
    return sizePrefix(values.size()) + Joiner.on(" , ").join(values
        .stream()
        .map(v -> v.toString(instrumentMaster, date))
        .iterator());
  }

  public static String formatPartitionOfAssetIdInIncreasingId(
      Partition<AssetId> partition, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return formatPartitionOfAssetIdHelper(
        partition, comparing(e -> e.getKey()), instrumentMaster, date, printInstrumentIds);
  }

  public static String formatPartitionOfAssetIdInDecreasingFraction(
      Partition<AssetId> partition, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return formatPartitionOfAssetIdHelper(
        partition, comparing(e -> e.getValue(), reverseOrder()), instrumentMaster, date, printInstrumentIds);
  }

  private static String formatPartitionOfAssetIdHelper(
      Partition<AssetId> partition, Comparator<Entry<AssetId, UnitFraction>> entryComparator,
      InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return partition.toStringInOrder(2, entryComparator, assetId -> assetId.visit(new AssetIdVisitor<String>() {
      @Override
      public String visitInstrumentId(InstrumentId instrumentId) {
        return displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds);
      }

      @Override
      public String visitCash(CashId cashId) {
        return "cash";
      }
    }));
  }

  public static String formatDeviationsOfAssetId(
      Deviations<AssetId> deviations, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return deviations.toStringInIncreasingAbsDeviation(4, assetId -> assetId.visit(new AssetIdVisitor<String>() {
      @Override
      public String visitInstrumentId(InstrumentId instrumentId) {
        return displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds);
      }

      @Override
      public String visitCash(CashId cashId) {
        return "cash";
      }
    }));
  }

  public static String formatNonZeroDeviationsOfAssetId(
      NonZeroDeviations<AssetId> nonZeroDeviations, InstrumentMaster instrumentMaster, LocalDate date, boolean printInstrumentIds) {
    return nonZeroDeviations.toString(4, assetId -> assetId.visit(new AssetIdVisitor<String>() {
      @Override
      public String visitInstrumentId(InstrumentId instrumentId) {
        return displaySymbol(instrumentId, instrumentMaster, date, printInstrumentIds);
      }

      @Override
      public String visitCash(CashId cashId) {
        return "cash";
      }
    }));
  }

  /**
   * This is handy in situations where we need to create a multiline string,
   * but want to avoid the ugliness of having plus signs in the Java code for concatenation,
   * which messes up indentation.
   *
   * <p> This also adds a space between any pair of consecutive lines, in the event that concatenating them
   * would result in concatenating two words. This method is used for human-readable text, so if the code author
   * forgot to add spaces between lines, we don't want </p>
   */
  public static String asSingleLine(String first, String ... rest) {
    StringBuilder sb = new StringBuilder(first);
    if (!lastCharacterIsWhitespace(first) && rest.length > 0 && !firstCharacterIsWhitespace(rest[0])) {
      // concatenating the first two lines ('first' and 'rest[0]') might end up joining two words and making the
      // text illegible, so let's add a space, although ideally the caller would avoid that in the first place.
      sb.append(' ');
    }
    for (int i = 0; i < rest.length; i++) {
      String s = rest[i];
      sb.append(s);
      // Similar to previous comment. If the current line doesn't end in whitespace, and there's another line after
      // it, and that next line doesn't start with a whitespace, add a space between the two lines.
      if (!lastCharacterIsWhitespace(s) && i < rest.length - 1 && !firstCharacterIsWhitespace(rest[i + 1])) {
        sb.append(' ');
      }
    }
    return sb.toString();
  }

  /**
   * Returns true if the last character of the string is a whitespace character (space, tab, newline, etc.)
   * Returns false for an empty string, since an empty string doesn't have a last character.
   */
  public static boolean lastCharacterIsWhitespace(String s) {
    if (s.isEmpty()) {
      return false;
    }
    return isWhitespace(s.charAt(s.length() - 1));
  }

  /**
   * Returns true if the last character of the string is a whitespace character (space, tab, newline, etc.)
   * Returns false for an empty string, since an empty string doesn't have a last character.
   */
  public static boolean firstCharacterIsWhitespace(String s) {
    if (s.isEmpty()) {
      return false;
    }
    return isWhitespace(s.charAt(0));
  }

  /**
   * This is handy in situations where we need to create a multiline string,
   * but want to avoid the ugliness of having plus signs in the Java code for concatenation,
   * which messes up indentation.
   */
  public static String asSingleLineWithNewlines(String first, String... rest) {
    StringBuilder sb = new StringBuilder(first).append('\n');
    for (String s : rest) {
      sb.append(s).append('\n');
    }
    return sb.toString();
  }

  /**
   * Returns a sentence with the conjunction of several items
   * in a way that reads well in English.
   */
  public static String joinWithHarvardComma(List<String> items) {
    int size = items.size();
    switch (size) {
      case 0:
        return "";

      case 1:
        return getOnlyElement(items);

      case 2:
        return Strings.format("%s and %s", items.get(0), items.get(1));

      default:
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size - 2; i++) {
          sb.append(items.get(i));
          sb.append(", ");
        }
        sb.append(items.get(size - 2));
        sb.append(", and ");
        sb.append(items.get(size - 1));
        return sb.toString();
    }
  }

  /**
   * This is not very rigorous, but it will capitalize the first letter, and add a period to the end,
   * if one is missing.
   *
   * Example: "we will buy $100 A" {@code ->} "We will buy $100 A."
   */
  public static String toTrimmedStandaloneSentence(String possiblyIncompleteSentence) {
    if (isUpperCase(possiblyIncompleteSentence.charAt(0))
        && possiblyIncompleteSentence.endsWith(".")) {
      // small performance optimization, so if we don't need to modify the string, we just return it.
      return possiblyIncompleteSentence;
    }
    StringBuilder sb = new StringBuilder();
    possiblyIncompleteSentence = possiblyIncompleteSentence.trim();
    sb.append(Character.toUpperCase(possiblyIncompleteSentence.charAt(0)));
    sb.append(possiblyIncompleteSentence.substring(1));
    if (!possiblyIncompleteSentence.endsWith(".")) {
      sb.append('.');
    }
    return sb.toString();
  }

  /**
   * This is a bit more general than using range.toString(), especially since it allows control of how to print
   * the individual bounds when they exist.
   */
  public static <C extends Comparable<? super C>> String formatRange(Range<C> range, Function<C, String> boundToString) {
    StringBuilder sb = new StringBuilder();
    if (!range.hasLowerBound()) {
      sb.append("(-∞");
    } else {
      switch (range.lowerBoundType()) {
        case OPEN:
          sb.append('(');
          break;

        case CLOSED:
          sb.append('[');
          break;

        default:
          throw new IllegalArgumentException(Strings.format("Cannot handle lower bound type %s", range.lowerBoundType()));
      }
      sb.append(boundToString.apply(range.lowerEndpoint()));
    }
    sb.append('…');
    if (!range.hasUpperBound()) {
      sb.append("∞)");
    } else {
      sb.append(boundToString.apply(range.upperEndpoint()));
      switch (range.upperBoundType()) {
        case OPEN:
          sb.append(')');
          break;

        case CLOSED:
          sb.append(']');
          break;

        default:
          throw new IllegalArgumentException(Strings.format("Cannot handle upper bound type %s", range.upperBoundType()));
      }
    }
    return sb.toString();
  }

  public static <C extends Comparable<? super C>> String formatRange(Range<C> range) {
    return formatRange(range, v -> v.toString());
  }

  /**
   * Use this for cases where you want to use 'natural' order for items implementing {@link Comparable}.
   */
  public static <T extends Comparable<? super T>> String formatCollectionInOrder(Collection<T> collection) {
    return formatCollectionInOrder(collection, Ordering.natural());
  }

  /**
   * Use this for cases where you want to specify the ordering, and T does not necessarily implement Comparable.
   */
  public static <T> String formatCollectionInOrder(Collection<T> collection, Comparator<T> comparator) {
    return sizePrefix(collection.size()) +
        Joiner.on(' ').join(collection.stream().sorted(comparator).iterator());
  }

  public static <T> String formatCollectionInDefaultOrder(Collection<T> collection) {
    return sizePrefix(collection.size()) +
        Joiner.on(' ').join(collection.stream().iterator());
  }

  public static <T> String formatListInExistingOrder(List<T> list) {
    return sizePrefix(list.size()) +
        Joiner.on(' ').join(list.stream().iterator());
  }

  public static <T extends PrintsInstruments> String formatListOfPrintsInstrumentsInExistingOrder(
      List<T> list, InstrumentMaster instrumentMaster, LocalDate date) {
    return sizePrefix(list.size()) +
        Joiner.on(' ').join(list.stream().map(v -> v.toString(instrumentMaster, date)).iterator());
  }

  public static String sizePrefix(int size) {
    return size <= 2
        ? ""
        : size + " : ";
  }

  public static <T> String formatOptional(Optional<T> optional) {
    return formatOptional(optional, v -> v.toString());
  }

  public static <T> String formatOptional(Optional<T> optional, Function<T, String> valueTransformer) {
    return optional.map(v -> valueTransformer.apply(v)).orElse("(none)");
  }

  public static <T extends PrintsInstruments> String formatOptionalPrintsInstruments(
      Optional<T> optional, InstrumentMaster instrumentMaster, LocalDate date) {
    return optional.map(v -> v.toString(instrumentMaster, date)).orElse("(none)");
  }

}
