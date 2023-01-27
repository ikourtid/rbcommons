package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.RBMap;

import java.util.Arrays;

import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;

/**
 * Contains information about how to convert an enum (the 'convertible' kind, i.e. {@link RoundTripStringConvertibleEnum})
 * back and forth from a unique (i.e. no two enum values share the same) and stable (i.e. does not depend on Java
 * reflection information that can change if we change the code) string representation.
 *
 * <p> Strictly speaking, this only converts <em> from </em> a string <em> to </em> an enum value;
 * the opposite is trivial, because one can always invoke {@link RoundTripStringConvertibleEnum#toUniqueStableString()}
 * on the enum value to find the string. </p>
 *
 * <p> Although we could always instantiate one of these on the fly, it's probably best practice to have a static
 * final object of this type inside every {@link RoundTripStringConvertibleEnum}, and expose a static method
 * to convert a string to an enum value, which then calls {@link EnumStringRoundTripConversionInfo#getEnumValue(String)}.
 * We can't enforce this across enums by making them implement some interface, because we're talking about a static
 * method, and static methods can't be part of an interface. But it can be a good convention. </p>
 */
public class EnumStringRoundTripConversionInfo<E extends Enum<E> & RoundTripStringConvertibleEnum<E>> {

  private final RBMap<String, E> rawMap;

  private EnumStringRoundTripConversionInfo(RBMap<String, E> rawMap) {
    this.rawMap = rawMap;
  }

  public static <E extends Enum<E> & RoundTripStringConvertibleEnum<E>>
  EnumStringRoundTripConversionInfo<E> enumStringRoundTripConversionInfo(Class<E> clazz) {
    RBPreconditions.checkArgument(
        clazz.getEnumConstants().length > 0,
        "Must have at least one enum defined in %s",
        clazz);
    return new EnumStringRoundTripConversionInfo<>(rbMapFromStream(
        Arrays.stream(clazz.getEnumConstants()),
        v -> v.toUniqueStableString()));
  }

  public E getEnumValue(String uniqueStableString) {
    return rawMap.getOrThrow(uniqueStableString);
  }

}
