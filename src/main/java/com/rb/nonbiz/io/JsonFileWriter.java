package com.rb.nonbiz.io;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Methods to write an entire JSON object to a file.
 *
 * <p> This will create any intermediate directories (as in 'mkdir -p') if needed. </p>
 */
public class JsonFileWriter {

  @Inject DirectoryCreator directoryCreator;

  public void writePrettyJsonToFile(String outputFile, JsonObject jsonObject) {
    try {
      directoryCreator.makeAllDirs(outputFile);
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
      new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(jsonObject, writer);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
