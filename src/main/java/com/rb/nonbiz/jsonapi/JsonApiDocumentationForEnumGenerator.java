package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.json.JsonApiEnumDescriptor;
import com.rb.nonbiz.jsonapi.JsonApiEnumDocumentation.JsonApiEnumDocumentationBuilder;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;

import static com.rb.nonbiz.text.HumanReadableDocumentation.documentation;


/**
 * Generates {@link JsonApiDocumentation} specifically in the case of {@link Enum}s.
 *
 * <p> Centralizing the text generation into this location helps standardize the documentation format of the various enums. </p>
 */
public class JsonApiDocumentationForEnumGenerator {

  public <E extends Enum<E> & JsonRoundTripStringConvertibleEnum<E>> JsonApiEnumDocumentation<E> generate(
      HumanReadableDocumentation singleLineSummary,
      HumanReadableDocumentation longDocumentationPrefix,
      JsonApiEnumDescriptor<E> jsonApiEnumDescriptor) {
    StringBuilder sb = new StringBuilder(Strings.format("<p> %s </p>\n", longDocumentationPrefix.getAsString()));
    sb.append("<p> The following values are valid:\n<ul>");
    jsonApiEnumDescriptor.getValidValuesToExplanations()
        .forEachEntryInKeyOrder( (enumConstant, humanReadableDocumentation) ->
            sb.append(Strings.format("<li> <strong>%s</strong> : %s </li>\n",
                enumConstant.toUniqueStableString(),
                humanReadableDocumentation.getAsString())));
    sb.append("</ul></p>\n");
    return JsonApiEnumDocumentationBuilder.<E>jsonApiEnumDocumentationBuilder()
        .setJsonApiEnumDescriptor(jsonApiEnumDescriptor)
        .setSingleLineSummary(singleLineSummary)
        .setLongDocumentation(documentation(sb.toString()))
        .build();
  }

}
