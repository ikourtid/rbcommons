package com.rb.biz.types;

public class StringFunctions {

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

}
