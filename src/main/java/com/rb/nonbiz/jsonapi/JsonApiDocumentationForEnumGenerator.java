package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaEnumJsonApiPropertyDescriptor;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;


/**
 * <p> Generates {@link JsonApiDocumentation} specifically in the case of {@link Enum}s.
 * Centralizing the text generation into this location will standardize the documentation format of the various enums.
 * </p>
 */
public class JsonApiDocumentationForEnumGenerator {

  public <E extends Enum<E>> JsonApiEnumDocumentation<E> generate(
      HumanReadableLabel singleLineSummary,
      String longDocumentationPrefix,
      JavaEnumJsonApiPropertyDescriptor<E> javaEnumJsonApiPropertyDescriptor) {
    StringBuilder sb = new StringBuilder(Strings.format("<p> %s </p>\n", longDocumentationPrefix));
    sb.append("<p> The following values are valid:\n<ul>");
    javaEnumJsonApiPropertyDescriptor.getValidValuesToExplanations()
        .values()
        .forEach(javaEnumSerializationAndExplanation ->
            sb.append(Strings.format("<li> <strong>%s</strong> : %s </li>\n",
                javaEnumSerializationAndExplanation.getJsonSerialization(),
                javaEnumSerializationAndExplanation.getExplanation())));
    sb.append("</ul></p>\n");
    return JsonApiEnumDocumentationBuilder.<E>jsonApiEnumDocumentationBuilder()
        .setEnumClass(javaEnumJsonApiPropertyDescriptor.getEnumClass())
        .setSingleLineSummary(singleLineSummary)
        .setLongDocumentation(sb.toString())
        .build();
  }

}
