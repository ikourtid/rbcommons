package com.rb.nonbiz.text;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.regex.Pattern;

/**
 * A portion of a full directory path. E.g. in /abc/def/file.txt, "abc" and "def"
 * would be path components.
 *
 * <p> The only characters we allow are letters (either uppercase or lowercase), digits,
 * underscores, and periods. The component can be at most 255 characters long. </p>
 *
 * <p> Modern directory names allow more characters and longer lengths, but we want to
 * be as compatible as possible with whatever system a client is using. </p>
 */
public class PathComponent {

  private final String rawString;

  private PathComponent(String rawString) {
    this.rawString = rawString;
  }

  public static PathComponent pathComponent(String rawString) {
    // Note: ^ and $ below denote the beginning and end of the string to be matched.
    // The folowing pattern says that the string must be made up entirely of
    // lower case letters, upper case letter, digits, underscores or periods.
    Pattern validDirectoryName = Pattern.compile("^([a-zA-Z0-9_\\.])+$");
    RBPreconditions.checkArgument(
        !rawString.isEmpty() && rawString.length() < 256 &&
            validDirectoryName.matcher(rawString).matches(),
        "PathComponent is invalid: %s",
        rawString);
    return new PathComponent(rawString);
  }

  public String asString() {
    return rawString;
  }

  @Override
  public String toString() {
    return Strings.format("[PC %s PC]", rawString);
  }

}
