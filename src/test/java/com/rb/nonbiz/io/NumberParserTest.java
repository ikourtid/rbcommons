package com.rb.nonbiz.io;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberParserTest {

  @Test
  public void parseDouble_simpleFewCases() {
    for (String stringRepresentation : ImmutableList.of(
        "-10", "-3.33333333333333333333333333333333333", "-1.5", "-1.0",
        "0", "1.0", "1.5", "3.3333333333333333333333333333", "10")) {
      assertEquals(Double.parseDouble(stringRepresentation), NumberParser.getDouble(stringRepresentation), 1e-14);
    }
  }

}