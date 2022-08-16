package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiPropertyDescriptor.JavaEnumJsonApiPropertyDescriptor;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;


/**
 * <p> Generates {@link JsonApiDocumentation} specifically in the case of {@link Enum}s.
 * Centralizing the text generation into this location will standardize the documentation format of the various enums.
 * </p>
 */
public class JsonApiDocumentationForEnumGenerator {

  public <E extends Enum<E>> JsonApiDocumentation generate(
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
    return jsonApiClassDocumentationBuilder()
        .setClass(javaEnumJsonApiPropertyDescriptor.getEnumClass())
        .setSingleLineSummary(singleLineSummary)
        .setLongDocumentation(sb.toString())
        // JsonValidationInstructions is for cases where there are properties, but n/a for a primitive such as Enum.
        .hasNoJsonValidationInstructions()
        // primitives such as Enum do not mention other entities under them that get serialized.
        .hasNoChildNodes()
        .noTrivialSampleJsonSupplied()
        .noNontrivialSampleJsonSupplied()
        .build();
  }

}
