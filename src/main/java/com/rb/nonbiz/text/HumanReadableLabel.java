package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;

/**
 * Just a String, except with clear semantics that it's meant for human consumption only
 * and not for basing any decision logic on.
 *
 * <p> A corollary is that we should (almost) never check the value of this (including in test)
 * and only use it for printing stuff to logs etc. </p>
 */
public interface HumanReadableLabel {

  String getLabelText();

  static void checkNonEmptyLabelWithNoWhitespace(String labelText) {
    RBPreconditions.checkArgument(
        !labelText.isEmpty(),
        "This label cannot be empty");

    for (char c : labelText.toCharArray()) {
      RBPreconditions.checkArgument(
          !Character.isWhitespace(c),
          "This label '%s' cannot contain a space",
          labelText);
    }
  }

}
