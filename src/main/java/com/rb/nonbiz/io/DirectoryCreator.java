package com.rb.nonbiz.io;

import com.rb.nonbiz.util.RBPreconditions;

import java.io.File;

/**
 * Creates a directory in the file system.
 *
 * <p> This is fairly simple, but having a separate class allows us to mock this in tests. </p>
 */
public class DirectoryCreator {

  /**
   * Creates (if needed) all intermediate directories for a file to exist.
   *
   * E.g. if filename is a/b/c.txt, it will create directories a and a/b.
   */
  synchronized public void makeAllDirs(String filename) {
    String dirname = filename.substring(0, filename.lastIndexOf('/'));
    File dirObject = new File(dirname);
    if (dirObject.exists()) {
      RBPreconditions.checkArgument(dirObject.isDirectory());
    } else {
      boolean ok = dirObject.mkdirs();
      RBPreconditions.checkArgument(
          ok,
          "Could not make parent directories %s for %s",
          dirObject.getAbsolutePath(), filename);
    }
  }

}
