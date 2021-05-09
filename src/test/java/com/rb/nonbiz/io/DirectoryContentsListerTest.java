package com.rb.nonbiz.io;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

public class DirectoryContentsListerTest extends RBTest<DirectoryContentsLister> {

  @Test
  public void testHomeDir() {
    // Test reading the contents of the HOME directory.
    // We can't compare to an expected output since we don't know what's there.
    makeTestObject().getSortedFilenamesInDirectory(
        RBIntegrationTest.makeRealObject(RBCommonsConstants.class)
        .getFullFilename("."));
  }

  @Override
  protected DirectoryContentsLister makeTestObject() {
    return new DirectoryContentsLister();
  }

}
