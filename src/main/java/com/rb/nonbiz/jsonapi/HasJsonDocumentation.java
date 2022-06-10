package com.rb.nonbiz.jsonapi;

import java.util.List;

// FIXME IAK JSONDOC
public interface HasJsonDocumentation {

  String getDocumentationString();

  List<Class<? extends HasJsonDocumentation>> getContainedObjects();

}
