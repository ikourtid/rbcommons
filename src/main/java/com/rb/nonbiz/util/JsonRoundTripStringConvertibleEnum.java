package com.rb.nonbiz.util;

import com.google.gson.JsonPrimitive;

import static com.rb.nonbiz.json.RBGson.jsonString;

/**
 * A generic interface that lets us mark enums as being convertible back and forth to strings.
 *
 * <p> This is meant to avoid situations where we use the Java identifier as the string representation, because if it
 * ever gets renamed, we don't want the string representation to change, as that will be part of an (ideally) stable
 * API. In particular, the JSON API converters should always use this approach instead of using the Java identifier. </p>
 */
public interface JsonRoundTripStringConvertibleEnum<E extends Enum<E>> {

  String toUniqueStableString();

  default JsonPrimitive toUniqueStableJsonString() {
    return jsonString(toUniqueStableString());
  }

}
