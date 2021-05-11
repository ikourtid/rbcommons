package com.rb.nonbiz.io;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.List;

import static com.rb.nonbiz.io.RBFileUtilities.isValidFilePath;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBFileUtilitiesTest {

  @Test
  public void test_isValidFilePath() {
    List<String> invalidPaths = ImmutableList.of(
        "", "\\", "a\\", "\\b", "a\\b",
        "?", "a?", "?b", "a?b",
        "*", "a*", "*a", "a*b",
        "a\tb", "Ηρακλής/Κουρτίδης");
    List<String> validPaths = ImmutableList.of("a", "a/b", "a:b", "a b");

    invalidPaths.forEach(invalidPath -> assertFalse(isValidFilePath(invalidPath)));
    validPaths  .forEach(  validPath -> assertTrue( isValidFilePath(  validPath)));
  }

}
