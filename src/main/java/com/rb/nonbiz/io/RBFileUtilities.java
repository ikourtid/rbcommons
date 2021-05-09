package com.rb.nonbiz.io;

import com.rb.nonbiz.collections.RBSet;
import org.apache.commons.lang3.CharUtils;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;

public class RBFileUtilities {

  private static final RBSet<Character> INVALID_CHARACTERS_IN_PATH = rbSetOf('?', '*', '\\');

  public static boolean isValidFilePath(String path) {
    if (path.isEmpty()) {
      return false;
    }
    return path.chars().allMatch(v ->
        v < 256 // Not sure if needed, but maybe the (char) cast below will take v modulo 256 and give false negatives
            && CharUtils.isAsciiPrintable( (char) v)
            && !INVALID_CHARACTERS_IN_PATH.contains((char) v));
  }

}
