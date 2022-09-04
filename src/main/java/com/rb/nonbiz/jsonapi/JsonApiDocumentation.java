package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.text.HumanReadableDocumentation;

/**
 * This is (mostly) human-readable text that explains how a Java object of this type
 * will get converted to/from JSON. It's useful mostly to 3rd party developers.
 */
public abstract class JsonApiDocumentation {

  public interface Visitor<T> {

    T visitJsonApiClassDocumentation(JsonApiClassDocumentation jsonApiClassDocumentation);
    T visitJsonApiEnumDocumentation(JsonApiEnumDocumentation<? extends Enum<?>> jsonApiEnumDocumentation);
    T visitJsonApiClassWithSubclassesDocumentation(JsonApiClassWithSubclassesDocumentation jsonApiClassWithSubclassesDocumentation);
    T visitJsonApiArrayDocumentation(JsonApiArrayDocumentation jsonApiArrayDocumentation);

  }

  public abstract <T> T visit(Visitor<T> visitor);

  public abstract Class<?> getClassBeingDocumented();
  public abstract HumanReadableDocumentation getSingleLineSummary();
  public abstract HumanReadableDocumentation getLongDocumentation();

}
