package com.rb.nonbiz.text;

/**
 * Like {@link HasHumanReadableLabel}, but with the tighter semantics that the label applies to documentation.
 */
public interface HasDocumentation {

  HumanReadableLabel getDocumentation();

}
