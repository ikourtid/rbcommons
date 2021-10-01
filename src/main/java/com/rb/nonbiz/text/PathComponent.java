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

  private final String pathComponent;

  private PathComponent(String pathComponent) {
    this.pathComponent = pathComponent;
  }

  public static PathComponent pathComponent(String pathComponent) {
    // Note: ^ and $ below denote the beginning and end of the string to be matched.
    // The folowing pattern says that the string must be made up entirely of
    // lower case letters, upper case letter, digits, and underscores or periods.
    Pattern validDirectoryName = Pattern.compile("^([a-zA-Z0-9_\\.])+$");
    RBPreconditions.checkArgument(
        !pathComponent.isEmpty() && pathComponent.length() < 256 &&
        validDirectoryName.matcher(pathComponent).matches(),
        "pathComponent invalid: %s",
        pathComponent);
    return new PathComponent(pathComponent);
  }

  public String asString() {
    return pathComponent;
  }

}
