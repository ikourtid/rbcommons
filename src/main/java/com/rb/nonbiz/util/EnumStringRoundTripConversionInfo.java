package com.rb.nonbiz.util;

import com.rb.nonbiz.collections.RBMap;

import java.util.Arrays;

import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;

public class EnumStringRoundTripConversionInfo<E extends Enum<E> & HasUniqueStableStringRepresentation> {

  private final RBMap<String, E> rawMap;

  private EnumStringRoundTripConversionInfo(RBMap<String, E> rawMap) {
    this.rawMap = rawMap;
  }

  public static <E extends Enum<E> & HasUniqueStableStringRepresentation>
  EnumStringRoundTripConversionInfo<E> enumStringRoundTripConversionInfo(Class<E> clazz) {
    return new EnumStringRoundTripConversionInfo<>(rbMapFromStream(
        Arrays.stream(clazz.getEnumConstants()),
        v -> v.toUniqueStableString()));
  }

  public E getEnumValue(String uniqueStableString) {
    return rawMap.getOrThrow(uniqueStableString);
  }

}
