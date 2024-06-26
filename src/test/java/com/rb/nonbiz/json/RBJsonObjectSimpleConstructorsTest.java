package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.RBMap;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addAllAssumingNoOverlap;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBJsonObjectSimpleConstructorsTest {

  @Test
  public void testJsonObjectFromRBMap() {
    RBMap<String, JsonElement> rbMap = rbMapOf(
        "key1", jsonInteger(111),
        "key2", jsonInteger(222));

    assertThat(
        jsonObject(rbMap),
        jsonObjectEpsilonMatcher(jsonObject(
            "key1", jsonInteger(111),
            "key2", jsonInteger(222))));
  }

  @Test
  public void testSingletonJsonObjectFromTwoStrings() {
    assertThat(
        singletonJsonObject("key1", "val1"),
        jsonObjectEpsilonMatcher(rbJsonObjectBuilder()
            .setString("key1", "val1")
            .build()));
  }

  @Test
  public void testConstructors() {
    JsonObject jsonObject8 = rbJsonObjectBuilder()
        .setInt("k1", 1)
        .setInt("k2", 2)
        .setInt("k3", 3)
        .setInt("k4", 4)
        .setInt("k5", 5)
        .setInt("k6", 6)
        .setInt("k7", 7)
        .setInt("k8", 8)
        .build();

    assertThat(
        addAllAssumingNoOverlap(
            emptyJsonObject(),
            jsonObject(
                "k1", jsonInteger(1), "k2", jsonInteger(2), "k3", jsonInteger(3), "k4", jsonInteger(4),
                "k5", jsonInteger(5), "k6", jsonInteger(6), "k7", jsonInteger(7), "k8", jsonInteger(8))),
        jsonObjectEpsilonMatcher(jsonObject8));

    assertThat(
        addAllAssumingNoOverlap(
            singletonJsonObject("k1", jsonInteger(1)),
            jsonObject(
                "k2", jsonInteger(2), "k3", jsonInteger(3), "k4", jsonInteger(4), "k5", jsonInteger(5),
                "k6", jsonInteger(6), "k7", jsonInteger(7), "k8", jsonInteger(8))),
        jsonObjectEpsilonMatcher(jsonObject8));

    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2)),
            jsonObject(
                "k3", jsonInteger(3), "k4", jsonInteger(4), "k5", jsonInteger(5), "k6", jsonInteger(6),
                "k7", jsonInteger(7), "k8", jsonInteger(8))),
        jsonObjectEpsilonMatcher(jsonObject8));

    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2), "k3", jsonInteger(3)),
            jsonObject(
                "k4", jsonInteger(4), "k5", jsonInteger(5), "k6", jsonInteger(6), "k7", jsonInteger(7),
                "k8", jsonInteger(8))),
        jsonObjectEpsilonMatcher(jsonObject8));

    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2), "k3", jsonInteger(3), "k4", jsonInteger(4)),
            jsonObject("k5", jsonInteger(5), "k6", jsonInteger(6), "k7", jsonInteger(7), "k8", jsonInteger(8))),
        jsonObjectEpsilonMatcher(jsonObject8));
  }

  // More tests after additional constructors added
  @Test
  public void testConstructors2() {
    JsonObject jsonObject20 = rbJsonObjectBuilder()
        .setInt("k1", 1)
        .setInt("k2", 2)
        .setInt("k3", 3)
        .setInt("k4", 4)
        .setInt("k5", 5)
        .setInt("k6", 6)
        .setInt("k7", 7)
        .setInt("k8", 8)
        .setInt("k9", 9)
        .setInt("k10", 10)
        .setInt("k11", 11)
        .setInt("k12", 12)
        .setInt("k13", 13)
        .setInt("k14", 14)
        .setInt("k15", 15)
        .setInt("k16", 16)
        .setInt("k17", 17)
        .setInt("k18", 18)
        .setInt("k19", 19)
        .setInt("k20", 20)
        .build();

    assertThat(
        addAllAssumingNoOverlap(
            singletonJsonObject("k1", jsonInteger(1)),
            jsonObject(
                "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),  "k5",  jsonInteger(5),
                "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2)),
            jsonObject(
                "k3",  jsonInteger(3),  "k4",  jsonInteger(4),  "k5",  jsonInteger(5),
                "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2), "k3",  jsonInteger(3)),
            jsonObject(
                "k4",  jsonInteger(4),  "k5",  jsonInteger(5),
                "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject("k1", jsonInteger(1), "k2", jsonInteger(2), "k3",  jsonInteger(3), "k4",  jsonInteger(4)),
            jsonObject(
                "k5",  jsonInteger(5),
                "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1",  jsonInteger(1),  "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5",  jsonInteger(5)),
            jsonObject(
                "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1",  jsonInteger(1),  "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5",  jsonInteger(5),  "k6",  jsonInteger(6)),
            jsonObject(
                "k7",  jsonInteger(7),  "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1",  jsonInteger(1),  "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5",  jsonInteger(5),  "k6",  jsonInteger(6),  "k7",  jsonInteger(7)),
            jsonObject(
                "k8",  jsonInteger(8),  "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1", jsonInteger(1),   "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5", jsonInteger(5),   "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8) ),
            jsonObject(
                "k9",  jsonInteger(9),
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1",  jsonInteger(1),  "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5",  jsonInteger(5),  "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),
                "k9",  jsonInteger(9)),
            jsonObject(
                "k10", jsonInteger(10), "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
    assertThat(
        addAllAssumingNoOverlap(
            jsonObject(
                "k1",  jsonInteger(1),  "k2",  jsonInteger(2),  "k3",  jsonInteger(3),  "k4",  jsonInteger(4),
                "k5",  jsonInteger(5),  "k6",  jsonInteger(6),  "k7",  jsonInteger(7),  "k8",  jsonInteger(8),
                "k9",  jsonInteger(9),  "k10", jsonInteger(10)),
            jsonObject(
                "k11", jsonInteger(11), "k12", jsonInteger(12), "k13", jsonInteger(13),
                "k14", jsonInteger(14), "k15", jsonInteger(15), "k16", jsonInteger(16), "k17", jsonInteger(17),
                "k18", jsonInteger(18), "k19", jsonInteger(19), "k20", jsonInteger(20))),
        jsonObjectEpsilonMatcher(jsonObject20));
  }

  @Test
  public void duplicateEntries_throws() {
    Function<String, JsonObject> maker = key1 -> jsonObject(
        key1,   jsonInteger(111),
        "key2", jsonInteger(2222));

    JsonObject doesNotThrow = maker.apply("key1");
    // duplicate keys:
    assertIllegalArgumentException( () -> maker.apply("key2"));
  }

  // as above, but using RbJsonObjectBuilder
  @Test
  public void duplicateEntriesUsingBuilder_throws() {
    Function<String, JsonObject> maker = key1 -> rbJsonObjectBuilder()
        .setInt(key1, 111)
        .setInt("key2", 222)
        .build();

    JsonObject doesNotThrow = maker.apply("key1");
    // duplicate keys:
    assertIllegalArgumentException( () -> maker.apply("key2"));
  }

  @Test
  public void testSimpleConstructors() {
    List<JsonObject> jsonObjects = ImmutableList.of(
        emptyJsonObject(),
        singletonJsonObject(
            "_1", jsonInteger(101)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102),
            "_3", jsonInteger(103)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102),
            "_3", jsonInteger(103),
            "_4", jsonInteger(104)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102),
            "_3", jsonInteger(103),
            "_4", jsonInteger(104),
            "_5", jsonInteger(105)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102),
            "_3", jsonInteger(103),
            "_4", jsonInteger(104),
            "_5", jsonInteger(105),
            "_6", jsonInteger(106)),
        jsonObject(
            "_1", jsonInteger(101),
            "_2", jsonInteger(102),
            "_3", jsonInteger(103),
            "_4", jsonInteger(104),
            "_5", jsonInteger(105),
            "_6", jsonInteger(106),
            "_7", jsonInteger(107)));
    for (int i = 0; i < 7; i++) {
      JsonObject jsonObject = jsonObjects.get(i);
      assertEquals(i, jsonObject.size());
      // The i-th object must have all properties from 0 to i
      for (int j = 0; j < i; j++) {
        assertEquals(i + 100, jsonObject.get("_" + i).getAsInt());
      }
    }
  }

}
