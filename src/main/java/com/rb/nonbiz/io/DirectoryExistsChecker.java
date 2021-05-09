package com.rb.nonbiz.io;

import java.io.File;

/**
 * Just tells us if a directory exists.
 *
 * This is super-simple, but having a separate class allows us to mock this operation elsewhere without
 * worrying about an actual directory existing.
 */
public class DirectoryExistsChecker {

  public boolean directoryExists(String filename) {
    File file = new File(filename);
    return file.exists() && file.isDirectory();
  }

}
