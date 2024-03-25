package com.rb.nonbiz.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Methods to read an entire JSON file into memory as either a {@link JsonArray} or a {@link JsonObject}; the
 * user must know what type to expect.
 *
 * <p> This is not intended to be the most efficient implementation, because the contents of the entire file
 * must be passed in as a single String. </p>
 */
public class JsonFileReader {

  public JsonObject readJsonFile(String inputFile) {
    try {
      return JsonParser.parseReader(new JsonReader(new BufferedReader(new FileReader(inputFile))))
          .getAsJsonObject();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonArray readJsonFileAsArray(String inputFile) {
    try {
      return JsonParser.parseReader(new JsonReader(new BufferedReader(new FileReader(inputFile))))
          .getAsJsonArray();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
