package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isWhitespace;

/**
 * Just a thin wrapper around a Java {@link String}, with the additional semantics that this is just human-readable.
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

  // IDE-generated. We don't normally implement this, but in this case some other code relies on this.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HumanReadableDocumentation that = (HumanReadableDocumentation) o;
    return asString.equals(that.asString);
  }

  // IDE-generated. We don't normally implement this, but in this case some other code relies on this.
  @Override
  public int hashCode() {
    return Objects.hash(asString);
  }

}
