package com.rb.nonbiz.text;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.PathComponent.pathComponent;

public class PathComponentTest extends RBTestMatcher<PathComponent> {

  @Test
  public void invalidDirectoryNames_throw() {
    PathComponent doesNotThrow;

    // path components consist of [a-z] [A-Z], [0-9] and "_" or "."
    doesNotThrow = pathComponent("abc");
    doesNotThrow = pathComponent("ABC");
    doesNotThrow = pathComponent("abc123");
    doesNotThrow = pathComponent(".abc.def");
    doesNotThrow = pathComponent("abc_123");
    // can start with a non-letter
    doesNotThrow = pathComponent("123_abc");
    doesNotThrow = pathComponent("_abc_123");
    doesNotThrow = pathComponent(".abc_123");

    // can't be empty
    assertIllegalArgumentException( () -> pathComponent(""));

    // can't have slashes; this is not the full path
    assertIllegalArgumentException( () -> pathComponent("abc/def"));

    // can't have spaces
    assertIllegalArgumentException( () -> pathComponent("abc def"));

    // can't contain unusual characters
    assertIllegalArgumentException( () -> pathComponent("abc-def"));
    assertIllegalArgumentException( () -> pathComponent("abc:def"));
    assertIllegalArgumentException( () -> pathComponent("abc&def"));
    assertIllegalArgumentException( () -> pathComponent("abc|def"));
    assertIllegalArgumentException( () -> pathComponent("abc^def"));
    assertIllegalArgumentException( () -> pathComponent("abc%def"));
  }

  @Test
  public void tooLong_throws() {
    Function<Integer, PathComponent> maker = length ->
        pathComponent(new String(new char[length]).replace('\0', 'A'));

    PathComponent doesNotThrow;
    doesNotThrow = maker.apply(1);
    doesNotThrow = maker.apply(255);

    assertIllegalArgumentException( () -> maker.apply(256));
    // empty also throws
    assertIllegalArgumentException( () -> maker.apply(0));
  }

  @Override
  public PathComponent makeTrivialObject() {
    return pathComponent("a");
  }

  @Override
  public PathComponent makeNontrivialObject() {
    return pathComponent("abc_123.DEF");
  }

  @Override
  public PathComponent makeMatchingNontrivialObject() {
    return pathComponent("abc_123.DEF");
  }

  @Override
  protected boolean willMatch(PathComponent expected, PathComponent actual) {
    return pathComponentMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PathComponent> pathComponentMatcher(PathComponent expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.asString()));
  }

}
