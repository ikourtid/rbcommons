package com.rb.nonbiz.jsonapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.nonbiz.json.RBJsonArrays.jsonArrayToStream;
import static com.rb.nonbiz.json.RBJsonArrays.streamToJsonArray;

// FIXME IAK GS comment and test; reuse the streamToJsonArray tests
public class JsonArrayToFromListConverter {

  public <T> JsonArray toJsonArray(List<T> list, Function<T, JsonElement> itemSerializer) {
    return streamToJsonArray(list.size(), list.stream(), itemSerializer);
  }

  public <T> List<T> fromJsonArray(JsonArray jsonArray, Function<JsonElement, T> itemDeserializer) {
    return jsonArrayToStream(jsonArray, itemDeserializer).collect(Collectors.toList());
  }

}
