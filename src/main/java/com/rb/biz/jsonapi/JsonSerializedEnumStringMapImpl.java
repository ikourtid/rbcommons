package com.rb.biz.jsonapi;

import com.google.common.collect.BiMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import com.rb.nonbiz.util.RBSimilarityPreconditions;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.newRBSet;

/**
 * A simple implementation of {@link JsonSerializedEnumStringMap}; see that class for more info.
 */
public class JsonSerializedEnumStringMapImpl<E extends Enum<E>> implements JsonSerializedEnumStringMap<E> {

  private final Class<E> enumClass;
  private final BiMap<E, String> rawBiMap;

  private JsonSerializedEnumStringMapImpl(Class<E> enumClass, BiMap<E, String> rawBiMap) {
    this.enumClass = enumClass;
    this.rawBiMap = rawBiMap;
  }

  public static <E extends Enum<E>> JsonSerializedEnumStringMap<E> jsonSerializedEnumStringMapImpl(
      Class<E> enumClass, BiMap<E, String> rawBiMap) {
    //noinspection rawtypes
    Enum[] enumConstants = enumClass.getEnumConstants();
    int sharedSize = RBSimilarityPreconditions.checkBothSame(
        enumConstants.length,
        rawBiMap.size(),
        "We must have the same # of enum constants for enum %s as in the bimap passed in: %s %s",
        enumClass, enumConstants, rawBiMap);
    RBPreconditions.checkArgument(
        sharedSize > 0,
        "Trying to serialize enum %s with 0 values; you probably don't mean to do this",
        enumClass);
    RBSimilarityPreconditions.checkBothSame(
        newRBSet(enumConstants),
        newRBSet(rawBiMap.keySet()),
        "For enum %s, we must have mappings for all constants",
        enumClass);
    return new JsonSerializedEnumStringMapImpl<>(enumClass, rawBiMap);
  }

  @Override
  public Class<E> getEnumClass() {
    return enumClass;
  }

  @Override
  public Optional<E> getOptionalEnumValue(String enumValueAsString) {
    return Optional.ofNullable(rawBiMap.inverse().get(enumValueAsString));
  }

  @Override
  public String getSerializationStringOrThrow(E enumValue) {
    return RBPreconditions.checkNotNull(
        rawBiMap.get(enumValue),
        "Enum value %s somehow did not have a string mapped to it!",
        enumValue);
  }

  @Override
  public String toString() {
    return Strings.format("[JSESMI %s %s JSESMI]", enumClass, rawBiMap);
  }

}
