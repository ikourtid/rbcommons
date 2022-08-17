package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptor;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.text.HumanReadableDocumentation.humanReadableDocumentation;


/**
 * <p> Generates {@link JsonApiDocumentation} specifically in the case of {@link Enum}s.
 * Centralizing the text generation into this location will standardize the documentation format of the various enums.
 * </p>
 */
public class JsonApiDocumentationForEnumGenerator {

  public <E extends Enum<E>> JsonApiEnumDocumentation<E> generate(
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentationPrefix,
      JsonApiEnumDescriptor<E> jsonApiEnumDescriptor) {
    StringBuilder sb = new StringBuilder(Strings.format("<p> %s </p>\n", longDocumentationPrefix));
    sb.append("<p> The following values are valid:\n<ul>");
    jsonApiEnumDescriptor.getValidValuesToExplanations()
        .values()
        .forEach(javaEnumSerializationAndExplanation ->
            sb.append(Strings.format("<li> <strong>%s</strong> : %s </li>\n",
                javaEnumSerializationAndExplanation.getJsonSerialization(),
                javaEnumSerializationAndExplanation.getExplanation())));
    sb.append("</ul></p>\n");
    return JsonApiEnumDocumentationBuilder.<E>jsonApiEnumDocumentationBuilder()
        .setJsonApiEnumDescriptor(jsonApiEnumDescriptor)
        .setSingleLineSummary(singleLineSummary)
        .setLongDocumentation(humanReadableDocumentation(sb.toString()))
        .build();
  }

}
