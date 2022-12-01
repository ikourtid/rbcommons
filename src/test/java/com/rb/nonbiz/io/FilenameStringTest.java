package com.rb.nonbiz.io;

import junit.framework.TestCase;

public class FilenameStringTest extends TestCase {

  public void testGetFilename() {
    FilenameString myFilename = new FilenameString("out.txt");
    assertEquals("out.txt", myFilename.getFilename());
    assertNotSame("someotherstring", myFilename.getFilename());
  }
}