package com.rb.biz.jsonapi;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

/**
 * This is an enum {@code <-->} serializable string bidirectional map.
 *
 * This is uses for cases where we want to serialize a string that's "prettier" / cleaner / shorter
 * than the native Java enum string. For example, if we have {@code enum OrderSide} with BUY_SIDE and SELL_SIDE,
 * this will allow us to convert to "b" and "s", or "buy" and "sell" (lowercase).
 * 
 * This is useful because the JSON API is somewhat human-readable, so it's good to keep it looking good.
 */
public interface JsonSerializedEnumStringMap<E extends Enum<E>> {

  Class<E> getEnumClass();
  
  Optional<E> getOptionalEnumValue(String enumValueAsString);

  default E getEnumValueOrThrow(String enumValueAsString) {
    return getOrThrow(
        getOptionalEnumValue(enumValueAsString),
        "String %s does not represent a valid value for enum %s",
        enumValueAsString, getEnumClass());
  }

  // This interface isn't supposed to know about JsonSerializedEnumStringMapImpl, but FWIW, that class checks that
  // enumValue will always have a string value mapped to it. Therefore, there are no optionals here.
  String getSerializationStringOrThrow(E enumValue);

}
