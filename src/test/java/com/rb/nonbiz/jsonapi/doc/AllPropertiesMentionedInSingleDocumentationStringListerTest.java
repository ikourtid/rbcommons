package com.rb.nonbiz.jsonapi.doc;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;
import static org.junit.Assert.assertEquals;

public class AllPropertiesMentionedInSingleDocumentationStringListerTest
    extends RBTest<AllPropertiesMentionedInSingleDocumentationStringLister> {

  @Test
  public void testNumberOfProperties() {
    BiConsumer<String, Integer> asserter = (line, expectedSize) ->
        assertEquals(
            expectedSize.intValue(),
            makeTestObject().listAll(documentation(line)).size());

    asserter.accept("No properties mentioned", 0);
    asserter.accept("<em>Html</em> tags <i>used</i>, but not bold.", 0);
    asserter.accept("<Lots> of <angle> <<brackets>>", 0);

    asserter.accept("One <b>property</b>", 1);
    asserter.accept("One <b>property</b> and its 'value'", 1);
    asserter.accept("Two properties: <b>first</b> and <b>second</b>", 2);
  }

  @Test
  public void testPropertyLists() {
    BiConsumer<String, RBSet<String>> asserter = (line, expectedContents) ->
        assertEquals(
            expectedContents,
            makeTestObject().listAll(documentation(line)));

    asserter.accept("No properties",                                 emptyRBSet());
    asserter.accept("<em>Html</em> tags <i>used</i>, but not bold.", emptyRBSet());

    asserter.accept("One <b>property</b>",                                     singletonRBSet("property"));
    asserter.accept("One <b>property</b> and its 'value'",                     singletonRBSet("property"));
    asserter.accept("Two properties: <b>first</b> and <b>second</b>",          rbSetOf("first", "second"));
    asserter.accept("Many properties: <b>A</b>, <b>B</b>, <b>C</b>, <b>D</b>", rbSetOf("A", "B", "C", "D"));

    asserter.accept("Duplicates: <b>property</b> and <b>property</b>", singletonRBSet("property"));

    // properties will be trimmed of whitespaces
    asserter.accept("Space-padded <b>  first  </b> and <b> second </b>",            rbSetOf("first", "second"));
    asserter.accept("Space-padded <b>\tfirst\t</b> and <b>\nsecond\n</b>",          rbSetOf("first", "second"));
    asserter.accept("Space-padded <b> \t first\r\n</b> and <b>\n second\r\n  </b>", rbSetOf("first", "second"));
  }

  @Test
  public void testIllFormedTags_throw() {
    Function<String, RBSet<String>> maker = line ->
        makeTestObject().listAll(documentation(line));

    RBSet<String> doesNotThrow = maker.apply("One <b>property</b>");

    assertIllegalArgumentException( () -> maker.apply("Empty property <b></b>"));
    assertIllegalArgumentException( () -> maker.apply("One empty property <b></b> and one <b>non-empty</b>"));

    assertIllegalArgumentException( () -> maker.apply("All space <b>  </b>"));
    assertIllegalArgumentException( () -> maker.apply("All whitespace <b> \t  </b>"));
    assertIllegalArgumentException( () -> maker.apply("All whitespace <b> \n \t \r  </b>"));

    assertIllegalArgumentException( () -> maker.apply("No closing <b>tag"));
    assertIllegalArgumentException( () -> maker.apply("No opening tag</b>"));

    assertIllegalArgumentException( () -> maker.apply("Two opening <b>tags<b>"));
    assertIllegalArgumentException( () -> maker.apply("Two closing </b>tags</b>"));

    assertIllegalArgumentException( () -> maker.apply("Mixed <B>tags</b>"));
    assertIllegalArgumentException( () -> maker.apply("Mixed <b>tags</B>"));

    assertIllegalArgumentException( () -> maker.apply("Tags in wrong order </b>tags<b>"));
    assertIllegalArgumentException( () -> maker.apply("Tags in wrong order: <b>good</b> </b>bad<b>"));

    assertIllegalArgumentException( () -> maker.apply("Nested tags <b> outer <b>inner</b> </b> aren't allowed"));
  }

  @Override
  protected AllPropertiesMentionedInSingleDocumentationStringLister makeTestObject() {
    return RBCommonsIntegrationTest.makeRealObject(AllPropertiesMentionedInSingleDocumentationStringLister.class);
  }

}