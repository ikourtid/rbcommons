package com.rb.nonbiz.text.csv;

import com.google.common.base.Joiner;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.intExplained;
import static com.rb.nonbiz.testutils.Asserters.valueExplained;
import static org.junit.Assert.assertArrayEquals;

public class CsvLineSplitterTest extends RBTest<CsvLineSplitter> {

  @Test
  public void happyPath() {
    assertArrayEquals(
        new String[]{ "A", "B", "C" },
        makeTestObject().splitLineIntoComponents("A,B,C", 3));
  }

  @Test
  public void throwsIfFewerFields() {
    assertIllegalArgumentException( () -> makeTestObject().splitLineIntoComponents("A,B,C", 4));
  }

  @Test
  public void throwsIfMoreFields() {
    assertIllegalArgumentException( () -> makeTestObject().splitLineIntoComponents("A,B,C", 2));
  }

  @Test
  public void canHandleCommasInQuotedFields() {
    assertArrayEquals(
        new String[]{ "Cambridge, MA", "USA" },
        makeTestObject().splitLineIntoComponents("\"Cambridge, MA\",USA", 2));
  }

  @Test
  public void weirdProblemWithActualFile() {
    String[] components = {
        "Z86Z", "0425B", "PCA INTL", "", "",
        "USD", "1987-03-24", "", "", "",
        "1.9583", "" };
    String line = valueExplained("Z86Z,0425B,PCA INTL,,,USD,1987-03-24,,,,1.9583,", Joiner.on(',').join(components));
    assertArrayEquals(
        components,
        makeTestObject().splitLineIntoComponents(line, intExplained(12, components.length)));
  }

  @Test
  public void emptyComponentsHandledAtBeginning() {
    assertArrayEquals(
        new String[] { "", "X" },
        makeTestObject().splitLineIntoComponents(",X", 2));
    assertArrayEquals(
        new String[] { "", "XX" },
        makeTestObject().splitLineIntoComponents(",XX", 2));
  }

  @Test
  public void emptyComponentsHandledAtEnd() {
    assertArrayEquals(
        new String[] { "X", "" },
        makeTestObject().splitLineIntoComponents("X,", 2));
    assertArrayEquals(
        new String[] { "XX", "" },
        makeTestObject().splitLineIntoComponents("XX,", 2));
  }

  @Override
  protected CsvLineSplitter makeTestObject() {
    return new CsvLineSplitter();
  }
  
}
