package com.rb.nonbiz.io;


import com.rb.nonbiz.text.Strings;

/*
This holds a filename.  It's intentionally light-weight and doesn't do a lot.
The main reason it exists is to make filenames more typesafe as function arguments.
This class is very much not intended to do on-disk validation of files, checking that
files exist, are open, are or aren't directories, etc.
 */
public class Filename {

  private final String rawFilenameString;

  private Filename(String filename) {
    this.rawFilenameString  = filename;
  }

  public static Filename filename(String rawString){
    return new Filename(rawString);
  }

  public String getFilename() {
    return rawFilenameString;
  }

  @Override
  public String toString() {
    return Strings.format("[F %s F]", rawFilenameString);
  }
  
}
