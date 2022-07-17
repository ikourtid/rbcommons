package com.rb.nonbiz.json;

import com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaEnumJsonApiDescriptor;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaEnumJsonApiDescriptor.JavaEnumSerializationAndExplanation.javaEnumSerializationAndExplanation;
import static com.rb.nonbiz.json.DataClassJsonApiDescriptor.JavaEnumJsonApiDescriptor.javaEnumJsonApiDescriptor;
import static com.rb.nonbiz.json.JavaEnumSerializationAndExplanationTest.javaEnumSerializationAndExplanationMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.enumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.emptyEnumMap;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.enumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonEnumMap;

public class JavaEnumJsonApiDescriptorTest extends RBTestMatcher<JavaEnumJsonApiDescriptor> {

  @Test
  public void mustHaveAtLeastOneItem() {
    assertIllegalArgumentException( () -> javaEnumJsonApiDescriptor(emptyEnumMap()));
  }

  @Override
  public JavaEnumJsonApiDescriptor makeTrivialObject() {
    return javaEnumJsonApiDescriptor(singletonEnumMap(
        TestEnumXYZ.X, javaEnumSerializationAndExplanation("x", label("y"))));
  }

  @Override
  public JavaEnumJsonApiDescriptor makeNontrivialObject() {
    return javaEnumJsonApiDescriptor(enumMapOf(
        TestEnumXYZ.X, javaEnumSerializationAndExplanation("_x", label("explanation for x")),
        TestEnumXYZ.Y, javaEnumSerializationAndExplanation("_y", label("explanation for y")),
        TestEnumXYZ.Z, javaEnumSerializationAndExplanation("_z", label("explanation for z"))));
  }

  @Override
  public JavaEnumJsonApiDescriptor makeMatchingNontrivialObject() {
    // Nothing to tweak here
    return javaEnumJsonApiDescriptor(enumMapOf(
        TestEnumXYZ.X, javaEnumSerializationAndExplanation("_x", label("explanation for x")),
        TestEnumXYZ.Y, javaEnumSerializationAndExplanation("_y", label("explanation for y")),
        TestEnumXYZ.Z, javaEnumSerializationAndExplanation("_z", label("explanation for z"))));
  }

  @Override
  protected boolean willMatch(JavaEnumJsonApiDescriptor expected, JavaEnumJsonApiDescriptor actual) {
    return javaEnumJsonApiDescriptorMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JavaEnumJsonApiDescriptor> javaEnumJsonApiDescriptorMatcher(
      JavaEnumJsonApiDescriptor expected) {
    return makeMatcher(expected,
        match(v -> v.getValidValuesToExplanations(), f -> enumMapMatcher(f,
            f2 -> javaEnumSerializationAndExplanationMatcher(f2))));
  }

}
