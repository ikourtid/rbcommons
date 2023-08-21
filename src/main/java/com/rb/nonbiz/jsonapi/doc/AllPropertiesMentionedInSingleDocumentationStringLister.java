package com.rb.nonbiz.jsonapi.doc;

import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.HumanReadableDocumentation;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSetFromPossibleDuplicates;

/**
 * Extracts all the JSON property names that may appear inside a line of
 * {@link HumanReadableDocumentation}.
 *
 * <p> It finds all content strings inside {@code <b>} and {@code </b>}. </p>
 *
 * <p> Note that property names may be used multiple times in the JSON documentation lines,
 * so we do not throw on finding duplicates. </p>
 */
public class AllPropertiesMentionedInSingleDocumentationStringLister {

  public RBSet<String> listAll(HumanReadableDocumentation humanReadableDocumentation) {
    String docString = humanReadableDocumentation.getAsString();

    // Check that we have equal number of opening and closing html tags
    final int nOpeningMarkers = StringUtils.countMatches(docString, "<b>");
    final int nClosingMarkers = StringUtils.countMatches(docString, "</b>");
    RBPreconditions.checkArgument(
        nOpeningMarkers == nClosingMarkers,
        "Must have equal numbers of '<b>' and '</b>' markers, but found %s and %s, respectively.",
        nOpeningMarkers, nClosingMarkers);

    // Get all the substrings between <b> and </b>
    String[] substringsBetween = StringUtils.substringsBetween(
        humanReadableDocumentation.toString(), "<b>", "</b>");

    // If no properties are mentioned, substringsBetween will be 'null'.
    if (substringsBetween == null) {
      // Check that there were no opening tags (and therefore also no closing tags). If there were tags, they
      // must have been in the wrong order (e.g. </b>property<b>) for substringsBetween to be null.
      RBPreconditions.checkArgument(
          nOpeningMarkers == 0,
          "No substringsBetween found, but have %s tag markers",
          nOpeningMarkers);
      return emptyRBSet();
    }

    List<String> contentStrings = Arrays.asList(substringsBetween);

    // Check that the number found matches the number of opening tags (and closing tags). Otherwise, some pair
    // of opening/closing tags is not ordered.
    RBPreconditions.checkArgument(
        contentStrings.size() == nOpeningMarkers,
        "Should have found %s strings, but found %s",
        nOpeningMarkers, contentStrings.size());

    // For some reason, a space is appended before and after each property.
    List<String> trimmedContentStrings = contentStrings.stream().map(String::trim).collect(Collectors.toList());

    // Make sure none of the properties are empty.
    RBPreconditions.checkArgument(
        trimmedContentStrings
            .stream()
            .noneMatch(v -> v.isEmpty()),
        "No properties may be the empty string: %s",
        trimmedContentStrings);

    return newRBSetFromPossibleDuplicates(trimmedContentStrings);
  }

}
