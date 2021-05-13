package com.rb.nonbiz.io;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBCommonsTestPlusIntegration;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;

public class StringFromFileReaderTest extends RBCommonsTestPlusIntegration<StringFromFileReader> {

  FileToBufferedReader fileToBufferedReader =
      mockery.mock(FileToBufferedReader.class);

  @Test
  public void fileExists() {
    mockery.checking(new Expectations() {{
      oneOf(fileToBufferedReader).getBufferedReader(
          with(equal(filenameOfRealFile())));
      will(returnValue(Optional.of(new BufferedReader(new StringReader("first line\nsecond line\n")))));
    }});

    rbSetOf(makeRealObject(), makeTestObject())
        .forEach(reader -> {
          assertOptionalEquals(
              "first line\nsecond line\n",
              reader.readFromFile(filenameOfRealFile()));
        });
  }

  @Test
  public void integrationTest_fileDoesNotExist() {
    mockery.checking(new Expectations() {{
      oneOf(fileToBufferedReader).getBufferedReader(
          with(equal("bad/file/name")));
      will(returnValue(Optional.empty()));
    }});

    rbSetOf(makeRealObject(), makeTestObject())
        .forEach(reader -> assertOptionalEmpty(reader.readFromFile("bad/file/name")));
  }

  private String filenameOfRealFile() {
    return RBCommonsIntegrationTest.makeRealObject(RBCommonsConstants.class)
        .getFullFilename("ssd/rbcommons/src/test/java/com/rb/nonbiz/io/test.txt");
  }

  @Override
  protected Class<StringFromFileReader> getClassBeingTested() {
    return StringFromFileReader.class;
  }

  @Override
  protected StringFromFileReader makeTestObject() {
    StringFromFileReader testObject = new StringFromFileReader();
    testObject.fileToBufferedReader = fileToBufferedReader;
    return testObject;
  }

}
