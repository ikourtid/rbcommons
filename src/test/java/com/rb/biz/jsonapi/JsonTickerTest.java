package com.rb.biz.jsonapi;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.biz.jsonapi.JsonTicker.jsonTicker;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class JsonTickerTest extends RBTestMatcher<JsonTicker> {

  @Test
  public void tickerIsEmpty_throws() {
    assertIllegalArgumentException( () -> jsonTicker(""));
  }

  @Test
  public void illegalCharacters_throws() {
    assertIllegalArgumentException( () -> jsonTicker("abc\u0000def"));
    assertIllegalArgumentException( () -> jsonTicker("abc\ndef"));
    assertIllegalArgumentException( () -> jsonTicker("abc\tdef"));
    assertIllegalArgumentException( () -> jsonTicker("abc\rdef"));
    assertIllegalArgumentException( () -> jsonTicker("abc\bdef"));

    // last unicode before printable ASCII-128
    assertIllegalArgumentException( () -> jsonTicker("abc \u001F def"));
    // first unicode after printable ASCII-128
    assertIllegalArgumentException( () -> jsonTicker("abc \u007F def"));

    // a control character ("PAD") in LATIN_SUPPLEMENT_1
    assertIllegalArgumentException( () -> jsonTicker("abc \u0080 def"));

    JsonTicker doesNotThrow;
    doesNotThrow = jsonTicker("abc def");   // spaces allowed
    doesNotThrow = jsonTicker("abc ¥ def");
    // most accented roman letters allowed
    doesNotThrow = jsonTicker("Banco Popular Español");
    doesNotThrow = jsonTicker("Société Générale");
    doesNotThrow = jsonTicker("ING Bank Śląski");
    doesNotThrow = jsonTicker("FŁT-Kraśnik");
  }

  @Test
  public void tooLong_throws() {
    Function<Integer, String> stringOfLengthNMaker = length ->
        new String(new char[length]).replace('\0', 'A');

    // max length 256
    JsonTicker doesNotThrow = jsonTicker(stringOfLengthNMaker.apply(256));

    // 257 characters is too long
    assertIllegalArgumentException( () -> jsonTicker(stringOfLengthNMaker.apply(257)));
  }

  @Override
  public JsonTicker makeTrivialObject() {
    return jsonTicker("x");
  }

  @Override
  public JsonTicker makeNontrivialObject() {
    return jsonTicker("abcdefghijklmnopqrstuvwxyz 1234567890 !@#$%^&*() []\\{}|;':,.<>\"");
  }

  @Override
  public JsonTicker makeMatchingNontrivialObject() {
    // nothing to tweak here; same as makeNontrivialObject
    return jsonTicker("abcdefghijklmnopqrstuvwxyz 1234567890 !@#$%^&*() []\\{}|;':,.<>\"");
  }

  @Override
  protected boolean willMatch(JsonTicker expected, JsonTicker actual) {
    return jsonTickerMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<JsonTicker> jsonTickerMatcher(JsonTicker expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getFreeFormString()));
  }

}
