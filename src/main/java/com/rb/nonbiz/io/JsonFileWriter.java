package com.rb.nonbiz.io;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JsonFileWriter {

  @Inject DirectoryCreator directoryCreator;

  public void writePrettyJsonToFile(String outputFile, JsonObject jsonObject) {
    try {
      directoryCreator.makeAllDirs(outputFile);
      BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
      new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject, writer);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
