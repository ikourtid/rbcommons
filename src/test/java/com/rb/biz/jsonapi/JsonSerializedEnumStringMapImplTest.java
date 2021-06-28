package com.rb.biz.jsonapi;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImpl.jsonSerializedEnumStringMapImpl;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum.VALUE1;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImplTest.SerializationTestEnum.VALUE2;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class JsonSerializedEnumStringMapImplTest
    extends RBTestMatcher<JsonSerializedEnumStringMap<SerializationTestEnum>> {

  protected enum SerializationTestEnum {

    VALUE1,
    VALUE2

  }

  protected enum EmptySerializationTestEnum {}

  public static <E extends Enum<E>> JsonSerializedEnumStringMap<E> jsonSerializedEnumStringMap(
      Class<E> enumClass,
      E enumValue1, String serializationString1,
      E enumValue2, String serializationString2) {
    return jsonSerializedEnumStringMapImpl(
        enumClass,
        HashBiMap.create(ImmutableMap.of(
            enumValue1, serializationString1,
            enumValue2, serializationString2)));
  }

  @Test
  public void emptyEnumMap_throws() {
    assertIllegalArgumentException( () -> jsonSerializedEnumStringMapImpl(
        EmptySerializationTestEnum.class,
        HashBiMap.create(ImmutableMap.of())));
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeTrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "v1",
        VALUE2, "v2");
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeNontrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "val1",
        VALUE2, "val2");
  }

  @Override
  public JsonSerializedEnumStringMap<SerializationTestEnum> makeMatchingNontrivialObject() {
    return jsonSerializedEnumStringMap(
        SerializationTestEnum.class,
        VALUE1, "val1",
        VALUE2, "val2");
  }

  @Override
  protected boolean willMatch(JsonSerializedEnumStringMap<SerializationTestEnum> expected,
                              JsonSerializedEnumStringMap<SerializationTestEnum> actual) {
    return jsonSerializedEnumStringMapMatcher(expected).matches(actual);
  }

  public static <E extends Enum<E>> TypeSafeMatcher<JsonSerializedEnumStringMap<E>>
  jsonSerializedEnumStringMapMatcher(JsonSerializedEnumStringMap<E> expected) {
    return makeMatcher(expected, actual ->
        expected.getEnumClass().equals(actual.getEnumClass())
            && Arrays.stream(expected.getEnumClass().getEnumConstants())
            .allMatch(enumConstant ->
                expected.getSerializationStringOrThrow(enumConstant).equals(
                    actual.getSerializationStringOrThrow(enumConstant))));
  }

}
