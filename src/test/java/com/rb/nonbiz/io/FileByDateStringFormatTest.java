package com.rb.nonbiz.io;

import com.rb.nonbiz.collections.RBVoid;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.io.FileByDateStringFormat.fileByDateStringFormat;
import static com.rb.nonbiz.io.FileByDateStringFormat.fixedFilenameIgnoringDate;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBEitherMatchers.eitherMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

// This test class is not generic, but the publicly exposed static matcher is.
// Note that the class inside the generic is irrelevant for logic purposes; it's really there just for type safety.
// So we'll just use RBVoid here, without loss of generality. It doesn't affect the logic.
public class FileByDateStringFormatTest extends RBTestMatcher<FileByDateStringFormat<RBVoid>> {

  @Test
  public void hasDateFormatAndIsNotFixedFileName_mustHaveExactlyOnePercentS() {
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
    assertEquals(
        fixedFilenameIgnoringDate("a/b/x").getFilePathForDate(LocalDate.of(2010, 12, 31)),
        "a/b/x");
  }

  @Override
  public FileByDateStringFormat<RBVoid> makeTrivialObject() {
    return fixedFilenameIgnoringDate("x");
  }

  @Override
  public FileByDateStringFormat<RBVoid> makeNontrivialObject() {
    return fileByDateStringFormat("a/b/x.%s.protobuf");
  }

  @Override
  public FileByDateStringFormat<RBVoid> makeMatchingNontrivialObject() {
    return fileByDateStringFormat("a/b/x.%s.protobuf");
  }

  @Override
  protected boolean willMatch(FileByDateStringFormat<RBVoid> expected,
                              FileByDateStringFormat<RBVoid> actual) {
    return fileByDateStringFormatMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<FileByDateStringFormat<T>> fileByDateStringFormatMatcher(
      FileByDateStringFormat<T> expected) {
    return makeMatcher(expected,
        match(v -> v.getRawFormatOrFixedFilename(), f -> eitherMatcher(f,
            f2 -> typeSafeEqualTo(f2),
            f3 -> typeSafeEqualTo(f3))));
  }

}
