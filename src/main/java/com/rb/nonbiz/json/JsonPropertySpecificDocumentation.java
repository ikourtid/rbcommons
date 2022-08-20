package com.rb.nonbiz.json;

import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDateTime;

import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;

/**
 * <p> A thin semantic wrapper for {@link HumanReadableDocumentation} that's mean to apply to a specific property,
 * vs. for an entire class. Example: a {@link LocalDateTime} (class) can be described as being in UTC format, etc. This is
 * specific to the {@link LocalDateTime} itself, not to one particular usage of it. On the other hand, if a property
 * of Trade is 'tradeTime', then that's not just any old {@link LocalDateTime}; it needs further documentation, e.g.
 * "the trade time as reported by the exchange" - which, in this case is property-specific. </p>
 *
 * <p> This is meant to be read as "(JSON property)-specific documentation,
 * not JSON (property-specific) documentation. </p>
 */
public class JsonPropertySpecificDocumentation {

  private final HumanReadableDocumentation rawDocumentation;

  private JsonPropertySpecificDocumentation(HumanReadableDocumentation rawDocumentation) {
    this.rawDocumentation = rawDocumentation;
  }

  public static JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation(
      HumanReadableDocumentation rawDocumentation) {
    return new JsonPropertySpecificDocumentation(rawDocumentation);
  }

  public static JsonPropertySpecificDocumentation jsonPropertySpecificDocumentation(
      String asString) {
    return jsonPropertySpecificDocumentation(documentation(asString));
  }

  public HumanReadableDocumentation getRawDocumentation() {
    return rawDocumentation;
  }

  @Override
  public String toString() {
    return Strings.format("[JPSD %s JPSD]");
  }

}
