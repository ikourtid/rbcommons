package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSets;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBSet.singletonRBSet;

/**
 * Used by JSON API converters (NOT the classes they convert) to denote that a class has human-readable documentation
 * (typically for developers) for its JSON format.
 */
public interface HasJsonApiDocumentation {

  JsonApiDocumentation getJsonApiDocumentation();

  /**
   * <p> In certain cases, a JSON API converter may do an in-line conversion of a data class or an enum,
   * without a separate JSON API converter. This is particularly convenient in cases where whatever is being
   * converted is simple, or (sometimes in the case of enums) only converted in a single location.
   * To avoid needing to create a separate JSON API converter just so we can tie the relevant
   * {@link JsonApiDocumentation} to an easy-to-find location (i.e. not those top-level lists of JSON entity
   * documentation, such as {@code RemainingJsonApiDocumentationLister} in rbengine), we have this additional mechanism
   * that allows a JSON API converter to supply additional documentation for whatever it's converting "in-line". </p>
   *
   * <p> We could have used a plain {@link RBSet} as a return type, and just return an empty set in most cases,
   * but we like using {@link Optional} to make it explicit that the return value is not even applicable
   * in certain cases. </p>
   */
  default Optional<RBSet<JsonApiDocumentation>> getAdditionalJsonApiDocumentation() {
    return Optional.empty();
  }

  default RBSet<JsonApiDocumentation> getAllJsonApiDocumentation() {
    JsonApiDocumentation main = getJsonApiDocumentation();
    Optional<RBSet<JsonApiDocumentation>> additional = getAdditionalJsonApiDocumentation();
    return additional.isPresent()
        ? RBSets.union(additional.get(), main)
        : singletonRBSet(main);
  }

}
