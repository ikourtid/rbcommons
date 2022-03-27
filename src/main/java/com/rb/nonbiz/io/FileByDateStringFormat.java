package com.rb.nonbiz.io;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.Either;
import com.rb.nonbiz.collections.Either.Visitor;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDate;

import static com.rb.nonbiz.date.RBDates.yyyyMMdd;

/**
 * This is the format (as in Strings.format) which, together with a date,
 * can give us a full file.
 *
 * To make things clearer, this is generic on the type of the class represented by the file being loaded.
 */
public class FileByDateStringFormat<T> {

  private final Either<String, String> rawFormatOrFixedFilename;

  private FileByDateStringFormat(Either<String, String> rawFormatOrFixedFilename) {
    this.rawFormatOrFixedFilename = rawFormatOrFixedFilename;
  }

  public static <T> FileByDateStringFormat<T> fileByDateStringFormat(String rawFormat) {
    int count = StringUtils.countMatches(rawFormat, "%s");
    RBPreconditions.checkArgument(
        count == 1,
        "Raw string format was <%s> but there must be exactly one percent-s in it; it had %s",
        rawFormat, count);
    return new FileByDateStringFormat<>(Either.left(rawFormat));
  }

  public static <T> FileByDateStringFormat<T> fixedFilenameIgnoringDate(String fixedFilename) {
    return new FileByDateStringFormat<>(Either.right(fixedFilename));
  }

  // Don't use this; this is here to test the matcher
  @VisibleForTesting
  Either<String, String> getRawEither() {
    return rawFormatOrFixedFilename;
  }

  // This is a bit unusual for a data class, but using the date and returning a file object takes care of some work
  // that the caller would always have to do anyway, given the semantics of this class.
  // This is better than exposing getRawFormat.
  public File getFileForDate(LocalDate date) {
    return new File(getFilePathForDate(date));
  }

  /**
   * This could be a relative path ( a/b/c.txt ) or an absolute one ( /home/iraklis/a/b/c.txt ),
   * depending on how we initialized rawFormat in this data class.
   * We currently (Dec 2018) use this for absolute paths, but that doesn't always have to be the case.
   */
  public String getFilePathForDate(LocalDate date) {
    return rawFormatOrFixedFilename.visit(new Visitor<String, String, String>() {
      @Override
      public String visitLeft(String rawFormat) {
        return Strings.format(rawFormat, yyyyMMdd(date));
      }

      @Override
      public String visitRight(String fixedFilename) {
        return fixedFilename; // In this case, the date doesn't affect the filename - intentionally so.
      }
    });
  }

  public boolean allDaysUseTheSameFile() {
    return rawFormatOrFixedFilename.visit(new Visitor<String, String, Boolean>() {
      @Override
      public Boolean visitLeft(String rawFormat) {
        return false;
      }

      @Override
      public Boolean visitRight(String fixedFilename) {
        return true;
      }
    });
  }

  @Override
  public String toString() {
    return Strings.format("[FBDSF %s FBDSF]",
        rawFormatOrFixedFilename.visit(new Visitor<String, String, String>() {
          @Override
          public String visitLeft(String rawFormat) {
            return Strings.format("raw format: %s", rawFormat);
          }

          @Override
          public String visitRight(String fixedFilename) {
            return Strings.format("fixed filename: %s", fixedFilename);
          }
        }));
  }

}
