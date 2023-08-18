package com.rb.nonbiz.io;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.Either;
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

  public interface Visitor<T2> {

    T2 visitFileFormatParameterizedByDate(String fileFormatParametrizedByDate);
    T2 visitFixedFilenameIgnoringDate(String fixedFilenameIgnoringDate);

  }


  private final Either<String, String> rawFormatOrFixedFilename;

  private FileByDateStringFormat(Either<String, String> rawFormatOrFixedFilename) {
    this.rawFormatOrFixedFilename = rawFormatOrFixedFilename;
  }

  public static <T> FileByDateStringFormat<T> fileByDateStringFormat(String fileFormatParametrizedByDate) {
    int count = StringUtils.countMatches(fileFormatParametrizedByDate, "%s");
    RBPreconditions.checkArgument(
        count == 1,
        "fileFormatParametrizedByDate was <%s> but there must be exactly one percent-s in it; it had %s",
        fileFormatParametrizedByDate, count);
    return new FileByDateStringFormat<>(Either.left(fileFormatParametrizedByDate));
  }

  public static <T> FileByDateStringFormat<T> fixedFilenameIgnoringDate(String fixedFilename) {
    return new FileByDateStringFormat<>(Either.right(fixedFilename));
  }

  // Don't use this; this is here to test the matcher
  @VisibleForTesting
  Either<String, String> getRawEither() {
    return rawFormatOrFixedFilename;
  }

  public <T2> T2 visit(Visitor<T2> visitor) {
    return rawFormatOrFixedFilename.visit(new Either.Visitor<String, String, T2>() {
      @Override
      public T2 visitLeft(String fileFormatParametrizedByDate) {
        return visitor.visitFileFormatParameterizedByDate(fileFormatParametrizedByDate);
      }

      @Override
      public T2 visitRight(String fixedFilenameIgnoringDate) {
        return visitor.visitFixedFilenameIgnoringDate(fixedFilenameIgnoringDate);
      }
    });
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
    return visit(new Visitor<String>() {
      @Override
      public String visitFileFormatParameterizedByDate(String fileFormatParametrizedByDate) {
        return Strings.format(fileFormatParametrizedByDate, yyyyMMdd(date));
      }

      @Override
      public String visitFixedFilenameIgnoringDate(String fixedFilenameIgnoringDate) {
        return fixedFilenameIgnoringDate; // In this case, the date doesn't affect the filename - intentionally so.
      }
    });
  }

  public boolean allDaysUseTheSameFile() {
    return visit(new Visitor<Boolean>() {
      @Override
      public Boolean visitFileFormatParameterizedByDate(String fileFormatParametrizedByDate) {
        return false;
      }

      @Override
      public Boolean visitFixedFilenameIgnoringDate(String fixedFilenameIgnoringDate) {
        return true;
      }
    });
  }

  @Override
  public String toString() {
    return Strings.format("[FBDSF %s FBDSF]",
        visit(new Visitor<String>() {
          @Override
          public String visitFileFormatParameterizedByDate(String fileFormatParametrizedByDate) {
            return Strings.format("format parameterized by date: %s", fileFormatParametrizedByDate);
          }

          @Override
          public String visitFixedFilenameIgnoringDate(String fixedFilenameIgnoringDate) {
            return Strings.format("fixed filename ignoring date: %s", fixedFilenameIgnoringDate);
          }
        }));
  }

}
