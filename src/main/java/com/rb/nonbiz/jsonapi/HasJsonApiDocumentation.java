package com.rb.nonbiz.jsonapi;

/**
 * Used by JSON API converters (NOT the classes they convert) to denote that a class has human-readable documentation
 * (typically for developers) for its JSON format.
 */
public interface HasJsonApiDocumentation {

  JsonApiDocumentation getJsonApiDocumentation();

}
