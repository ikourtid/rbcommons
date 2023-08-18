package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static org.junit.Assert.fail;

public class FileByDateStringFormatJsonApiConverterTest
    extends RBCommonsIntegrationTest<FileByDateStringFormatJsonApiConverter> {

  @Test
  public void testRoundTripConversions() {
    fail("");
  }

  @Override
  protected Class<FileByDateStringFormatJsonApiConverter> getClassBeingTested() {
    return FileByDateStringFormatJsonApiConverter.class;
  }

}
