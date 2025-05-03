package com.rb.nonbiz.collections;

/**
 * Just a thin typesafe wrapper over {@link HasIidSet} to denote that the returned {@link IidSet} is not empty.
 *
 * <p> Since this is an interface, there's no good way to verify that (e.g. with a constructor precondition),
 * but at least the classes that implement this will know that they should verify this condition. </p>
 */
public interface HasNonEmptyIidSet extends HasIidSet {

}
