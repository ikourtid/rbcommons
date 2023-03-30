package com.rb.nonbiz.testutils;

import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.enumMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.util.RBPreconditions.checkUnique;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * For enums that implement {@link JsonRoundTripStringConvertibleEnum},
 * if we want to test the conversion back and forth to a 'unique stable string', the test class for that enum
 * should extend this. Just like with {@link RBTestMatcher}, some tests will be automatically created and run
 * to confirm that all enums are covered, and that the string representations are all unique and equal to whatever
 * values are returned by {@link #getEnumConstantsToRepresentations()}.
 *
 * <p> We use a lot of reflection to get this to work, because there are static methods involved here. </p>
 */
public abstract class RBJsonRoundTripStringConvertibleEnumTest<E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>> {

  protected abstract RBMap<E, String> getEnumConstantsToRepresentations();

  @Test
  public void testJsonRoundTripStringConvertibleEnum() {
    RBMap<E, String> enumConstantsToRepresentations = getEnumConstantsToRepresentations();
    try {
      Class<E> enumClass = getOrThrow(
          enumConstantsToRepresentations.keySet().stream().findFirst(),
          "Trying to test an enum with no values")
          .getDeclaringClass();

      //noinspection unchecked
      E[] allEnumConstants = (E[]) enumClass.getMethod("values").invoke(null); // null is right, because this is a static method.

      assertThat(
          "All enum constants must be in the map of expectations passed in",
          newRBSet(allEnumConstants),
          rbSetEqualsMatcher(newRBSet(enumConstantsToRepresentations.keySet())));

      checkUnique(
          Arrays.stream(allEnumConstants)
              .map(v -> v.toUniqueStableString()),
          "All enum constants must have a unique toUniqueStableString() result");

      // There is no way to enforce via an interface that all Enums that implement JsonRoundTripStringConvertibleEnum
      // also have a static method called fromUniqueStableString, since it's static. But we have that convention.
      // It's reasonable to expect it in tests.
      Method fromUniqueStableString = enumClass.getMethod("fromUniqueStableString", String.class);

      enumConstantsToRepresentations.forEachEntry( (enumConstant, expectedUniqueStableString) -> {
        try {
          // null is right because we are calling a static method.
          //noinspection unchecked
          assertThat(
              (E) fromUniqueStableString.invoke(null, expectedUniqueStableString),
              enumMatcher(enumConstant));
          assertThat(
              enumConstant.toUniqueStableString(),
              stringMatcher(expectedUniqueStableString));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

}
