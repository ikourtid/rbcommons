package com.rb.nonbiz.io;

import com.rb.biz.investing.modeling.RBCommonsConstants;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;

import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FileToBufferedReaderTest extends RBTest<FileToBufferedReader> {

  @Test
  public void testWhenFileExists() throws IOException {
    Optional<BufferedReader> bufferedReader = makeTestObject().getBufferedReader(
        makeRealObject(RBCommonsConstants.class).getFullFilename("ssd/rbcommons/src/test/java/com/rb/nonbiz/io/test.txt"));
    assertTrue(bufferedReader.isPresent());
    assertEquals("first line", bufferedReader.get().readLine());
    assertEquals("second line", bufferedReader.get().readLine());
    assertNull(bufferedReader.get().readLine());
    bufferedReader.get().close();
  }

  @Test
  public void testWhenFileDoesNotExist() {
    assertOptionalEmpty(makeTestObject().getBufferedReader("bad/file/name"));
  }

  @Override
  protected FileToBufferedReader makeTestObject() {
    return new FileToBufferedReader();
  }

}
