package com.rb.nonbiz.io;


import org.apache.commons.lang3.StringUtils;
import com.rb.nonbiz.text.Strings;

/**
 * This class wraps a string file name
 *
 * <p> It exists to make filenames more typesafe as function arguments. </p>
 *
 * <p> This class is very much not intended to do on-disk validation of files, checks that
 * files exist, are open, are or aren't directories, etc. </p>
 */
public class Filename {

  private final String rawFilenameString;
  private static final String ILLEGAL_CHARACTERS = "\n\r\t\0\f`?*<>|\":";

  private Filename(String filename) {
    this.rawFilenameString  = filename;
  }

  public static Filename filename(String rawString){
    if (rawString.length() == 0) {
      throw new IllegalArgumentException("Filename cannot be an empty string");
    }
    if (StringUtils.containsAny(rawString, ILLEGAL_CHARACTERS)) {
      throw new IllegalArgumentException("Filename cannot contain illegal characters: " + rawString);
    }
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
