package com.rb.nonbiz.io;

import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.util.RBPreconditions;
import org.junit.Test;

import static com.rb.nonbiz.io.Filename.filename;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotSame;

public class FilenameTest extends RBTestMatcher<Filename> {

  @Test
  public void testGetFilename() {
    Filename myFilename = filename("out.txt");
    assertEquals("out.txt", myFilename.getFilename());
    assertNotSame("someotherstring", myFilename.getFilename());
  }

  @Test
  public void testFilenameValidation(){
    Runnable emptyFilename = () -> filename("");
    Runnable badFilename = () -> filename("\\!@#$%^&*()");
    Runnable goodFilename = () -> filename("output.txt");

    RBPreconditions.checkThrowsThisException(emptyFilename, IllegalArgumentException.class, "");
    RBPreconditions.checkThrowsThisException(badFilename, IllegalArgumentException.class, "");
    RBPreconditions.checkDoesNotThrow(goodFilename);
  }

  // I really don't have a strong opinion on what makes a filename trivial, so this is a short filename
  @Override
  public Filename makeTrivialObject() {
    return filename("a.txt");
  }

  @Override
  public Filename makeNontrivialObject() {
    return filename("longer_filename.txt");
  }

  @Override
  public Filename makeMatchingNontrivialObject() {
    return makeNontrivialObject();
  }

  @Override
  protected boolean willMatch(Filename expected, Filename actual) {
    return expected.getFilename() == actual.getFilename();
  }

}