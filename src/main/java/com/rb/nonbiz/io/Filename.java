package com.rb.nonbiz.io;


import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;

/*
This holds a filename.  It's intentionally light-weight and doesn't do a lot.
The main reason it exists is to make filenames more typesafe as function arguments.
This class is very much not intended to do on-disk validation of files, checking that
files exist, are open, are or aren't directories, etc.
 */
public class Filename {

  private final String rawFilenameString;
  private static final String ILLEGAL_CHARACTERS = "\n\r\t\0\f`?*<>|\":";

  private Filename(String filename) {
    this.rawFilenameString  = filename;
  }

  public static Filename filename(String rawString){
    if (rawString.length() == 0) {
      throw new IllegalArgumentException("Filename cannot be an empty stirng");
    }
    if (StringUtils.containsAny(rawString, ILLEGAL_CHARACTERS)) {
      throw new IllegalArgumentException("Filename cannot contain illegal characters");
    }
    return new Filename(rawString);
  }

  public String getFilename() {
    return rawFilenameString;
  }
  
}
