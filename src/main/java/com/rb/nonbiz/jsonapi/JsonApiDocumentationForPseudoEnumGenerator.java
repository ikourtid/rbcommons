package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiPropertyDescriptor.PseudoEnumJsonApiPropertyDescriptor;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;

import static com.rb.nonbiz.jsonapi.JsonApiClassDocumentation.JsonApiClassDocumentationBuilder.jsonApiClassDocumentationBuilder;

/**
 * <p> Generates {@link JsonApiDocumentation} specifically in the case of {@link PseudoEnumJsonApiPropertyDescriptor}s.
 * Centralizing the text generation into this location will standardize the documentation format.
 * </p>
 */
public class JsonApiDocumentationForPseudoEnumGenerator {

  public JsonApiDocumentation generate(
      Class<?> clazz,
      HumanReadableLabel singleLineSummary,
      String longDocumentationPrefix,
      PseudoEnumJsonApiPropertyDescriptor pseudoEnumJsonApiPropertyDescriptor) {
    StringBuilder sb = new StringBuilder(Strings.format("<p> %s </p>\n", longDocumentationPrefix));
    sb.append("<p> The following values are valid:\n<ul>");
    pseudoEnumJsonApiPropertyDescriptor.getValidValuesToExplanations()
        .forEachEntry( (key, explanationLabel) ->
            sb.append(Strings.format("<li> <strong>%s</strong> : %s </li>\n",
                key,
                explanationLabel.getLabelText())));
    sb.append("</ul></p>\n");
    return jsonApiClassDocumentationBuilder()
        .setClass(clazz)
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
