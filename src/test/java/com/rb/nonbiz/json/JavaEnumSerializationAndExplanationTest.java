package com.rb.nonbiz.json;

import com.rb.nonbiz.json.JsonApiEnumDescriptor.JavaEnumSerializationAndExplanation;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;

import static com.rb.nonbiz.json.JsonApiEnumDescriptor.JavaEnumSerializationAndExplanation.javaEnumSerializationAndExplanation;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static com.rb.nonbiz.text.HumanReadableDocumentationTest.humanReadableDocumentationMatcher;

public class JavaEnumSerializationAndExplanationTest extends RBTestMatcher<JavaEnumSerializationAndExplanation> {

  @Test
  public void enumSerializationOrExplanationAreEmpty_throws() {
    BiFunction<String, String, JavaEnumSerializationAndExplanation> maker =
        (jsonSerialization, explanation) -> javaEnumSerializationAndExplanation(
            jsonSerialization, documentation(explanation));
    assertIllegalArgumentException( () -> maker.apply("", ""));
    assertIllegalArgumentException( () -> maker.apply("x", ""));
    assertIllegalArgumentException( () -> maker.apply("", "x"));
    JavaEnumSerializationAndExplanation doesNotThrow;
    doesNotThrow = maker.apply("x", "x");
    doesNotThrow = maker.apply("x", "y");
  }

  @Override
  public JavaEnumSerializationAndExplanation makeTrivialObject() {
    return javaEnumSerializationAndExplanation("a", documentation("a"));
  }

  @Override
  public JavaEnumSerializationAndExplanation makeNontrivialObject() {
    return javaEnumSerializationAndExplanation("someEnumValueAsJson", documentation("sample explanation"));
  }

  @Override
  public JavaEnumSerializationAndExplanation makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return javaEnumSerializationAndExplanation("someEnumValueAsJson", documentation("sample explanation"));
  }

  @Override
  protected boolean willMatch(JavaEnumSerializationAndExplanation expected, JavaEnumSerializationAndExplanation actual) {
    return javaEnumSerializationAndExplanationMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JavaEnumSerializationAndExplanation> javaEnumSerializationAndExplanationMatcher(
      JavaEnumSerializationAndExplanation expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getJsonSerialization()),
        // We almost never match HumanReadableLabel.
        // However, this rule applies to labels that are attached to various runtime objects
        // (e.g. daily time series) and which are intended for Rowboat developers to read.
        // In this case here, this is a HumanReadableDocumentation, which is intended for 3rd party developers.
        // So its contents matter.
        match(           v -> v.getExplanation(), f -> humanReadableDocumentationMatcher(f)));
  }

}
