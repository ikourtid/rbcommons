package com.rb.nonbiz.text;

/**
 * Sometimes you want some objects to have a human-readable label that's not quite the same as
 * what would show in toString(). E.g. a timeseries could be labeled "monthly housing returns".
 *
 * @see HumanReadableLabel
 */
public interface HasHumanReadableLabel {

  HumanReadableLabel getHumanReadableLabel();

}
