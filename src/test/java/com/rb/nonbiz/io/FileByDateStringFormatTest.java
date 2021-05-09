package com.rb.nonbiz.io;

import com.rb.biz.investing.modeling.selection.InstrumentSelectionStatus;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.io.FileByDateStringFormat.fileByDateStringFormat;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is.
public class FileByDateStringFormatTest extends RBTestMatcher<FileByDateStringFormat<InstrumentSelectionStatus>> {

  @Test
  public void mustHaveExactlyOnePercentS() {
    assertIllegalArgumentException( () -> fileByDateStringFormat(""));
    assertIllegalArgumentException( () -> fileByDateStringFormat("abc"));
    FileByDateStringFormat<Object> doesNotThrow = fileByDateStringFormat("%s");
    assertIllegalArgumentException( () -> fileByDateStringFormat("%s%s"));
    assertIllegalArgumentException( () -> fileByDateStringFormat("%s/%s"));
    assertIllegalArgumentException( () -> fileByDateStringFormat("%s.%s"));
    assertIllegalArgumentException( () -> fileByDateStringFormat("abc/%s/123/%s"));
    assertIllegalArgumentException( () -> fileByDateStringFormat("%s%s%s"));
  }

  @Test
  public void testSimpleGetFileByDate() {
    assertEquals(
        fileByDateStringFormat("a/b/x.%s.protobuf").getFileForDate(LocalDate.of(2010, 12, 31)).getPath(),
        "a/b/x.2010-12-31.protobuf");
    assertEquals(
        fileByDateStringFormat("a/b/x.%s.protobuf").getFilePathForDate(LocalDate.of(2010, 12, 31)),
        "a/b/x.2010-12-31.protobuf");
  }

  @Override
  public FileByDateStringFormat<InstrumentSelectionStatus> makeTrivialObject() {
    return fileByDateStringFormat("%s");
  }

  @Override
  public FileByDateStringFormat<InstrumentSelectionStatus> makeNontrivialObject() {
    return fileByDateStringFormat("a/b/x.%s.protobuf");
  }

  @Override
  public FileByDateStringFormat<InstrumentSelectionStatus> makeMatchingNontrivialObject() {
    return fileByDateStringFormat("a/b/x.%s.protobuf");
  }

  @Override
  protected boolean willMatch(FileByDateStringFormat<InstrumentSelectionStatus> expected,
                              FileByDateStringFormat<InstrumentSelectionStatus> actual) {
    return fileByDateStringFormatMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<FileByDateStringFormat<T>> fileByDateStringFormatMatcher(
      FileByDateStringFormat<T> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getRawFormat()));
  }

}
