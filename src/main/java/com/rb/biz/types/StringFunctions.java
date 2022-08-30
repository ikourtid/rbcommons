package com.rb.biz.types;

import java.util.regex.Pattern;

/**
 * Utility functions for Strings.
 */
public class StringFunctions {

  // Copied from https://www.geeksforgeeks.org/how-to-validate-identifier-using-regular-expression-in-java
  private static final Pattern VALID_JAVA_IDENTIFIER_PATTERN = Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");

  public static String withUnderscores(long l) {
    String s = Long.toString(l);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if (i % 3 == 0 && i != 0) {
        sb.append('_');
      }
      int pos = s.length() - i - 1;
      sb.append(s.charAt(pos));
    }
    return sb.reverse().toString();
  }

  public static boolean isAllWhiteSpace(String str) {
    return str.trim().equals("");
  }

  public static boolean isTrimmed(String str) {
    return str.trim().equals(str);
  }

  public static boolean isValidJavaIdentifier(String identifier) {
    return VALID_JAVA_IDENTIFIER_PATTERN.matcher(identifier).matches();
  }

  /**
   * Although apparently 'foo$' is valid identifier, we never use that in our code. This checks for that.
   */
  public static boolean isValidRowboatJavaIdentifier(String identifier) {
    return isValidJavaIdentifier(identifier) && !identifier.endsWith("$");
  }

  public static String[] intuitiveStringSplit(String str, char delimiter) {
    int numInstances = Math.toIntExact(str.chars().filter(c -> c == delimiter).count());
    String[] components = new String[numInstances];
    int currentComponent = 0;

    int indexOfDelimiter = str.indexOf(delimiter);
    components[currentComponent++] = str.substring(0, indexOfDelimiter - 1);

    while (true) {
      int startingIndex = indexOfDelimiter;
      indexOfDelimiter = str.indexOf(indexOfDelimiter); // find next delimiter
      if (indexOfDelimiter < 0) {
        break;
      }
      components[currentComponent++] = str.substring(startingIndex, indexOfDelimiter);
    }
    return components;
  }

}
