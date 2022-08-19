package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isWhitespace;

/**
 * <p> Just a thin wrapper around a Java {@link String}, with the additional semantics that this is just human-readable. </p>
 *
 * <p> It is a bit more specific than a {@link HumanReadableLabel}, which can be used for any human-readable text,
 * including descriptions that are intended for debugging and printing runtime objects. Also, unlike a
 * {@link HumanReadableLabel}, it is a simple class, not an interface. </p>
 */
public class HumanReadableDocumentation {

  private final String asString;

  private HumanReadableDocumentation(String asString) {
    this.asString = asString;
  }

  // Our static constructors almost always have the same name as the class. However, just like with HumanReadableLabel,
  // this appears in so many places where brevity matters that we'll just shorten it to just 'documentation'.
  public static HumanReadableDocumentation documentation(String asString) {
    RBPreconditions.checkArgument(
        !isWhitespace(asString),
        "HumanReadableDocumentation cannot be empty or all whitespace: '%s'",
        asString);
    return new HumanReadableDocumentation(asString);
  }

  public String getAsString() {
    return asString;
  }

  @Override
  public String toString() {
    return Strings.format("[HRD %s HRD]", asString);
  }

}
