package com.rb.nonbiz.jsonapi;

import org.hamcrest.TypeSafeMatcher;

import static com.rb.nonbiz.jsonapi.JsonApiDocumentationTest.jsonApiDocumentationMatcher;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

public class HasJsonApiDocumentationTest {

  public static TypeSafeMatcher<HasJsonApiDocumentation> hasJsonApiDocumentationMatcher(
      HasJsonApiDocumentation expected) {
    return makeMatcher(expected,
        match(v -> v.getJsonApiDocumentation(), f -> jsonApiDocumentationMatcher(f)));
  }

}
