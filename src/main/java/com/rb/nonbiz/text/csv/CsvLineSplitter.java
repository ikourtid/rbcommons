package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.text.Strings;

/**
 * Splits a single line of text into comma-separated components, while allowing
 * the possibility of commas being escaped inside doublequotes.
 * That is, we don't just return line.split(",").
 * I'm not sure if this implements some super-general CSV standard, but at least it takes
 * escaped commas into account.
 * For example, it does not handle the case where there is an escaped doublequote WITHIN a doublequoted field,
 * but it will suffice for now.
 */
public class CsvLineSplitter {

  public String[] splitLineIntoComponents(String line, int numFields) {
    String[] components = new String[numFields];
    int start = 0;
    int fieldNum = 0;
    for (; ; fieldNum++) {
      if (line.length() == start) {
        // last field is empty
        components[fieldNum] = "";
        return components;
      }
      boolean inQuotes = line.charAt(start) == '"';
      int end;
      // This does not handle the case where there is an escaped doublequote WITHIN a doublequoted field,
      // but it will suffice for now
      final int nextSeparatorIndex = inQuotes
          ? line.indexOf('"', start + 1) + 1
          : line.indexOf(',', start);
      boolean isLast = fieldNum == (numFields - 1);
      if (isLast) {
        if (nextSeparatorIndex >= 0) {
          throw new IllegalArgumentException(smartFormat("Too many fields (over %s ) in line %s",
              numFields, line));
        }
        end = line.length();
      } else {
        if (nextSeparatorIndex < 0) {
          throw new IllegalArgumentException(smartFormat("Too few fields ( %s instead of %s ) in line %s",
              fieldNum, numFields, line));
        }
        end = nextSeparatorIndex;
      }
      components[fieldNum] = inQuotes
          ? line.substring(start + 1, end - 1)
          : line.substring(start, end);
      start = end + 1;
      if (isLast) {
        return components;
      }
    }
  }

}
