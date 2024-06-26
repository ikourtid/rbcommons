package com.rb.nonbiz.io;


import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A typesafe wrapper around a string, representing a filename.
 *
 * <p> It exists to make filenames more typesafe as function arguments. </p>
 *
 * <p> This class is very much not intended to do on-disk validation of files, or run checks that
 * files exist, are open, are or aren't directories, etc. </p>
 */
public class Filename {

  private final String rawString;

  private Filename(String rawString) {
    this.rawString = rawString;
  }

  public static Filename filename(String rawString){
    RBPreconditions.checkArgument(!rawString.isEmpty(), "Filenames cannot be an empty string");
    return new Filename(rawString);
  }

  public String getRawString() {
    return rawString;
  }

  @Override
  public String toString() {
    return Strings.format("[F %s F]", rawString);
  }
  
}
