package com.rb.nonbiz.testmatchers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hamcrest.TypeSafeMatcher;

import java.util.Set;

import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaCase;
import static com.rb.nonbiz.testmatchers.LambdaSwitchCase.lambdaSwitchMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.alwaysMatchingMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.assertValidEpsilon;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.numberMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class RBJsonMatchers {

  public static TypeSafeMatcher<JsonObject> jsonObjectMatcher(JsonObject expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected, actual -> {
      // Even though JsonObject is essentially a map, we cannot use our own rbMapMatcher or anything like that
      // because it isn't really a Map. But this will do, even though it's not as succinct.
      Set<String> keys = expected.keySet();
      if (!keys.equals(actual.keySet())) {
        return false;
      }
      return keys.stream()
          .allMatch(key -> jsonElementMatcher(expected.get(key), epsilon).matches(actual.get(key)));
    });
  }

  public static TypeSafeMatcher<JsonObject> jsonObjectEpsilonMatcher(JsonObject expected) {
    return jsonObjectMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<JsonArray> jsonArrayMatcher(JsonArray expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> iteratorMatcher(f, f2 -> jsonElementMatcher(f2, epsilon))));
  }

  public static TypeSafeMatcher<JsonArray> jsonArrayEpsilonMatcher(JsonArray expected) {
    return jsonArrayMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<JsonPrimitive> jsonPrimitiveMatcher(JsonPrimitive expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return lambdaSwitchMatcher(expected,
        lambdaCase(v -> v.isBoolean(), v -> v.getAsBoolean(), f -> typeSafeEqualTo(f)),
        lambdaCase(v -> v.isString(),  v -> v.getAsString(),  f -> typeSafeEqualTo(f)),
        lambdaCase(v -> v.isNumber(),  v -> v.getAsNumber(),  f -> numberMatcher(f, epsilon)));
  }

  public static TypeSafeMatcher<JsonPrimitive> jsonPrimitiveEpsilonMatcher(JsonPrimitive expected) {
    return jsonPrimitiveMatcher(expected, 1e-8);
  }

  public static TypeSafeMatcher<JsonElement> jsonElementMatcher(JsonElement expected, double epsilon) {
    assertValidEpsilon(epsilon);
    return lambdaSwitchMatcher(expected,
        lambdaCase(v -> v.isJsonObject(),    v -> v.getAsJsonObject(),    f -> jsonObjectMatcher(f, epsilon)),
        lambdaCase(v -> v.isJsonArray(),     v -> v.getAsJsonArray(),     f -> jsonArrayMatcher(f, epsilon)),
        lambdaCase(v -> v.isJsonPrimitive(), v -> v.getAsJsonPrimitive(), f -> jsonPrimitiveMatcher(f, epsilon)),
        lambdaCase(v -> v.isJsonNull(),      v -> v.getAsJsonNull(),      f -> alwaysMatchingMatcher()));
  }

  public static TypeSafeMatcher<JsonElement> jsonElementEpsilonMatcher(JsonElement expected) {
    return jsonElementMatcher(expected, 1e-8);
  }

}
