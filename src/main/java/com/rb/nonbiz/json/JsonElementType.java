package com.rb.nonbiz.json;

import com.rb.nonbiz.text.Strings;

/**
 * Different types that a JsonElement could be.
 * Note that JSON only supports a single number, not double/long/integer. So all those get lumped under JSON_NUMBER.
 */
public enum JsonElementType {

  JSON_ARRAY,
  JSON_BOOLEAN,
  JSON_NULL,
  JSON_NUMBER,
  JSON_STRING,
  JSON_OBJECT;

  public interface Visitor<T> {

    T visitJsonArray();
    T visitJsonBoolean();
    T visitJsonNull();
    T visitJsonNumber();
    T visitJsonString();
    T visitJsonObject();

  }

  public <T> T visit(Visitor<T> visitor) {
    switch (this) {
      case JSON_ARRAY:   return visitor.visitJsonArray();
      case JSON_BOOLEAN: return visitor.visitJsonBoolean();
      case JSON_NULL:    return visitor.visitJsonNull();
      case JSON_NUMBER:  return visitor.visitJsonNumber();
      case JSON_STRING:  return visitor.visitJsonString();
      case JSON_OBJECT:  return visitor.visitJsonObject();
      default:
        throw new IllegalArgumentException(Strings.format("Unsupported type %s", this));
    }
  }

}
