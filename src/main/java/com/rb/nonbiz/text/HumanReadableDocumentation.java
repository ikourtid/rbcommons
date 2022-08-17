package com.rb.nonbiz.text;

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

  public static HumanReadableDocumentation humanReadableDocumentation(String asString) {
    return new HumanReadableDocumentation(asString);
  }

  public static HumanReadableDocumentation emptyHumanReadableDocumentation() {
    return humanReadableDocumentation("");
  }

  public String getAsString() {
    return asString;
  }

  @Override
  public String toString() {
    return Strings.format("[HRD %s HRD]", asString);
  }

}
