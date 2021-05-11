package com.rb.nonbiz.io;

import com.google.inject.Inject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

import static java.util.Collections.singletonList;

public class StringToFileWriter {

  @Inject DirectoryCreator directoryCreator;

  /**
   * Creates file (and any intermediate directories, if applicable)
   * and writes 'contents' into it.
   */
  public void writeToFile(String filename, String contents) {
    writeToFile_helper(filename, singletonList(contents).iterator(), false);
  }

  public void writeToFile(String filename, Iterator<String> contents) {
    writeToFile_helper(filename, contents, false);
  }

  public void appendToFile(String filename, String contents) {
    writeToFile_helper(filename, singletonList(contents).iterator(), true);
  }

  public void appendToFile(String filename, Iterator<String> contents) {
    writeToFile_helper(filename, contents, true);
  }

  private void writeToFile_helper(String filename, Iterator<String> contents, boolean append) {
    try {
      directoryCreator.makeAllDirs(filename);
      File file = new File(filename);
      if (!file.exists()) {
        file.createNewFile();
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), append));
      while (contents.hasNext()) {
        bw.write(contents.next());
      }
      bw.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
