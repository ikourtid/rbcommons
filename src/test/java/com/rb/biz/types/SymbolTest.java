package com.rb.biz.types;

import org.junit.Test;

import static com.rb.biz.types.Symbol.isValidSymbol;
import static com.rb.biz.types.Symbol.symbol;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SymbolTest {

  @Test
  public void implementsEquals() {
    assertEquals(symbol("abc"), symbol("abc"));
  }

  @Test
  public void checkValidity() {
    assertFalse("9 chars is too long", isValidSymbol("ABCDEFGHI"));
    assertTrue("8 chars is acceptable", isValidSymbol("ABCDEFGH"));
    assertFalse("no spaces allowed", isValidSymbol("BRK A"));
    assertFalse("no parentheses allowed", isValidSymbol("BRK(A)"));
    assertTrue("numbers are valid", isValidSymbol("ABC123"));
    assertFalse("Empty string is invalid", isValidSymbol(""));
  }

}
