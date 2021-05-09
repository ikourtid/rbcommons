package com.rb.nonbiz.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

/**
 * This class does not have unit tests; I don't know how to mock the file stuff.
 * But at least I isolated file I/O to this small class, so that we can mock the I/O
 * in other tests.
 */
public class FileLinesIteratorGenerator {

  public Iterator<String> getFileLinesIterator(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return reader.lines().iterator();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
