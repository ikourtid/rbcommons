package com.rb.nonbiz.io;

import java.io.File;

/**
 * Just tells us if a file exists.
 *
 * This is super-simple, but having a separate class allows us to mock this operation elsewhere without
 * worrying about an actual file existing.
 */
public class FileExistsChecker {

  public boolean fileExists(String filename) {
    File file = new File(filename);
    return file.exists() && file.isFile();
  }

}
