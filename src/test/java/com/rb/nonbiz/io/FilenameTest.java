package com.rb.nonbiz.io;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.junit.Test;

import static com.rb.nonbiz.io.Filename.filename;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotSame;

public class FilenameTest extends RBTestMatcher<Filename> {

  @Test
  public void testGetFilename() {
    Filename myFilename = filename("out.txt");
    assertEquals("out.txt", myFilename.getRawString());
    assertNotSame("someotherstring", myFilename.getRawString());
  }

  @Test
  public void testFilenameValidation(){
    assertIllegalArgumentException( () -> filename(""));

    // This is a good file...it shouldn't throw, and sizes should match
    assertEquals("output.txt", filename("output.txt").getRawString());
  }

  // I really don't have a strong opinion on what makes a filename trivial, so this is a short filename
  @Override
  public Filename makeTrivialObject() {
    return filename("a");
  }

  @Override
  public Filename makeNontrivialObject() {
    return filename("Longer_Filename.txt");
  }

  @Override
  public Filename makeMatchingNontrivialObject() {
    return filename("Longer_Filename.txt");
  }

  @Override
  protected boolean willMatch(Filename expected, Filename actual) {
    return expected.getRawString() == actual.getRawString();
  }

}