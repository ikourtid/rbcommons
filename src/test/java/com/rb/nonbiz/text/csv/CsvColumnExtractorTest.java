package com.rb.nonbiz.text.csv;

import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.text.csv.CsvColumnExtractor.csvColumnExtractorAllowingInvalidValues;
import static com.rb.nonbiz.text.csv.CsvColumnExtractor.csvColumnExtractorThatThrowsOnInvalidValues;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;
import static org.junit.Assert.assertTrue;

// This is not a good candidate for extending RBTestMatcher, because of the way we do matching.
// Matching data classes that have lambdas in them is, in the general case, impossible.
// In order to have *some* reasonable matching ability, the publicly exposed typesafe matchers here
// will let you specify a few sample conversions, so that we can somehow match the lambdas of two objects,
// albeit imperfectly. So the idea is that if two lambdas convert a string to the same value, or if they both throw
// when they are supposed to throw on bad data, then the two lambdas will be considered to match.
// Obviously, by only testing this on a finite set of cases, we can never be 100% sure that the two match /
// 'are the same'. But this is the best we can do.
public class CsvColumnExtractorTest {

  @Test
  public void test_csvColumnExtractorAllowingInvalidValues() {
    CsvColumnExtractor<UnitFraction> csvColumnExtractor =
        csvColumnExtractorAllowingInvalidValues("weight", cellContents -> {
          String[] s = cellContents.split(" ");
          if (s.length != 2) {
            return Optional.empty();
          }
          String percentage = s[0];
          try {
            return Optional.of(unitFractionInPct(Double.parseDouble(percentage)));
          } catch (IllegalArgumentException e) { // includes NumberFormatException
            return Optional.empty();
          }
        });

    assertTrue(
        csvColumnExtractorAllowingInvalidValuesMatcher(
            csvColumnExtractor, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8),
            // valid conversion samples
            rbMapOf(
                "100 %",   UNIT_FRACTION_1,
                "100.0 %", UNIT_FRACTION_1,
                "12.34 %", unitFraction(0.1234),
                "0.0 %",   UNIT_FRACTION_0,
                "0 %",     UNIT_FRACTION_0),
            // invalid conversions
            rbSetOf(
                "100.01 %",  // > 100%
                "-0.01 %",   // negative
                "99%",       // no space
                "",          // empty
                "foo",       // not a number
                "99 % foo")) // junk at end
            .matches(csvColumnExtractor));
  }

  @Test
  public void test_csvColumnExtractorThatThrowsOnInvalidValues() {
    CsvColumnExtractor<UnitFraction> csvColumnExtractor =
        csvColumnExtractorThatThrowsOnInvalidValues("weight", cellContents -> {
          String[] s = cellContents.split(" ");
          RBPreconditions.checkArgument(s.length == 2);
          return unitFractionInPct(Double.parseDouble(s[0]));
        });

    assertTrue(
        csvColumnExtractorThatThrowsOnInvalidValuesMatcher(
            csvColumnExtractor, f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8),
            // valid conversion samples
            rbMapOf(
                "100 %",   UNIT_FRACTION_1,
                "100.0 %", UNIT_FRACTION_1,
                "12.34 %", unitFraction(0.1234),
                "0.0 %",   UNIT_FRACTION_0,
                "0 %",     UNIT_FRACTION_0),
            // invalid conversions
            rbSetOf(
                "100.01 %",  // > 100%
                "-0.01 %",   // negative
                "99%",       // no space
                "",          // empty
                "foo",       // not a number
                "99 % foo")) // junk at end
            .matches(csvColumnExtractor));
  }

  public static <T> TypeSafeMatcher<CsvColumnExtractor<T>> csvColumnExtractorAllowingInvalidValuesMatcher(
      CsvColumnExtractor<T> expected,
      MatcherGenerator<T> matcherGenerator,
      RBMap<String, T> validConversionSamples,
      RBSet<String> invalidConversionSamples) {
    return makeMatcher(expected, actual -> {
      if (!expected.getColumnName().equals(actual.getColumnName())) {
        return false;
      }
      return validConversionSamples.entrySet()
          .stream()
          .allMatch(entry -> {
            String cellValue = entry.getKey();
            T expectedParsedValue = entry.getValue();
            return nonEmptyOptionalMatcher(matcherGenerator.apply(expectedParsedValue))
                .matches(expected.extractOptionalValue(cellValue));
          })
          && invalidConversionSamples
          .stream()
          .noneMatch(invalidCellContents -> expected.extractOptionalValue(invalidCellContents).isPresent());
    });
  }

  public static <T> TypeSafeMatcher<CsvColumnExtractor<T>> csvColumnExtractorThatThrowsOnInvalidValuesMatcher(
      CsvColumnExtractor<T> expected,
      MatcherGenerator<T> matcherGenerator,
      RBMap<String, T> validConversionSamples,
      RBSet<String> invalidConversionSamples) {
    return makeMatcher(expected, actual -> {
      if (!expected.getColumnName().equals(actual.getColumnName())) {
        return false;
      }
      return validConversionSamples.entrySet()
          .stream()
          .allMatch(entry -> {
            String cellValue = entry.getKey();
            T expectedParsedValue = entry.getValue();
            try {
              Optional<T> optionalExtractedValue = expected.extractOptionalValue(cellValue);
              return transformOptional(
                  optionalExtractedValue,
                  v -> matcherGenerator.apply(expectedParsedValue).matches(v))
                  .orElse(false); // empty optional means not a match, since this is for 'throws on invalid values'
            } catch (Exception e) {
              // If there was an exception in the conversion; we shouldn't just propagate it up; it means
              // that the two items don't match.
              return false;
            }
          })
          && invalidConversionSamples
          .stream()
          .allMatch(invalidConversionSample -> {
            try {
              T mustThrow = expected.extractValueOrThrow(invalidConversionSample);
              return false; // false = entire object did not match, since we got no exception
            } catch (IllegalArgumentException e) {
              return true;
            }
          });
    });
  }

}
