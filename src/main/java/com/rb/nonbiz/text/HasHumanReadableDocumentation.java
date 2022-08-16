package com.rb.nonbiz.text;

import com.rb.nonbiz.text.csv.HumanReadableDocumentation;

/**
 * Like {@link HasHumanReadableLabel}, but with the tighter semantics that the label applies to documentation.
 */
public interface HasHumanReadableDocumentation {

  HumanReadableDocumentation getDocumentation();

}
