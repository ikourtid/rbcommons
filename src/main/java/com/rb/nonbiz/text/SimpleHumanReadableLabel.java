package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.text.HumanReadableLabel.checkNonEmptyLabelWithNoWhitespace;

/**
 * A human-readable label used to document our output. It's a simple implementation of {@link HumanReadableLabel}.
 *
 * <p> We never use labels in our business logic; they exist only for explanatory purposes.
 * In other words, only humans are supposed to read  a {@link HumanReadableLabel}. </p>
 */
public class SimpleHumanReadableLabel implements HumanReadableLabel {

  private final String labelText;

  private SimpleHumanReadableLabel(String labelText) {
    this.labelText = labelText;
  }

  // This static constructor name breaks the usual naming convention,
  // but it is used all over the place, so it's worth making it shorter.
  public static SimpleHumanReadableLabel label(String labelText) {
    return new SimpleHumanReadableLabel(labelText);
  }

  public static SimpleHumanReadableLabel backtestLabelWithoutCommas(String labelText) {
    RBPreconditions.checkArgument(
        !labelText.contains(","),
        "A backtest label cannot contain a comma: %s",
        labelText);
    return label(labelText);
  }

  public static SimpleHumanReadableLabel simpleNonEmptyHumanReadableLabelWithoutSpaces(String labelText) {
    checkNonEmptyLabelWithNoWhitespace(labelText);
    return new SimpleHumanReadableLabel(labelText);
  }

  // this doesn't include all valid filenames, but should cover the cases we're interested in
  // Note: we're constructing just the filename, not the full path, so "/" is not allowed.
  public static SimpleHumanReadableLabel simpleFileNameHumanReadableLabel(String labelText) {
    RBPreconditions.checkArgument(
        !labelText.isEmpty() && labelText.matches("^[a-zA-Z0-9_\\.]*$"),
        "This label '%s' must contain only letters, numbers, '_' or '.'",
        labelText);
    return new SimpleHumanReadableLabel(labelText);
  }

  public static SimpleHumanReadableLabel emptyLabel() {
    return label("");
  }

  @Override
  public String getLabelText() {
    return labelText;
  }

  @Override
  public String toString() {
    return labelText;
  }

}
