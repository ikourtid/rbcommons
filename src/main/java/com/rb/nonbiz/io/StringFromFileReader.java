package com.rb.nonbiz.io;

import com.google.inject.Inject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

/**
 * Reads the contents of an entire file into a single string.
 *
 * <p> Don't use this for files of any serious size! In most cases, you want to iterate one line at a time. </p>
 *
 * <p> This abstracts away the reading of a file, which is a low-level operation that's hard to mock in tests otherwise. </p>
 */
public class StringFromFileReader {

  @Inject FileToBufferedReader fileToBufferedReader;

  public Optional<String> readFromFile(String filename) {
    try {
      // We will not use transformOptional because it becomes unwieldy in the presence of checked exceptions
      Optional<BufferedReader> bufferedReader = fileToBufferedReader.getBufferedReader(filename);
      if (!bufferedReader.isPresent()) {
        return Optional.empty(); // file not found
      }
      BufferedReader br = bufferedReader.get();
      StringBuilder sb = new StringBuilder();
      while(true) {
        String line = br.readLine();
        if (line == null) {
          break;
        }
        sb.append(line);
        sb.append('\n');
      }
      br.close();
      return Optional.of(sb.toString());
    } catch (IOException e) {
      // This IOException would get thrown by readLine, not due to a missing file, so we have to propagate it.
      throw new RuntimeException(e);
    }
  }

  public String readFromFileOrThrow(String filename) {
    return getOrThrow(
        readFromFile(filename),
        "Could not read file: %s",
        filename);
  }

}
