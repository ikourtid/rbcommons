package com.rb.nonbiz.util;

/**
 * A generic interface that lets us mark enums as being convertible back and forth to strings.
 * This is meant to avoid situations where we use the Java identifier as the string representation, because if it
 * ever gets renamed, we don't want the string representation to change, as that will be part of an (ideally) stable
 * API.
 */
public interface HasUniqueStableStringRepresentation {

  String toUniqueStableString();

}
