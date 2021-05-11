package com.rb.nonbiz.io;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DirectoryExistsCheckerTest extends RBTest<DirectoryExistsChecker> {

  @Test
  public void testHomeDir_exists() {
    String homeDirName = RBCommonsIntegrationTest.makeRealObject(RBCommonsConstants.class)
        .getFullFilename(".");
    assertTrue(makeTestObject().directoryExists(homeDirName));
  }

  @Test
  public void testMissingDir_fails() {
    assertFalse(makeTestObject().directoryExists("a/missing/directory!"));
  }

  @Override
  protected DirectoryExistsChecker makeTestObject() {
    return new DirectoryExistsChecker();
  }

}
